
import SwiftUI

struct ContentView: View {

    @StateObject private var appState = AppState()
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {

        NavigationStack {
            //OnboardingView()
            MainTabView()
        }
        .environmentObject(appState)
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
            }
        }
    }
}

#Preview {
    ContentView()
}
