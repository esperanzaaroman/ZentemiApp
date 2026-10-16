import SwiftUI

struct LoginView: View {
    
    @State private var animateBackground = false
    @State private var mascotScaleY: CGFloat = 1.0
    @EnvironmentObject var appState: AppState
    
    var body: some View {
        ZStack {
            
            background
            
            VStack(spacing: 25) {
                
                Spacer()
                
                // Mascota
                
                ZStack {
                    
                    Circle()
                        .fill(.white.opacity(0.18))
                        .frame(width: 220, height: 220)
                        .blur(radius: 20)
                    
                    Circle()
                        .fill(.yellow.opacity(0.15))
                        .frame(width: 180, height: 180)
                        .blur(radius: 25)
                    
                    Image("mascota")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 180, height: 180)
                        .scaleEffect(
                            x: 1.0,
                            y: mascotScaleY,
                            anchor: .bottom
                        )
                }
                
                VStack(spacing: 8) {
                    
                    Text("¡Bienvenido!")
                        .font(.system(size: 34, weight: .bold))
                        .foregroundStyle(.white)
                    
                    Text("Comencemos tu aventura aprendiendo Yuhmu")
                        .font(.system(size: 17, weight: .medium))
                        .foregroundStyle(.white.opacity(0.85))
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 35)
                }
                
                Spacer()
                
                // Crear cuenta
                
                NavigationLink(destination: NameView()) {
                    
                    Text("Crear cuenta")
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
                        .background(.white)
                        .clipShape(
                            RoundedRectangle(cornerRadius: 18)
                        )
                }
                .padding(.horizontal, 30)
                
                
                // Iniciar sesión
                
                NavigationLink(destination: SignInView()) {
                    
                    Text("Ya tengo una cuenta")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(
                            .white.opacity(0.15)
                        )
                        .overlay {
                            RoundedRectangle(cornerRadius: 18)
                                .stroke(
                                    .white.opacity(0.6),
                                    lineWidth: 1.5
                                )
                        }
                        .clipShape(
                            RoundedRectangle(cornerRadius: 18)
                        )
                }
                .padding(.horizontal, 30)
                
                
                Text("Tu progreso estará asociado a tu cuenta")
                    .font(.system(size: 13))
                    .foregroundStyle(.white.opacity(0.60))
                
                Spacer()
                    .frame(height: 30)
            }
        }
        .navigationBarBackButtonHidden(true)
        .onAppear {
            
            withAnimation(
                .easeInOut(duration: 1.6)
                .repeatForever(autoreverses: true)
            ) {
                mascotScaleY = 1.06
            }
            
            withAnimation(
                .easeInOut(duration: 7)
                .repeatForever(autoreverses: true)
            ) {
                animateBackground = true
            }
        }
    }
    
    
    // MARK: - Fondo
    
    private var background: some View {
        ZStack {
            
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
            
            Circle()
                .fill(.white.opacity(0.07))
                .frame(width: 220, height: 220)
                .offset(
                    x: animateBackground ? 125 : 165,
                    y: animateBackground ? -270 : -320
                )
            
            Circle()
                .fill(.white.opacity(0.08))
                .frame(width: 160, height: 160)
                .offset(
                    x: animateBackground ? -135 : -175,
                    y: animateBackground ? -60 : -120
                )
            
            Circle()
                .fill(.white.opacity(0.06))
                .frame(width: 250, height: 250)
                .offset(
                    x: animateBackground ? -110 : -155,
                    y: animateBackground ? 290 : 350
                )
            
            Circle()
                .fill(.white.opacity(0.05))
                .frame(width: 180, height: 180)
                .offset(
                    x: animateBackground ? 135 : 175,
                    y: animateBackground ? 220 : 280
                )
        }
        .ignoresSafeArea()
    }
}

#Preview {
    NavigationStack {
        LoginView()
            .environmentObject(AppState())
    }
}
