package com.zentemi.app.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.zentemi.app.audio.MusicManager
import com.zentemi.app.data.AuthService
import com.zentemi.app.data.ChildAppSettings
import com.zentemi.app.data.ChildAvatarSettings
import com.zentemi.app.data.ChildProfile
import com.zentemi.app.data.FirestoreService
import com.zentemi.app.data.InventoryItemRecord
import com.zentemi.app.data.LessonProgressRecord
import com.zentemi.app.data.NetworkMonitor
import com.zentemi.app.data.ItemSlot
import com.zentemi.app.data.ShopCatalog
import com.zentemi.app.data.ShopItem
import com.zentemi.app.R
import com.zentemi.app.data.GAME_POINTS_GOAL
import com.zentemi.app.data.LEVEL_NAMES
import com.zentemi.app.data.LessonReward
import com.zentemi.app.data.MazeReward
import com.zentemi.app.data.MemoryLevel
import com.zentemi.app.data.PathLesson
import com.zentemi.app.data.ProfileAchievement
import com.zentemi.app.data.WordSearchLevel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Estado global de la app. Igual que AppState.swift, casi todo se guarda en
 * el teléfono (SharedPreferences) y se recupera al abrir la app.
 *
 * Con sesión iniciada, el perfil del menor además se sincroniza con
 * Firestore (ver "Sincronización con Firestore" al final).
 */
@OptIn(FlowPreview::class)
class AppState @JvmOverloads constructor(
    application: Application,
    /** Reloj de la app. Las pruebas pasan uno propio para controlar la hora. */
    private val clock: Clock = Clock.systemUTC()
) : AndroidViewModel(application) {

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

    /** Suma XP y recalcula el nivel (tope en [maxLevel]). Ignora 0 o negativos. */
    fun addXP(amount: Int) {
        if (amount <= 0) return
        totalXP += amount
        currentLevel = minOf((totalXP / xpRequiredPerLevel) + 1, maxLevel)
    }

    // ── Avatar customization ──
    var avatarBackgroundHue by saved(prefs.persistedFloat("avatarBackgroundHue", 0.72f))
    var mascotHue by saved(prefs.persistedFloat("mascotHue", 0f))
    var profileBackgroundHue by saved(prefs.persistedFloat("profileBackgroundHue", 0.73f))

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
    var familyExerciseCount by mutableIntStateOf(5)

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
        get() = LocalDate.now(clock.withZone(streakZone)).toEpochDay()

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
        bestAchievementStreak = maxOf(bestAchievementStreak, streak)
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
            val now = clock.millis()
            return mealTimesRaw.split(",")
                .mapNotNull { it.toLongOrNull() }
                .filter { now - it in 0 until feedingIntervalMs }
        }

    val availableMeals: Int
        get() = maxOf(0, maxMeals - recentMealTimes.size)

    /** Registra una comida. Devuelve false (y no la cuenta) si ya comió 3 veces en 4 horas. */
    fun registerMeal(): Boolean {
        val recent = recentMealTimes
        if (recent.size >= maxMeals) return false
        mealTimesRaw = (recent + clock.millis()).joinToString(",")
        totalFeedings += 1
        return true
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
        recordInventoryItem(itemId = item.id, purchased = true, equipped = false)
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

    /** Quita todo lo que Maicito trae puesto en esas zonas (botón "quitar" de la tienda). */
    fun unequipSlots(slots: Set<ItemSlot>) {
        ShopCatalog.items
            .filter { it.slot in slots && isEquipped(it) }
            .forEach { setEquipped(it, false) }
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
        addCoins(coinsEarned)
        happiness = minOf(100, happiness + happinessEarned)
        return true
    }

    /** Los juegos repasan lo aprendido, así que se abren al terminar la lección 1. */
    val gamesUnlocked: Boolean
        get() = greetingsRewardClaimed

    // ── Monedas ganadas (para los logros; gastarlas no resta) ──
    private val lifetimeCoinsSaved = saved(prefs.persistedInt("lifetimeEarnedCoins", 0))
    var lifetimeEarnedCoins by lifetimeCoinsSaved
        private set

    /** Suma monedas ganadas en lecciones o juegos (no regalos ni devoluciones). */
    fun addCoins(amount: Int) {
        coins += amount
        if (amount > 0) lifetimeEarnedCoins += amount
    }

    // ── Logros: comidas y mejor racha ──
    private val totalFeedingsSaved = saved(prefs.persistedInt("totalFeedings", 0))
    var totalFeedings by totalFeedingsSaved
        private set
    var bestAchievementStreak by saved(prefs.persistedInt("bestAchievementStreak", 0))
        private set

    // ── Lecciones 2 a 6 (Familia, Números, Colores, Animales, Alimentos) ──
    // Mismas claves y premios que AppState.swift.
    private val lessonRewardClaimed = PathLesson.entries.associateWith {
        saved(prefs.persistedBoolean("${it.key}RewardClaimed", false))
    }
    private val lessonBestCorrect = PathLesson.entries.associateWith {
        saved(prefs.persistedInt("${it.key}BestCorrectAnswers", 0))
    }
    private val lessonBestSeconds = PathLesson.entries.associateWith {
        saved(prefs.persistedFloat("${it.key}BestSeconds", 0f))
    }

    /**
     * Ya terminó la lección. Además del valor guardado en el teléfono se
     * revisa el camino (que sí viaja por Firestore), para que en otro
     * teléfono con la misma cuenta la lección aparezca terminada.
     */
    fun isLessonCompleted(lesson: PathLesson): Boolean =
        lessonRewardClaimed.getValue(lesson).value || pathLessonsCompleted >= lesson.number

    /** Se puede abrir: la lección anterior está terminada. */
    fun isLessonUnlocked(lesson: PathLesson): Boolean {
        val previous = lesson.previous
        return if (previous == null) greetingsRewardClaimed || pathLessonsCompleted >= 1
        else isLessonCompleted(previous)
    }

    fun lessonBestCorrectAnswers(lesson: PathLesson): Int = lessonBestCorrect.getValue(lesson).value
    fun lessonBestSecondsOf(lesson: PathLesson): Double = lessonBestSeconds.getValue(lesson).value.toDouble()

    /**
     * Se llama al terminar los cinco ejercicios (no al mostrar resultados).
     * La primera vez entrega monedas y XP y abre la siguiente lección.
     * Devuelve null si los datos no son válidos o la lección sigue bloqueada.
     */
    fun completeLesson(lesson: PathLesson, correctAnswers: Int, seconds: Double): LessonReward? {
        if (!isLessonUnlocked(lesson) || correctAnswers !in 0..lesson.exerciseCount ||
            !seconds.isFinite() || seconds < 0
        ) return null

        val best = lessonBestCorrect.getValue(lesson)
        best.value = maxOf(best.value, correctAnswers)
        val bestTime = lessonBestSeconds.getValue(lesson)
        if (bestTime.value == 0f || seconds < bestTime.value) {
            bestTime.value = maxOf(0.01f, seconds.toFloat())
        }

        val startingXP = totalXP
        // Si otro teléfono ya la terminó (camino sincronizado), no se paga dos veces.
        val firstCompletion = !isLessonCompleted(lesson)
        lessonRewardClaimed.getValue(lesson).value = true
        if (firstCompletion) {
            addCoins(lesson.coins)
            addXP(lesson.xp)
            lessonsCompleted += 1
        }
        pathLessonsCompleted = maxOf(pathLessonsCompleted, lesson.number)
        unlockedPathLesson = maxOf(unlockedPathLesson, lesson.number + 1)

        saveLessonProgressToCloud(
            lessonId = lesson.key,
            correctAnswers = correctAnswers,
            totalAnswers = lesson.exerciseCount,
            elapsedSeconds = seconds.toInt()
        )
        return LessonReward(
            coins = if (firstCompletion) lesson.coins else 0,
            xp = if (firstCompletion) lesson.xp else 0,
            startingXP = startingXP
        )
    }

    // ── Puntos de los juegos (Memorama, Sopa de letras y Laberinto) ──
    var gamePoints by saved(prefs.persistedInt("gamePoints", 0))
        private set
    val gamePointsGoal = GAME_POINTS_GOAL

    // ── Memorama por niveles ──
    private val memoryCompletedState = MemoryLevel.entries.associateWith {
        saved(prefs.persistedBoolean("${it.key}Completed", false))
    }
    private val memoryBestState = MemoryLevel.entries.associateWith {
        saved(prefs.persistedFloat("${it.key}BestSeconds", 0f))
    }

    fun memoryCompleted(level: MemoryLevel): Boolean = memoryCompletedState.getValue(level).value
    fun memoryUnlocked(level: MemoryLevel): Boolean =
        level == MemoryLevel.ONE || memoryCompleted(MemoryLevel.entries[level.ordinal - 1])
    fun memoryBestSeconds(level: MemoryLevel): Double = memoryBestState.getValue(level).value.toDouble()

    /** Devuelve si esta victoria entregó el premio (solo la primera vez). */
    fun completeMemory(level: MemoryLevel, seconds: Double): Boolean {
        if (!memoryUnlocked(level) || !seconds.isFinite() || seconds < 0) return false
        val best = memoryBestState.getValue(level)
        if (best.value == 0f || seconds < best.value) best.value = maxOf(0.01f, seconds.toFloat())
        val completed = memoryCompletedState.getValue(level)
        if (completed.value) return false
        completed.value = true
        addCoins(level.coins)
        gamePoints += level.points
        return true
    }

    // ── Sopa de letras por niveles ──
    private val wordSearchCompletedState = WordSearchLevel.entries.associateWith {
        saved(prefs.persistedBoolean("${it.key}Completed", false))
    }
    private val wordSearchBestState = WordSearchLevel.entries.associateWith {
        saved(prefs.persistedFloat("${it.key}BestSeconds", 0f))
    }

    fun wordSearchCompleted(level: WordSearchLevel): Boolean = wordSearchCompletedState.getValue(level).value
    fun wordSearchUnlocked(level: WordSearchLevel): Boolean =
        level == WordSearchLevel.ONE || wordSearchCompleted(WordSearchLevel.entries[level.ordinal - 1])
    fun wordSearchBestSeconds(level: WordSearchLevel): Double =
        wordSearchBestState.getValue(level).value.toDouble()

    fun completeWordSearch(level: WordSearchLevel, seconds: Double): Boolean {
        if (!wordSearchUnlocked(level) || !seconds.isFinite() || seconds < 0) return false
        val best = wordSearchBestState.getValue(level)
        if (best.value == 0f || seconds < best.value) best.value = maxOf(0.01f, seconds.toFloat())
        val completed = wordSearchCompletedState.getValue(level)
        if (completed.value) return false
        completed.value = true
        addCoins(level.coins)
        gamePoints += level.points
        return true
    }

    // ── Laberinto, 5 niveles ──
    /** Monedas y puntos del primer triunfo de cada nivel (iOS: mazeLevelOneCoins...). */
    val mazeLevelCoins = listOf(300, 450, 600, 800, 1_000)
    val mazeLevelPoints = listOf(500, 1_400, 2_000, 2_800, 3_500)
    /** Cada moneda recogida en el laberinto vale 10. */
    val mazeCoinValue = 10

    private val mazeCompletedState = (1..5).map {
        saved(prefs.persistedBoolean("mazeLevel${LEVEL_NAMES[it - 1]}Completed", false))
    }
    private val mazeBestState = (1..5).map {
        saved(prefs.persistedFloat("mazeLevel${LEVEL_NAMES[it - 1]}BestSeconds", 0f))
    }
    /** Un bit por moneda ya cobrada (el nivel 1 no tiene monedas). */
    private val mazeCoinMaskState = (1..5).map {
        saved(prefs.persistedInt("mazeLevel${LEVEL_NAMES[it - 1]}ClaimedCoinMask", 0))
    }

    fun mazeCompleted(level: Int): Boolean = mazeCompletedState.getOrNull(level - 1)?.value ?: false
    fun mazeUnlocked(level: Int): Boolean = level == 1 || mazeCompleted(level - 1)
    fun mazeBestSeconds(level: Int): Double = mazeBestState.getOrNull(level - 1)?.value?.toDouble() ?: 0.0
    fun mazeClaimedCoinMask(level: Int): Int =
        if (level <= 1) 0 else mazeCoinMaskState.getOrNull(level - 1)?.value ?: 0

    /** Se llama al llegar a la salida correcta. Las monedas solo se cobran una vez cada una. */
    fun completeMaze(level: Int, seconds: Double, collectedCoinIds: Set<Int>, coinCount: Int): MazeReward {
        if (level !in 1..5 || !mazeUnlocked(level) || !seconds.isFinite() || seconds < 0) return MazeReward.NONE
        val index = level - 1
        val best = mazeBestState[index]
        if (best.value == 0f || seconds < best.value) best.value = maxOf(0.01f, seconds.toFloat())

        val firstWin = !mazeCompletedState[index].value
        val baseCoins = if (firstWin) mazeLevelCoins[index] else 0
        val points = if (firstWin) mazeLevelPoints[index] else 0
        var collected = 0
        if (level > 1) {
            var mask = mazeCoinMaskState[index].value
            for (id in collectedCoinIds) {
                if (id !in 0 until coinCount) continue
                val bit = 1 shl id
                if (mask and bit == 0) {
                    mask = mask or bit
                    collected += mazeCoinValue
                }
            }
            mazeCoinMaskState[index].value = mask
        }
        mazeCompletedState[index].value = true
        addCoins(baseCoins + collected)
        gamePoints += points
        return MazeReward(baseCoins, collected, points)
    }

    // ── Logros del perfil (ProfileAchievement.swift) ──
    val profileAchievements: List<ProfileAchievement>
        get() {
            val completedGames = listOf(
                (1..5).all { mazeCompleted(it) },
                MemoryLevel.entries.all { memoryCompleted(it) },
                WordSearchLevel.entries.all { wordSearchCompleted(it) }
            ).count { it }
            val completedLessons = (if (greetingsRewardClaimed) 1 else 0) +
                PathLesson.entries.count { isLessonCompleted(it) }
            return listOf(
                ProfileAchievement("games", "Todos los juegos",
                    "Completa todos los niveles de Laberinto, Memorama y Sopa de letras.",
                    R.drawable.achievement_games, completedGames, 3),
                ProfileAchievement("lessons", "Todas las lecciones",
                    "Completa las seis lecciones disponibles, de Primeras frases a Alimentos.",
                    R.drawable.achievement_lessons, completedLessons, 6),
                ProfileAchievement("feed10", "10 comidas", "Dale de comer a $petName 10 veces.",
                    R.drawable.achievement_feed_10, totalFeedings, 10),
                ProfileAchievement("feed50", "50 comidas", "Dale de comer a $petName 50 veces.",
                    R.drawable.achievement_feed_50, totalFeedings, 50),
                ProfileAchievement("feed100", "100 comidas", "Dale de comer a $petName 100 veces.",
                    R.drawable.achievement_feed_100, totalFeedings, 100),
                ProfileAchievement("coins1000", "1,000 monedas",
                    "Gana 1,000 monedas en juegos y lecciones. Gastarlas no resta avance.",
                    R.drawable.achievement_coins_1000, lifetimeEarnedCoins, 1_000),
                ProfileAchievement("coins5000", "5,000 monedas",
                    "Gana 5,000 monedas en juegos y lecciones. Gastarlas no resta avance.",
                    R.drawable.achievement_coins_5000, lifetimeEarnedCoins, 5_000),
                ProfileAchievement("coins10000", "10,000 monedas",
                    "Gana 10,000 monedas en juegos y lecciones. Gastarlas no resta avance.",
                    R.drawable.achievement_coins_10000, lifetimeEarnedCoins, 10_000),
                ProfileAchievement("streak5", "Racha de 5 días",
                    "Entra 5 días consecutivos. El logro se conserva aunque termine la racha.",
                    R.drawable.achievement_streak_5, bestAchievementStreak, 5),
                ProfileAchievement("streak10", "Racha de 10 días",
                    "Entra 10 días consecutivos. El logro se conserva aunque termine la racha.",
                    R.drawable.achievement_streak_10, bestAchievementStreak, 10),
                ProfileAchievement("streak20", "Racha de 20 días",
                    "Entra 20 días consecutivos. El logro se conserva aunque termine la racha.",
                    R.drawable.achievement_streak_20, bestAchievementStreak, 20)
            )
        }

    init {
        // Versiones anteriores no contaban esto: se reconstruye una sola vez
        // con lo que ya se ganó (igual que loadAchievementProgress en iOS).
        if (!totalFeedingsSaved.isStored) totalFeedings = recentMealTimes.size
        bestAchievementStreak = maxOf(bestAchievementStreak, streak)
        if (!lifetimeCoinsSaved.isStored) {
            var recovered = if (greetingsRewardClaimed) 700 else 0
            PathLesson.entries.forEach { if (lessonRewardClaimed.getValue(it).value) recovered += it.coins }
            MemoryLevel.entries.forEach { if (memoryCompleted(it)) recovered += it.coins }
            WordSearchLevel.entries.forEach { if (wordSearchCompleted(it)) recovered += it.coins }
            (1..5).forEach {
                if (mazeCompleted(it)) recovered += mazeLevelCoins[it - 1]
                recovered += Integer.bitCount(mazeClaimedCoinMask(it)) * mazeCoinValue
            }
            lifetimeEarnedCoins = recovered
        }
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

    fun completeGreetingsLesson(
        correctAnswers: Int = 5,
        totalAnswers: Int = 5,
        elapsedSeconds: Int = 120
    ) {
        if (!greetingsRewardClaimed) {
            greetingsRewardClaimed = true
            addCoins(700)
            addXP(140)
            lessonsCompleted += 1
            if (pathLessonsCompleted < 1) {
                pathLessonsCompleted = 1
                unlockedPathLesson = maxOf(unlockedPathLesson, 2)
            }

            saveLessonProgressToCloud("greetings", correctAnswers, totalAnswers, elapsedSeconds)
        }
    }

    /** Con sesión iniciada, guarda el resultado de una lección en Firestore. */
    private fun saveLessonProgressToCloud(
        lessonId: String,
        correctAnswers: Int,
        totalAnswers: Int,
        elapsedSeconds: Int
    ) {
        val parentUID = activeParentUID ?: return
        val childId = activeChildId ?: return
        viewModelScope.launch {
            runCatching {
                FirestoreService.saveLessonProgress(
                    parentUID = parentUID,
                    childId = childId,
                    progress = LessonProgressRecord(
                        lessonId = lessonId,
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

    // ── Name change cooldown ──

    val canChangeUserName: Boolean
        get() {
            val last = lastUserNameChange ?: return true
            val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
            return clock.millis() - last >= sevenDaysMs
        }

    // ── Modo invitado ──
    // La cuenta es opcional. Sin sesión todo se guarda solo en el teléfono y
    // no se llama a Firebase (activeParentUID queda en null).

    /** Ya vio el onboarding y escribió su nombre; la app abre directo en Inicio. */
    var hasCompletedOnboarding by saved(prefs.persistedBoolean("hasCompletedOnboarding", false))

    /** Última vez que respondió al diálogo "¿Quieres guardar tu progreso en la nube?". */
    private var lastAccountPromptAnswered by saved(prefs.persistedLong("lastAccountPromptDismissed", null))

    /** Cuenta cuyo progreso es el que hay ahora en el teléfono ("" = ninguna). */
    private var boundAccountUid by saved(prefs.persistedString("boundAccountUid", ""))

    /**
     * Huella del progreso en el último momento en que el teléfono estaba
     * igual que una cuenta (o "" = valores iniciales). Si el progreso actual
     * es distinto, hay progreso hecho como invitado.
     */
    private var accountProgressBaseline by saved(prefs.persistedString("accountProgressBaseline", ""))

    /** true mientras no haya sesión iniciada. */
    var isGuest by mutableStateOf(AuthService.currentUser.value == null)
        private set

    /** Correo de la cuenta con sesión, o null en modo invitado. */
    var accountEmail by mutableStateOf<String?>(AuthService.currentUser.value?.email)
        private set

    private val accountPromptIntervalMs = 7L * 24 * 60 * 60 * 1000

    /** Si toca sugerir crear cuenta (falta revisar que haya red, lo hace la navegación). */
    val isAccountSuggestionDue: Boolean
        get() {
            if (!isGuest) return false
            val last = lastAccountPromptAnswered ?: return true
            return clock.millis() - last >= accountPromptIntervalMs
        }

    /** Se llama al responder el diálogo (con cualquiera de los dos botones). */
    fun markAccountSuggestionAnswered() {
        lastAccountPromptAnswered = clock.millis()
    }

    private fun progressSignature(): String = listOf(
        totalXP,
        coins,
        pathLessonsCompleted,
        lessonsCompleted,
        ownedItemIds().sorted().joinToString(",")
    ).joinToString("|")

    private val initialProgressSignature = "0|1500|0|0|"

    /** Hay progreso hecho sin cuenta que no está en ninguna cuenta. */
    private val hasGuestProgress: Boolean
        get() = progressSignature() != accountProgressBaseline.ifEmpty { initialProgressSignature }

    // ── Elegir progreso al iniciar sesión ──

    /** Cuenta con perfil en la nube esperando que el niño elija qué progreso usar. */
    class PendingProgressChoice internal constructor(
        val parentUID: String,
        internal val cloudProfile: ChildProfile
    )

    var pendingProgressChoice by mutableStateOf<PendingProgressChoice?>(null)
        private set

    /** "Usar el progreso de este teléfono": se sube a la cuenta y reemplaza el de la nube. */
    fun keepPhoneProgress() {
        val choice = pendingProgressChoice ?: return
        pendingProgressChoice = null
        val childId = choice.cloudProfile.id ?: return
        childCreatedAt = choice.cloudProfile.createdAt
        activeChildId = childId
        val profile = exportChildProfile() ?: return
        lastSyncedProfile = profile
        // set() se registra en la caché local antes de conectar el listener,
        // así que el listener recibe ya el progreso del teléfono (con o sin red).
        viewModelScope.launch {
            runCatching { FirestoreService.saveChildProfile(choice.parentUID, profile) }
                .onFailure { Log.w(TAG, "No se pudo subir el progreso del teléfono", it) }
        }
        finishBinding(choice.parentUID, childId)
    }

    /** "Usar el progreso de mi cuenta": se descarga el de la nube. */
    fun useAccountProgress() {
        val choice = pendingProgressChoice ?: return
        pendingProgressChoice = null
        val childId = choice.cloudProfile.id ?: return
        activeChildId = childId
        applyChildProfile(choice.cloudProfile)
        updateDailyStreak()
        finishBinding(choice.parentUID, childId)
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
        // Quien ya tenía sesión en la v1.1 (antes del modo invitado) no vuelve
        // a ver el onboarding, y su progreso ya es el de su cuenta.
        AuthService.currentUser.value?.let { user ->
            hasCompletedOnboarding = true
            if (boundAccountUid.isEmpty()) boundAccountUid = user.uid
        }

        // Vincula o desvincula la cuenta según la sesión de Firebase Auth
        viewModelScope.launch {
            AuthService.currentUser.collect { user ->
                isGuest = user == null
                accountEmail = user?.email
                if (user != null) bindParentAccount(user.uid, user.email ?: "")
                else unbindAccount()
            }
        }

        // Si la vinculación falló por falta de red, se reintenta al volver
        viewModelScope.launch {
            NetworkMonitor.isOnline.collect { online ->
                val user = AuthService.currentUser.value
                if (online && user != null && activeParentUID == null) {
                    bindParentAccount(user.uid, user.email ?: "")
                }
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
                runCatching { FirestoreService.createOrUpdateParentProfile(uid = uid, email = email) }
                    .onFailure { Log.w(TAG, "No se pudo actualizar el perfil del tutor", it) }

                // 2. Buscar el perfil del menor. Si es la cuenta que ya está en
                //    el teléfono basta la caché (funciona sin red); si es otra,
                //    se pregunta al servidor para no confundir "sin red" con
                //    "cuenta sin perfil" y crear un perfil duplicado.
                val cloudChild = FirestoreService
                    .fetchChildren(uid, fromServer = boundAccountUid != uid)
                    .firstOrNull { it.id != null }
                if (activeParentUID != uid) return@launch // cerró sesión mientras tanto

                when {
                    // Cuenta nueva (recién registrada): el perfil nace con el
                    // progreso que el niño ya tenía en el teléfono.
                    cloudChild == null -> {
                        childCreatedAt = Timestamp.now()
                        val created = FirestoreService.createChildProfile(
                            parentUID = uid,
                            profile = buildChildProfile(id = null)
                        )
                        val childId = created.id ?: return@launch
                        activeChildId = childId
                        lastSyncedProfile = exportChildProfile()
                        finishBinding(uid, childId)
                    }

                    // Cuenta con perfil y progreso de invitado en el teléfono:
                    // se pregunta cuál usar antes de sincronizar nada.
                    boundAccountUid != uid && hasGuestProgress -> {
                        pendingProgressChoice = PendingProgressChoice(uid, cloudChild)
                    }

                    // Misma cuenta de antes, o teléfono sin progreso propio.
                    else -> {
                        val childId = cloudChild.id ?: return@launch
                        activeChildId = childId
                        applyChildProfile(cloudChild)
                        updateDailyStreak()
                        finishBinding(uid, childId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar con Firestore: ${e.message}", e)
                // Se libera para que el próximo cambio de sesión lo vuelva a intentar
                if (activeParentUID == uid && activeChildId == null && pendingProgressChoice == null) {
                    activeParentUID = null
                }
            } finally {
                isSyncingWithCloud = false
            }
        }
    }

    /** Marca el teléfono como copia de esa cuenta y escucha sus cambios. */
    private fun finishBinding(uid: String, childId: String) {
        boundAccountUid = uid
        accountProgressBaseline = progressSignature()

        profileListener?.remove()
        profileListener = FirestoreService.listenToChildProfile(uid, childId) { result ->
            result.onSuccess { applyChildProfile(it) }
        }
    }

    private fun unbindAccount() {
        profileListener?.remove()
        profileListener = null
        pendingProgressChoice = null
        activeParentUID = null
        activeChildId = null
        childCreatedAt = null
        lastSyncedProfile = null

        // Al cerrar sesión el progreso se queda en el teléfono y la app sigue
        // en modo invitado. Desde aquí, lo nuevo cuenta como progreso de invitado.
        if (boundAccountUid.isNotEmpty()) {
            boundAccountUid = ""
            accountProgressBaseline = progressSignature()
        }
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
        lastStreakDay = child.lastPracticeDate?.let { epochDayOf(it) }
        lessonsCompleted = child.lessonsCompleted

        avatarBackgroundHue = child.avatar.avatarBackgroundHue.toFloat()
        mascotHue = child.avatar.mascotHue.toFloat()
        profileBackgroundHue = child.avatar.profileBackgroundHue.toFloat()

        musicVolume = child.settings.musicVolume.toFloat()
        soundVolume = child.settings.soundVolume.toFloat()

        // Campos nuevos: si el documento no los trae (perfil viejo o de iOS),
        // se queda lo que ya había en el teléfono.
        child.pathLessonsCompleted?.let { pathLessonsCompleted = it }
        child.unlockedPathLesson?.let { unlockedPathLesson = it }
        child.greetingsRewardClaimed?.let { greetingsRewardClaimed = it }
        child.ownedItems?.let { applyOwnedItems(it.toSet()) }
        child.equippedItems?.let { applyEquippedItems(it.toSet()) }

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

    /** Día (en Ciudad de México) de una fecha de Firestore, contado desde 1970. */
    private fun epochDayOf(timestamp: Timestamp): Long =
        Instant.ofEpochSecond(timestamp.seconds).atZone(streakZone).toLocalDate().toEpochDay()

    /** Inicio de ese día en Ciudad de México, como lo guarda iOS en lastPracticeDate. */
    private fun timestampOfDay(epochDay: Long): Timestamp =
        Timestamp(LocalDate.ofEpochDay(epochDay).atStartOfDay(streakZone).toEpochSecond(), 0)

    /** Ids de los artículos comprados (sin los que vienen de regalo). */
    private fun ownedItemIds(): List<String> =
        ShopCatalog.items.filter { it.price > 0 && isOwned(it) }.map { it.id }

    private fun equippedItemIds(): List<String> =
        ShopCatalog.items.filter { isEquipped(it) }.map { it.id }

    private fun applyOwnedItems(ids: Set<String>) {
        sunglassesPurchased = "sunglasses" in ids
        pinkHairPurchased = "pinkHair" in ids
        swordPurchased = "sword" in ids
        suitPurchased = "suit" in ids
        ownedItemsRaw = (ids - LEGACY_ITEM_IDS).sorted().joinToString(",")
    }

    private fun applyEquippedItems(ids: Set<String>) {
        glassesEquipped = "glasses" in ids
        hairEquipped = "hair" in ids
        scarfEquipped = "scarf" in ids
        sunglassesEquipped = "sunglasses" in ids
        pinkHairEquipped = "pinkHair" in ids
        swordEquipped = "sword" in ids
        suitEquipped = "suit" in ids
        equippedItemsRaw = (ids - LEGACY_ITEM_IDS).sorted().joinToString(",")
    }

    /** Estado actual como documento de Firestore, o null si no hay cuenta vinculada. */
    fun exportChildProfile(): ChildProfile? {
        val childId = activeChildId ?: return null
        return buildChildProfile(childId)
    }

    private fun buildChildProfile(id: String?): ChildProfile {
        return ChildProfile(
            id = id,
            userName = userName,
            lastUserNameChange = lastUserNameChange?.let { Timestamp(java.util.Date(it)) },
            petName = petName,
            happiness = happiness,
            coins = coins,
            totalXP = totalXP,
            currentLevel = currentLevel,
            streak = streak,
            lastPracticeDate = lastStreakDay?.let { timestampOfDay(it) },
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
            createdAt = childCreatedAt ?: Timestamp.now(),
            pathLessonsCompleted = pathLessonsCompleted,
            unlockedPathLesson = unlockedPathLesson,
            greetingsRewardClaimed = greetingsRewardClaimed,
            ownedItems = ownedItemIds(),
            equippedItems = equippedItemIds()
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

        /** Artículos originales que se guardan con sus propios valores (compatibles con iOS). */
        val LEGACY_ITEM_IDS = setOf("glasses", "hair", "scarf", "sunglasses", "pinkHair", "sword", "suit")
    }
}
