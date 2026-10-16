import SwiftUI

struct RecoveryKeyView: View {
    
    @Environment(\.dismiss) private var dismiss
    
    let recoveryType: SignInView.RecoveryType
    
    @State private var recoveryWord = ""
    @State private var goToNewPIN = false
    
    var body: some View {
        NavigationStack {
            
            ZStack {
                
                Color(
                    red: 0.97,
                    green: 0.95,
                    blue: 0.99
                )
                .ignoresSafeArea()
                
                VStack(spacing: 24) {
                    
                    Spacer()
                    
                    Image(systemName: "key.fill")
                        .font(.system(size: 55))
                        .foregroundStyle(.purple)
                    
                    Text(titleText)
                        .font(.system(size: 28, weight: .bold))
                        .multilineTextAlignment(.center)
                    
                    Text("Escribe la palabra clave que recibiste cuando creaste tu cuenta.")
                        .font(.system(size: 16))
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 30)
                    
                    TextField(
                        "Palabra clave",
                        text: $recoveryWord
                    )
                    .textInputAutocapitalization(.words)
                    .autocorrectionDisabled()
                    .padding()
                    .background(.white)
                    .clipShape(
                        RoundedRectangle(cornerRadius: 14)
                    )
                    .padding(.horizontal, 30)
                    
                    Button {
                        
                        /*
                         Más adelante Firebase comprobará
                         si la palabra clave pertenece
                         realmente a esa cuenta.
                         */
                        
                        goToNewPIN = true
                        
                    } label: {
                        
                        Text("Continuar")
                            .font(.system(size: 17, weight: .bold))
                            .foregroundStyle(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(
                                recoveryWord
                                    .trimmingCharacters(
                                        in: .whitespacesAndNewlines
                                    )
                                    .isEmpty
                                ? Color.gray.opacity(0.4)
                                : Color.purple
                            )
                            .clipShape(
                                RoundedRectangle(cornerRadius: 16)
                            )
                    }
                    .disabled(
                        recoveryWord
                            .trimmingCharacters(
                                in: .whitespacesAndNewlines
                            )
                            .isEmpty
                    )
                    .padding(.horizontal, 30)
                    
                    Button {
                        dismiss()
                    } label: {
                        Text("Cancelar")
                            .foregroundStyle(.secondary)
                    }
                    
                    Spacer()
                }
            }
            .navigationDestination(
                isPresented: $goToNewPIN
            ) {
                NewPINView()
            }
        }
    }
    
    private var titleText: String {
        
        switch recoveryType {
        case .pin:
            return "Recuperar PIN"
            
        case .username:
            return "Recuperar nombre de usuario"
        }
    }
}

#Preview {
    RecoveryKeyView(
        recoveryType: .pin
    )
}
