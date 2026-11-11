import SwiftUI

struct ContentView: View {

    @StateObject private var appState = AppState()
    @StateObject private var authService = AuthService.shared
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        Group {
            if authService.isAuthenticated {
                if authService.isEmailVerified {
                    NavigationStack {
                        MainTabView()
                    }
                } else {
                    NavigationStack {
                        EmailVerificationView()
                    }
                }
            } else {
                NavigationStack {
                    LoginView()
                }
            }
        }
        .environmentObject(appState)
        .environmentObject(authService)
        .onAppear {
            if appState.musicVolume > 0 {
                MusicManager.shared.playMusic(
                    named: "musicaInicio",
                    volume: Float(appState.musicVolume)
                )
            }
        }
        .onChange(of: scenePhase) { _, newPhase in
            if newPhase == .active {
                appState.updateDailyStreak()
                authService.registerUserActivity()
            }
        }
    }
}

#Preview {
    ContentView()
}
