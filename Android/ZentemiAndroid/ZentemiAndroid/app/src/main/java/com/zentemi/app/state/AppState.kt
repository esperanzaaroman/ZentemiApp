package com.zentemi.app.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.zentemi.app.data.AuthService
import com.zentemi.app.data.ChildAppSettings
import com.zentemi.app.data.ChildAvatarSettings
import com.zentemi.app.data.ChildProfile
import com.zentemi.app.data.FirestoreService
import com.zentemi.app.data.InventoryItemRecord
import com.zentemi.app.data.LessonProgressRecord
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@OptIn(FlowPreview::class)
class AppState : ViewModel() {

    // ── User ──
    var userName by mutableStateOf("Jugador")
    var lastUserNameChange by mutableStateOf<Long?>(null)

    // ── Pet ──
    var petName by mutableStateOf("Maicito")
    var happiness by mutableIntStateOf(50)

    // ── Profile ──
    var coins by mutableIntStateOf(1500)
    var streak by mutableIntStateOf(0)
    var lastPracticeDate by mutableStateOf<Long?>(null)
    var lessonsCompleted by mutableIntStateOf(0)
    val totalLessons = 30

    // ── Levels ──
    var currentLevel by mutableIntStateOf(1)
    var totalXP by mutableIntStateOf(0)
    val xpRequiredPerLevel = 100
    val maxLevel = 10

    val currentLevelXP: Int
        get() = totalXP % xpRequiredPerLevel

    val levelProgress: Float
        get() = currentLevelXP.toFloat() / xpRequiredPerLevel

    val xpRemaining: Int
        get() = xpRequiredPerLevel - currentLevelXP

    // ── Avatar customization ──
    var avatarBackgroundHue by mutableFloatStateOf(0.72f)
    var mascotHue by mutableFloatStateOf(0f)
    var profileBackgroundHue by mutableFloatStateOf(0.73f)

    // ── Accessories equipped ──
    var glassesEquipped by mutableStateOf(false)
    var hairEquipped by mutableStateOf(false)
    var scarfEquipped by mutableStateOf(false)
    var sunglassesEquipped by mutableStateOf(false)
    var pinkHairEquipped by mutableStateOf(false)
    var swordEquipped by mutableStateOf(false)
    var suitEquipped by mutableStateOf(false)

    // ── Store purchases ──
    var sunglassesPurchased by mutableStateOf(false)
    var pinkHairPurchased by mutableStateOf(false)
    var swordPurchased by mutableStateOf(false)
    var suitPurchased by mutableStateOf(false)

    // ── Learning path ──
    var pathLessonsCompleted by mutableIntStateOf(0)
    var unlockedPathLesson by mutableIntStateOf(1)
    var greetingsRewardClaimed by mutableStateOf(false)
    var greetingsExerciseCount by mutableIntStateOf(4)
    var familyExerciseCount by mutableIntStateOf(6)

    // ── Audio ──
    var musicVolume by mutableFloatStateOf(0.35f)
    var soundVolume by mutableFloatStateOf(1.0f)

    // ── Notifications ──
    var notificationsEnabled by mutableStateOf(true)

    // ── Language ──
    var selectedLanguage by mutableStateOf("Español")

    // ── Purchase animation ──
    var purchasedItemAnimation by mutableStateOf<String?>(null)

    // ── Toggle functions with mutual exclusion ──

    fun toggleGlasses() {
        glassesEquipped = !glassesEquipped
        if (glassesEquipped) sunglassesEquipped = false
    }

    fun toggleSunglasses() {
        sunglassesEquipped = !sunglassesEquipped
        if (sunglassesEquipped) glassesEquipped = false
    }

    fun toggleHair() {
        hairEquipped = !hairEquipped
        if (hairEquipped) pinkHairEquipped = false
    }

    fun togglePinkHair() {
        pinkHairEquipped = !pinkHairEquipped
        if (pinkHairEquipped) hairEquipped = false
    }

    fun toggleScarf() {
        scarfEquipped = !scarfEquipped
    }

    fun toggleSword() {
        swordEquipped = !swordEquipped
    }

    fun toggleSuit() {
        suitEquipped = !suitEquipped
    }

    // ── Level names ──

    val levelNames = listOf(
        "Semilla",
        "Brote",
        "Raíz",
        "Tallo",
        "Hoja",
        "Flor",
        "Fruto",
        "Árbol",
        "Guardián del Yuhmu",
        "Maestro Zentemi"
    )

    fun levelName(level: Int): String {
        return levelNames.getOrElse(level - 1) { "Semilla" }
    }

    // ── Lesson completion ──

    fun completeGreetingsLesson(
        correctAnswers: Int = 5,
        totalAnswers: Int = 5,
        elapsedSeconds: Int = 120
    ) {
        if (!greetingsRewardClaimed) {
            greetingsRewardClaimed = true
            coins += 700
            totalXP += 140
            currentLevel = minOf((totalXP / xpRequiredPerLevel) + 1, maxLevel)
            lessonsCompleted += 1
            if (pathLessonsCompleted < 1) {
                pathLessonsCompleted = 1
                unlockedPathLesson = maxOf(unlockedPathLesson, 2)
            }

            val parentUID = activeParentUID
            val childId = activeChildId
            if (parentUID != null && childId != null) {
                viewModelScope.launch {
                    runCatching {
                        FirestoreService.saveLessonProgress(
                            parentUID = parentUID,
                            childId = childId,
                            progress = LessonProgressRecord(
                                lessonId = "greetings",
                                completed = true,
                                rewardClaimed = true,
                                correctAnswers = correctAnswers,
                                totalAnswers = totalAnswers,
                                elapsedSeconds = elapsedSeconds
                            )
                        )
                    }.onFailure { Log.w(TAG, "No se pudo guardar el progreso", it) }
                }
            }
        }
    }

    // ── Name change cooldown ──

    val canChangeUserName: Boolean
        get() {
            val last = lastUserNameChange ?: return true
            val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
            return System.currentTimeMillis() - last >= sevenDaysMs
        }

    // ── Racha diaria (misma regla que iOS, hora de Ciudad de México) ──

    fun updateDailyStreak() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Mexico_City"))
        val today = startOfDay(calendar, System.currentTimeMillis())
        val previous = lastPracticeDate

        if (previous != null) {
            val lastDay = startOfDay(calendar, previous)
            val days = Math.round((today - lastDay).toDouble() / DAY_MS).toInt()
            if (days <= 0) return
            streak = if (days == 1) streak + 1 else 1
        } else {
            streak = 1
        }
        lastPracticeDate = today
    }

    private fun startOfDay(calendar: Calendar, millis: Long): Long {
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // ── Sincronización con Firestore (equivalente a iOS AppState) ──

    var activeParentUID by mutableStateOf<String?>(null)
        private set
    var activeChildId by mutableStateOf<String?>(null)
        private set
    var isSyncingWithCloud by mutableStateOf(false)
        private set

    private var profileListener: ListenerRegistration? = null
    private var childCreatedAt: Timestamp? = null

    /** Último perfil escrito o recibido de la nube; evita reenviar lo mismo. */
    private var lastSyncedProfile: ChildProfile? = null

    init {
        // Vincula o desvincula la cuenta según la sesión de Firebase Auth
        viewModelScope.launch {
            AuthService.currentUser.collect { user ->
                if (user != null) bindParentAccount(user.uid, user.email ?: "")
                else unbindAccount()
            }
        }

        // Cualquier cambio del perfil se sube a Firestore con debounce de 0.5 s
        viewModelScope.launch {
            snapshotFlow { exportChildProfile() }
                .filterNotNull()
                .debounce(500)
                .collect { syncProfileToFirestore(it) }
        }
    }

    private fun bindParentAccount(uid: String, email: String) {
        if (activeParentUID == uid) return
        activeParentUID = uid

        viewModelScope.launch {
            isSyncingWithCloud = true
            try {
                // 1. Asegurar documento del tutor
                FirestoreService.createOrUpdateParentProfile(uid = uid, email = email)

                // 2. Obtener o crear perfil del menor
                val child = FirestoreService.fetchChildren(uid).firstOrNull { it.id != null }
                    ?: FirestoreService.createChildProfile(parentUID = uid, userName = userName)
                val childId = child.id ?: return@launch

                // 3. Aplicar datos de la nube al estado local
                activeChildId = childId
                applyChildProfile(child)
                updateDailyStreak()

                // 4. Conectar listener en tiempo real
                profileListener?.remove()
                profileListener = FirestoreService.listenToChildProfile(uid, childId) { result ->
                    result.onSuccess { applyChildProfile(it) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar con Firestore: ${e.message}", e)
            } finally {
                isSyncingWithCloud = false
            }
        }
    }

    private fun unbindAccount() {
        profileListener?.remove()
        profileListener = null
        activeParentUID = null
        activeChildId = null
        childCreatedAt = null
        lastSyncedProfile = null
    }

    private fun applyChildProfile(child: ChildProfile) {
        userName = child.userName
        lastUserNameChange = child.lastUserNameChange?.toDate()?.time
        petName = child.petName
        happiness = child.happiness
        coins = child.coins
        totalXP = child.totalXP
        currentLevel = child.currentLevel
        streak = child.streak
        lastPracticeDate = child.lastPracticeDate?.toDate()?.time
        lessonsCompleted = child.lessonsCompleted

        avatarBackgroundHue = child.avatar.avatarBackgroundHue.toFloat()
        mascotHue = child.avatar.mascotHue.toFloat()
        profileBackgroundHue = child.avatar.profileBackgroundHue.toFloat()

        musicVolume = child.settings.musicVolume.toFloat()
        soundVolume = child.settings.soundVolume.toFloat()

        childCreatedAt = child.createdAt
        // Lo que acabamos de recibir no hace falta volver a subirlo
        lastSyncedProfile = exportChildProfile()
    }

    private fun syncProfileToFirestore(profile: ChildProfile) {
        val parentUID = activeParentUID ?: return
        if (profile == lastSyncedProfile) return
        lastSyncedProfile = profile

        viewModelScope.launch {
            runCatching { FirestoreService.saveChildProfile(parentUID, profile) }
                .onFailure { Log.w(TAG, "No se pudo guardar el perfil", it) }
        }
    }

    /** Estado actual como documento de Firestore, o null si no hay cuenta vinculada. */
    fun exportChildProfile(): ChildProfile? {
        val childId = activeChildId ?: return null
        return ChildProfile(
            id = childId,
            userName = userName,
            lastUserNameChange = lastUserNameChange?.let { Timestamp(Date(it)) },
            petName = petName,
            happiness = happiness,
            coins = coins,
            totalXP = totalXP,
            currentLevel = currentLevel,
            streak = streak,
            lastPracticeDate = lastPracticeDate?.let { Timestamp(Date(it)) },
            lessonsCompleted = lessonsCompleted,
            avatar = ChildAvatarSettings(
                avatarBackgroundHue = avatarBackgroundHue.toDouble(),
                mascotHue = mascotHue.toDouble(),
                profileBackgroundHue = profileBackgroundHue.toDouble()
            ),
            settings = ChildAppSettings(
                musicVolume = musicVolume.toDouble(),
                soundVolume = soundVolume.toDouble()
            ),
            createdAt = childCreatedAt ?: Timestamp.now()
        )
    }

    fun recordInventoryItem(itemId: String, purchased: Boolean, equipped: Boolean) {
        val parentUID = activeParentUID ?: return
        val childId = activeChildId ?: return
        viewModelScope.launch {
            runCatching {
                FirestoreService.saveInventoryItem(
                    parentUID = parentUID,
                    childId = childId,
                    item = InventoryItemRecord(
                        itemId = itemId,
                        purchased = purchased,
                        equipped = equipped,
                        purchasedAt = Timestamp.now()
                    )
                )
            }.onFailure { Log.w(TAG, "No se pudo guardar el inventario", it) }
        }
    }

    override fun onCleared() {
        profileListener?.remove()
        super.onCleared()
    }

    private companion object {
        const val TAG = "AppState"
        const val DAY_MS = 24L * 60 * 60 * 1000
    }
}
