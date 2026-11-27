import SwiftUI

struct ContentView: View {

    @StateObject private var appState = AppState()
    @StateObject private var authService = AuthService.shared
    @StateObject private var networkMonitor = NetworkMonitor.shared
    @Environment(\.scenePhase) private var scenePhase

    @State private var showAccountSuggestion = false
    @State private var showCreateAccountFromSuggestion = false

    private var hasPendingProgressChoice: Binding<Bool> {
        Binding(
            get: { appState.pendingProgressChoice != nil },
            set: { _ in }
        )
    }

    var body: some View {
        Group {
            if appState.hasCompletedOnboarding {
                NavigationStack {
                    MainTabView()
                }
            } else {
                NavigationStack {
                    OnboardingView()
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
            evaluateAccountSuggestion()
        }
        .onChange(of: scenePhase) { _, newPhase in
            if newPhase == .active {
                appState.updateDailyStreak()
                authService.registerUserActivity()
            }
        }
        .onChange(of: appState.hasCompletedOnboarding) { _, _ in
            evaluateAccountSuggestion()
        }
        .onChange(of: networkMonitor.isOnline) { _, _ in
            evaluateAccountSuggestion()
        }
        .onChange(of: appState.isGuest) { _, isGuest in
            if !isGuest {
                showAccountSuggestion = false
            }
        }
        // Sugerencia periódica (cada 7 días con internet) para guardar progreso en la nube
        .alert(
            "¿Quieres guardar tu progreso en la nube?",
            isPresented: $showAccountSuggestion
        ) {
            Button("Crear cuenta") {
                appState.markAccountSuggestionAnswered()
                showCreateAccountFromSuggestion = true
            }
            Button("Ahora no", role: .cancel) {
                appState.markAccountSuggestionAnswered()
            }
        } message: {
            Text("Pídele a un adulto que cree una cuenta para no perder tu progreso con \(appState.petName) si cambias de teléfono.")
        }
        // Elección al iniciar sesión cuando ya había progreso como invitado en el teléfono
        .alert(
            "¿Qué progreso quieres usar?",
            isPresented: hasPendingProgressChoice
        ) {
            Button("Usar el progreso de este teléfono") {
                appState.keepPhoneProgress()
            }
            Button("Usar el progreso de mi cuenta") {
                appState.useAccountProgress()
            }
        } message: {
            Text("Tienes progreso guardado en este teléfono y también en tu cuenta de Zentemi. Elige cuál quieres conservar.")
        }
        .sheet(isPresented: $showCreateAccountFromSuggestion) {
            NavigationStack {
                CreateAccountView()
                    .environmentObject(appState)
            }
        }
    }

    private func evaluateAccountSuggestion() {
        if appState.hasCompletedOnboarding,
           networkMonitor.isOnline,
           appState.isAccountSuggestionDue,
           appState.pendingProgressChoice == nil {
            showAccountSuggestion = true
        }
    }
}

#Preview {
    ContentView()
}
