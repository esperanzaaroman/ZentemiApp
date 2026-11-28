package com.zentemi.app.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/*
 * Modelos documentales de Firestore.
 *
 * Son el espejo exacto de Swift/Zentemi/Zentemi/Models/FirestoreModels.swift
 * (rama feature/firebase-backend). Los nombres de los campos deben coincidir
 * uno a uno con iOS para que ambas apps lean y escriban la misma base de datos.
 *
 * Firestore necesita valores por defecto en todos los campos para poder
 * crear los objetos con toObject(), y `var` para que pueda asignarlos.
 */

// ── Perfil de Adulto / Tutor (`users/{uid}`) ──

data class ParentProfile(
    @DocumentId var id: String? = null,
    var email: String = "",
    var acceptedPrivacyPolicy: Boolean = true,
    var createdAt: Timestamp = Timestamp.now(),
    var lastLogin: Timestamp? = Timestamp.now()
)

// ── Perfil del Menor (`users/{uid}/children/{childId}`) ──

data class ChildProfile(
    @DocumentId var id: String? = null,
    var userName: String = "Jugador",
    var lastUserNameChange: Timestamp? = null,
    var petName: String = "Maicito",
    var happiness: Int = 50,
    var coins: Int = 1500,
    var totalXP: Int = 0,
    var currentLevel: Int = 1,
    var streak: Int = 0,
    var lastPracticeDate: Timestamp? = null,
    var lessonsCompleted: Int = 0,
    var avatar: ChildAvatarSettings = ChildAvatarSettings(),
    var settings: ChildAppSettings = ChildAppSettings(),
    var createdAt: Timestamp = Timestamp.now(),

    // ── Campos nuevos de Android (iOS todavía no los usa) ──
    // Son null cuando el documento no los tiene (perfiles viejos o creados
    // desde iOS). Así se distingue "no viene el dato" de "viene vacío" y no
    // se borra el progreso local al aplicar un documento viejo.
    var pathLessonsCompleted: Int? = null,
    var unlockedPathLesson: Int? = null,
    var greetingsRewardClaimed: Boolean? = null,
    /** Ids de los artículos comprados en la tienda (incluye los 7 originales). */
    var ownedItems: List<String>? = null,
    /** Ids de los artículos que Maicito trae puestos. */
    var equippedItems: List<String>? = null,

    // ── Lecciones 2 a 6, juegos por niveles y logros ──
    // Las claves son las mismas que se guardan en el teléfono (y en
    // UserDefaults de iOS): "familyLesson", "memoryLevelOne", "mazeLevelTwo"...
    /** Lecciones 2 a 6 cuyo premio ya se entregó (claves de PathLesson). */
    var claimedLessonRewards: List<String>? = null,
    var lessonBestCorrectAnswers: Map<String, Int>? = null,
    var lessonBestSeconds: Map<String, Double>? = null,
    /** Niveles terminados de Memorama, Sopa de letras y Laberinto. */
    var completedGameLevels: List<String>? = null,
    /** Mejor tiempo por nivel (solo los que se han jugado). */
    var gameBestSeconds: Map<String, Double>? = null,
    /** Monedas ya cobradas de cada laberinto, un bit por moneda. */
    var mazeClaimedCoinMasks: Map<String, Int>? = null,
    var gamePoints: Int? = null,
    var totalFeedings: Int? = null,
    var bestAchievementStreak: Int? = null,
    /** Monedas ganadas en total (gastarlas no resta). */
    var lifetimeEarnedCoins: Int? = null
)

// ── Configuraciones de Avatar ──

data class ChildAvatarSettings(
    var avatarBackgroundHue: Double = 0.72,
    var mascotHue: Double = 0.0,
    var profileBackgroundHue: Double = 0.73
)

// ── Configuraciones de la App ──

data class ChildAppSettings(
    var musicVolume: Double = 0.35,
    var soundVolume: Double = 1.0
)

// ── Progreso de Lección (`users/{uid}/children/{childId}/progress/{lessonId}`) ──

data class LessonProgressRecord(
    @DocumentId var id: String? = null,
    var lessonId: String = "",
    var completed: Boolean = false,
    var rewardClaimed: Boolean = false,
    var correctAnswers: Int = 0,
    var totalAnswers: Int = 0,
    var elapsedSeconds: Int = 0,
    var completedAt: Timestamp = Timestamp.now()
)

// ── Inventario y Tienda (`users/{uid}/children/{childId}/inventory/{itemId}`) ──

data class InventoryItemRecord(
    @DocumentId var id: String? = null,
    var itemId: String = "",
    var purchased: Boolean = false,
    var equipped: Boolean = false,
    var purchasedAt: Timestamp? = null
)

// ── Bitácora de Auditoría en la Nube (`users/{uid}/audit_logs/{logId}`) ──

data class AuditLogRecord(
    @DocumentId var id: String? = null,
    var eventType: String = "",
    var details: String = "",
    var deviceModel: String = "Android Device",
    @ServerTimestamp var timestamp: Timestamp? = null
)
