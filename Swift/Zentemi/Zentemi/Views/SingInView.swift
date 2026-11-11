import SwiftUI

struct SignInView: View {

    public enum RecoveryType {
        case pin
        case username
    }

    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject var appState: AppState
    @ObservedObject private var authService = AuthService.shared

    @State private var email = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMessage: String? = nil

    // Recuperación de contraseña oficial vía Firebase
    @State private var showForgotPassword = false
    @State private var resetEmail = ""
    @State private var resetMessage: String? = nil
    @State private var isResetting = false

    private var canLogin: Bool {
        !email.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            && password.count >= 6
            && !authService.isLockedOut
            && !isLoading
    }

    var body: some View {
        ZStack {
            background

            VStack(spacing: 20) {

                // Botón regresar
                HStack {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(.white)
                            .frame(width: 44, height: 44)
                            .background(.white.opacity(0.15))
                            .clipShape(Circle())
                    }

                    Spacer()
                }
                .padding(.horizontal, 22)
                .padding(.top, 10)

                Spacer()

                Image(systemName: "person.crop.circle.badge.checkmark")
                    .font(.system(size: 65))
                    .foregroundStyle(.white)

                VStack(spacing: 8) {
                    Text("Iniciar sesión")
                        .font(.system(size: 32, weight: .bold))
                        .foregroundStyle(.white)

                    Text("Cuenta de tutor / adulto responsable")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundStyle(.white.opacity(0.85))
                }

                // Banner de Circuit Breaker (Lockout anti fuerza bruta)
                if authService.isLockedOut {
                    HStack(spacing: 10) {
                        Image(systemName: "lock.shield.fill")
                            .font(.system(size: 22))
                            .foregroundStyle(.yellow)

                        VStack(alignment: .leading, spacing: 2) {
                            Text("Acceso bloqueado por seguridad")
                                .font(.system(size: 13, weight: .bold))
                                .foregroundStyle(.white)
                            let minutes = max(1, Int(ceil(Double(authService.lockoutRemainingSeconds) / 60.0)))
                            Text("Podrás reintentar en \(minutes) minuto(s).")
                                .font(.system(size: 12))
                                .foregroundStyle(.white.opacity(0.9))
                        }
                        Spacer()
                    }
                    .padding(14)
                    .background(Color.red.opacity(0.35))
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .padding(.horizontal, 25)
                }

                // Mensaje de Error
                if let errorMessage = errorMessage {
                    HStack(spacing: 8) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundStyle(.yellow)
                        Text(errorMessage)
                            .font(.system(size: 13, weight: .medium))
                            .foregroundStyle(.white)
                        Spacer()
                    }
                    .padding(12)
                    .background(Color.red.opacity(0.30))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.horizontal, 25)
                }

                // Campos de Entrada (Adulto / Tutor)
                VStack(spacing: 16) {

                    HStack {
                        Image(systemName: "envelope.fill")
                            .foregroundStyle(Color(red: 0.47, green: 0.28, blue: 0.78))
                        TextField("Correo del tutor", text: $email)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                            .keyboardType(.emailAddress)
                    }
                    .padding()
                    .background(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 14))

                    HStack {
                        Image(systemName: "lock.fill")
                            .foregroundStyle(Color(red: 0.47, green: 0.28, blue: 0.78))
                        SecureField("Contraseña", text: $password)
                    }
                    .padding()
                    .background(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                }
                .padding(20)
                .background(.white.opacity(0.15))
                .clipShape(RoundedRectangle(cornerRadius: 22))
                .padding(.horizontal, 25)

                // Enlace de Recuperación Oficial
                Button {
                    resetEmail = email
                    resetMessage = nil
                    showForgotPassword = true
                } label: {
                    Text("¿Olvidaste tu contraseña?")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(.white)
                        .underline()
                }

                // Botón Iniciar sesión
                Button {
                    executeLogin()
                } label: {
                    HStack(spacing: 10) {
                        if isLoading {
                            ProgressView()
                                .tint(Color(red: 0.35, green: 0.17, blue: 0.65))
                        }
                        Text(isLoading ? "Validando..." : "Iniciar sesión")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(
                                Color(red: 0.35, green: 0.17, blue: 0.65)
                            )
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(
                        canLogin ? Color.white : Color.white.opacity(0.40)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 18))
                }
                .disabled(!canLogin)
                .padding(.horizontal, 30)

                Spacer()
            }
        }
        .navigationBarBackButtonHidden(true)
        .sheet(isPresented: $showForgotPassword) {
            forgotPasswordSheet
        }
    }

    // MARK: - Acción de Inicio de Sesión

    private func executeLogin() {
        guard canLogin else { return }
        isLoading = true
        errorMessage = nil

        Task {
            do {
                _ = try await authService.signInParent(email: email, password: password)
                // Al autenticar, ContentView detecta authService.isAuthenticated automáticamente
            } catch let error as ZentemiAuthError {
                await MainActor.run {
                    errorMessage = error.errorDescription
                    isLoading = false
                }
            } catch {
                await MainActor.run {
                    errorMessage = error.localizedDescription
                    isLoading = false
                }
            }
        }
    }

    // MARK: - Modal de Recuperación por Correo

    private var forgotPasswordSheet: some View {
        NavigationStack {
            VStack(spacing: 20) {
                Image(systemName: "envelope.badge.shield.half.filled")
                    .font(.system(size: 60))
                    .foregroundStyle(Color(red: 0.47, green: 0.28, blue: 0.78))
                    .padding(.top, 25)

                Text("Recuperar contraseña")
                    .font(.system(size: 24, weight: .bold))

                Text("Ingresa el correo electrónico del tutor registrado. Te enviaremos un enlace seguro para restablecerla.")
                    .font(.system(size: 14))
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 20)

                TextField("Correo electrónico", text: $resetEmail)
                    .textInputAutocapitalization(.never)
                    .keyboardType(.emailAddress)
                    .autocorrectionDisabled()
                    .padding()
                    .background(Color.gray.opacity(0.12))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.horizontal, 25)

                if let resetMessage = resetMessage {
                    Text(resetMessage)
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundStyle(resetMessage.contains("éxito") ? .green : .red)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 20)
                }

                Button {
                    sendResetLink()
                } label: {
                    HStack {
                        if isResetting {
                            ProgressView().tint(.white)
                        }
                        Text(isResetting ? "Enviando..." : "Enviar enlace")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(.white)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(
                        resetEmail.contains("@")
                        ? Color(red: 0.47, green: 0.28, blue: 0.78)
                        : Color.gray.opacity(0.4)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                }
                .disabled(!resetEmail.contains("@") || isResetting)
                .padding(.horizontal, 25)

                Spacer()
            }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cerrar") {
                        showForgotPassword = false
                    }
                }
            }
        }
        .presentationDetents([.medium])
    }

    private func sendResetLink() {
        isResetting = true
        resetMessage = nil
        Task {
            do {
                try await authService.sendPasswordReset(to: resetEmail)
                await MainActor.run {
                    resetMessage = "¡Enlace enviado con éxito! Revisa tu bandeja de entrada."
                    isResetting = false
                }
            } catch {
                await MainActor.run {
                    resetMessage = "Error: \(error.localizedDescription)"
                    isResetting = false
                }
            }
        }
    }

    // MARK: - Fondo

    private var background: some View {
        LinearGradient(
            colors: [
                Color(red: 0.47, green: 0.28, blue: 0.78),
                Color(red: 0.34, green: 0.17, blue: 0.64)
            ],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
        .ignoresSafeArea()
    }
}

#Preview {
    NavigationStack {
        SignInView()
            .environmentObject(AppState())
    }
}
