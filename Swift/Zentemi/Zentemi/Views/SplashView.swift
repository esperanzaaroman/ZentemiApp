import SwiftUI

struct SplashView: View {
    
    var body: some View {
        ZStack {
            
            // Fondo morado
            LinearGradient(
                colors: [
                    Color(red: 0.34, green: 0.16, blue: 0.66),
                    Color(red: 0.48, green: 0.28, blue: 0.78)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()
            
            VStack(spacing: 16) {
                
                Spacer()
                
                Image("mascota")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 180, height: 180)
                
                Text("Zentemi")
                    .font(.system(size: 36, weight: .bold))
                    .foregroundStyle(.white)
                
                Text("Aprende Yuhmu jugando")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundStyle(.white.opacity(0.85))
                
                Spacer()
                
                Text("• • •")
                    .font(.system(size: 14))
                    .foregroundStyle(.white.opacity(0.7))
                    .padding(.bottom, 35)
            }
        }
    }
}

#Preview {
    SplashView()
}
