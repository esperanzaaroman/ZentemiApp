import SwiftUI

/// "Maicito pide": Maicito dice algo en Yuhmu (audio) y hay que elegir qué significa.
/// Cuenta como acierto solo si se elige bien al primer intento.
/// Espejo exacto de `MaicitoPideGame.kt` en Android.
struct MaicitoPideGameView: View {

    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) private var dismiss

    @State private var gameId = 0

    var body: some View {
        MaicitoPideSessionView(
            onPlayAgain: { gameId += 1 },
            onExit: { dismiss() }
        )
        .id(gameId)
        .onAppear {
            MusicManager.shared.pauseMusic()
        }
        .onDisappear {
            if appState.musicVolume > 0 {
                MusicManager.shared.playMusic(
                    named: "audiolobby",
                    volume: Float(appState.musicVolume)
                )
            }
        }
    }
}

private struct PideRound {
    let target: YuhmuWord
    let options: [YuhmuWord]
}

private struct MaicitoPideSessionView: View {

    @EnvironmentObject var appState: AppState
    @StateObject private var phrasePlayer = PhrasePlayer()

    let onPlayAgain: () -> Void
    let onExit: () -> Void

    private static let totalRounds = 5
    private static let optionsPerRound = 3

    @State private var rounds: [PideRound] = []
    @State private var roundIndex = 0
    @State private var firstTryCorrect = 0
    @State private var wrongPicks: Set<String> = []
    @State private var solved = false
    @State private var showConfetti = false
    @State private var finished = false

    // Animaciones de Maicito y sacudida
    @State private var screenShake: CGFloat = 0
    @State private var maicitoJumpY: CGFloat = 0
    @State private var maicitoWobble: Double = 0
    @State private var breathScale: CGFloat = 1.0

    var body: some View {
        ZStack {
            if !rounds.isEmpty {
                let round = rounds[roundIndex]

                VStack(spacing: 0) {
                    GameTopBar(
                        title: "Maicito pide",
                        progress: "\(roundIndex + 1)/\(rounds.count)",
                        titleIcon: MaicitoGameType.maicitoPide.icon,
                        onClose: {
                            phrasePlayer.stop()
                            onExit()
                        }
                    )

                    Spacer()

                    // Globo de Maicito
                    VStack(spacing: 12) {
                        Text(solved ? "¡Eso es! ¡Gracias!" : "¿Sabes qué te estoy diciendo?")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundStyle(GameTheme.darkText)
                            .multilineTextAlignment(.center)

                        GameListenButton(isPlaying: phrasePlayer.isPlaying) {
                            phrasePlayer.play(round.target.audio, volume: appState.soundVolume)
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 16)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 22))
                    .shadow(color: GameTheme.purple.opacity(0.18), radius: 6, y: 3)
                    .padding(.horizontal, 28)

                    Spacer()
                        .frame(height: 44)

                    // Maicito parado en el piso de la plaza con su sombra
                    ZStack(alignment: .bottom) {
                        Ellipse()
                            .fill(Color.black.opacity(0.18))
                            .frame(width: 120, height: 22)
                            .offset(y: 6)

                        PetView(size: 190)
                            .scaleEffect(x: 1.0, y: breathScale, anchor: .bottom)
                            .rotationEffect(.degrees(maicitoWobble), anchor: .bottom)
                            .offset(y: maicitoJumpY)
                    }
                    .padding(.bottom, 18)

                    // Panel inferior de opciones
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Elige lo que significa")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundStyle(GameTheme.darkText)

                        ForEach(round.options) { option in
                            optionRow(option: option, target: round.target)
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 18)
                    .padding(.bottom, 24)
                    .background(
                        GameTheme.backgroundLight
                            .clipShape(
                                UnevenRoundedRectangle(
                                    topLeadingRadius: 26,
                                    topTrailingRadius: 26
                                )
                            )
                            .ignoresSafeArea(edges: .bottom)
                    )
                }
                .offset(x: screenShake)
            }

            if showConfetti {
                GameConfettiOverlay()
            }

            if finished {
                GameResultOverlay(
                    title: "¡Bien jugado!",
                    message: "Adivinaste \(firstTryCorrect) de \(rounds.count) al primer intento.",
                    stars: GameTheme.starsFor(correct: firstTryCorrect, total: rounds.count),
                    onPlayAgain: {
                        phrasePlayer.stop()
                        onPlayAgain()
                    },
                    onExit: {
                        phrasePlayer.stop()
                        onExit()
                    }
                )
            }
        }
        .background {
            GeometryReader { geo in
                Image("fondoPF_04")
                    .resizable()
                    .scaledToFill()
                    .frame(width: geo.size.width, height: geo.size.height)
                    .clipped()
            }
            .ignoresSafeArea()
        }
        .onAppear {
            let unlockedWords = YuhmuVocabulary.unlocked(
                lessonsCompleted: max(1, appState.pathLessonsCompleted)
            )
            rounds = Self.buildRounds(words: unlockedWords)
            withAnimation(.easeInOut(duration: 1.6).repeatForever(autoreverses: true)) {
                breathScale = 1.05
            }
            scheduleAutoPlay()
        }
        .onChange(of: roundIndex) { _, _ in
            wrongPicks = []
            solved = false
            scheduleAutoPlay()
        }
        .onDisappear {
            phrasePlayer.stop()
        }
    }

    private func optionRow(option: YuhmuWord, target: YuhmuWord) -> some View {
        let isWrong = wrongPicks.contains(option.id)
        let isRight = solved && option == target

        let borderColor: Color = {
            if isRight { return GameTheme.successGreen }
            if isWrong { return GameTheme.errorRed }
            return GameTheme.lockedNode
        }()

        let fillColor: Color = {
            if isRight { return Color(red: 0.94, green: 0.99, blue: 0.95) }
            if isWrong { return Color(red: 1.0, green: 0.95, blue: 0.95) }
            return Color.white
        }()

        return Button {
            pick(option)
        } label: {
            HStack(spacing: 13) {
                ZStack {
                    Circle()
                        .fill(GameTheme.purple.opacity(0.10))
                        .frame(width: 40, height: 40)
                    ZentemiIconView(name: option.icon, size: 32)
                }

                Text(option.spanish)
                    .font(.system(size: 17, weight: .bold))
                    .foregroundStyle(GameTheme.darkText)

                Spacer()

                if isRight {
                    Image(systemName: "checkmark")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(GameTheme.successGreen)
                }
                if isWrong {
                    Image(systemName: "xmark")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(GameTheme.errorRed)
                }
            }
            .padding(.horizontal, 14)
            .frame(maxWidth: .infinity)
            .frame(height: 64)
            .background(fillColor)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(borderColor, lineWidth: 2)
            )
            .shadow(color: .black.opacity(0.04), radius: 2, y: 1)
        }
        .buttonStyle(.plain)
        .disabled(solved || isWrong)
    }

    private func pick(_ word: YuhmuWord) {
        guard !rounds.isEmpty else { return }
        let target = rounds[roundIndex].target
        guard !solved, !wrongPicks.contains(word.id) else { return }

        if word == target {
            solved = true
            if wrongPicks.isEmpty {
                firstTryCorrect += 1
            }
            SoundManager.shared.play("audioBien", volume: Float(appState.soundVolume))
            triggerConfetti()

            withAnimation(.easeOut(duration: 0.18)) {
                maicitoJumpY = -40
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.18) {
                withAnimation(.spring(response: 0.35, dampingFraction: 0.45)) {
                    maicitoJumpY = 0
                }
            }

            DispatchQueue.main.asyncAfter(deadline: .now() + 1.3) {
                phrasePlayer.stop()
                if roundIndex < rounds.count - 1 {
                    roundIndex += 1
                } else {
                    finished = true
                }
            }
        } else {
            wrongPicks.insert(word.id)
            SoundManager.shared.play("audioMalaSeleccion", volume: Float(appState.soundVolume))
            triggerShakeAndWobble()
        }
    }

    private func scheduleAutoPlay() {
        let currentIndex = roundIndex
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.45) {
            guard !finished, currentIndex == roundIndex, rounds.indices.contains(currentIndex) else { return }
            phrasePlayer.play(rounds[currentIndex].target.audio, volume: appState.soundVolume)
        }
    }

    private func triggerConfetti() {
        showConfetti = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.6) {
            showConfetti = false
        }
    }

    private func triggerShakeAndWobble() {
        let offsets: [CGFloat] = [-7, 7, -5, 5, 0]
        for (i, val) in offsets.enumerated() {
            DispatchQueue.main.asyncAfter(deadline: .now() + Double(i) * 0.06) {
                withAnimation(.linear(duration: 0.06)) {
                    screenShake = val
                }
            }
        }

        let angles: [Double] = [-12, 10, -6, 0]
        for (i, angle) in angles.enumerated() {
            DispatchQueue.main.asyncAfter(deadline: .now() + Double(i) * 0.09) {
                withAnimation(.easeInOut(duration: 0.09)) {
                    maicitoWobble = angle
                }
            }
        }
    }

    private static func buildRounds(words: [YuhmuWord]) -> [PideRound] {
        guard !words.isEmpty else { return [] }
        var built: [PideRound] = []
        var previous: YuhmuWord? = nil

        for _ in 0..<totalRounds {
            let candidates = words.filter { $0 != previous }
            let target = (candidates.isEmpty ? words : candidates).randomElement() ?? words[0]
            let others = Array(words.filter { $0 != target }.shuffled().prefix(optionsPerRound - 1))
            let options = (others + [target]).shuffled()
            built.append(PideRound(target: target, options: options))
            previous = target
        }
        return built
    }
}

#Preview {
    MaicitoPideGameView()
        .environmentObject(AppState())
}
