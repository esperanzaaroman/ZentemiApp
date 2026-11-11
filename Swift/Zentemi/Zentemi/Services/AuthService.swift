import Foundation
import FirebaseAuth
import Combine

/// Errores personalizados y tipados para el flujo de autenticación de Zentemi
public enum ZentemiAuthError: LocalizedError, Equatable {
    case privacyConsentRequired
    case invalidEmail
    case weakPassword
    case accountLockedOut(remainingSeconds: Int)
    case invalidCredentials
    case userNotFound
    case emailAlreadyInUse
    case networkError
    case unknown(String)

    public var errorDescription: String? {
        switch self {
        case .privacyConsentRequired:
            return "Debe aceptar el aviso de privacidad y consentir el tratamiento de datos de menores conforme a la LFPDPPP y LGDNNA."
        case .invalidEmail:
            return "El correo electrónico ingresado no tiene un formato válido."
        case .weakPassword:
            return "La contraseña debe tener al menos 6 caracteres."
        case .accountLockedOut(let remainingSeconds):
            let minutes = max(1, Int(ceil(Double(remainingSeconds) / 60.0)))
            return "Cuenta temporalmente bloqueada por exceso de intentos fallidos. Intente nuevamente en \(minutes) minuto(s)."
        case .invalidCredentials:
            return "El correo o la contraseña son incorrectos."
        case .userNotFound:
            return "No existe una cuenta registrada con este correo electrónico."
        case .emailAlreadyInUse:
            return "Ya existe una cuenta registrada con este correo."
        case .networkError:
            return "Error de conexión de red. Compruebe su acceso a internet."
        case .unknown(let message):
            return message
        }
    }
}

/// Servicio central de autenticación de Zentemi basado en Firebase Auth
/// Implementa el modelo de acceso adulto/tutor, Circuit Breaker anti fuerza bruta y cumplimiento LFPDPPP/LGDNNA.
public final class AuthService: ObservableObject {

    public static let shared = AuthService()

    // MARK: - Estado Publicado (Combine / SwiftUI)

    @Published public private(set) var currentUser: User? = nil
    @Published public private(set) var isAuthenticated: Bool = false
    @Published public private(set) var isEmailVerified: Bool = false
    @Published public private(set) var isLockedOut: Bool = false
    @Published public private(set) var lockoutRemainingSeconds: Int = 0
    @Published public private(set) var failedAttempts: Int = 0

    /// Correo del usuario actual si existe una sesión activa
    public var userEmail: String? {
        currentUser?.email
    }

    // MARK: - Configuración de Circuit Breaker (RNF-03 / Hallazgo M-01)

    private let maxFailedAttempts = 5
    private let extendedFailedAttempts = 10
    private let baseLockoutDuration: TimeInterval = 15 * 60       // 15 minutos
    private let extendedLockoutDuration: TimeInterval = 60 * 60   // 1 hora

    private let lockoutTimestampKey = "zentemi.auth.lockoutTimestamp"
    private let lockoutDurationKey = "zentemi.auth.lockoutDuration"
    private let failedAttemptsKey = "zentemi.auth.failedAttempts"

    private var authStateListenerHandle: AuthStateDidChangeListenerHandle?
    private var lockoutTimer: Timer?
    private var inactivityTimer: Timer?

    // MARK: - Inicialización

    private init() {
        self.failedAttempts = UserDefaults.standard.integer(forKey: failedAttemptsKey)
        self.currentUser = Auth.auth().currentUser
        self.isAuthenticated = (self.currentUser != nil)
        self.isEmailVerified = self.currentUser?.isEmailVerified ?? false

        // Escucha cambios de sesión de Firebase Auth en tiempo real
        self.authStateListenerHandle = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            DispatchQueue.main.async {
                self?.currentUser = user
                self?.isAuthenticated = (user != nil)
                self?.isEmailVerified = user?.isEmailVerified ?? false
                if user != nil {
                    self?.resetInactivityTimer()
                } else {
                    self?.stopInactivityTimer()
                }
            }
        }

        // Evalúa si el dispositivo arranca en estado de bloqueo
        _ = evaluateLockoutStatus()
    }

    deinit {
        if let handle = authStateListenerHandle {
            Auth.auth().removeStateDidChangeListener(handle)
        }
        lockoutTimer?.invalidate()
        inactivityTimer?.invalidate()
    }

    // MARK: - Registro de Adulto / Tutor (RF-01, LFPDPPP Art. 8)

    /// Registra un nuevo tutor legal con email, contraseña y consentimiento de privacidad obligatorio.
    @MainActor
    public func registerParent(
        email: String,
        password: String,
        acceptedPrivacyConsent: Bool
    ) async throws -> User {

        guard acceptedPrivacyConsent else {
            throw ZentemiAuthError.privacyConsentRequired
        }

        let cleanEmail = email.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard isValidEmail(cleanEmail) else {
            throw ZentemiAuthError.invalidEmail
        }

        guard password.count >= 6 else {
            throw ZentemiAuthError.weakPassword
        }

        do {
            let authResult = try await Auth.auth().createUser(withEmail: cleanEmail, password: password)
            self.resetFailedAttempts()
            self.currentUser = authResult.user
            self.isAuthenticated = true
            self.isEmailVerified = false

            // Enviar correo de verificación automáticamente
            try? await authResult.user.sendEmailVerification()

            // Registrar evento inmutable de auditoría en la nube
            FirestoreService.shared.recordAuditLog(
                parentUID: authResult.user.uid,
                eventType: "REGISTER_SUCCESS",
                details: "Registro familiar completado con consentimiento informado LFPDPPP."
            )

            return authResult.user
        } catch let error as NSError {
            throw mapFirebaseError(error)
        }
    }

    // MARK: - Inicio de Sesión con Circuit Breaker (RNF-03 / M-01)

    /// Inicia sesión del adulto/tutor validando las credenciales contra Firebase Auth.
    /// Si se han superado los 5 intentos erróneos consecutivos, se rechaza la petición por Circuit Breaker.
    @MainActor
    public func signInParent(email: String, password: String) async throws -> User {

        // 1. Verificar si el circuito está abierto (bloqueo activo)
        if evaluateLockoutStatus() {
            throw ZentemiAuthError.accountLockedOut(remainingSeconds: lockoutRemainingSeconds)
        }

        let cleanEmail = email.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard isValidEmail(cleanEmail) else {
            throw ZentemiAuthError.invalidEmail
        }

        guard !password.isEmpty else {
            throw ZentemiAuthError.invalidCredentials
        }

        do {
            let authResult = try await Auth.auth().signIn(withEmail: cleanEmail, password: password)
            // Éxito: cerrar circuito y reiniciar contador de fallos
            self.resetFailedAttempts()
            self.currentUser = authResult.user
            self.isAuthenticated = true
            self.isEmailVerified = authResult.user.isEmailVerified

            // Registrar en auditoría de la nube
            FirestoreService.shared.recordAuditLog(
                parentUID: authResult.user.uid,
                eventType: "LOGIN_SUCCESS",
                details: "Inicio de sesión de tutor exitoso."
            )

            return authResult.user
        } catch let error as NSError {
            // Fallo: registrar intento fallido y evaluar apertura de circuito
            self.recordFailedAttempt()
            throw mapFirebaseError(error)
        }
    }

    // MARK: - Verificación de Correo del Tutor (LFPDPPP / RF-01)

    /// Envía o reenvía un correo de confirmación oficial de Firebase al correo del tutor
    @MainActor
    public func sendEmailVerification() async throws {
        guard let user = Auth.auth().currentUser else {
            throw ZentemiAuthError.userNotFound
        }
        try await user.sendEmailVerification()
        FirestoreService.shared.recordAuditLog(
            parentUID: user.uid,
            eventType: "VERIFICATION_EMAIL_SENT",
            details: "Correo de verificación reenviado a \(user.email ?? "desconocido")."
        )
    }

    /// Recarga el estado del usuario en Firebase para comprobar si ya confirmó el enlace
    @MainActor
    public func reloadAndCheckVerification() async throws -> Bool {
        guard let user = Auth.auth().currentUser else { return false }
        try await user.reload()
        self.currentUser = Auth.auth().currentUser
        self.isEmailVerified = self.currentUser?.isEmailVerified ?? false

        if self.isEmailVerified {
            FirestoreService.shared.recordAuditLog(
                parentUID: user.uid,
                eventType: "EMAIL_VERIFIED",
                details: "Correo del tutor verificado con éxito."
            )
        }

        return self.isEmailVerified
    }

    // MARK: - Recuperación de Contraseña Oficial (Elimina C-01 "AGUA")

    /// Envía un correo electrónico oficial de restablecimiento de contraseña.
    @MainActor
    public func sendPasswordReset(to email: String) async throws {
        let cleanEmail = email.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        guard isValidEmail(cleanEmail) else {
            throw ZentemiAuthError.invalidEmail
        }

        do {
            try await Auth.auth().sendPasswordReset(withEmail: cleanEmail)
        } catch let error as NSError {
            throw mapFirebaseError(error)
        }
    }

    // MARK: - Cierre de Sesión Seguro (M-03)

    /// Invalida los tokens en el servidor y limpia la sesión local del dispositivo.
    @MainActor
    public func signOut() throws {
        if let uid = currentUser?.uid {
            FirestoreService.shared.recordAuditLog(
                parentUID: uid,
                eventType: "LOGOUT",
                details: "Cierre de sesión manual."
            )
        }
        do {
            try Auth.auth().signOut()
            self.currentUser = nil
            self.isAuthenticated = false
            self.isEmailVerified = false
            self.stopInactivityTimer()
        } catch let error as NSError {
            throw ZentemiAuthError.unknown(error.localizedDescription)
        }
    }

    // MARK: - Monitoreo de Sesión Inactiva (30 minutos)

    public func registerUserActivity() {
        resetInactivityTimer()
    }

    private func resetInactivityTimer() {
        inactivityTimer?.invalidate()
        // 30 minutos de inactividad
        inactivityTimer = Timer.scheduledTimer(withTimeInterval: 30 * 60, repeats: false) { [weak self] _ in
            DispatchQueue.main.async {
                print("[AuthService] Sesión cerrada automáticamente por 30 minutos de inactividad.")
                try? self?.signOut()
            }
        }
    }

    private func stopInactivityTimer() {
        inactivityTimer?.invalidate()
        inactivityTimer = nil
    }

    // MARK: - Lógica Interna de Circuit Breaker

    private func recordFailedAttempt() {
        failedAttempts += 1
        UserDefaults.standard.set(failedAttempts, forKey: failedAttemptsKey)

        if failedAttempts >= extendedFailedAttempts {
            // 10+ intentos: 1 hora de lockout
            triggerLockout(duration: extendedLockoutDuration)
        } else if failedAttempts >= maxFailedAttempts {
            // 5 intentos: 15 minutos de lockout
            triggerLockout(duration: baseLockoutDuration)
        }
    }

    private func triggerLockout(duration: TimeInterval) {
        let now = Date().timeIntervalSince1970
        UserDefaults.standard.set(now, forKey: lockoutTimestampKey)
        UserDefaults.standard.set(duration, forKey: lockoutDurationKey)

        isLockedOut = true
        lockoutRemainingSeconds = Int(duration)

        startLockoutCountdown()
    }

    @discardableResult
    public func evaluateLockoutStatus() -> Bool {
        let timestamp = UserDefaults.standard.double(forKey: lockoutTimestampKey)
        let duration = UserDefaults.standard.double(forKey: lockoutDurationKey)

        guard timestamp > 0, duration > 0 else {
            isLockedOut = false
            lockoutRemainingSeconds = 0
            return false
        }

        let elapsed = Date().timeIntervalSince1970 - timestamp
        let remaining = duration - elapsed

        if remaining > 0 {
            isLockedOut = true
            lockoutRemainingSeconds = Int(remaining)
            startLockoutCountdown()
            return true
        } else {
            // El tiempo de bloqueo ya expiró
            clearLockoutData()
            return false
        }
    }

    private func startLockoutCountdown() {
        lockoutTimer?.invalidate()
        lockoutTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] timer in
            guard let self = self else { return }
            if self.lockoutRemainingSeconds > 1 {
                self.lockoutRemainingSeconds -= 1
            } else {
                timer.invalidate()
                self.clearLockoutData()
            }
        }
    }

    public func resetFailedAttempts() {
        failedAttempts = 0
        UserDefaults.standard.removeObject(forKey: failedAttemptsKey)
        clearLockoutData()
    }

    private func clearLockoutData() {
        isLockedOut = false
        lockoutRemainingSeconds = 0
        UserDefaults.standard.removeObject(forKey: lockoutTimestampKey)
        UserDefaults.standard.removeObject(forKey: lockoutDurationKey)
        lockoutTimer?.invalidate()
        lockoutTimer = nil
    }

    // MARK: - Validaciones Auxiliares

    private func isValidEmail(_ email: String) -> Bool {
        let emailRegex = "^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$"
        let predicate = NSPredicate(format: "SELF MATCHES %@", emailRegex)
        return predicate.evaluate(with: email)
    }

    private func mapFirebaseError(_ error: NSError) -> ZentemiAuthError {
        guard let errorCode = AuthErrorCode(rawValue: error.code) else {
            return .unknown(error.localizedDescription)
        }

        switch errorCode {
        case .invalidEmail:
            return .invalidEmail
        case .wrongPassword:
            return .invalidCredentials
        case .userNotFound:
            return .userNotFound
        case .emailAlreadyInUse:
            return .emailAlreadyInUse
        case .weakPassword:
            return .weakPassword
        case .networkError:
            return .networkError
        case .tooManyRequests:
            return .accountLockedOut(remainingSeconds: 900)
        default:
            return .unknown(error.localizedDescription)
        }
    }
}
