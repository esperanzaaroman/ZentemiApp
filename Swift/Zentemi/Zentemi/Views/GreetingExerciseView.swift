import SwiftUI
import AVFoundation

struct GreetingExerciseView: View {

    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject var appState: AppState

    let exerciseNumber: Int
    let totalExercises: Int
    let onLessonCompleted: () -> Void

    @State private var selectedAnswer: Int? = nil
    @State private var verifiedAnswer: Int? = nil
    @State private var audioPlayer: AVAudioPlayer?

    @State private var isAudioPlaying = false
    @State private var showConfetti = false
    @State private var errorShake: CGFloat = 0
    @State private var showLessonComplete = false
    @State private var exerciseStartDate = Date()

    private let correctAnswer = 2

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

    private let answers = [
        "Nzengwa",
        "Hä nzengwa",
        "Ra hyadi",
        "Nänä"
    ]

    var body: some View {

        ZStack {

            backgroundColor
                .ignoresSafeArea()

            VStack(spacing: 0) {

                // MARK: - ENCABEZADO

                header

                ScrollView(
                    showsIndicators: false
                ) {

                    VStack(
                        alignment: .leading,
                        spacing: 18
                    ) {

                        // MARK: - PREGUNTA

                        Text("¿Cómo se dice «hola»?")
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

                            playGreetingAudio()

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
                                        systemName:
                                            "speaker.wave.2.fill"
                                    )
                                    .font(
                                        .system(size: 15)
                                    )
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
                                color:
                                    .black.opacity(0.05),
                                radius: 7,
                                y: 3
                            )
                        }
                        .buttonStyle(.plain)

                        // MARK: - RESPUESTAS

                        VStack(spacing: 12) {

                            ForEach(
                                answers.indices,
                                id: \.self
                            ) { index in

                                answerCard(
                                    number: index + 1,
                                    text: answers[index]
                                )
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 18)
                    .padding(.bottom, 120)
                }
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

                ConfettiView()
                    .allowsHitTesting(false)
                    .ignoresSafeArea()
                    .zIndex(100)
            }
        }
        .offset(x: errorShake)
        .fullScreenCover(
            isPresented: $showLessonComplete
        ) {

            LessonCompleteView(
                correctAnswers:
                    verifiedAnswer == correctAnswer
                    ? 1
                    : 0,
                totalAnswers: 1,
                elapsedSeconds:
                    max(
                        1,
                        Int(
                            Date()
                                .timeIntervalSince(
                                    exerciseStartDate
                                )
                        )
                    )
            ) {
                onLessonCompleted()
            }
            .environmentObject(appState)
        }
        }

    // MARK: - ENCABEZADO

    private var header: some View {

        HStack(spacing: 12) {

            Button {

                dismiss()

            } label: {

                Image(
                    systemName: "xmark"
                )
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

            // MARK: Progreso

            GeometryReader { geometry in

                ZStack(
                    alignment: .leading
                ) {

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
                                / CGFloat(totalExercises)
                        )
                }
            }
            .frame(height: 8)

            Text("\(exerciseNumber) / \(totalExercises)")
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

    // MARK: - RESPUESTA

    private func answerCard(
        number: Int,
        text: String
    ) -> some View {

        let isSelected =
            selectedAnswer == number

        let wasVerified =
            verifiedAnswer != nil

        let isCorrect =
            number == correctAnswer

        let isWrongSelection =
            wasVerified
            && isSelected
            && !isCorrect

        return Button {

            guard verifiedAnswer == nil else {
                return
            }
            SoundManager.shared.play(
                "audioSeleccion",
                volume: Float(appState.soundVolume)
            )

            withAnimation(
                .easeInOut(duration: 0.15)
            ) {
                selectedAnswer = number
            }

        } label: {

            HStack(spacing: 13) {

                ZStack {

                    RoundedRectangle(
                        cornerRadius: 8
                    )
                    .fill(
                        isSelected
                        ? purple
                        : Color(
                            red: 0.95,
                            green: 0.94,
                            blue: 0.98
                        )
                    )
                    .frame(
                        width: 30,
                        height: 30
                    )

                    Text(
                        answerLetter(number)
                    )
                    .font(
                        .system(
                            size: 12,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(
                        isSelected
                        ? .white
                        : Color.gray.opacity(0.55)
                    )
                }

                Text(text)
                    .font(
                        .system(
                            size: 15,
                            weight: .medium
                        )
                    )
                    .foregroundStyle(
                        isSelected
                        ? purple
                        : Color(
                            red: 0.29,
                            green: 0.27,
                            blue: 0.32
                        )
                    )

                Spacer()

                if wasVerified && isCorrect {

                    Image(
                        systemName:
                            "checkmark"
                    )
                    .font(
                        .system(
                            size: 15,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(turquoise)

                } else if isWrongSelection {

                    Image(
                        systemName:
                            "xmark"
                    )
                    .font(
                        .system(
                            size: 14,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(.red)
                }
            }
            .padding(.horizontal, 14)
            .frame(height: 78)
            .background(.white)
            .overlay {

                RoundedRectangle(
                    cornerRadius: 13
                )
                .stroke(
                    isWrongSelection
                    ? Color.red.opacity(0.70)
                    : isSelected
                        ? purple
                        : Color.clear,
                    lineWidth:
                        isSelected
                        ? 1.5
                        : 0
                )
            }
            .clipShape(
                RoundedRectangle(
                    cornerRadius: 13
                )
            )
            .shadow(
                color:
                    .black.opacity(0.035),
                radius: 6,
                y: 3
            )
        }
        .buttonStyle(.plain)
    }

    // MARK: - BOTÓN VERIFICAR

    private var verifyButton: some View {

        Button {

            guard let selectedAnswer else {
                return
            }

            if verifiedAnswer == nil {

                withAnimation(
                    .easeInOut(duration: 0.2)
                ) {
                    verifiedAnswer =
                        selectedAnswer
                }

                if selectedAnswer == correctAnswer {

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

            } else {

                if exerciseNumber >= totalExercises {
                    showLessonComplete = true
                }
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
            .frame(
                maxWidth: .infinity
            )
            .frame(height: 54)
            .background(
                selectedAnswer == nil
                ? purple.opacity(0.35)
                : purple
            )
            .clipShape(
                RoundedRectangle(
                    cornerRadius: 14
                )
            )
            .shadow(
                color:
                    selectedAnswer == nil
                    ? .clear
                    : purple.opacity(0.25),
                radius: 8,
                y: 5
            )
        }
        .disabled(
            selectedAnswer == nil
        )
    }

    // MARK: - LETRA DE RESPUESTA

    private func answerLetter(
        _ number: Int
    ) -> String {

        switch number {

        case 1:
            return "A"

        case 2:
            return "B"

        case 3:
            return "C"

        case 4:
            return "D"

        default:
            return ""
        }
    }

    // MARK: - AUDIO

    private func playGreetingAudio() {

        guard let url = Bundle.main.url(
            forResource: "audiovoz00",
            withExtension: "mp3"
        ) else {
            print("No se encontró el audio.")
            return
        }

        do {

            audioPlayer = try AVAudioPlayer(
                contentsOf: url
            )

            audioPlayer?.prepareToPlay()
            audioPlayer?.play()

            isAudioPlaying = true

            let duration =
                audioPlayer?.duration ?? 0

            DispatchQueue.main.asyncAfter(
                deadline: .now() + duration
            ) {
                isAudioPlaying = false
            }

        } catch {

            print(
                "Error al reproducir audio: \(error)"
            )
        }
    }
}


// MARK: - CONFETI

private struct ConfettiView: View {

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

    GreetingExerciseView(
        exerciseNumber: 1,
        totalExercises: 1
    ) {
        print("Lección terminada")
    }
    .environmentObject(
        AppState()
    )
}
