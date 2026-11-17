package com.zentemi.app.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.zentemi.app.audio.MusicManager
import java.time.LocalDate
import java.time.ZoneId

/**
 * Estado global de la app. Igual que AppState.swift, casi todo se guarda en
 * el teléfono (SharedPreferences) y se recupera al abrir la app.
 */
class AppState(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences("zentemi", Context.MODE_PRIVATE)

    /** Todos los valores guardados, para poder reiniciarlos juntos. */
    private val persisted = mutableListOf<PersistedState<*>>()
    private fun <T> saved(state: PersistedState<T>) = state.also { persisted += it }

    // ── User ──
    var userName by saved(prefs.persistedString("userName", "Jugador"))
    var lastUserNameChange by saved(prefs.persistedLong("lastUserNameChange", null))

    // ── Pet ──
    var petName by saved(prefs.persistedString("petName", "Maicito"))
    var happiness by saved(prefs.persistedInt("happiness", 50))

    // ── Profile ──
    var coins by saved(prefs.persistedInt("coins", 1500))
    var streak by saved(prefs.persistedInt("streak", 0))
    var lessonsCompleted by saved(prefs.persistedInt("lessonsCompleted", 0))
    val totalLessons = 30

    // ── Levels ──
    var currentLevel by saved(prefs.persistedInt("currentLevel", 1))
    var totalXP by saved(prefs.persistedInt("totalXP", 0))
    val xpRequiredPerLevel = 100
    val maxLevel = 10

    val currentLevelXP: Int
        get() = totalXP % xpRequiredPerLevel

    val levelProgress: Float
        get() = currentLevelXP.toFloat() / xpRequiredPerLevel

    val xpRemaining: Int
        get() = xpRequiredPerLevel - currentLevelXP

    // ── Avatar customization ──
    var avatarBackgroundHue by saved(prefs.persistedFloat("avatarBackgroundHue", 0f))
    var mascotHue by saved(prefs.persistedFloat("mascotHue", 0f))
    var profileBackgroundHue by saved(prefs.persistedFloat("profileBackgroundHue", 0f))

    // ── Accessories equipped ──
    var glassesEquipped by saved(prefs.persistedBoolean("glassesEquipped", false))
    var hairEquipped by saved(prefs.persistedBoolean("hairEquipped", false))
    var scarfEquipped by saved(prefs.persistedBoolean("scarfEquipped", false))
    var sunglassesEquipped by saved(prefs.persistedBoolean("sunglassesEquipped", false))
    var pinkHairEquipped by saved(prefs.persistedBoolean("pinkHairEquipped", false))
    var swordEquipped by saved(prefs.persistedBoolean("swordEquipped", false))
    var suitEquipped by saved(prefs.persistedBoolean("suitEquipped", false))

    // ── Store purchases ──
    var sunglassesPurchased by saved(prefs.persistedBoolean("sunglassesPurchased", false))
    var pinkHairPurchased by saved(prefs.persistedBoolean("pinkHairPurchased", false))
    var swordPurchased by saved(prefs.persistedBoolean("swordPurchased", false))
    var suitPurchased by saved(prefs.persistedBoolean("suitPurchased", false))

    // ── Learning path ──
    var pathLessonsCompleted by saved(prefs.persistedInt("pathLessonsCompleted", 0))
    var unlockedPathLesson by saved(prefs.persistedInt("unlockedPathLesson", 1))
    var greetingsRewardClaimed by saved(prefs.persistedBoolean("greetingsRewardClaimed", false))
    var greetingsExerciseCount by mutableIntStateOf(5)
    var familyExerciseCount by mutableIntStateOf(6)

    // ── Audio ──
    // Igual que iOS: al cambiar el volumen de música se pausa o reanuda.
    var musicVolume by saved(
        prefs.persistedFloat("musicVolume", 0.35f) { MusicManager.onVolumeChanged(it) }
    )
    var soundVolume by saved(prefs.persistedFloat("soundVolume", 1.0f))

    // ── Daily streak (hora de Ciudad de México, igual que iOS) ──
    private var lastStreakDay by saved(prefs.persistedLong("lastStreakDate", null))
    var lastCelebratedStreakDay by saved(prefs.persistedLong("lastCelebratedStreakDate", null))

    private val streakZone: ZoneId = ZoneId.of("America/Mexico_City")

    /** Día de hoy en Ciudad de México, contado como días desde 1970. */
    val todayEpochDay: Long
        get() = LocalDate.now(streakZone).toEpochDay()

    /**
     * Suma 1 a la racha si ayer se abrió la app, la reinicia a 1 si pasó más
     * de un día y no hace nada si ya se contó hoy. Se llama al abrir y al
     * volver a la app.
     */
    fun updateDailyStreak() {
        val today = todayEpochDay
        val previous = lastStreakDay
        if (previous != null) {
            val days = today - previous
            if (days <= 0) return
            streak = if (days == 1L) streak + 1 else 1
        } else {
            streak = 1
        }
        lastStreakDay = today
    }

    /** La celebración de racha se muestra una vez al día. */
    val shouldCelebrateStreak: Boolean
        get() = streak > 0 && lastCelebratedStreakDay != todayEpochDay

    fun markStreakCelebrated() {
        lastCelebratedStreakDay = todayEpochDay
    }

    // ── Comidas de Maicito (máximo 3 cada 4 horas) ──
    val maxMeals = 3
    private val feedingIntervalMs = 4L * 60 * 60 * 1000
    private var mealTimesRaw by saved(prefs.persistedString("mealDates", ""))

    /** Comidas de las últimas 4 horas. */
    val recentMealTimes: List<Long>
        get() {
            val now = System.currentTimeMillis()
            return mealTimesRaw.split(",")
                .mapNotNull { it.toLongOrNull() }
                .filter { now - it in 0 until feedingIntervalMs }
        }

    val availableMeals: Int
        get() = maxOf(0, maxMeals - recentMealTimes.size)

    fun registerMeal() {
        mealTimesRaw = (recentMealTimes + System.currentTimeMillis()).joinToString(",")
    }

    init {
        MusicManager.init(application, musicVolume)
        updateDailyStreak()
    }

    /** Borra todo lo guardado y vuelve a los valores iniciales (solo para pruebas). */
    fun resetDevelopmentProgress() {
        prefs.edit().clear().apply()
        persisted.forEach { it.reset() }
        MusicManager.onVolumeChanged(musicVolume)
    }

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

    fun completeGreetingsLesson() {
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
        }
    }

    // ── Name change cooldown ──

    val canChangeUserName: Boolean
        get() {
            val last = lastUserNameChange ?: return true
            val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
            return System.currentTimeMillis() - last >= sevenDaysMs
        }
}
