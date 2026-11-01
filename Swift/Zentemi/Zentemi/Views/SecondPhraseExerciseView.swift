
import SwiftUI
import AVFoundation

struct SecondPhraseExerciseView: View {

    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject var appState: AppState

    let exerciseNumber: Int
    let totalExercises: Int
    let previousCorrectAnswers: Int
    let lessonStartDate: Date
    let onLessonCompleted: () -> Void

    @State private var writtenAnswer = ""
    @State private var verifiedAnswer: Bool? = nil
    @State private var audioPlayer: AVAudioPlayer?
    @State private var isAudioPlaying = false
    @State private var showConfetti = false
    @State private var errorShake: CGFloat = 0

    // MARK: - NAVEGACIÓN A LA ESCENA 3

    @State private var showThirdFlow = false
    @State private var thirdSceneCompleted = false

    @FocusState private var isAnswerFocused: Bool

    private let purple = Color(
        red: 0.48,
        green: 0.29,
        blue: 0.79
    )

    private let turquoise = Color(
        red: 0.16,
        green: 0.78,
        blue: 0.70
    )

    private let backgroundColor = Color(
        red: 0.97,
        green: 0.95,
        blue: 0.99
    )

    private var isCorrectAnswer: Bool {
        normalize(writtenAnswer) == "como te llamas"
    }

    var body: some View {

        ZStack {

            backgroundColor
                .ignoresSafeArea()

            VStack(spacing: 0) {

                // MARK: - ENCABEZADO

                header

                ScrollView(showsIndicators: false) {

                    VStack(
                        alignment: .leading,
                        spacing: 20
                    ) {

                        // MARK: - PREGUNTA

                        Text("Escucha la frase en Yuhmu y escribe su significado en español.")
                            .font(
                                .system(
                                    size: 23,
                                    weight: .bold
                                )
                            )
                            .foregroundStyle(
                                Color(
                                    red: 0.23,
                                    green: 0.21,
                                    blue: 0.28
                                )
                            )

                        // MARK: - AUDIO

                        Button {
                            playPhraseAudio()
                        } label: {

                            HStack(spacing: 10) {

                                ZStack {

                                    RoundedRectangle(
                                        cornerRadius: 10
                                    )
                                    .fill(
                                        isAudioPlaying
                                        ? .white.opacity(0.18)
                                        : purple
                                    )
                                    .frame(
                                        width: 38,
                                        height: 38
                                    )

                                    Image(
                                        systemName: "speaker.wave.2.fill"
                                    )
                                    .font(.system(size: 15))
                                    .foregroundStyle(.white)
                                }

                                Text(
                                    isAudioPlaying
                                    ? "Reproduciendo..."
                                    : "Escuchar audio"
                                )
                                .font(
                                    .system(
                                        size: 14,
                                        weight: .medium
                                    )
                                )
                                .foregroundStyle(
                                    isAudioPlaying
                                    ? .white
                                    : purple
                                )
                            }
                            .padding(.trailing, 14)
                            .padding(.vertical, 6)
                            .padding(.leading, 6)
                            .background(
                                isAudioPlaying
                                ? purple
                                : .white
                            )
                            .clipShape(
                                RoundedRectangle(
                                    cornerRadius: 13
                                )
                            )
                            .shadow(
                                color: .black.opacity(0.05),
                                radius: 7,
                                y: 3
                            )
                        }
                        .buttonStyle(.plain)

                        // MARK: - RESPUESTA ESCRITA

                        VStack(
                            alignment: .leading,
                            spacing: 12
                        ) {

                            Text("Escribe la traducción")
                                .font(
                                    .system(
                                        size: 15,
                                        weight: .semibold
                                    )
                                )
                                .foregroundStyle(.secondary)

                            TextField(
                                "Escribe tu respuesta aquí",
                                text: $writtenAnswer
                            )
                            .font(.system(size: 17))
                            .textInputAutocapitalization(.sentences)
                            .autocorrectionDisabled()
                            .submitLabel(.done)
                            .focused($isAnswerFocused)
                            .disabled(verifiedAnswer != nil)
                            .padding(17)
                            .background(.white)
                            .clipShape(
                                RoundedRectangle(
                                    cornerRadius: 14
                                )
                            )
                            .overlay {

                                RoundedRectangle(
                                    cornerRadius: 14
                                )
                                .stroke(
                                    verifiedAnswer == nil
                                    ? purple.opacity(0.25)
                                    : verifiedAnswer == true
                                        ? turquoise
                                        : Color.red,
                                    lineWidth: 1.5
                                )
                            }

                            if let verifiedAnswer {

                                HStack(spacing: 8) {

                                    Image(
                                        systemName: verifiedAnswer
                                        ? "checkmark.circle.fill"
                                        : "xmark.circle.fill"
                                    )

                                    Text(
                                        verifiedAnswer
                                        ? "¡Respuesta correcta!"
                                        : "Respuesta incorrecta"
                                    )
                                }
                                .font(
                                    .system(
                                        size: 15,
                                        weight: .semibold
                                    )
                                )
                                .foregroundStyle(
                                    verifiedAnswer
                                    ? turquoise
                                    : .red
                                )

                                if !verifiedAnswer {

                                    Text(
                                        "Respuesta correcta: ¿Cómo te llamas?"
                                    )
                                    .font(.system(size: 14))
                                    .foregroundStyle(.secondary)
                                }
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 18)
                    .padding(.bottom, 120)
                }
                .scrollDismissesKeyboard(.interactively)
            }

            // MARK: - BOTÓN INFERIOR

            VStack {

                Spacer()

                verifyButton
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 18)

            // MARK: - CONFETI

            if showConfetti {

                SecondPhraseConfettiView()
                    .allowsHitTesting(false)
                    .ignoresSafeArea()
                    .zIndex(100)
            }
        }
        .offset(x: errorShake)
        .onDisappear {
            audioPlayer?.stop()
        }

        // MARK: - ESCENA 3 Y EJERCICIO 3

        .fullScreenCover(
            isPresented: $showThirdFlow
        ) {

            Group {

                if thirdSceneCompleted {

                    ThirdPhraseExerciseView(
                        exerciseNumber: 3,
                        totalExercises: totalExercises,
                        previousCorrectAnswers:
                            previousCorrectAnswers
                            + (verifiedAnswer == true ? 1 : 0),
                        lessonStartDate: lessonStartDate
                    ) {
                        onLessonCompleted()
                    }
                    .environmentObject(appState)

                } else {

                    ThirdPhraseSceneView {
                        thirdSceneCompleted = true
                    }
                    .environmentObject(appState)
                }
            }
        }
    }

    // MARK: - ENCABEZADO

    private var header: some View {

        HStack(spacing: 12) {

            Button {
                audioPlayer?.stop()
                dismiss()
            } label: {

                Image(systemName: "xmark")
                    .font(
                        .system(
                            size: 14,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(
                        Color.gray.opacity(0.65)
                    )
                    .frame(
                        width: 38,
                        height: 38
                    )
                    .background(.white)
                    .clipShape(
                        RoundedRectangle(
                            cornerRadius: 12
                        )
                    )
                    .shadow(
                        color: .black.opacity(0.05),
                        radius: 5,
                        y: 2
                    )
            }

            // MARK: - PROGRESO

            GeometryReader { geometry in

                ZStack(alignment: .leading) {

                    Capsule()
                        .fill(
                            Color.gray.opacity(0.10)
                        )

                    Capsule()
                        .fill(turquoise)
                        .frame(
                            width:
                                geometry.size.width
                                * CGFloat(exerciseNumber)
                                / CGFloat(max(totalExercises, 1))
                        )
                }
            }
            .frame(height: 8)

            Text(
                "\(exerciseNumber) / \(totalExercises)"
            )
            .font(
                .system(
                    size: 14,
                    weight: .bold
                )
            )
            .foregroundStyle(turquoise)
        }
        .padding(.horizontal, 20)
        .padding(.top, 14)
        .padding(.bottom, 8)
    }

    // MARK: - VERIFICAR

    private var verifyButton: some View {

        Button {

            if verifiedAnswer == nil {

                isAnswerFocused = false

                let correct = isCorrectAnswer

                withAnimation(
                    .easeInOut(duration: 0.2)
                ) {
                    verifiedAnswer = correct
                }

                if correct {

                    SoundManager.shared.play(
                        "audioBien",
                        volume: Float(appState.soundVolume)
                    )

                    showConfetti = true

                    DispatchQueue.main.asyncAfter(
                        deadline: .now() + 1.6
                    ) {
                        showConfetti = false
                    }

                } else {

                    SoundManager.shared.play(
                        "audioMalaSeleccion",
                        volume: Float(appState.soundVolume)
                    )

                    shakeScreen()
                }

            } else {

                // MARK: - CONTINUAR A LA ESCENA 3

                audioPlayer?.stop()
                thirdSceneCompleted = false
                showThirdFlow = true
            }

        } label: {

            Text(
                verifiedAnswer == nil
                ? "Verificar"
                : "Continuar"
            )
            .font(
                .system(
                    size: 16,
                    weight: .bold
                )
            )
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity)
            .frame(height: 54)
            .background(
                writtenAnswer.trimmingCharacters(
                    in: .whitespacesAndNewlines
                ).isEmpty
                ? purple.opacity(0.35)
                : purple
            )
            .clipShape(
                RoundedRectangle(
                    cornerRadius: 14
                )
            )
            .shadow(
                color: purple.opacity(0.25),
                radius: 8,
                y: 5
            )
        }
        .disabled(
            verifiedAnswer == nil
            && writtenAnswer.trimmingCharacters(
                in: .whitespacesAndNewlines
            ).isEmpty
        )
    }

    // MARK: - VALIDACIÓN DE RESPUESTA

    private func normalize(_ text: String) -> String {

        let lowercase = text
            .folding(
                options: [
                    .diacriticInsensitive,
                    .caseInsensitive
                ],
                locale: Locale(identifier: "es_MX")
            )
            .lowercased()

        let lettersAndSpaces = lowercase.map { character in
            character.isLetter || character.isWhitespace
                ? character
                : " "
        }

        return String(lettersAndSpaces)
            .split(whereSeparator: \.isWhitespace)
            .joined(separator: " ")
    }

    // MARK: - ANIMACIÓN DE ERROR

    private func shakeScreen() {

        withAnimation(.linear(duration: 0.06)) {
            errorShake = -7
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 0.06
        ) {
            withAnimation(.linear(duration: 0.06)) {
                errorShake = 7
            }
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 0.12
        ) {
            withAnimation(.linear(duration: 0.06)) {
                errorShake = -5
            }
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 0.18
        ) {
            withAnimation(.linear(duration: 0.06)) {
                errorShake = 5
            }
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 0.24
        ) {
            withAnimation(.linear(duration: 0.06)) {
                errorShake = 0
            }
        }
    }

    // MARK: - AUDIO

    private func playPhraseAudio() {

        guard let url = Bundle.main.url(
            forResource: "comoTeLlamas",
            withExtension: "m4a"
        ) else {
            print("No se encontró comoTeLlamas.m4a")
            return
        }

        do {

            audioPlayer?.stop()

            audioPlayer = try AVAudioPlayer(
                contentsOf: url
            )

            audioPlayer?.volume = Float(appState.soundVolume)
            audioPlayer?.prepareToPlay()
            audioPlayer?.play()

            isAudioPlaying = true

            let duration = audioPlayer?.duration ?? 0

            DispatchQueue.main.asyncAfter(
                deadline: .now() + duration
            ) {
                isAudioPlaying = false
            }

        } catch {
            print("Error al reproducir audio: \(error)")
        }
    }
}

// MARK: - CONFETI

private struct SecondPhraseConfettiView: View {

    @State private var animate = false

    private let symbols = [
        "circle.fill",
        "square.fill",
        "triangle.fill",
        "star.fill"
    ]

    private let colors: [Color] = [
        .purple,
        .yellow,
        .green,
        .pink,
        .cyan,
        .orange
    ]

    var body: some View {

        GeometryReader { geometry in

            ZStack {

                ForEach(0..<30, id: \.self) { index in

                    Image(
                        systemName:
                            symbols[index % symbols.count]
                    )
                    .font(
                        .system(
                            size: CGFloat(7 + (index % 6))
                        )
                    )
                    .foregroundStyle(
                        colors[index % colors.count]
                    )
                    .rotationEffect(
                        .degrees(
                            animate
                            ? Double(index * 45)
                            : 0
                        )
                    )
                    .position(
                        x:
                            CGFloat((index * 47) % 340) + 20,
                        y:
                            animate
                            ? -40
                            : geometry.size.height + 30
                    )
                    .animation(
                        .easeOut(
                            duration:
                                0.8
                                + Double(index % 5) * 0.12
                        )
                        .delay(
                            Double(index % 8) * 0.025
                        ),
                        value: animate
                    )
                }
            }
            .onAppear {
                animate = true
            }
        }
    }
}

// MARK: - PREVIEW

#Preview {

    SecondPhraseExerciseView(
        exerciseNumber: 2,
        totalExercises: 3,
        previousCorrectAnswers: 1,
        lessonStartDate: Date()
    ) {
        print("Lección terminada")
    }
    .environmentObject(AppState())
}
