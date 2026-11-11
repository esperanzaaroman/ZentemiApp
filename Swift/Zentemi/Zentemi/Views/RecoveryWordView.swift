import SwiftUI

struct RecoveryWordView: View {
    
    @EnvironmentObject var appState: AppState
    
    @State private var secondsRemaining = 20
    @State private var goToMain = false
    
    private let recoveryWord = "AGUA"
    
    var body: some View {
        ZStack {
            
            background
            
            VStack(spacing: 24) {
                
                Spacer()
                
                Image(systemName: "key.fill")
                    .font(.system(size: 55))
                    .foregroundStyle(.white)
                
                Text("Guarda tu palabra clave")
                    .font(.system(size: 30, weight: .bold))
                    .foregroundStyle(.white)
                    .multilineTextAlignment(.center)
                
                VStack(spacing: 18) {
                    
                    Text("Tu palabra clave es:")
                        .font(.system(size: 16))
                        .foregroundStyle(.secondary)
                    
                    Text(recoveryWord)
                        .font(.system(size: 38, weight: .bold))
                        .foregroundStyle(.purple)
                    
                    Text(
                        "Anota esta palabra en tu libreta o pídele a un tutor que la guarde. La necesitarás si algún día olvidas tu PIN o tu nombre de usuario."
                    )
                    .font(.system(size: 15))
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    
                    Divider()
                    
                    if secondsRemaining > 0 {
                        
                        VStack(spacing: 8) {
                            
                            Text("Podrás continuar en:")
                                .font(.system(size: 13))
                                .foregroundStyle(.secondary)
                            
                            Text("\(secondsRemaining)")
                                .font(.system(size: 30, weight: .bold))
                                .foregroundStyle(.purple)
                            
                            Text("segundos")
                                .font(.system(size: 13))
                                .foregroundStyle(.secondary)
                        }
                        
                    } else {
                        
                        Text("¡Listo! Ya puedes continuar.")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(.green)
                    }
                }
                .padding(24)
                .background(.white)
                .clipShape(RoundedRectangle(cornerRadius: 22))
                .padding(.horizontal, 25)
                
                if secondsRemaining == 0 {
                    
                    Button {
                        goToMain = true
                    } label: {
                        
                        Text("Entendido")
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
                }
                
                Spacer()
            }
        }
        .navigationBarBackButtonHidden(true)
        .navigationDestination(
            isPresented: $goToMain
        ) {
            MainTabView()
        }
        .onAppear {
            startTimer()
        }
    }
    
    private func startTimer() {
        
        secondsRemaining = 20
        
        Timer.scheduledTimer(
            withTimeInterval: 1,
            repeats: true
        ) { timer in
            
            if secondsRemaining > 0 {
                secondsRemaining -= 1
            } else {
                timer.invalidate()
            }
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
        RecoveryWordView()
            .environmentObject(AppState())
    }
}
