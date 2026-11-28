import SwiftUI

struct MazeGameView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss
    @Environment(\.scenePhase) private var scenePhase
    @State private var session: MazeGameSession
    @State private var showResults = false
    @State private var showCompletionCelebration = false
    @State private var reward = MazeReward.none
    @State private var coinSounds = LessonResultSounds()
    @State private var joystickID = UUID()

    init(level: MazeLevel = .one, claimedCoinMask: Int = 0) {
        _session = State(initialValue: MazeGameSession(level: level, claimedCoinMask: claimedCoinMask))
    }

    var body: some View {
        ZStack {
            MazeGrassBackground(imageName: session.level.backgroundName)

            if showCompletionCelebration {
                MazeCompletionCelebrationView {
                    showCompletionCelebration = false
                    showResults = true
                }
            } else if showResults {
                MazeResultsView(
                    level: session.level.id,
                    elapsedSeconds: session.elapsedSeconds,
                    earnedCoins: reward.totalCoins,
                    earnedPoints: reward.points,
                    collectedCoins: reward.collectedCoins
                ) {
                    dismiss()
                }
            } else {
                VStack(spacing: 10) {
                    MazeGameHeader(session: session) {
                        dismiss()
                    }

                    MazeBoardView(session: session)
                        .padding(.horizontal, 12)

                    MazeGameControls(session: session)
                        .id(joystickID)
                        .disabled(session.finished || session.wrongExit != nil)
                        .padding(.horizontal, 24)
                        .padding(.bottom, 8)
                }
                .padding(.top, 8)
                .allowsHitTesting(!session.finished && session.wrongExit == nil)
                .accessibilityHidden(session.finished || session.wrongExit != nil)

                if session.finished {
                    MazeExitFeedbackView(isCorrect: true, phrase: session.level.phrase,
                                         translation: session.level.exits.first(where: { $0.isCorrect })?.translation ?? "") {
                        if session.level.id == 5 && reward.baseCoins > 0 {
                            showCompletionCelebration = true
                        } else {
                            showResults = true
                        }
                    }
                    .transition(.opacity)
                } else if session.wrongExit != nil {
                    MazeExitFeedbackView(isCorrect: false, phrase: session.level.phrase, translation: "") {
                        session.continueAfterWrongExit()
                        joystickID = UUID()
                    }
                    .transition(.opacity)
                }
            }
        }
        .task(id: scenePhase) {
            guard scenePhase == .active else { return }
            await runMovement()
        }
        .onChange(of: scenePhase) { _, _ in
            session.direction = .zero
            joystickID = UUID()
        }
        .onChange(of: session.collectedCoinIDs.count) { _, _ in
            coinSounds.playCoin(volume: Float(appState.soundVolume))
        }
        .onDisappear {
            session.direction = .zero
            coinSounds.stop()
        }
    }

    @MainActor
    private func runMovement() async {
        var lastTick = ProcessInfo.processInfo.systemUptime
        while !Task.isCancelled && !session.finished {
            do {
                try await Task.sleep(for: .milliseconds(16))
            } catch {
                return
            }
            guard !Task.isCancelled, scenePhase == .active else { return }
            let now = ProcessInfo.processInfo.systemUptime
            let elapsed = now - lastTick
            lastTick = now

            if let exit = session.advance(seconds: elapsed) {
                joystickID = UUID()
                SoundManager.shared.play(
                    exit.isCorrect ? "audioBien" : "audioMalaSeleccion",
                    volume: Float(appState.soundVolume)
                )
                if exit.isCorrect {
                    reward = appState.completeMaze(
                        level: session.level.id,
                        seconds: session.elapsedSeconds,
                        collectedCoinIDs: session.collectedCoinIDs
                    )
                }
            }
        }
    }
}

private struct MazeGameHeader: View {
    let session: MazeGameSession
    let onClose: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            HStack {
                Button(action: onClose) {
                    Image(systemName: "xmark")
                        .font(.headline)
                        .frame(width: 40, height: 40)
                        .background(.white, in: Circle())
                }
                .accessibilityLabel("Salir del laberinto")
                Spacer()
                Text("Laberinto · Nivel \(session.level.id)").font(.headline)
                Spacer()
                Label(MazeLevel.timeText(session.elapsedSeconds), systemImage: "stopwatch")
                    .font(.caption.bold())
                    .monospacedDigit()
            }
            Text(session.level.phrase)
                .font(.title2.bold())
            Text("Busca su traducción y llega a esa salida.")
                .font(.caption)
            if !session.level.coins.isEmpty {
                Label("+\(session.collectedCoinIDs.count * 10) monedas al completar", systemImage: "circle.fill")
                    .font(.caption.bold())
                    .foregroundStyle(.orange)
            }
        }
        .foregroundStyle(Color(red: 0.35, green: 0.17, blue: 0.65))
        .padding(12)
        .background(.white.opacity(0.95), in: RoundedRectangle(cornerRadius: 22))
        .padding(.horizontal, 16)
    }
}

private struct MazeGameControls: View {
    @Bindable var session: MazeGameSession

    var body: some View {
        HStack(spacing: 20) {
            MaicitoJoystick(direction: $session.direction)
                .accessibilityLabel("Control para mover a tu mascota")
            Text(session.level.startsInside
                 ? "Empiezas en el centro. Explora arriba y abajo: hay \(session.level.exits.count) salidas."
                 : session.level.followsPlayerVertically
                    ? "Explora hacia arriba. La cámara sigue a tu mascota por el laberinto."
                    : "Mueve a tu mascota por los caminos hasta la salida correcta.")
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(Color(red: 0.23, green: 0.34, blue: 0.15))
                .padding(14)
                .background(.white.opacity(0.93), in: RoundedRectangle(cornerRadius: 18))
        }
    }
}

private struct MazeExitFeedbackView: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    let isCorrect: Bool
    let phrase: String
    let translation: String
    let onContinue: () -> Void
    @State private var appeared = false

    var body: some View {
        ZStack {
            Color.black.opacity(0.45).ignoresSafeArea()
            VStack(spacing: 20) {
                Image(systemName: isCorrect ? "checkmark.seal.fill" : "xmark.circle.fill")
                    .font(.system(size: 74))
                    .foregroundStyle(isCorrect ? Color.teal : Color.orange)
                    .scaleEffect(appeared || reduceMotion ? 1 : 0.3)
                    .rotationEffect(.degrees(appeared || reduceMotion || isCorrect ? 0 : -15))

                PetView(size: 100)

                Text(isCorrect ? "¡Esa es la salida correcta!" : "Esta no es la salida correcta")
                    .font(.title2.bold())
                    .multilineTextAlignment(.center)

                Text(isCorrect
                     ? "\(phrase) · \(translation)"
                     : "¡Sigue buscando! Puedes regresar por el camino y probar otra salida.")
                    .font(.body)
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.secondary)

                Button(isCorrect ? "Siguiente" : "Seguir buscando", action: onContinue)
                    .buttonStyle(MazePrimaryButtonStyle())
            }
            .padding(26)
            .frame(maxWidth: 380)
            .background(.white, in: RoundedRectangle(cornerRadius: 28))
            .padding(22)
        }
        .onAppear {
            withAnimation(reduceMotion ? nil : .spring(response: 0.5, dampingFraction: 0.5)) {
                appeared = true
            }
        }
    }
}
