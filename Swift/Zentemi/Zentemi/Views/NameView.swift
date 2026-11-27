import SwiftUI

/// Pantalla inicial de nombre del aprendiz (Modo Invitado sin cuenta obligatoria, igual que Android v1.1).
struct NameView: View {

    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) private var dismiss

    @State private var childName = ""
    @State private var mascotScaleY: CGFloat = 1.0

    private let purple = Color(red: 0.43, green: 0.27, blue: 0.78)
    private let successGreen = Color(red: 0.18, green: 0.72, blue: 0.44)

    private var isValidChildName: Bool {
        childName.trimmingCharacters(in: .whitespacesAndNewlines).count >= 3
    }

    var body: some View {
        ZStack {
            Color(red: 0.97, green: 0.95, blue: 0.99)
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 22) {

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

                    Spacer(minLength: 8)

                    Image("mascota")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 110, height: 110)
                        .scaleEffect(x: 1.0, y: mascotScaleY, anchor: .bottom)

                    VStack(spacing: 8) {
                        Text("¿Cómo te llamas?")
                            .font(.system(size: 30, weight: .bold))
                            .foregroundStyle(.primary)

                        Text("Maicito quiere saber tu nombre para\nempezar la aventura.")
                            .font(.system(size: 15))
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .padding(.horizontal, 24)

                    // Tarjeta de nombre
                    VStack(alignment: .leading, spacing: 14) {
                        Text("Tu nombre")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(.secondary)

                        TextField("Escribe tu nombre", text: $childName)
                            .textInputAutocapitalization(.words)
                            .autocorrectionDisabled()
                            .padding()
                            .background(Color.gray.opacity(0.07))
                            .clipShape(RoundedRectangle(cornerRadius: 14))

                        HStack(spacing: 10) {
                            ZStack {
                                RoundedRectangle(cornerRadius: 6)
                                    .fill(
                                        isValidChildName
                                            ? successGreen.opacity(0.15)
                                            : Color.gray.opacity(0.12)
                                    )
                                    .frame(width: 24, height: 24)

                                if isValidChildName {
                                    Image(systemName: "checkmark")
                                        .font(.system(size: 13, weight: .bold))
                                        .foregroundStyle(successGreen)
                                }
                            }

                            Text(isValidChildName ? "Se ve muy bien" : "Escribe al menos 3 caracteres")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundStyle(.secondary)
                        }
                    }
                    .padding(22)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .shadow(color: purple.opacity(0.09), radius: 12, y: 6)
                    .padding(.horizontal, 22)

                    Text("Podrás cambiarlo después en tu perfil")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundStyle(.secondary.opacity(0.75))

                    // Botón ¡Empezar! (entra directo en modo invitado)
                    Button {
                        let cleanName = childName.trimmingCharacters(in: .whitespacesAndNewlines)
                        appState.userName = cleanName
                        appState.hasCompletedOnboarding = true
                    } label: {
                        Text("¡Empezar!")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(
                                isValidChildName ? purple : Color.gray.opacity(0.40)
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 24))
                    }
                    .disabled(!isValidChildName)
                    .padding(.horizontal, 28)

                    // Opción para tutores que ya tienen cuenta previa
                    NavigationLink(destination: SignInView()) {
                        Text("Ya tengo una cuenta")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(purple)
                            .padding(.top, 4)
                    }

                    Spacer(minLength: 25)
                }
                .padding(.top, 15)
            }
            .scrollDismissesKeyboard(.interactively)
        }
        .navigationBarBackButtonHidden(true)
        .onAppear {
            withAnimation(
                .easeInOut(duration: 1.6)
                .repeatForever(autoreverses: true)
            ) {
                mascotScaleY = 1.06
            }
        }
    }
}

#Preview {
    NavigationStack {
        NameView()
            .environmentObject(AppState())
    }
}
