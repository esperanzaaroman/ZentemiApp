import SwiftUI

struct SignInView: View {
    
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject var appState: AppState
    
    @State private var userName = ""
    @State private var pin = ""
    
    @State private var goToMain = false
    @State private var showRecovery = false
    
    @State private var recoveryType: RecoveryType = .pin
    
    enum RecoveryType {
        case pin
        case username
    }
    
    private var canLogin: Bool {
        !userName
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .isEmpty
        &&
        pin.count == 4
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
                
                
                Image(systemName: "person.crop.circle.fill")
                    .font(.system(size: 70))
                    .foregroundStyle(.white)
                
                
                VStack(spacing: 8) {
                    
                    Text("Iniciar sesión")
                        .font(.system(size: 32, weight: .bold))
                        .foregroundStyle(.white)
                    
                    Text("Ingresa a tu aventura en Zentemi")
                        .font(.system(size: 16))
                        .foregroundStyle(.white.opacity(0.80))
                }
                
                
                // Campos
                
                VStack(spacing: 16) {
                    
                    TextField(
                        "Nombre de usuario",
                        text: $userName
                    )
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .padding()
                    .background(.white)
                    .clipShape(
                        RoundedRectangle(cornerRadius: 14)
                    )
                    
                    
                    SecureField(
                        "PIN de 4 dígitos",
                        text: $pin
                    )
                    .keyboardType(.numberPad)
                    .textContentType(.oneTimeCode)
                    .padding()
                    .background(.white)
                    .clipShape(
                        RoundedRectangle(cornerRadius: 14)
                    )
                    .onChange(of: pin) { _, newValue in
                        
                        pin = String(
                            newValue
                                .filter { $0.isNumber }
                                .prefix(4)
                        )
                    }
                }
                .padding(20)
                .background(.white.opacity(0.15))
                .clipShape(
                    RoundedRectangle(cornerRadius: 22)
                )
                .padding(.horizontal, 25)
                
                
                // Recuperación
                
                VStack(spacing: 12) {
                    
                    Button {
                        
                        recoveryType = .pin
                        showRecovery = true
                        
                    } label: {
                        
                        Text("Olvidé mi PIN")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(.white)
                    }
                    
                    
                    Button {
                        
                        recoveryType = .username
                        showRecovery = true
                        
                    } label: {
                        
                        Text("Olvidé mi nombre de usuario")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(.white.opacity(0.85))
                    }
                }
                
                
                // Iniciar sesión
                
                Button {
                    
                    /*
                     Más adelante:
                     
                     Firebase validará:
                     - nombre de usuario
                     - PIN
                     - cuenta existente
                     */
                    
                    appState.userName =
                    userName.trimmingCharacters(
                        in: .whitespacesAndNewlines
                    )
                    
                    goToMain = true
                    
                } label: {
                    
                    Text("Iniciar sesión")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(
                            Color(
                                red: 0.35,
                                green: 0.17,
                                blue: 0.65
                            )
                        )
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(
                            canLogin
                            ? Color.white
                            : Color.white.opacity(0.40)
                        )
                        .clipShape(
                            RoundedRectangle(cornerRadius: 18)
                        )
                }
                .disabled(!canLogin)
                .padding(.horizontal, 30)
                
                
                Spacer()
            }
        }
        .navigationBarBackButtonHidden(true)
        
        .navigationDestination(
            isPresented: $goToMain
        ) {
            MainTabView()
        }
        
        .sheet(
            isPresented: $showRecovery
        ) {
            
            RecoveryKeyView(
                recoveryType: recoveryType
            )
            .environmentObject(appState)
        }
    }
    
    
    // MARK: - Fondo
    
    private var background: some View {
        
        LinearGradient(
            colors: [
                
                Color(
                    red: 0.47,
                    green: 0.28,
                    blue: 0.78
                ),
                
                Color(
                    red: 0.34,
                    green: 0.17,
                    blue: 0.64
                )
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
