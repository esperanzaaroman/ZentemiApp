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
    var createdAt: Timestamp = Timestamp.now()
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
