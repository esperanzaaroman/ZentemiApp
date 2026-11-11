import SwiftUI

struct CreatePINView: View {
    
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject var appState: AppState
    
    @State private var pin = ""
    @State private var confirmPin = ""
    @State private var goToRecoveryWord = false
    
    private var isValidPIN: Bool {
        pin.count == 4 &&
        confirmPin.count == 4 &&
        pin == confirmPin
    }
    
    var body: some View {
        ZStack {
            
            background
            
            VStack(spacing: 22) {
                
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
                
                Image(systemName: "lock.fill")
                    .font(.system(size: 55))
                    .foregroundStyle(.white)
                
                Text("Crea tu PIN")
                    .font(.system(size: 32, weight: .bold))
                    .foregroundStyle(.white)
                
                Text("Usarás este PIN para entrar a tu cuenta.")
                    .font(.system(size: 16))
                    .foregroundStyle(.white.opacity(0.80))
                    .multilineTextAlignment(.center)
                
                VStack(spacing: 16) {
                    
                    SecureField("PIN de 4 dígitos", text: $pin)
                        .keyboardType(.numberPad)
                        .textContentType(.oneTimeCode)
                        .padding()
                        .background(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .onChange(of: pin) { _, newValue in
                            pin = String(
                                newValue
                                    .filter { $0.isNumber }
                                    .prefix(4)
                            )
                        }
                    
                    SecureField("Confirma tu PIN", text: $confirmPin)
                        .keyboardType(.numberPad)
                        .textContentType(.oneTimeCode)
                        .padding()
                        .background(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .onChange(of: confirmPin) { _, newValue in
                            confirmPin = String(
                                newValue
                                    .filter { $0.isNumber }
                                    .prefix(4)
                            )
                        }
                    
                    if !confirmPin.isEmpty {
                        
                        HStack {
                            
                            Image(
                                systemName:
                                    isValidPIN
                                    ? "checkmark.circle.fill"
                                    : "xmark.circle.fill"
                            )
                            .foregroundStyle(
                                isValidPIN ? .green : .red
                            )
                            
                            Text(
                                isValidPIN
                                ? "Los PIN coinciden"
                                : "Los PIN no coinciden"
                            )
                            .font(.system(size: 14))
                            .foregroundStyle(.white.opacity(0.85))
                            
                            Spacer()
                        }
                    }
                }
                .padding(20)
                .background(.white.opacity(0.15))
                .clipShape(RoundedRectangle(cornerRadius: 22))
                .padding(.horizontal, 25)
                
                Button {
                    
                    /*
                     Después guardaremos el PIN
                     de forma segura en el backend.
                     */
                    
                    goToRecoveryWord = true
                    
                } label: {
                    
                    Text("Continuar")
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
                        .clipShape(RoundedRectangle(cornerRadius: 18))
                }
                .disabled(!isValidPIN)
                .padding(.horizontal, 30)
                
                Spacer()
            }
        }
        .navigationBarBackButtonHidden(true)
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
        CreatePINView()
            .environmentObject(AppState())
    }
}
