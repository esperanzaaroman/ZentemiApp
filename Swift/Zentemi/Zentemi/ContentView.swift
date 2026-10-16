import SwiftUI

struct ContentView: View {

    @StateObject private var appState = AppState()

    var body: some View {

        NavigationStack {

            OnboardingView()
            //MainTabView()

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

    }

}

#Preview {

    ContentView()

}
