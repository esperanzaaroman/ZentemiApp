package com.zentemi.app.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.ceil
import kotlin.math.max

/** Errores tipados del flujo de autenticación (mismos mensajes que iOS). */
sealed class ZentemiAuthError(message: String) : Exception(message) {
    object PrivacyConsentRequired : ZentemiAuthError(
        "Debe aceptar el aviso de privacidad y consentir el tratamiento de datos de menores conforme a la LFPDPPP y LGDNNA."
    )
    object InvalidEmail : ZentemiAuthError("El correo electrónico ingresado no tiene un formato válido.")
    object WeakPassword : ZentemiAuthError("La contraseña debe tener al menos 6 caracteres.")
    class AccountLockedOut(val remainingSeconds: Int) : ZentemiAuthError(
        "Cuenta temporalmente bloqueada por exceso de intentos fallidos. Intente nuevamente en " +
            "${max(1, ceil(remainingSeconds / 60.0).toInt())} minuto(s)."
    )
    object InvalidCredentials : ZentemiAuthError("El correo o la contraseña son incorrectos.")
    object UserNotFound : ZentemiAuthError("No existe una cuenta registrada con este correo electrónico.")
    object EmailAlreadyInUse : ZentemiAuthError("Ya existe una cuenta registrada con este correo.")
    object NetworkError : ZentemiAuthError("Error de conexión de red. Compruebe su acceso a internet.")
    class Unknown(message: String) : ZentemiAuthError(message)
}

/**
 * Servicio central de autenticación basado en Firebase Auth.
 *
 * Equivalente Android de Swift/Zentemi/Zentemi/Services/AuthService.swift:
 * cuenta de tutor con correo y contraseña, verificación de correo,
 * circuit breaker anti fuerza bruta (5 intentos → 15 min, 10 → 1 h)
 * y bitácora de auditoría.
 *
 * La cuenta es opcional (modo invitado): sin sesión la app funciona igual y
 * guarda todo en el teléfono. Por eso aquí no hay cierre de sesión por
 * inactividad como en iOS: cerraría la sesión sin avisar y la app seguiría
 * en modo invitado sin sincronizar.
 *
 * Debe inicializarse una vez con [init] (lo hace MainActivity).
 */
object AuthService {

    private const val TAG = "AuthService"

    // ── Configuración de Circuit Breaker (mismos valores y claves que iOS) ──

    private const val MAX_FAILED_ATTEMPTS = 5
    private const val EXTENDED_FAILED_ATTEMPTS = 10
    private const val BASE_LOCKOUT_SECONDS = 15 * 60L
    private const val EXTENDED_LOCKOUT_SECONDS = 60 * 60L

    private const val LOCKOUT_TIMESTAMP_KEY = "zentemi.auth.lockoutTimestamp"
    private const val LOCKOUT_DURATION_KEY = "zentemi.auth.lockoutDuration"
    private const val FAILED_ATTEMPTS_KEY = "zentemi.auth.failedAttempts"

    // ── Estado observable ──

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

    private val _isLockedOut = MutableStateFlow(false)
    val isLockedOut: StateFlow<Boolean> = _isLockedOut.asStateFlow()

    private val _lockoutRemainingSeconds = MutableStateFlow(0)
    val lockoutRemainingSeconds: StateFlow<Int> = _lockoutRemainingSeconds.asStateFlow()

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    val userEmail: String? get() = _currentUser.value?.email

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var prefs: SharedPreferences
    private val mainHandler = Handler(Looper.getMainLooper())
    private var initialized = false

    private val lockoutTick = object : Runnable {
        override fun run() {
            if (_lockoutRemainingSeconds.value > 1) {
                _lockoutRemainingSeconds.value -= 1
                mainHandler.postDelayed(this, 1000)
            } else {
                clearLockoutData()
            }
        }
    }

    // ── Inicialización ──

    fun init(context: Context) {
        if (initialized) return
        initialized = true

        prefs = context.applicationContext.getSharedPreferences("zentemi", Context.MODE_PRIVATE)
        _failedAttempts.value = prefs.getInt(FAILED_ATTEMPTS_KEY, 0)

        val user = auth.currentUser
        _currentUser.value = user
        _isAuthenticated.value = user != null
        _isEmailVerified.value = user?.isEmailVerified ?: false

        // Escucha cambios de sesión de Firebase Auth en tiempo real
        auth.addAuthStateListener { firebaseAuth ->
            val u = firebaseAuth.currentUser
            _currentUser.value = u
            _isAuthenticated.value = u != null
            _isEmailVerified.value = u?.isEmailVerified ?: false
        }

        // Evalúa si el dispositivo arranca en estado de bloqueo
        evaluateLockoutStatus()
    }

    // ── Registro de Adulto / Tutor ──

    suspend fun registerParent(
        email: String,
        password: String,
        acceptedPrivacyConsent: Boolean
    ): FirebaseUser {
        if (!acceptedPrivacyConsent) throw ZentemiAuthError.PrivacyConsentRequired

        val cleanEmail = email.trim().lowercase()
        if (!isValidEmail(cleanEmail)) throw ZentemiAuthError.InvalidEmail
        if (password.length < 6) throw ZentemiAuthError.WeakPassword

        try {
            val user = auth.createUserWithEmailAndPassword(cleanEmail, password).await().user
                ?: throw ZentemiAuthError.Unknown("No se pudo crear la cuenta.")
            resetFailedAttempts()
            _currentUser.value = user
            _isAuthenticated.value = true
            _isEmailVerified.value = false

            // Enviar correo de verificación automáticamente
            runCatching { user.sendEmailVerification().await() }

            FirestoreService.recordAuditLog(
                parentUID = user.uid,
                eventType = "REGISTER_SUCCESS",
                details = "Registro familiar completado con consentimiento informado LFPDPPP."
            )
            return user
        } catch (e: Exception) {
            throw mapFirebaseError(e)
        }
    }

    // ── Inicio de Sesión con Circuit Breaker ──

    suspend fun signInParent(email: String, password: String): FirebaseUser {
        if (evaluateLockoutStatus()) {
            throw ZentemiAuthError.AccountLockedOut(_lockoutRemainingSeconds.value)
        }

        val cleanEmail = email.trim().lowercase()
        if (!isValidEmail(cleanEmail)) throw ZentemiAuthError.InvalidEmail
        if (password.isEmpty()) throw ZentemiAuthError.InvalidCredentials

        try {
            val user = auth.signInWithEmailAndPassword(cleanEmail, password).await().user
                ?: throw ZentemiAuthError.InvalidCredentials
            resetFailedAttempts()
            _currentUser.value = user
            _isAuthenticated.value = true
            _isEmailVerified.value = user.isEmailVerified

            FirestoreService.recordAuditLog(
                parentUID = user.uid,
                eventType = "LOGIN_SUCCESS",
                details = "Inicio de sesión de tutor exitoso."
            )
            return user
        } catch (e: Exception) {
            // Fallo: registrar intento fallido y evaluar apertura de circuito
            recordFailedAttempt()
            throw mapFirebaseError(e)
        }
    }

    // ── Verificación de Correo del Tutor ──

    suspend fun sendEmailVerification() {
        val user = auth.currentUser ?: throw ZentemiAuthError.UserNotFound
        user.sendEmailVerification().await()
        FirestoreService.recordAuditLog(
            parentUID = user.uid,
            eventType = "VERIFICATION_EMAIL_SENT",
            details = "Correo de verificación reenviado a ${user.email ?: "desconocido"}."
        )
    }

    /** Recarga el usuario en Firebase para comprobar si ya confirmó el enlace. */
    suspend fun reloadAndCheckVerification(): Boolean {
        val user = auth.currentUser ?: return false
        user.reload().await()
        val refreshed = auth.currentUser
        _currentUser.value = refreshed
        _isEmailVerified.value = refreshed?.isEmailVerified ?: false

        if (_isEmailVerified.value) {
            FirestoreService.recordAuditLog(
                parentUID = user.uid,
                eventType = "EMAIL_VERIFIED",
                details = "Correo del tutor verificado con éxito."
            )
        }
        return _isEmailVerified.value
    }

    // ── Recuperación de Contraseña ──

    suspend fun sendPasswordReset(email: String) {
        val cleanEmail = email.trim().lowercase()
        if (!isValidEmail(cleanEmail)) throw ZentemiAuthError.InvalidEmail
        try {
            auth.sendPasswordResetEmail(cleanEmail).await()
        } catch (e: Exception) {
            throw mapFirebaseError(e)
        }
    }

    // ── Cierre de Sesión ──

    fun signOut() {
        _currentUser.value?.uid?.let { uid ->
            FirestoreService.recordAuditLog(
                parentUID = uid,
                eventType = "LOGOUT",
                details = "Cierre de sesión manual."
            )
        }
        auth.signOut()
        _currentUser.value = null
        _isAuthenticated.value = false
        _isEmailVerified.value = false
    }

    // ── Lógica Interna de Circuit Breaker ──

    private fun recordFailedAttempt() {
        val attempts = _failedAttempts.value + 1
        _failedAttempts.value = attempts
        prefs.edit().putInt(FAILED_ATTEMPTS_KEY, attempts).apply()

        when {
            attempts >= EXTENDED_FAILED_ATTEMPTS -> triggerLockout(EXTENDED_LOCKOUT_SECONDS)
            attempts >= MAX_FAILED_ATTEMPTS -> triggerLockout(BASE_LOCKOUT_SECONDS)
        }
    }

    private fun triggerLockout(durationSeconds: Long) {
        prefs.edit()
            .putLong(LOCKOUT_TIMESTAMP_KEY, System.currentTimeMillis())
            .putLong(LOCKOUT_DURATION_KEY, durationSeconds)
            .apply()

        _isLockedOut.value = true
        _lockoutRemainingSeconds.value = durationSeconds.toInt()
        startLockoutCountdown()
    }

    /** Devuelve true si el circuito está abierto (bloqueo activo). */
    fun evaluateLockoutStatus(): Boolean {
        val timestamp = prefs.getLong(LOCKOUT_TIMESTAMP_KEY, 0L)
        val duration = prefs.getLong(LOCKOUT_DURATION_KEY, 0L)

        if (timestamp <= 0 || duration <= 0) {
            _isLockedOut.value = false
            _lockoutRemainingSeconds.value = 0
            return false
        }

        val elapsedSeconds = (System.currentTimeMillis() - timestamp) / 1000
        val remaining = duration - elapsedSeconds

        return if (remaining > 0) {
            _isLockedOut.value = true
            _lockoutRemainingSeconds.value = remaining.toInt()
            startLockoutCountdown()
            true
        } else {
            clearLockoutData()
            false
        }
    }

    private fun startLockoutCountdown() {
        mainHandler.removeCallbacks(lockoutTick)
        mainHandler.postDelayed(lockoutTick, 1000)
    }

    fun resetFailedAttempts() {
        _failedAttempts.value = 0
        prefs.edit().remove(FAILED_ATTEMPTS_KEY).apply()
        clearLockoutData()
    }

    private fun clearLockoutData() {
        _isLockedOut.value = false
        _lockoutRemainingSeconds.value = 0
        prefs.edit()
            .remove(LOCKOUT_TIMESTAMP_KEY)
            .remove(LOCKOUT_DURATION_KEY)
            .apply()
        mainHandler.removeCallbacks(lockoutTick)
    }

    // ── Validaciones Auxiliares ──

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun mapFirebaseError(e: Exception): ZentemiAuthError = when (e) {
        is ZentemiAuthError -> e
        is FirebaseAuthWeakPasswordException -> ZentemiAuthError.WeakPassword
        is FirebaseAuthUserCollisionException -> ZentemiAuthError.EmailAlreadyInUse
        is FirebaseAuthInvalidUserException -> ZentemiAuthError.UserNotFound
        is FirebaseAuthInvalidCredentialsException ->
            if (e.errorCode == "ERROR_INVALID_EMAIL") ZentemiAuthError.InvalidEmail
            else ZentemiAuthError.InvalidCredentials
        is FirebaseNetworkException -> ZentemiAuthError.NetworkError
        is FirebaseTooManyRequestsException -> ZentemiAuthError.AccountLockedOut(900)
        else -> ZentemiAuthError.Unknown(e.localizedMessage ?: "Error desconocido")
    }
}
