package com.zentemi.app.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AppState : ViewModel() {

    // ── User ──
    var userName by mutableStateOf("Jugador")
    var lastUserNameChange by mutableStateOf<Long?>(null)

    // ── Pet ──
    var petName by mutableStateOf("Maicito")
    var happiness by mutableIntStateOf(99)

    // ── Profile ──
    var coins by mutableIntStateOf(1500)
    var streak by mutableIntStateOf(5)
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
    var avatarBackgroundHue by mutableFloatStateOf(0f)
    var mascotHue by mutableFloatStateOf(0f)
    var profileBackgroundHue by mutableFloatStateOf(0f)

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
