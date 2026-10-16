import SwiftUI

struct NewPINView: View {
    
    @EnvironmentObject var appState: AppState
    
    @State private var newPIN = ""
    @State private var confirmPIN = ""
    
    @State private var goToRecoveryWord = false
    
    private var isValidPIN: Bool {
        newPIN.count == 4 &&
        confirmPIN.count == 4 &&
        newPIN == confirmPIN
    }
    
    var body: some View {
        ZStack {
            
            background
            
            VStack(spacing: 22) {
                
                Spacer()
                
                Image(systemName: "lock.rotation")
                    .font(.system(size: 55))
                    .foregroundStyle(.white)
                
                Text("Crea un nuevo PIN")
                    .font(.system(size: 30, weight: .bold))
                    .foregroundStyle(.white)
                
                Text("Elige un nuevo PIN de 4 dígitos.")
                    .font(.system(size: 16))
                    .foregroundStyle(.white.opacity(0.80))
                
                
                VStack(spacing: 16) {
                    
                    SecureField(
                        "Nuevo PIN",
                        text: $newPIN
                    )
                    .keyboardType(.numberPad)
                    .textContentType(.oneTimeCode)
                    .padding()
                    .background(.white)
                    .clipShape(
                        RoundedRectangle(cornerRadius: 14)
                    )
                    .onChange(of: newPIN) { _, newValue in
                        
                        newPIN = String(
                            newValue
                                .filter { $0.isNumber }
                                .prefix(4)
                        )
                    }
                    
                    
                    SecureField(
                        "Confirma tu nuevo PIN",
                        text: $confirmPIN
                    )
                    .keyboardType(.numberPad)
                    .textContentType(.oneTimeCode)
                    .padding()
                    .background(.white)
                    .clipShape(
                        RoundedRectangle(cornerRadius: 14)
                    )
                    .onChange(of: confirmPIN) { _, newValue in
                        
                        confirmPIN = String(
                            newValue
                                .filter { $0.isNumber }
                                .prefix(4)
                        )
                    }
                    
                    
                    if !confirmPIN.isEmpty {
                        
                        HStack {
                            
                            Image(
                                systemName:
                                    isValidPIN
                                    ? "checkmark.circle.fill"
                                    : "xmark.circle.fill"
                            )
                            .foregroundStyle(
                                isValidPIN
                                ? .green
                                : .red
                            )
                            
                            Text(
                                isValidPIN
                                ? "Los PIN coinciden"
                                : "Los PIN no coinciden"
                            )
                            .font(.system(size: 14))
                            .foregroundStyle(.secondary)
                            
                            Spacer()
                        }
                    }
                }
                .padding(20)
                .background(.white.opacity(0.15))
                .clipShape(
                    RoundedRectangle(cornerRadius: 22)
                )
                .padding(.horizontal, 25)
                
                
                Button {
                    
                    /*
                     Más adelante aquí actualizaremos
                     el PIN real en Firebase.
                     */
                    
                    goToRecoveryWord = true
                    
                } label: {
                    
                    Text("Guardar nuevo PIN")
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
                            isValidPIN
                            ? Color.white
                            : Color.white.opacity(0.40)
                        )
                        .clipShape(
                            RoundedRectangle(cornerRadius: 18)
                        )
                }
                .disabled(!isValidPIN)
                .padding(.horizontal, 30)
                
                
                Spacer()
            }
        }
        .navigationDestination(
            isPresented: $goToRecoveryWord
        ) {
            RecoveryWordView()
        }
    }
    
    
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
        NewPINView()
            .environmentObject(AppState())
    }
}
