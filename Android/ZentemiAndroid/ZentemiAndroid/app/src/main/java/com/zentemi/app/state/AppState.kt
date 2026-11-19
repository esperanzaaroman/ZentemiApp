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
import com.zentemi.app.data.ItemSlot
import com.zentemi.app.data.ShopCatalog
import com.zentemi.app.data.ShopItem
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

    // ── Artículos de la tienda (catálogo en data/ShopCatalog.kt) ──
    // Los 7 artículos originales siguen usando sus propios valores de arriba
    // (compatibles con iOS y Firebase); los nuevos se guardan en estas listas.
    private var ownedItemsRaw by saved(prefs.persistedString("ownedItems", ""))
    private var equippedItemsRaw by saved(prefs.persistedString("equippedItems", ""))

    private fun parseIds(raw: String): Set<String> =
        raw.split(",").filter { it.isNotBlank() }.toSet()

    fun isOwned(item: ShopItem): Boolean = when (item.id) {
        "glasses", "hair", "scarf" -> true
        "sunglasses" -> sunglassesPurchased
        "pinkHair" -> pinkHairPurchased
        "sword" -> swordPurchased
        "suit" -> suitPurchased
        else -> item.price == 0 || item.id in parseIds(ownedItemsRaw)
    }

    fun isEquipped(item: ShopItem): Boolean = when (item.id) {
        "glasses" -> glassesEquipped
        "hair" -> hairEquipped
        "scarf" -> scarfEquipped
        "sunglasses" -> sunglassesEquipped
        "pinkHair" -> pinkHairEquipped
        "sword" -> swordEquipped
        "suit" -> suitEquipped
        else -> item.id in parseIds(equippedItemsRaw)
    }

    private fun setEquipped(item: ShopItem, value: Boolean) {
        when (item.id) {
            "glasses" -> glassesEquipped = value
            "hair" -> hairEquipped = value
            "scarf" -> scarfEquipped = value
            "sunglasses" -> sunglassesEquipped = value
            "pinkHair" -> pinkHairEquipped = value
            "sword" -> swordEquipped = value
            "suit" -> suitEquipped = value
            else -> {
                val ids = parseIds(equippedItemsRaw)
                equippedItemsRaw = (if (value) ids + item.id else ids - item.id).joinToString(",")
            }
        }
    }

    /** Compra un artículo. Devuelve false si no alcanzan las monedas. */
    fun purchase(item: ShopItem): Boolean {
        if (isOwned(item)) return true
        if (coins < item.price) return false
        coins -= item.price
        when (item.id) {
            "sunglasses" -> sunglassesPurchased = true
            "pinkHair" -> pinkHairPurchased = true
            "sword" -> swordPurchased = true
            "suit" -> suitPurchased = true
            else -> ownedItemsRaw = (parseIds(ownedItemsRaw) + item.id).joinToString(",")
        }
        return true
    }

    /** Pone o quita un artículo; al ponerlo se quita lo que había en su zona. */
    fun toggleEquip(item: ShopItem) {
        if (!isOwned(item)) return
        if (isEquipped(item)) {
            setEquipped(item, false)
            return
        }
        ShopCatalog.items
            .filter { it.slot == item.slot && it.id != item.id && isEquipped(it) }
            .forEach { setEquipped(it, false) }
        setEquipped(item, true)
    }

    /** Accesorios puestos, en el orden en que se dibujan sobre Maicito. */
    val equippedAccessories: List<ShopItem>
        get() = ShopCatalog.items
            .filter { it.isAccessory && isEquipped(it) }
            .sortedBy { it.slot.ordinal }

    /** Fondo puesto detrás de Maicito, si hay. */
    val equippedBackdrop: ShopItem?
        get() = ShopCatalog.items.firstOrNull { it.slot == ItemSlot.BACKGROUND && isEquipped(it) }

    // ── Juegos con Maicito: recompensa en las primeras 3 partidas del día ──
    val maxGameRewardsPerDay = 3
    private var gameRewardDay by saved(prefs.persistedLong("gameRewardDay", null))
    private var gameRewardsToday by saved(prefs.persistedInt("gameRewardsToday", 0))

    val gameRewardsLeft: Int
        get() = if (gameRewardDay != todayEpochDay) maxGameRewardsPerDay
        else maxOf(0, maxGameRewardsPerDay - gameRewardsToday)

    /**
     * Da monedas y felicidad por terminar un juego, si quedan recompensas hoy.
     * Devuelve false si ya se usaron las del día (se puede seguir jugando).
     */
    fun claimGameReward(coinsEarned: Int, happinessEarned: Int): Boolean {
        val today = todayEpochDay
        if (gameRewardDay != today) {
            gameRewardDay = today
            gameRewardsToday = 0
        }
        if (gameRewardsToday >= maxGameRewardsPerDay) return false
        gameRewardsToday += 1
        coins += coinsEarned
        happiness = minOf(100, happiness + happinessEarned)
        return true
    }

    /** Los juegos repasan lo aprendido, así que se abren al terminar la lección 1. */
    val gamesUnlocked: Boolean
        get() = greetingsRewardClaimed

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
