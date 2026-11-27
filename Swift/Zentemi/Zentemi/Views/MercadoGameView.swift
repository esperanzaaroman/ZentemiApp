import SwiftUI

/// "El mercado de Ixtenco": Maicito visita el tianguis y platica con el vendedor (el ajolote).
/// Cada paso es entender o decir una frase en Yuhmu.
/// Espejo exacto de `MercadoGame.kt` en Android.
struct MercadoGameView: View {

    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) private var dismiss

    @State private var gameId = 0

    var body: some View {
        MercadoSessionView(
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

private struct MarketStep {
    let vendorLine: String
    let prompt: String
    let answer: YuhmuWord
    let speakWithAudio: Bool
    let successLine: String
}

private struct MercadoSessionView: View {

    @EnvironmentObject var appState: AppState
    @StateObject private var phrasePlayer = PhrasePlayer()

    let onPlayAgain: () -> Void
    let onExit: () -> Void

    @State private var story: [MarketStep] = []
    @State private var stepOptionsList: [[YuhmuWord]] = []
    @State private var stepIndex = 0
    @State private var firstTryCorrect = 0
    @State private var selected: YuhmuWord? = nil
    @State private var wrongPicks: Set<String> = []
    @State private var solved = false
    @State private var showConfetti = false
    @State private var finished = false

    @State private var screenShake: CGFloat = 0
    @State private var breath: CGFloat = 0

    var body: some View {
        ZStack {
            if !story.isEmpty, stepOptionsList.indices.contains(stepIndex) {
                let step = story[stepIndex]
                let stepOptions = stepOptionsList[stepIndex]

                VStack(spacing: 0) {
                    GameTopBar(
                        title: "El mercado de Ixtenco",
                        progress: "\(stepIndex + 1)/\(story.count)",
                        onClose: {
                            phrasePlayer.stop()
                            onExit()
                        }
                    )

                    // Globo del vendedor
                    HStack(spacing: 10) {
                        Text(solved ? step.successLine : step.vendorLine)
                            .font(.system(size: 17, weight: .bold))
                            .foregroundStyle(GameTheme.darkText)
                            .frame(maxWidth: .infinity, alignment: .leading)

                        if !step.speakWithAudio && !solved {
                            Button {
                                phrasePlayer.play(step.answer.audio, volume: appState.soundVolume)
                            } label: {
                                ZStack {
                                    Circle()
                                        .fill(GameTheme.purple.opacity(0.10))
                                        .frame(width: 44, height: 44)
                                    ZentemiIconView(name: "ic_escuchar_audio", size: 34)
                                }
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(16)
                    .background(Color.white.opacity(0.96))
                    .clipShape(RoundedRectangle(cornerRadius: 22))
                    .shadow(color: .black.opacity(0.12), radius: 6, y: 3)
                    .padding(.leading, 52)
                    .padding(.trailing, 20)
                    .padding(.top, 6)
                    .animation(.easeInOut(duration: 0.25), value: solved)
                    .animation(.easeInOut(duration: 0.25), value: stepIndex)

                    Spacer(minLength: 8)

                    // Maicito y el vendedor (ajolote)
                    HStack(alignment: .bottom, spacing: 8) {
                        PetView(size: 130)
                            .scaleEffect(x: 1.0, y: 0.985 + 0.06 * breath, anchor: .bottom)
                            .frame(maxWidth: .infinity)

                        Image("personaje00")
                            .resizable()
                            .scaledToFit()
                            .frame(maxHeight: 170)
                            .scaleEffect(x: 1.0, y: 1.045 - 0.06 * breath, anchor: .bottom)
                            .frame(maxWidth: .infinity)
                    }
                    .padding(.horizontal, 16)

                    // Panel inferior de respuesta
                    VStack(alignment: .leading, spacing: 10) {
                        Text(step.prompt)
                            .font(.system(size: 17, weight: .bold))
                            .foregroundStyle(GameTheme.darkText)
                            .fixedSize(horizontal: false, vertical: true)

                        ForEach(Array(stepOptions.enumerated()), id: \.element.id) { index, option in
                            marketOptionRow(
                                index: index,
                                option: option,
                                step: step
                            )
                        }

                        if solved {
                            GameChunkyButton(
                                text: stepIndex < story.count - 1 ? "Continuar" : "Terminar",
                                color: GameTheme.successGreen,
                                lipColor: GameTheme.successGreenDark,
                                height: 50
                            ) {
                                nextStep()
                            }
                            .padding(.top, 2)
                        } else if step.speakWithAudio {
                            GameChunkyButton(
                                text: "Responder",
                                enabled: selected != nil,
                                height: 50
                            ) {
                                if let selected {
                                    submitAnswer(selected)
                                }
                            }
                            .padding(.top, 2)
                        } else {
                            Text("Escucha al vendedor y toca lo que significa.")
                                .font(.system(size: 13))
                                .foregroundStyle(.gray)
                                .frame(maxWidth: .infinity, alignment: .center)
                                .padding(.top, 2)
                        }
                    }
                    .padding(20)
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
                    title: "¡Visita completada!",
                    message: "Platicaste con el vendedor y respondiste \(firstTryCorrect) de \(story.count) al primer intento.",
                    stars: GameTheme.starsFor(correct: firstTryCorrect, total: story.count),
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
                Image("fondoPF_02")
                    .resizable()
                    .scaledToFill()
                    .frame(width: geo.size.width, height: geo.size.height)
                    .clipped()
            }
            .ignoresSafeArea()
        }
        .onAppear {
            let builtStory = Self.buildStory(userName: appState.userName)
            story = builtStory
            let vocab = YuhmuVocabulary.all
            stepOptionsList = builtStory.map { step in
                let distractors = Array(vocab.filter { $0 != step.answer }.shuffled().prefix(2))
                return (distractors + [step.answer]).shuffled()
            }
            withAnimation(.easeInOut(duration: 1.6).repeatForever(autoreverses: true)) {
                breath = 1.0
            }
            scheduleVendorVoiceIfNeeded()
        }
        .onChange(of: stepIndex) { _, _ in
            selected = nil
            wrongPicks = []
            solved = false
            scheduleVendorVoiceIfNeeded()
        }
        .onDisappear {
            phrasePlayer.stop()
        }
    }

    private func marketOptionRow(index: Int, option: YuhmuWord, step: MarketStep) -> some View {
        let isWrong = wrongPicks.contains(option.id)
        let isRight = solved && option == step.answer
        let isSelected = selected == option

        let borderColor: Color = {
            if isRight { return GameTheme.successGreen }
            if isWrong { return GameTheme.errorRed }
            if isSelected { return GameTheme.purple }
            return GameTheme.lockedNode
        }()

        let fillColor: Color = {
            if isRight { return Color(red: 0.94, green: 0.99, blue: 0.95) }
            if isWrong { return Color(red: 1.0, green: 0.95, blue: 0.95) }
            if isSelected { return Color(red: 0.96, green: 0.94, blue: 0.99) }
            return Color.white
        }()

        let optionLabel: String = {
            if step.speakWithAudio {
                if isRight {
                    return "«\(option.yuhmu ?? option.spanish)»"
                }
                let letter = String(UnicodeScalar(65 + index) ?? "A")
                return "Audio \(letter)"
            } else {
                return option.spanish
            }
        }()

        return Button {
            if step.speakWithAudio {
                selected = option
                phrasePlayer.play(option.audio, volume: appState.soundVolume)
            } else {
                submitAnswer(option)
            }
        } label: {
            HStack(spacing: 12) {
                ZStack {
                    Circle()
                        .fill(GameTheme.purple.opacity(0.10))
                        .frame(width: 34, height: 34)
                    ZentemiIconView(
                        name: step.speakWithAudio ? "ic_escuchar_audio" : option.icon,
                        size: 26
                    )
                }

                Text(optionLabel)
                    .font(.system(size: 16, weight: .bold))
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
            .frame(height: 56)
            .background(fillColor)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(borderColor, lineWidth: 2)
            )
        }
        .buttonStyle(.plain)
        .disabled(solved || isWrong)
    }

    private func submitAnswer(_ choice: YuhmuWord) {
        guard story.indices.contains(stepIndex) else { return }
        let step = story[stepIndex]
        guard !solved, !wrongPicks.contains(choice.id) else { return }

        if choice == step.answer {
            solved = true
            if wrongPicks.isEmpty {
                firstTryCorrect += 1
            }
            SoundManager.shared.play("audioBien", volume: Float(appState.soundVolume))
            showConfetti = true
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.6) {
                showConfetti = false
            }
        } else {
            wrongPicks.insert(choice.id)
            selected = nil
            SoundManager.shared.play("audioMalaSeleccion", volume: Float(appState.soundVolume))
            triggerShake()
        }
    }

    private func nextStep() {
        phrasePlayer.stop()
        if stepIndex < story.count - 1 {
            stepIndex += 1
        } else {
            finished = true
        }
    }

    private func scheduleVendorVoiceIfNeeded() {
        let currentIdx = stepIndex
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            guard !finished, currentIdx == stepIndex, story.indices.contains(currentIdx) else { return }
            let step = story[currentIdx]
            if !step.speakWithAudio {
                phrasePlayer.play(step.answer.audio, volume: appState.soundVolume)
            }
        }
    }

    private func triggerShake() {
        let offsets: [CGFloat] = [-7, 7, -5, 5, 0]
        for (i, val) in offsets.enumerated() {
            DispatchQueue.main.asyncAfter(deadline: .now() + Double(i) * 0.06) {
                withAnimation(.linear(duration: 0.06)) {
                    screenShake = val
                }
            }
        }
    }

    private static func buildStory(userName: String) -> [MarketStep] {
        [
            MarketStep(
                vendorLine: "¡Bienvenidos al mercado de Ixtenco!",
                prompt: "Saluda al vendedor. ¿Cuál audio dice «Buenos días»?",
                answer: YuhmuVocabulary.byId("buenos_dias"),
                speakWithAudio: true,
                successLine: "¡Té xki hats’i! ¡Qué gusto verlos por aquí!"
            ),
            MarketStep(
                vendorLine: "El vendedor te pregunta algo en Yuhmu…",
                prompt: "¿Qué te preguntó?",
                answer: YuhmuVocabulary.byId("como_te_llamas"),
                speakWithAudio: false,
                successLine: "¡Mucho gusto, \(userName)!"
            ),
            MarketStep(
                vendorLine: "Ahora te pregunta otra cosa…",
                prompt: "¿Qué te preguntó ahora?",
                answer: YuhmuVocabulary.byId("cuantos_anios"),
                speakWithAudio: false,
                successLine: "¡Ya eres todo un experto del mercado!"
            ),
            MarketStep(
                vendorLine: "¡Oh no! Se te cayó la canasta con las tortillas.",
                prompt: "Pide ayuda al vendedor. ¿Cuál audio dice «Ayúdame por favor»?",
                answer: YuhmuVocabulary.byId("ayudame"),
                speakWithAudio: true,
                successLine: "¡Claro que te ayudo! Y por ser tan amable, aquí tienes un tamal para Maicito."
            )
        ]
    }
}

#Preview {
    MercadoGameView()
        .environmentObject(AppState())
}
