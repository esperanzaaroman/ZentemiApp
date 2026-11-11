import SwiftUI
import FirebaseAuth

/// Vista de espera y confirmación de correo para el adulto/tutor
/// Garantiza el consentimiento informado del tutor legal conforme a LFPDPPP y LGDNNA
public struct EmailVerificationView: View {

    @ObservedObject private var authService = AuthService.shared

    @State private var isChecking = false
    @State private var isResending = false
    @State private var statusMessage: String? = nil
    @State private var isSuccessMessage = false

    private let purple = Color(red: 0.47, green: 0.28, blue: 0.78)

    public init() {}

    public var body: some View {
        ZStack {
            LinearGradient(
                colors: [
                    Color(red: 0.47, green: 0.28, blue: 0.78),
                    Color(red: 0.34, green: 0.17, blue: 0.64)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            VStack(spacing: 24) {

                Spacer()

                // Icono con escudo y correo
                ZStack {
                    Circle()
                        .fill(Color.white.opacity(0.18))
                        .frame(width: 130, height: 130)

                    Image(systemName: "envelope.badge.shield.half.filled")
                        .font(.system(size: 65))
                        .foregroundStyle(.white)
                }

                // Títulos
                VStack(spacing: 10) {
                    Text("Confirma tu correo")
                        .font(.system(size: 30, weight: .bold))
                        .foregroundStyle(.white)

                    Text("Validación del Tutor Legal")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(Color.yellow.opacity(0.95))

                    Text("Enviamos un enlace de verificación a:")
                        .font(.system(size: 14))
                        .foregroundStyle(.white.opacity(0.85))
                        .padding(.top, 4)

                    Text(authService.userEmail ?? "tu correo")
                        .font(.system(size: 16, weight: .bold, design: .monospaced))
                        .foregroundStyle(.white)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(Color.white.opacity(0.18))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                }
                .multilineTextAlignment(.center)
                .padding(.horizontal, 25)

                // Mensaje Legal y Explicativo
                VStack(spacing: 8) {
                    HStack(spacing: 8) {
                        Image(systemName: "lock.shield.fill")
                            .foregroundStyle(.green)
                        Text("Cumplimiento LFPDPPP y LGDNNA")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundStyle(.white)
                    }

                    Text("Por protección de datos de los menores, el padre o tutor debe validar su identidad antes de que el alumno comience sus lecciones de Yuhmu.")
                        .font(.system(size: 12))
                        .foregroundStyle(.white.opacity(0.85))
                        .multilineTextAlignment(.center)
                }
                .padding(14)
                .background(Color.black.opacity(0.18))
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .padding(.horizontal, 25)

                // Mensaje de Estado / Alerta
                if let statusMessage = statusMessage {
                    HStack(spacing: 8) {
                        Image(systemName: isSuccessMessage ? "checkmark.circle.fill" : "info.circle.fill")
                            .foregroundStyle(isSuccessMessage ? .green : .yellow)
                        Text(statusMessage)
                            .font(.system(size: 13, weight: .medium))
                            .foregroundStyle(.white)
                        Spacer()
                    }
                    .padding(12)
                    .background(Color.black.opacity(0.25))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.horizontal, 25)
                }

                Spacer()

                // Botones de Acción
                VStack(spacing: 12) {

                    // 1. Botón "Ya confirmé mi correo"
                    Button {
                        checkVerificationStatus()
                    } label: {
                        HStack(spacing: 10) {
                            if isChecking {
                                ProgressView().tint(purple)
                            }
                            Text(isChecking ? "Comprobando..." : "Ya confirmé mi correo")
                                .font(.system(size: 17, weight: .bold))
                                .foregroundStyle(purple)
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 54)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 18))
                    }
                    .disabled(isChecking)

                    // 2. Botón "Reenviar correo"
                    Button {
                        resendEmail()
                    } label: {
                        HStack(spacing: 6) {
                            if isResending {
                                ProgressView().tint(.white)
                            }
                            Text(isResending ? "Reenviando..." : "Reenviar enlace de confirmación")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundStyle(.white)
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 44)
                        .background(Color.white.opacity(0.15))
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                    }
                    .disabled(isResending)

                    // 3. Botón "Cerrar sesión"
                    Button {
                        try? authService.signOut()
                    } label: {
                        Text("Iniciar con otra cuenta")
                            .font(.system(size: 13))
                            .foregroundStyle(.white.opacity(0.7))
                            .padding(.top, 4)
                    }
                }
                .padding(.horizontal, 25)
                .padding(.bottom, 20)
            }
        }
        .onAppear {
            // Comprobación pasiva al entrar a la pantalla
            checkVerificationStatus()
        }
    }

    private func checkVerificationStatus() {
        isChecking = true
        statusMessage = nil

        Task {
            do {
                let verified = try await authService.reloadAndCheckVerification()
                await MainActor.run {
                    isChecking = false
                    if verified {
                        statusMessage = "¡Correo verificado con éxito! Entrando..."
                        isSuccessMessage = true
                    } else {
                        statusMessage = "Aún no se ha verificado. Haz clic en el enlace que enviamos a tu correo."
                        isSuccessMessage = false
                    }
                }
            } catch {
                await MainActor.run {
                    isChecking = false
                    statusMessage = "Error al comprobar: \(error.localizedDescription)"
                    isSuccessMessage = false
                }
            }
        }
    }

    private func resendEmail() {
        isResending = true
        statusMessage = nil

        Task {
            do {
                try await authService.sendEmailVerification()
                await MainActor.run {
                    isResending = false
                    statusMessage = "¡Nuevo enlace enviado! Revisa tu bandeja de entrada o spam."
                    isSuccessMessage = true
                }
            } catch {
                await MainActor.run {
                    isResending = false
                    statusMessage = "Error al reenviar: \(error.localizedDescription)"
                    isSuccessMessage = false
                }
            }
        }
    }
}

#Preview {
    EmailVerificationView()
}
