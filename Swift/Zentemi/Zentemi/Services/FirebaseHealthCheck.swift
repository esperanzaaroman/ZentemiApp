import Foundation
import FirebaseCore
import FirebaseAuth
import FirebaseFirestore

/// Diagnóstico de inicialización y conectividad de los servicios de Firebase
public final class FirebaseHealthCheck {
    public static let shared = FirebaseHealthCheck()

    private init() {}

    public struct HealthStatus {
        public let isConfigured: Bool
        public let appName: String?
        public let projectID: String?
        public let currentUserID: String?
        public let errorDescription: String?
    }

    /// Comprueba que FirebaseApp esté correctamente inicializado con sus credenciales
    public func verifyFirebaseSetup() -> HealthStatus {
        guard let defaultApp = FirebaseApp.app() else {
            return HealthStatus(
                isConfigured: false,
                appName: nil,
                projectID: nil,
                currentUserID: nil,
                errorDescription: "FirebaseApp no ha sido inicializado. Verifica que GoogleService-Info.plist esté presente y sea válido."
            )
        }

        let projectId = defaultApp.options.projectID
        let currentUser = Auth.auth().currentUser?.uid

        return HealthStatus(
            isConfigured: true,
            appName: defaultApp.name,
            projectID: projectId,
            currentUserID: currentUser,
            errorDescription: nil
        )
    }
}
