
import SwiftUI
import AVFoundation

struct FourthPhraseExerciseView: View {

    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var appState: AppState

    let exerciseNumber: Int
    let totalExercises: Int
    let previousCorrectAnswers: Int
    let lessonStartDate: Date
    let onExerciseCompleted: (Int) -> Void

    @State private var audioPlayer: AVAudioPlayer?
    @State private var selectedAnswer: Int? = nil
    @State private var verifiedAnswer: Bool? = nil
    @State private var showConfetti = false
    @State private var errorShake: CGFloat = 0

    // MARK: - EJERCICIO 5

    @State private var showFifthExercise = false

    private let correctAnswerIndex = 2

    private let answers = [
        "Buenos días",
        "¿Cómo te llamas?",
        "Ayúdame por favor",
        "¿Cuántos años tienes?"
    ]

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

    var body: some View {

        ZStack {

            backgroundColor
                .ignoresSafeArea()

            VStack(spacing: 0) {

                header

                ScrollView(showsIndicators: false) {

                    VStack(spacing: 24) {

                        // MARK: - PREGUNTA

                        Text("¿Qué significa esta frase en Yuhmu?")
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
                            .frame(
                                maxWidth: .infinity,
                                alignment: .leading
                            )

                        // MARK: - AUDIO

                        VStack(spacing: 16) {

                            Image(
                                systemName: "waveform"
                            )
                            .font(.system(size: 38))
                            .foregroundStyle(purple)

                            Text("Escucha la frase")
                                .font(
                                    .system(
                                        size: 21,
                                        weight: .bold
                                    )
                                )
                                .foregroundStyle(purple)

                            Text(
                                "Selecciona su significado en español."
                            )
                            .font(.system(size: 14))
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)

                            Button {
                                playReferenceAudio()
                            } label: {

                                Label(
                                    "Escuchar audio",
                                    systemImage: "speaker.wave.2.fill"
                                )
                                .font(
                                    .system(
                                        size: 16,
                                        weight: .bold
                                    )
                                )
                                .foregroundStyle(.white)
                                .padding(.horizontal, 22)
                                .frame(height: 48)
                                .background(
                                    purple,
                                    in: Capsule()
                                )
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding(22)
                        .background(
                            .white,
                            in: RoundedRectangle(
                                cornerRadius: 20
                            )
                        )

                        // MARK: - OPCIONES

                        VStack(spacing: 12) {

                            ForEach(
                                answers.indices,
                                id: \.self
                            ) { index in

                                answerButton(
                                    index: index
                                )
                            }
                        }

                        // MARK: - RESULTADO

                        if let verifiedAnswer {

                            Label(
                                verifiedAnswer
                                    ? "¡Correcto! Has aprendido a pedir ayuda."
                                    : "Respuesta incorrecta. Inténtalo de nuevo.",
                                systemImage:
                                    verifiedAnswer
                                    ? "checkmark.circle.fill"
                                    : "xmark.circle.fill"
                            )
                            .font(
                                .system(
                                    size: 14,
                                    weight: .semibold
                                )
                            )
                            .foregroundStyle(
                                verifiedAnswer
                                    ? turquoise
                                    : .red
                            )
                            .multilineTextAlignment(.center)
                        }

                        // MARK: - CONSEJO DE MAICITO

                        HStack(spacing: 14) {

                            PetView(size: 75)

                            Text(
                                "¡Escucha con atención! Recuerda lo que aprendimos durante nuestra conversación."
                            )
                            .font(
                                .system(
                                    size: 14,
                                    weight: .medium
                                )
                            )
                            .foregroundStyle(
                                Color(
                                    red: 0.29,
                                    green: 0.27,
                                    blue: 0.32
                                )
                            )

                            Spacer(minLength: 0)
                        }
                        .padding(16)
                        .background(
                            .white,
                            in: RoundedRectangle(
                                cornerRadius: 18
                            )
                        )
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 18)
                    .padding(.bottom, 120)
                }
            }

            // MARK: - BOTÓN INFERIOR

            VStack {

                Spacer()

                Button {
                    verifyOrContinue()
                } label: {

                    Text(
                        verifiedAnswer == true
                            ? "Continuar"
                            : "Verificar"
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
                        selectedAnswer != nil
                            ? purple
                            : Color.gray.opacity(0.40),
                        in: RoundedRectangle(
                            cornerRadius: 14
                        )
                    )
                }
                .disabled(selectedAnswer == nil)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 18)

            if showConfetti {

                FourthPhraseConfettiView()
                    .allowsHitTesting(false)
                    .ignoresSafeArea()
            }
        }
        .offset(x: errorShake)

        // MARK: - NAVEGACIÓN AL EJERCICIO 5

        .fullScreenCover(
            isPresented: $showFifthExercise
        ) {

            FifthPhraseExerciseView(
                exerciseNumber: 5,
                totalExercises: totalExercises,
                previousCorrectAnswers:
                    previousCorrectAnswers + 1,
                lessonStartDate: lessonStartDate
            ) { correctAnswers in

                onExerciseCompleted(correctAnswers)
            }
            .environmentObject(appState)
        }

        .onDisappear {
            audioPlayer?.stop()
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
                    .frame(width: 38, height: 38)
                    .background(.white)
                    .clipShape(
                        RoundedRectangle(
                            cornerRadius: 12
                        )
                    )
            }

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

    // MARK: - OPCIÓN

    private func answerButton(
        index: Int
    ) -> some View {

        Button {

            guard verifiedAnswer != true else {
                return
            }

            SoundManager.shared.play(
                "audioSeleccion",
                volume: Float(appState.soundVolume)
            )

            selectedAnswer = index
            verifiedAnswer = nil

        } label: {

            HStack(spacing: 14) {

                Text(
                    String(
                        UnicodeScalar(65 + index)!
                    )
                )
                .font(
                    .system(
                        size: 16,
                        weight: .bold
                    )
                )
                .foregroundStyle(purple)
                .frame(width: 32, height: 32)
                .background(
                    purple.opacity(0.10),
                    in: Circle()
                )

                Text(answers[index])
                    .font(
                        .system(
                            size: 16,
                            weight: .semibold
                        )
                    )
                    .foregroundStyle(
                        Color(
                            red: 0.23,
                            green: 0.21,
                            blue: 0.28
                        )
                    )

                Spacer()

                if selectedAnswer == index {

                    Image(
                        systemName: "checkmark.circle.fill"
                    )
                    .foregroundStyle(
                        verifiedAnswer == true
                            ? turquoise
                            : purple
                    )
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity)
            .background(
                selectedAnswer == index
                    ? purple.opacity(0.10)
                    : Color.white,
                in: RoundedRectangle(
                    cornerRadius: 16
                )
            )
            .overlay {

                RoundedRectangle(
                    cornerRadius: 16
                )
                .stroke(
                    selectedAnswer == index
                        ? purple
                        : Color.clear,
                    lineWidth: 2
                )
            }
        }
        .disabled(verifiedAnswer == true)
    }

    // MARK: - VERIFICACIÓN

    private func verifyOrContinue() {

        if verifiedAnswer == true {

            audioPlayer?.stop()

            // Antes terminaba el ejercicio 4.
            // Ahora abre el repaso final.
            showFifthExercise = true

            return
        }

        guard let selectedAnswer else {
            return
        }

        if selectedAnswer == correctAnswerIndex {

            verifiedAnswer = true

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

            verifiedAnswer = false
            shakeScreen()
        }
    }

    // MARK: - AUDIO

    private func playReferenceAudio() {

        guard let url = Bundle.main.url(
            forResource: "ayudamePorfavor",
            withExtension: "m4a"
        ) else {
            print(
                "No se encontró ayudamePorfavor.m4a"
            )
            return
        }

        do {

            audioPlayer?.stop()

            audioPlayer = try AVAudioPlayer(
                contentsOf: url
            )

            audioPlayer?.volume = Float(
                appState.soundVolume
            )

            audioPlayer?.prepareToPlay()
            audioPlayer?.play()

        } catch {
            print(
                "Error al reproducir audio: \(error)"
            )
        }
    }

    // MARK: - ANIMACIÓN DE ERROR

    private func shakeScreen() {

        withAnimation(
            .linear(duration: 0.06)
        ) {
            errorShake = -7
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 0.06
        ) {
            withAnimation(
                .linear(duration: 0.06)
            ) {
                errorShake = 7
            }
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 0.12
        ) {
            withAnimation(
                .linear(duration: 0.06)
            ) {
                errorShake = -5
            }
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 0.18
        ) {
            withAnimation(
                .linear(duration: 0.06)
            ) {
                errorShake = 5
            }
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 0.24
        ) {
            withAnimation(
                .linear(duration: 0.06)
            ) {
                errorShake = 0
            }
        }
    }
}

// MARK: - CONFETI

private struct FourthPhraseConfettiView: View {

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

                ForEach(
                    0..<30,
                    id: \.self
                ) { index in

                    Image(
                        systemName:
                            symbols[
                                index % symbols.count
                            ]
                    )
                    .font(
                        .system(
                            size:
                                CGFloat(
                                    7 + (index % 6)
                                )
                        )
                    )
                    .foregroundStyle(
                        colors[
                            index % colors.count
                        ]
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
                            CGFloat(
                                (index * 47) % 340
                            ) + 20,
                        y:
                            animate
                                ? -40
                                : geometry.size.height + 30
                    )
                    .animation(
                        .easeOut(
                            duration:
                                0.8
                                + Double(
                                    index % 5
                                ) * 0.12
                        )
                        .delay(
                            Double(
                                index % 8
                            ) * 0.025
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
