
import SwiftUI

struct NameView: View {

    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) private var dismiss

    @State private var name = ""
    @State private var pin = ""
    @State private var confirmPIN = ""
    @State private var goToMain = false
    @State private var animateBackground = false

    private let purple = Color(
        red: 0.43,
        green: 0.27,
        blue: 0.78
    )

    private var isValidName: Bool {
        name.trimmingCharacters(
            in: .whitespacesAndNewlines
        ).count >= 3
    }

    private var isValidPIN: Bool {
        pin.count == 4 &&
        confirmPIN.count == 4 &&
        pin == confirmPIN
    }

    private var canCreateAccount: Bool {
        isValidName && isValidPIN
    }

    var body: some View {

        ZStack {

            background

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
                        }

                        Spacer()
                    }
                    .padding(.horizontal, 22)

                    Image("mascota")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 110, height: 110)
                        .padding(.top, 12)

                    VStack(spacing: 8) {

                        Text("Crear cuenta")
                            .font(.system(size: 30, weight: .bold))
                            .foregroundStyle(purple)

                        Text("¡Comienza tu aventura aprendiendo Yuhmu!")
                            .font(.system(size: 15))
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                    }

                    VStack(alignment: .leading, spacing: 16) {

                        VStack(alignment: .leading, spacing: 7) {

                            Text("Nombre de usuario")
                                .font(.system(size: 15, weight: .semibold))

                            TextField("Escribe tu nombre", text: $name)
                                .textInputAutocapitalization(.words)
                                .autocorrectionDisabled()
                                .padding()
                                .background(Color.gray.opacity(0.07))
                                .clipShape(
                                    RoundedRectangle(cornerRadius: 13)
                                )

                            Text(
                                isValidName
                                ? "Nombre válido"
                                : "Mínimo 3 caracteres"
                            )
                            .font(.system(size: 12))
                            .foregroundStyle(
                                isValidName ? .green : .secondary
                            )
                        }

                        VStack(alignment: .leading, spacing: 7) {

                            Text("Crear PIN")
                                .font(.system(size: 15, weight: .semibold))

                            SecureField("PIN de 4 dígitos", text: $pin)
                                .keyboardType(.numberPad)
                                .textContentType(.oneTimeCode)
                                .padding()
                                .background(Color.gray.opacity(0.07))
                                .clipShape(
                                    RoundedRectangle(cornerRadius: 13)
                                )
                                .onChange(of: pin) { _, value in
                                    pin = String(
                                        value.filter { $0.isNumber }.prefix(4)
                                    )
                                }
                        }

                        VStack(alignment: .leading, spacing: 7) {

                            Text("Confirmar PIN")
                                .font(.system(size: 15, weight: .semibold))

                            SecureField(
                                "Confirma tu PIN",
                                text: $confirmPIN
                            )
                            .keyboardType(.numberPad)
                            .textContentType(.oneTimeCode)
                            .padding()
                            .background(Color.gray.opacity(0.07))
                            .clipShape(
                                RoundedRectangle(cornerRadius: 13)
                            )
                            .onChange(of: confirmPIN) { _, value in
                                confirmPIN = String(
                                    value.filter { $0.isNumber }.prefix(4)
                                )
                            }

                            if !confirmPIN.isEmpty {
                                Label(
                                    isValidPIN
                                    ? "Los PIN coinciden"
                                    : "Los PIN no coinciden",
                                    systemImage: isValidPIN
                                    ? "checkmark.circle.fill"
                                    : "xmark.circle.fill"
                                )
                                .font(.system(size: 12))
                                .foregroundStyle(
                                    isValidPIN ? .green : .red
                                )
                            }
                        }
                    }
                    .padding(22)
                    .background(.white)
                    .clipShape(
                        RoundedRectangle(cornerRadius: 22)
                    )
                    .shadow(
                        color: .black.opacity(0.05),
                        radius: 10,
                        y: 5
                    )
                    .padding(.horizontal, 22)

                    Button {

                        appState.userName = name.trimmingCharacters(
                            in: .whitespacesAndNewlines
                        )

                        // Registro visual por ahora.
                        // El backend se integrará después.

                        goToMain = true

                    } label: {

                        Text("Crear cuenta")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 55)
                            .background(
                                canCreateAccount
                                ? purple
                                : Color.gray.opacity(0.45)
                            )
                            .clipShape(
                                RoundedRectangle(cornerRadius: 17)
                            )
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
        .navigationDestination(isPresented: $goToMain) {
            MainTabView()
        }
    }

    private var background: some View {

        ZStack {

            Color(
                red: 0.97,
                green: 0.95,
                blue: 0.99
            )

            Circle()
                .fill(Color.purple.opacity(0.06))
                .frame(width: 230, height: 230)
                .offset(
                    x: animateBackground ? 120 : 160,
                    y: -320
                )

            Circle()
                .fill(Color.cyan.opacity(0.06))
                .frame(width: 170, height: 170)
                .offset(x: -150, y: 180)
        }
        .ignoresSafeArea()
        .onAppear {
            withAnimation(
                .easeInOut(duration: 6)
                    .repeatForever(autoreverses: true)
            ) {
                animateBackground = true
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
