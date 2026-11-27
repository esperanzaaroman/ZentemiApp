package com.zentemi.app.data

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Servicio central para la gestión y sincronización de datos con Cloud Firestore.
 *
 * Equivalente Android de Swift/Zentemi/Zentemi/Services/FirestoreService.swift.
 * Usa exactamente las mismas colecciones y documentos:
 *
 *   users/{uid}
 *   users/{uid}/children/{childId}
 *   users/{uid}/children/{childId}/progress/{lessonId}
 *   users/{uid}/children/{childId}/inventory/{itemId}
 *   users/{uid}/audit_logs/{logId}
 */
object FirestoreService {

    private const val TAG = "FirestoreService"

    /** Mismo texto que usa iOS ("iOS Device") para distinguir la plataforma en la bitácora. */
    private const val DEVICE_MODEL = "Android Device"

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            // Persistencia en caché local para soporte offline (igual que iOS)
            firestoreSettings = firestoreSettings {
                setLocalCacheSettings(persistentCacheSettings { })
            }
        }
    }

    /** Alcance para escrituras "dispara y olvida" (bitácora de auditoría). */
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Referencias a Colecciones ──

    private fun usersCollection(): CollectionReference =
        db.collection("users")

    private fun childrenCollection(parentUID: String): CollectionReference =
        usersCollection().document(parentUID).collection("children")

    private fun progressCollection(parentUID: String, childId: String): CollectionReference =
        childrenCollection(parentUID).document(childId).collection("progress")

    private fun inventoryCollection(parentUID: String, childId: String): CollectionReference =
        childrenCollection(parentUID).document(childId).collection("inventory")

    private fun auditLogsCollection(parentUID: String): CollectionReference =
        usersCollection().document(parentUID).collection("audit_logs")

    // ── Gestión de Cuenta de Adulto / Tutor (`users/{uid}`) ──

    /** Registra o actualiza los datos del tutor legal en Firestore. */
    suspend fun createOrUpdateParentProfile(
        uid: String,
        email: String,
        acceptedPrivacyPolicy: Boolean = true
    ) {
        val profile = ParentProfile(
            email = email,
            acceptedPrivacyPolicy = acceptedPrivacyPolicy
        )
        usersCollection().document(uid)
            .set(profile, SetOptions.merge())
            .await()
    }

    /** Obtiene el perfil del tutor legal. */
    suspend fun fetchParentProfile(uid: String): ParentProfile? {
        val snapshot = usersCollection().document(uid).get().await()
        return runCatching { snapshot.toObject(ParentProfile::class.java) }.getOrNull()
    }

    // ── Gestión de Perfiles Infantiles (`users/{uid}/children/{childId}`) ──

    /**
     * Obtiene la lista de perfiles infantiles asociados a la cuenta del tutor.
     *
     * Con [fromServer] = true no se acepta la caché: sin red lanza error en vez
     * de devolver una lista vacía (que haría creer que la cuenta es nueva).
     */
    suspend fun fetchChildren(parentUID: String, fromServer: Boolean = false): List<ChildProfile> {
        val source = if (fromServer) Source.SERVER else Source.DEFAULT
        val snapshot = childrenCollection(parentUID).get(source).await()
        return snapshot.documents.mapNotNull { doc ->
            runCatching { doc.toObject(ChildProfile::class.java) }.getOrNull()
        }
    }

    /**
     * Crea un nuevo perfil infantil bajo la tutela del adulto autenticado.
     * Recibe el perfil completo para que una cuenta nueva empiece con el
     * progreso que el menor ya tenía en el teléfono como invitado.
     *
     * No espera la confirmación del servidor: sin red, la escritura queda en
     * la caché offline de Firestore y se sube sola al volver la conexión.
     */
    fun createChildProfile(parentUID: String, profile: ChildProfile): ChildProfile {
        val childRef = childrenCollection(parentUID).document()
        val newProfile = profile.copy(id = childRef.id)
        childRef.set(newProfile)
            .addOnFailureListener { Log.w(TAG, "No se pudo crear el perfil del menor", it) }
        return newProfile
    }

    /** Guarda o actualiza el estado completo del perfil infantil (monedas, XP, racha, etc.). */
    suspend fun saveChildProfile(parentUID: String, child: ChildProfile) {
        val childId = requireNotNull(child.id) {
            "El perfil del menor carece de identificador (id)."
        }
        childrenCollection(parentUID).document(childId)
            .set(child, SetOptions.merge())
            .await()
    }

    /** Listener en tiempo real para mantener sincronizado el perfil infantil con la nube. */
    fun listenToChildProfile(
        parentUID: String,
        childId: String,
        onChange: (Result<ChildProfile>) -> Unit
    ): ListenerRegistration {
        return childrenCollection(parentUID).document(childId)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> onChange(Result.failure(error))
                    snapshot == null || !snapshot.exists() -> onChange(
                        Result.failure(NoSuchElementException("Documento de menor no encontrado"))
                    )
                    else -> onChange(
                        runCatching { snapshot.toObject(ChildProfile::class.java)!! }
                    )
                }
            }
    }

    // ── Progreso de Lecciones (`users/{uid}/children/{childId}/progress/{lessonId}`) ──

    /** Registra el resultado obtenido en una lección. */
    suspend fun saveLessonProgress(
        parentUID: String,
        childId: String,
        progress: LessonProgressRecord
    ) {
        progressCollection(parentUID, childId).document(progress.lessonId)
            .set(progress, SetOptions.merge())
            .await()
    }

    /** Consulta el progreso específico de una lección. */
    suspend fun fetchLessonProgress(
        parentUID: String,
        childId: String,
        lessonId: String
    ): LessonProgressRecord? {
        val snapshot = progressCollection(parentUID, childId).document(lessonId).get().await()
        return runCatching { snapshot.toObject(LessonProgressRecord::class.java) }.getOrNull()
    }

    // ── Inventario y Accesorios (`users/{uid}/children/{childId}/inventory/{itemId}`) ──

    /** Guarda o actualiza el estado de compra y equipamiento de un ítem de Maicito. */
    suspend fun saveInventoryItem(
        parentUID: String,
        childId: String,
        item: InventoryItemRecord
    ) {
        inventoryCollection(parentUID, childId).document(item.itemId)
            .set(item, SetOptions.merge())
            .await()
    }

    /** Obtiene todos los ítems adquiridos o equipados en el inventario del menor. */
    suspend fun fetchInventory(parentUID: String, childId: String): List<InventoryItemRecord> {
        val snapshot = inventoryCollection(parentUID, childId).get().await()
        return snapshot.documents.mapNotNull { doc ->
            runCatching { doc.toObject(InventoryItemRecord::class.java) }.getOrNull()
        }
    }

    // ── Bitácora de Auditoría de Seguridad (`users/{uid}/audit_logs`) ──

    /** Registra un evento inmutable de seguridad con estampa de tiempo del servidor. */
    fun recordAuditLog(parentUID: String, eventType: String, details: String) {
        backgroundScope.launch {
            val logData = mapOf(
                "eventType" to eventType,
                "details" to details,
                "deviceModel" to DEVICE_MODEL,
                "timestamp" to FieldValue.serverTimestamp()
            )
            runCatching {
                auditLogsCollection(parentUID).document().set(logData).await()
            }.onFailure { Log.w(TAG, "No se pudo registrar auditoría $eventType", it) }
        }
    }

    /** Consulta los últimos eventos de auditoría registrados. */
    suspend fun fetchAuditLogs(parentUID: String, limit: Long = 20): List<AuditLogRecord> {
        val snapshot = auditLogsCollection(parentUID)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            runCatching { doc.toObject(AuditLogRecord::class.java) }.getOrNull()
        }
    }
}
