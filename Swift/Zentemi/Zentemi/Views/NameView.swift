import SwiftUI

struct NameView: View {
    
    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) private var dismiss
    
    @State private var name = ""
    
    @State private var mascotScaleY: CGFloat = 1.0
    @State private var animateBackground = false
    
    @State private var goToMain = false
    
    var isValidName: Bool {
        name.trimmingCharacters(in: .whitespacesAndNewlines).count >= 3
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
                            .font(.system(size: 17, weight: .bold))
                            .foregroundStyle(
                                Color(
                                    red: 0.43,
                                    green: 0.27,
                                    blue: 0.78
                                )
                            )
                            .frame(width: 40, height: 40)
                            .background(.white)
                            .clipShape(Circle())
                            .shadow(
                                color: .black.opacity(0.05),
                                radius: 5,
                                y: 2
                            )
                    }

                    Spacer()
                }
                .padding(.horizontal, 20)
                
                Spacer()
                    .frame(height: 45)
                
                Image("mascota")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 90, height: 90)
                    .scaleEffect(
                        x: 1.0,
                        y: mascotScaleY,
                        anchor: .bottom
                    )
                
                VStack(spacing: 8) {
                    
                    Text("¿Cómo te llamas?")
                        .font(.system(size: 30, weight: .bold))
                        .foregroundStyle(
                            Color(
                                red: 0.18,
                                green: 0.17,
                                blue: 0.22
                            )
                        )
                    
                    Text("Maicito quiere saber tu nombre para\nempezar la aventura.")
                        .font(.system(size: 16))
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                }
                
                VStack(alignment: .leading, spacing: 14) {
                    
                    Text("Tu nombre")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundStyle(.secondary)
                    
                    TextField(
                        "Escribe tu nombre",
                        text: $name
                    )
                    .padding(.horizontal, 16)
                    .frame(height: 50)
                    .background(
                        RoundedRectangle(cornerRadius: 15)
                            .fill(Color.white)
                    )
                    .overlay {
                        RoundedRectangle(cornerRadius: 15)
                            .stroke(
                                Color.purple.opacity(0.18),
                                lineWidth: 1
                            )
                    }
                    
                    HStack(spacing: 10) {
                        
                        ZStack {
                            
                            RoundedRectangle(cornerRadius: 6)
                                .fill(
                                    isValidName
                                    ? Color.green.opacity(0.15)
                                    : Color.gray.opacity(0.12)
                                )
                                .frame(width: 24, height: 24)
                            
                            if isValidName {
                                Image(systemName: "checkmark")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundStyle(.green)
                            }
                        }
                        
                        Text(
                            isValidName
                            ? "Se ve muy bien"
                            : "Escribe al menos 3 caracteres"
                        )
                        .font(.system(size: 14))
                        .foregroundStyle(.secondary)
                    }
                }
                .padding(22)
                .background(
                    RoundedRectangle(cornerRadius: 24)
                        .fill(Color.white.opacity(0.95))
                        .shadow(
                            color: .black.opacity(0.05),
                            radius: 12,
                            x: 0,
                            y: 6
                        )
                )
                .padding(.horizontal, 22)
                
                Text("Podrás cambiarlo después en tu perfil")
                    .font(.system(size: 14))
                    .foregroundStyle(.secondary.opacity(0.65))
                
                Button {
                    
                    appState.userName =
                    name.trimmingCharacters(in: .whitespacesAndNewlines)
                    
                    goToMain = true
                    
                } label: {
                    
                    Text("¡Empezar!")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 17)
                        .background(
                            isValidName
                            ? Color(
                                red: 0.43,
                                green: 0.27,
                                blue: 0.78
                            )
                            : Color.gray.opacity(0.45)
                        )
                        .clipShape(
                            RoundedRectangle(cornerRadius: 18)
                        )
                }
                .disabled(!isValidName)
                .padding(.horizontal, 28)
                
                Spacer()
            }
        }
        .navigationBarBackButtonHidden(true)
        .navigationDestination(isPresented: $goToMain) {
            CreatePINView()
        }
        .onAppear {
            
            withAnimation(
                .easeInOut(duration: 1.6)
                .repeatForever(autoreverses: true)
            ) {
                mascotScaleY = 1.06
            }
            
            withAnimation(
                .easeInOut(duration: 6)
                .repeatForever(autoreverses: true)
            ) {
                animateBackground = true
            }
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
                .frame(width: 220, height: 220)
                .offset(
                    x: animateBackground ? 125 : 165,
                    y: animateBackground ? -300 : -345
                )
            
            Circle()
                .fill(Color.cyan.opacity(0.06))
                .frame(width: 150, height: 150)
                .offset(
                    x: animateBackground ? -135 : -175,
                    y: animateBackground ? -20 : -65
                )
            
            Circle()
                .fill(Color.yellow.opacity(0.10))
                .frame(width: 120, height: 120)
                .offset(
                    x: animateBackground ? 120 : 155,
                    y: animateBackground ? 280 : 320
                )
        }
        .ignoresSafeArea()
    }
}

#Preview {
    NavigationStack {
        NameView()
            .environmentObject(AppState())
    }
}
