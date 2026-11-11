import SwiftUI

struct OnboardingView: View {
    
    @State private var currentPage = 0
    @State private var mascotScale: CGFloat = 1.0
    @State private var animateBackground = false
    
    private let pages = [
        OnboardingPage(
            title: "Zentemi",
            subtitle: "Aprende Yuhmu jugando"
        ),
        
        OnboardingPage(
            title: "Aprende paso a paso",
            subtitle: "Descubre palabras, escucha su pronunciación y avanza a tu ritmo"
        ),
        
        OnboardingPage(
            title: "Conecta con tu cultura",
            subtitle: "Aprende Yuhmu mientras juegas y completas nuevos retos"
        )
    ]
    
    var body: some View {
        ZStack {
            
            background
            
            TabView(selection: $currentPage) {
                
                ForEach(0..<pages.count, id: \.self) { index in
                    
                    VStack {
                        
                        Spacer()
                        
                        mascotView
                        
                        Spacer()
                            .frame(height: 30)
                        
                        Text(pages[index].title)
                            .font(.system(size: 36, weight: .bold))
                            .foregroundStyle(.white)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 30)
                        
                        Text(pages[index].subtitle)
                            .font(.system(size: 18, weight: .medium))
                            .foregroundStyle(.white.opacity(0.85))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 35)
                            .padding(.top, 4)
                        
                        Spacer()
                        
                        pageIndicator
                        
                        if currentPage == 2 {
                            NavigationLink(destination: LoginView()) {
                                Text("Siguiente")
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
                            .padding(.top, 20)
                        }
                        
                        Spacer()
                            .frame(height: 40)
                    }
                    .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
        }
        .onAppear {
            startMascotAnimation()
            withAnimation(
                .easeInOut(duration: 7)
                .repeatForever(autoreverses: true)
            ) {
                animateBackground = true
            }
        }
    }
    
    
    // MARK: - Mascota
    
    private var mascotView: some View {
        ZStack {
            
            Circle()
                .fill(.white.opacity(0.18))
                .frame(width: 240, height: 240)
                .blur(radius: 20)
            
            Circle()
                .fill(.yellow.opacity(0.18))
                .frame(width: 190, height: 190)
                .blur(radius: 30)
            
            Image("mascota")
                .resizable()
                .scaledToFit()
                .frame(width: 200, height: 200)
                .scaleEffect(mascotScale)
        }
    }
    
    
    // MARK: - Indicadores
    
    private var pageIndicator: some View {
        HStack(spacing: 14) {
            
            ForEach(0..<pages.count, id: \.self) { index in
                
                Circle()
                    .fill(
                        currentPage == index
                        ? Color.white
                        : Color.white.opacity(0.35)
                    )
                    .frame(
                        width: currentPage == index ? 14 : 11,
                        height: currentPage == index ? 14 : 11
                    )
                    .animation(
                        .easeInOut(duration: 0.2),
                        value: currentPage
                    )
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
                    x: animateBackground ? 125 : 165,
                    y: animateBackground ? -270 : -320
                )
            
            Circle()
                .fill(.white.opacity(0.06))
                .frame(width: 250, height: 250)
                .offset(
                    x: animateBackground ? 125 : 165,
                    y: animateBackground ? -270 : -320
                )
            
            Circle()
                .fill(.white.opacity(0.05))
                .frame(width: 180, height: 180)
                .offset(
                    x: animateBackground ? 125 : 165,
                    y: animateBackground ? -270 : -320
                )
        }
        .ignoresSafeArea()
    }
    
    
    // MARK: - Animación
    
    private func startMascotAnimation() {
        withAnimation(
            .easeInOut(duration: 0.9)
            .repeatForever(autoreverses: true)
        ) {
            mascotScale = 1.08
        }
    }
}


struct OnboardingPage {
    let title: String
    let subtitle: String
}


#Preview {
    OnboardingView()
}
