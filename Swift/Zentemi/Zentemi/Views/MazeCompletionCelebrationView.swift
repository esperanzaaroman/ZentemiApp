import SwiftUI

/// Celebración de la primera victoria del nivel final; no entrega premios.
struct MazeCompletionCelebrationView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @Environment(\.scenePhase) private var scenePhase
    let onContinue: () -> Void

    @State private var beat = 0
    @State private var revealed = false
    @State private var ready = false
    @State private var sounds = LessonResultSounds()

    private var accent: Color {
        revealed ? Color(red: 0.10, green: 0.52, blue: 0.25) : .purple
    }

    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 28) {
                    Text(revealed ? "¡Laberinto completado!" : "Un último reconocimiento…")
                        .font(.largeTitle.bold())
                        .multilineTextAlignment(.center)
                        .foregroundStyle(.white)

                    VStack(spacing: 22) {
                        Image("laberintoFoto")
                            .resizable()
                            .scaledToFit()
                            .frame(maxWidth: 240)
                            .clipShape(RoundedRectangle(cornerRadius: 24))
                            .accessibilityHidden(true)

                        Text("Laberinto")
                            .font(.title.bold())
                            .foregroundStyle(accent)

                        Label(revealed ? "¡Completado!" : "5 niveles superados",
                              systemImage: revealed ? "checkmark.seal.fill" : "sparkles")
                            .font(.title3.bold())
                            .foregroundStyle(accent)
                    }
                    .padding(28)
                    .frame(maxWidth: 340)
                    .background(revealed ? Color(red: 0.78, green: 0.96, blue: 0.80) : .white,
                                in: RoundedRectangle(cornerRadius: 32))
                    .overlay {
                        RoundedRectangle(cornerRadius: 32)
                            .stroke(accent, lineWidth: revealed ? 5 : 2)
                    }
                    .shadow(color: accent.opacity(0.45), radius: revealed ? 28 : 10)
                    .scaleEffect(reduceMotion ? 1 : revealed ? 1.03 : (beat.isMultiple(of: 2) ? 0.97 : 1.01))

                    Text(revealed
                         ? "¡Lo lograste! Puedes volver a jugar y mejorar tus tiempos."
                         : "Tu esfuerzo merece algo especial.")
                        .font(.headline)
                        .multilineTextAlignment(.center)
                        .foregroundStyle(.white)

                    Button("Ver resultados", action: onContinue)
                        .buttonStyle(MazePrimaryButtonStyle())
                        .disabled(!ready)
                        .opacity(ready ? 1 : 0)
                        .accessibilityHidden(!ready)
                        .frame(maxWidth: 340)
                }
                .padding(28)
                .frame(maxWidth: .infinity, minHeight: geometry.size.height)
            }
        }
        .background {
            LinearGradient(colors: revealed
                           ? [Color(red: 0.07, green: 0.31, blue: 0.22), .green.opacity(0.75)]
                           : [Color(red: 0.18, green: 0.10, blue: 0.30), .purple],
                           startPoint: .top, endPoint: .bottom)
                .ignoresSafeArea()
        }
        .sensoryFeedback(.impact(weight: .medium, intensity: 0.5), trigger: beat)
        .sensoryFeedback(.success, trigger: revealed)
        .task(id: scenePhase) {
            guard scenePhase == .active else {
                sounds.stop()
                return
            }
            await celebrate()
        }
        .onDisappear { sounds.stop() }
    }

    @MainActor
    private func celebrate() async {
        // El estado permite continuar al volver del segundo plano sin repetir
        // los pulsos ya mostrados ni volver a conceder recompensas.
        do {
            while beat < 4 {
                try await Task.sleep(for: .milliseconds(550))
                try Task.checkCancellation()
                guard scenePhase == .active else { return }
                withAnimation(reduceMotion ? nil : .easeInOut(duration: 0.28)) {
                    beat += 1
                }
                sounds.playCompletionPulse(beat, volume: Float(appState.soundVolume))
            }
            if !revealed {
                try await Task.sleep(for: .milliseconds(650))
                try Task.checkCancellation()
                guard scenePhase == .active else { return }
                withAnimation(.easeInOut(duration: reduceMotion ? 0.2 : 0.85)) {
                    revealed = true
                }
                sounds.playMazeCompletion(volume: Float(appState.soundVolume))
            }
            if !ready {
                try await Task.sleep(for: .milliseconds(1100))
                try Task.checkCancellation()
                guard scenePhase == .active else { return }
                withAnimation(.easeIn(duration: 0.2)) { ready = true }
            }
        } catch {
            sounds.stop()
        }
    }
}
