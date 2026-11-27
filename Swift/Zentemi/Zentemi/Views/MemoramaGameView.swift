import SwiftUI

/// Memorama: junta cada carta de audio en Yuhmu con la carta de su significado en español.
/// Al voltear una carta de audio se escucha la frase.
/// Espejo exacto de `MemoramaGame.kt` en Android.
struct MemoramaGameView: View {

    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) private var dismiss

    @State private var gameId = 0

    var body: some View {
        MemoramaSessionView(
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

private struct MemoCard: Identifiable {
    let id: Int
    let word: YuhmuWord
    let isAudio: Bool
}

private struct MemoramaSessionView: View {

    @EnvironmentObject var appState: AppState
    @StateObject private var phrasePlayer = PhrasePlayer()

    let onPlayAgain: () -> Void
    let onExit: () -> Void

    private static let maxPairs = 4

    @State private var deck: [MemoCard] = []
    @State private var faceUp: [Int] = []
    @State private var matched: Set<String> = []
    @State private var attempts = 0
    @State private var busy = false
    @State private var finished = false

    private var pairs: Int {
        max(1, deck.count / 2)
    }

    private var stars: Int {
        if attempts <= pairs + 2 { return 3 }
        if attempts <= pairs * 2 + 1 { return 2 }
        return 1
    }

    var body: some View {
        ZStack {
            GameTheme.backgroundLight
                .ignoresSafeArea()

            VStack(spacing: 0) {
                GameTopBar(
                    title: "Memorama",
                    progress: "\(matched.count)/\(pairs)",
                    onClose: {
                        phrasePlayer.stop()
                        onExit()
                    }
                )

                Text("Intentos: \(attempts)")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(GameTheme.purple)
                    .padding(.bottom, 6)

                // Tablero 2 x 4
                VStack(spacing: 12) {
                    let rows = stride(from: 0, to: deck.count, by: 2).map {
                        Array(deck[$0..<min($0 + 2, deck.count)])
                    }

                    ForEach(Array(rows.enumerated()), id: \.offset) { _, row in
                        HStack(spacing: 12) {
                            ForEach(row) { card in
                                MemoCardTile(
                                    card: card,
                                    isFaceUp: faceUp.contains(card.id) || matched.contains(card.word.id),
                                    isMatched: matched.contains(card.word.id)
                                ) {
                                    flip(card)
                                }
                            }
                        }
                        .frame(maxHeight: .infinity)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 6)
                .frame(maxHeight: .infinity)

                MaicitoTipCardView(
                    text: "Las cartas con bocina suenan en Yuhmu. ¡Júntalas con su significado!"
                )
                .padding(.horizontal, 20)
                .padding(.vertical, 14)
            }

            if finished {
                GameResultOverlay(
                    title: "¡Memorama completo!",
                    message: "Encontraste las \(pairs) parejas en \(attempts) intentos.",
                    stars: stars,
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
        .onAppear {
            let words = YuhmuVocabulary.unlocked(
                lessonsCompleted: max(1, appState.pathLessonsCompleted)
            )
            deck = Self.buildDeck(words: words)
        }
        .onDisappear {
            phrasePlayer.stop()
        }
    }

    private func flip(_ card: MemoCard) {
        guard !busy,
              !faceUp.contains(card.id),
              !matched.contains(card.word.id) else { return }

        if card.isAudio {
            phrasePlayer.play(card.word.audio, volume: appState.soundVolume)
        } else {
            SoundManager.shared.play("audioSeleccion", volume: Float(appState.soundVolume))
        }

        faceUp.append(card.id)
        guard faceUp.count == 2 else { return }

        attempts += 1
        guard let first = deck.first(where: { $0.id == faceUp[0] }),
              let second = deck.first(where: { $0.id == faceUp[1] }) else { return }

        busy = true

        if first.word == second.word {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.45) {
                matched.insert(first.word.id)
                SoundManager.shared.play("audioBien", volume: Float(appState.soundVolume))
                faceUp.removeAll()
                busy = false

                if matched.count == pairs {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.7) {
                        finished = true
                    }
                }
            }
        } else {
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.1) {
                faceUp.removeAll()
                busy = false
            }
        }
    }

    private static func buildDeck(words: [YuhmuWord]) -> [MemoCard] {
        let chosen = Array(words.shuffled().prefix(maxPairs))
        let rawCards = chosen.flatMap { word in
            [
                MemoCard(id: 0, word: word, isAudio: true),
                MemoCard(id: 0, word: word, isAudio: false)
            ]
        }.shuffled()

        return rawCards.enumerated().map { idx, card in
            MemoCard(id: idx, word: card.word, isAudio: card.isAudio)
        }
    }
}

private struct MemoCardTile: View {
    let card: MemoCard
    let isFaceUp: Bool
    let isMatched: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            ZStack {
                // Reverso morado con Maicito
                ZStack {
                    RoundedRectangle(cornerRadius: 18)
                        .fill(
                            LinearGradient(
                                colors: [GameTheme.purpleGradStart, GameTheme.purpleGradEnd],
                                startPoint: .top,
                                endPoint: .bottom
                            )
                        )

                    Image("mascota")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 54, height: 54)
                        .opacity(0.92)
                }
                .opacity(isFaceUp ? 0 : 1)

                // Frente (rotado 180° para verse derecho cuando la carta gira)
                VStack(spacing: 6) {
                    if card.isAudio {
                        ZentemiIconView(name: "ic_escuchar_audio", size: 40)
                        Text(card.word.yuhmu ?? "Yuhmu")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundStyle(GameTheme.purple)
                            .multilineTextAlignment(.center)
                    } else {
                        ZentemiIconView(name: card.word.icon, size: 40)
                        Text(card.word.spanish)
                            .font(.system(size: 14, weight: .bold))
                            .foregroundStyle(GameTheme.darkText)
                            .multilineTextAlignment(.center)
                    }

                    if isMatched {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 18))
                            .foregroundStyle(GameTheme.successGreen)
                    }
                }
                .padding(10)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(frontBackground)
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 18))
                .overlay(
                    RoundedRectangle(cornerRadius: 18)
                        .stroke(isMatched ? GameTheme.successGreen : Color.clear, lineWidth: 2)
                )
                .rotation3DEffect(.degrees(180), axis: (x: 0, y: 1, z: 0))
                .opacity(isFaceUp ? 1 : 0)
            }
            .shadow(color: .black.opacity(0.10), radius: 4, y: 2)
            .rotation3DEffect(
                .degrees(isFaceUp ? 180 : 0),
                axis: (x: 0, y: 1, z: 0),
                perspective: 0.5
            )
            .animation(.easeInOut(duration: 0.35), value: isFaceUp)
        }
        .buttonStyle(.plain)
        .disabled(isFaceUp)
    }

    private var frontBackground: Color {
        if isMatched { return GameTheme.successGreen.opacity(0.12) }
        if card.isAudio { return GameTheme.purple.opacity(0.10) }
        return Color.white
    }
}

#Preview {
    MemoramaGameView()
        .environmentObject(AppState())
}
