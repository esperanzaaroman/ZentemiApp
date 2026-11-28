import SwiftUI

/// Pantalla opcional para que el tutor legal cree una cuenta y guarde el progreso en la nube.
/// Se abre desde Perfil o desde la sugerencia periódica de guardar progreso (Modo Invitado).
struct CreateAccountView: View {

    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var authService = AuthService.shared

    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var acceptedPrivacyConsent = false

    @State private var isLoading = false
    @State private var errorMessage: String? = nil

    private let purple = Color(red: 0.43, green: 0.27, blue: 0.78)

    private var isValidEmail: Bool {
        let clean = email.trimmingCharacters(in: .whitespacesAndNewlines)
        return clean.contains("@") && clean.contains(".") && clean.count >= 6
    }

    private var isValidPassword: Bool {
        password.count >= 6 && password == confirmPassword
    }

    private var canCreateAccount: Bool {
        isValidEmail && isValidPassword && acceptedPrivacyConsent && !isLoading
    }

    var body: some View {
        ZStack {
            Color(red: 0.97, green: 0.95, blue: 0.99)
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 20) {

                    HStack {
                        Button {
                            dismiss()
                        } label: {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 17, weight: .bold))
                                .foregroundStyle(purple)
                                .frame(width: 42, height: 42)
                                .background(.white)
                                .clipShape(Circle())
                                .shadow(color: .black.opacity(0.06), radius: 4, y: 2)
                        }

                        Spacer()
                    }
                    .padding(.horizontal, 22)

                    Image("mascota")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 90, height: 90)

                    VStack(spacing: 6) {
                        Text("Guardar en la nube")
                            .font(.system(size: 28, weight: .bold))
                            .foregroundStyle(purple)

                        Text("Cuenta supervisada por un adulto para el progreso de \(appState.userName)")
                            .font(.system(size: 14))
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 24)
                    }

                    if let errorMessage = errorMessage {
                        HStack(spacing: 8) {
                            Image(systemName: "exclamationmark.circle.fill")
                                .foregroundStyle(.red)
                            Text(errorMessage)
                                .font(.system(size: 12, weight: .medium))
                                .foregroundStyle(.red)
                            Spacer()
                        }
                        .padding(10)
                        .background(Color.red.opacity(0.12))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                        .padding(.horizontal, 22)
                    }

                    VStack(alignment: .leading, spacing: 16) {

                        // Correo del Tutor
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Correo del tutor legal")
                                .font(.system(size: 14, weight: .semibold))

                            TextField("tutor@ejemplo.com", text: $email)
                                .textInputAutocapitalization(.never)
                                .autocorrectionDisabled()
                                .keyboardType(.emailAddress)
                                .padding()
                                .background(Color.gray.opacity(0.07))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        }

                        // Contraseña
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Contraseña del tutor (mínimo 6 caracteres)")
                                .font(.system(size: 14, weight: .semibold))

                            SecureField("Contraseña", text: $password)
                                .padding()
                                .background(Color.gray.opacity(0.07))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        }

                        // Confirmar Contraseña
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Confirmar contraseña")
                                .font(.system(size: 14, weight: .semibold))

                            SecureField("Repite la contraseña", text: $confirmPassword)
                                .padding()
                                .background(Color.gray.opacity(0.07))
                                .clipShape(RoundedRectangle(cornerRadius: 12))

                            if !confirmPassword.isEmpty {
                                Label(
                                    isValidPassword ? "Las contraseñas coinciden" : "Las contraseñas no coinciden",
                                    systemImage: isValidPassword ? "checkmark.circle.fill" : "xmark.circle.fill"
                                )
                                .font(.system(size: 12))
                                .foregroundStyle(isValidPassword ? .green : .red)
                            }
                        }

                        // Consentimiento Parental (LFPDPPP Art. 8 / LGDNNA Art. 76)
                        Button {
                            acceptedPrivacyConsent.toggle()
                        } label: {
                            HStack(alignment: .top, spacing: 12) {
                                Image(systemName: acceptedPrivacyConsent ? "checkmark.square.fill" : "square")
                                    .font(.system(size: 20))
                                    .foregroundStyle(acceptedPrivacyConsent ? purple : .gray)

                                Text("Soy mayor de edad y autorizo como tutor el tratamiento de datos para el aprendizaje del menor en Zentemi (cumplimiento LFPDPPP y LGDNNA).")
                                    .font(.system(size: 12))
                                    .foregroundStyle(.primary.opacity(0.8))
                                    .multilineTextAlignment(.leading)
                            }
                        }
                        .buttonStyle(.plain)
                        .padding(.top, 4)
                    }
                    .padding(20)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 22))
                    .shadow(color: .black.opacity(0.05), radius: 10, y: 5)
                    .padding(.horizontal, 22)

                    Button {
                        executeRegister()
                    } label: {
                        HStack(spacing: 10) {
                            if isLoading {
                                ProgressView().tint(.white)
                            }
                            Text(isLoading ? "Creando cuenta..." : "Crear cuenta y guardar progreso")
                                .font(.system(size: 17, weight: .bold))
                                .foregroundStyle(.white)
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 55)
                        .background(
                            canCreateAccount ? purple : Color.gray.opacity(0.45)
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 17))
                    }
                    .disabled(!canCreateAccount)
                    .padding(.horizontal, 28)

                    Spacer(minLength: 25)
                }
                .padding(.top, 15)
            }
            .scrollDismissesKeyboard(.interactively)
        }
        .navigationBarBackButtonHidden(true)
    }

    private func executeRegister() {
        guard canCreateAccount else { return }
        isLoading = true
        errorMessage = nil

        Task {
            do {
                _ = try await authService.registerParent(
                    email: email,
                    password: password,
                    acceptedPrivacyConsent: acceptedPrivacyConsent
                )
                await MainActor.run {
                    appState.hasCompletedOnboarding = true
                    isLoading = false
                    dismiss()
                }
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
}

#Preview {
    NavigationStack {
        CreateAccountView()
            .environmentObject(AppState())
    }
}
