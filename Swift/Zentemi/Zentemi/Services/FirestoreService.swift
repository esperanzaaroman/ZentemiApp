import Foundation
import FirebaseFirestore
import Combine

/// Servicio central para la gestión y sincronización de datos con Cloud Firestore
/// Cumple con la estructura de colecciones modelada en la Etapa 2 de Zentemi
public final class FirestoreService: ObservableObject {

    public static let shared = FirestoreService()

    private let db: Firestore

    private init() {
        self.db = Firestore.firestore()
        let settings = FirestoreSettings()
        // Habilitar persistencia en caché local para soporte offline
        settings.cacheSettings = PersistentCacheSettings()
        self.db.settings = settings
    }

    // MARK: - Referencias a Colecciones

    private func usersCollection() -> CollectionReference {
        return db.collection("users")
    }

    private func childrenCollection(parentUID: String) -> CollectionReference {
        return usersCollection().document(parentUID).collection("children")
    }

    private func progressCollection(parentUID: String, childId: String) -> CollectionReference {
        return childrenCollection(parentUID: parentUID).document(childId).collection("progress")
    }

    private func inventoryCollection(parentUID: String, childId: String) -> CollectionReference {
        return childrenCollection(parentUID: parentUID).document(childId).collection("inventory")
    }

    // MARK: - Gestión de Cuenta de Adulto / Tutor (`users/{uid}`)

    /// Registra o actualiza los datos del tutor legal en Firestore
    public func createOrUpdateParentProfile(
        uid: String,
        email: String,
        acceptedPrivacyPolicy: Bool = true
    ) async throws {
        let parentDoc = usersCollection().document(uid)
        let profile = ParentProfile(
            email: email,
            acceptedPrivacyPolicy: acceptedPrivacyPolicy,
            createdAt: Date(),
            lastLogin: Date()
        )
        try parentDoc.setData(from: profile, merge: true)
    }

    /// Obtiene el perfil del tutor legal
    public func fetchParentProfile(uid: String) async throws -> ParentProfile? {
        let snapshot = try await usersCollection().document(uid).getDocument()
        return try? snapshot.data(as: ParentProfile.self)
    }

    // MARK: - Gestión de Perfiles Infantiles (`users/{uid}/children/{childId}`)

    /// Obtiene la lista de perfiles infantiles asociados a la cuenta del tutor
    public func fetchChildren(parentUID: String) async throws -> [ChildProfile] {
        let snapshot = try await childrenCollection(parentUID: parentUID).getDocuments()
        return snapshot.documents.compactMap { doc -> ChildProfile? in
            try? doc.data(as: ChildProfile.self)
        }
    }

    /// Crea un nuevo perfil infantil bajo la tutela del adulto autenticado
    public func createChildProfile(
        parentUID: String,
        userName: String
    ) async throws -> ChildProfile {
        let childRef = childrenCollection(parentUID: parentUID).document()
        var newProfile = ChildProfile(
            userName: userName,
            petName: "Maicito",
            coins: 1500,
            currentLevel: 1,
            streak: 1
        )
        newProfile.id = childRef.documentID
        try childRef.setData(from: newProfile)
        return newProfile
    }

    /// Guarda o actualiza el estado completo del perfil infantil (monedas, XP, racha, etc.)
    public func saveChildProfile(
        parentUID: String,
        child: ChildProfile
    ) async throws {
        guard let childId = child.id else {
            throw NSError(
                domain: "ZentemiFirestore",
                code: 400,
                userInfo: [NSLocalizedDescriptionKey: "El perfil del menor carece de identificador (id)."]
            )
        }
        let childDoc = childrenCollection(parentUID: parentUID).document(childId)
        try childDoc.setData(from: child, merge: true)
    }

    /// Listener en tiempo real para mantener sincronizado el perfil infantil con la nube
    public func listenToChildProfile(
        parentUID: String,
        childId: String,
        onChange: @escaping (Result<ChildProfile, Error>) -> Void
    ) -> ListenerRegistration {
        let childDoc = childrenCollection(parentUID: parentUID).document(childId)
        return childDoc.addSnapshotListener { snapshot, error in
            if let error = error {
                onChange(.failure(error))
                return
            }
            guard let snapshot = snapshot, snapshot.exists else {
                onChange(.failure(NSError(
                    domain: "ZentemiFirestore",
                    code: 404,
                    userInfo: [NSLocalizedDescriptionKey: "Documento de menor no encontrado"]
                )))
                return
            }
            do {
                let profile = try snapshot.data(as: ChildProfile.self)
                onChange(.success(profile))
            } catch {
                onChange(.failure(error))
            }
        }
    }

    // MARK: - Progreso de Lecciones (`users/{uid}/children/{childId}/progress/{lessonId}`)

    /// Registra el resultado obtenido en una lección
    public func saveLessonProgress(
        parentUID: String,
        childId: String,
        progress: LessonProgressRecord
    ) async throws {
        let docRef = progressCollection(parentUID: parentUID, childId: childId).document(progress.lessonId)
        try docRef.setData(from: progress, merge: true)
    }

    /// Consulta el progreso específico de una lección
    public func fetchLessonProgress(
        parentUID: String,
        childId: String,
        lessonId: String
    ) async throws -> LessonProgressRecord? {
        let docRef = progressCollection(parentUID: parentUID, childId: childId).document(lessonId)
        let snapshot = try await docRef.getDocument()
        return try? snapshot.data(as: LessonProgressRecord.self)
    }

    // MARK: - Inventario y Accesorios (`users/{uid}/children/{childId}/inventory/{itemId}`)

    /// Guarda o actualiza el estado de compra y equipamiento de un ítem de Maicito
    public func saveInventoryItem(
        parentUID: String,
        childId: String,
        item: InventoryItemRecord
    ) async throws {
        let docRef = inventoryCollection(parentUID: parentUID, childId: childId).document(item.itemId)
        try docRef.setData(from: item, merge: true)
    }

    /// Obtiene todos los ítems adquiridos o equipados en el inventario del menor
    public func fetchInventory(
        parentUID: String,
        childId: String
    ) async throws -> [InventoryItemRecord] {
        let snapshot = try await inventoryCollection(parentUID: parentUID, childId: childId).getDocuments()
        return snapshot.documents.compactMap { doc -> InventoryItemRecord? in
            try? doc.data(as: InventoryItemRecord.self)
        }
    }

    // MARK: - Bitácora de Auditoría de Seguridad (`users/{uid}/audit_logs`)

    private func auditLogsCollection(parentUID: String) -> CollectionReference {
        return usersCollection().document(parentUID).collection("audit_logs")
    }

    /// Registra un evento inmutable de seguridad en la nube con estampa de tiempo del servidor (tamper-proof)
    public func recordAuditLog(
        parentUID: String,
        eventType: String,
        details: String
    ) {
        Task {
            let docRef = auditLogsCollection(parentUID: parentUID).document()
            let logData: [String: Any] = [
                "eventType": eventType,
                "details": details,
                "deviceModel": "iOS Device",
                "timestamp": FieldValue.serverTimestamp()
            ]
            try? await docRef.setData(logData)
        }
    }

    /// Consulta los últimos eventos de auditoría registrados
    public func fetchAuditLogs(parentUID: String, limit: Int = 20) async throws -> [AuditLogRecord] {
        let snapshot = try await auditLogsCollection(parentUID: parentUID)
            .order(by: "timestamp", descending: true)
            .limit(to: limit)
            .getDocuments()

        return snapshot.documents.compactMap { doc -> AuditLogRecord? in
            try? doc.data(as: AuditLogRecord.self)
        }
    }
}
