
import SwiftUI
import AVFoundation

struct ThirdPhraseExerciseView: View {

    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var appState: AppState

    let exerciseNumber: Int
    let totalExercises: Int
    let previousCorrectAnswers: Int
    let lessonStartDate: Date
    let onLessonCompleted: () -> Void

    @State private var audioPlayer: AVAudioPlayer?
    @State private var recorder: AVAudioRecorder?

    @State private var isRecording = false
    @State private var hasRecordedVoice = false
    @State private var recordedVoiceDetected = false
    @State private var verifiedAnswer: Bool? = nil

    @State private var showSkipConfirmation = false
    @State private var showFourthFlow = false
    @State private var fourthSceneCompleted = false
    @State private var fourthExerciseCompleted = false
    @State private var finalCorrectAnswers = 0

    @State private var showConfetti = false
    @State private var errorShake: CGFloat = 0

    @State private var recordingURL: URL?
    @State private var recordingStartDate: Date?
    @State private var recordingHasSound = false
    @State private var recordingTimer: Timer?

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

                        Text("Pronuncia la frase en Yuhmu")
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

                        // MARK: - FRASE

                        VStack(spacing: 12) {

                            Text("¿Cuántos años tienes?")
                                .font(
                                    .system(
                                        size: 25,
                                        weight: .bold
                                    )
                                )
                                .multilineTextAlignment(.center)
                                .foregroundStyle(purple)

                            Text("Escucha la frase en Yuhmu e intenta repetirla.")
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

                        // MARK: - MICRÓFONO

                        VStack(spacing: 16) {

                            Button {
                                toggleRecording()
                            } label: {

                                Image(
                                    systemName:
                                        isRecording
                                        ? "stop.fill"
                                        : "mic.fill"
                                )
                                .font(.system(size: 36))
                                .foregroundStyle(.white)
                                .frame(
                                    width: 100,
                                    height: 100
                                )
                                .background(
                                    isRecording
                                    ? Color.red
                                    : purple,
                                    in: Circle()
                                )
                                .shadow(
                                    color:
                                        purple.opacity(0.20),
                                    radius: 10,
                                    y: 5
                                )
                            }
                            .disabled(verifiedAnswer != nil)

                            Text(
                                isRecording
                                ? "Grabando... toca para detener"
                                : hasRecordedVoice
                                    ? "Grabación lista"
                                    : "Toca el micrófono para hablar"
                            )
                            .font(
                                .system(
                                    size: 15,
                                    weight: .semibold
                                )
                            )
                            .foregroundStyle(
                                isRecording
                                ? .red
                                : purple
                            )

                            if let verifiedAnswer {

                                Label(
                                    verifiedAnswer
                                    ? "¡Muy bien! Has practicado la frase."
                                    : "No se detectó voz suficiente. Inténtalo de nuevo.",
                                    systemImage:
                                        verifiedAnswer
                                        ? "checkmark.circle.fill"
                                        : "xmark.circle.fill"
                                )
                                .font(.system(size: 14))
                                .foregroundStyle(
                                    verifiedAnswer
                                    ? turquoise
                                    : .red
                                )
                                .multilineTextAlignment(.center)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)

                        // MARK: - CONSEJO DE MAICITO

                        HStack(spacing: 14) {

                            PetView(size: 75)

                            Text("¡Habla fuerte y claro! Acerca un poco el micrófono para que pueda escucharte.")
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
                    .padding(.bottom, 160)
                }
            }

            // MARK: - BOTONES INFERIORES

            VStack(spacing: 10) {

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
                        hasRecordedVoice
                        ? purple
                        : Color.gray.opacity(0.40),
                        in: RoundedRectangle(
                            cornerRadius: 14
                        )
                    )
                }
                .disabled(
                    !hasRecordedVoice || isRecording
                )

                Button {
                    showSkipConfirmation = true
                } label: {

                    Text("Omitir")
                        .font(
                            .system(
                                size: 15,
                                weight: .semibold
                            )
                        )
                        .foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity)
                        .frame(height: 44)
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 18)

            if showConfetti {

                ThirdPhraseConfettiView()
                    .allowsHitTesting(false)
                    .ignoresSafeArea()
            }
        }
        .offset(x: errorShake)
        .confirmationDialog(
            "¿Seguro que quieres omitir?",
            isPresented: $showSkipConfirmation,
            titleVisibility: .visible
        ) {

            Button(
                "Sí, omitir ejercicio",
                role: .destructive
            ) {
                stopRecordingIfNeeded()
                fourthSceneCompleted = false
                fourthExerciseCompleted = false
                showFourthFlow = true
            }

            Button(
                "Cancelar",
                role: .cancel
            ) {}

        } message: {

            Text(
                "Si no cuentas con micrófono o no puedes hablar, puedes omitir este ejercicio. No contará como acierto."
            )
        }
        .fullScreenCover(
            isPresented: $showFourthFlow
        ) {

            Group {

                if fourthExerciseCompleted {

                    LessonCompleteView(
                        correctAnswers: finalCorrectAnswers,
                        totalAnswers: totalExercises,
                        elapsedSeconds: max(
                            1,
                            Int(
                                Date().timeIntervalSince(
                                    lessonStartDate
                                )
                            )
                        )
                    ) {
                        onLessonCompleted()
                    }
                    .environmentObject(appState)

                } else if fourthSceneCompleted {

                    FourthPhraseExerciseView(
                        exerciseNumber: 4,
                        totalExercises: totalExercises,
                        previousCorrectAnswers:
                            previousCorrectAnswers
                            + (verifiedAnswer == true ? 1 : 0),
                        lessonStartDate: lessonStartDate
                    ) { correctAnswers in

                        finalCorrectAnswers = correctAnswers
                        fourthExerciseCompleted = true
                    }
                    .environmentObject(appState)

                } else {

                    FourthPhraseSceneView {
                        fourthSceneCompleted = true
                    }
                    .environmentObject(appState)
                }
            }
        }
        .onDisappear {
            audioPlayer?.stop()
            stopRecordingIfNeeded()
        }
    }

    // MARK: - ENCABEZADO

    private var header: some View {

        HStack(spacing: 12) {

            Button {
                stopRecordingIfNeeded()
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

    // MARK: - VERIFICACIÓN

    private func verifyOrContinue() {

        if verifiedAnswer == true {
            fourthSceneCompleted = false
            fourthExerciseCompleted = false
            showFourthFlow = true
            return
        }

        if recordedVoiceDetected {

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

            shakeScreen()

            hasRecordedVoice = false
            verifiedAnswer = nil
        }
    }

    // MARK: - GRABACIÓN

    private func toggleRecording() {

        if isRecording {
            finishRecording()
        } else {
            requestMicrophoneAndRecord()
        }
    }

    private func requestMicrophoneAndRecord() {

        AVAudioApplication.requestRecordPermission { granted in

            DispatchQueue.main.async {

                if granted {
                    startRecording()
                } else {
                    showSkipConfirmation = true
                }
            }
        }
    }

    private func startRecording() {

        audioPlayer?.stop()

        let session = AVAudioSession.sharedInstance()

        do {

            try session.setCategory(
                .playAndRecord,
                mode: .default,
                options: [.defaultToSpeaker]
            )

            try session.setActive(true)

            let url = FileManager.default
                .temporaryDirectory
                .appendingPathComponent(
                    "thirdPhraseRecording.m4a"
                )

            let settings: [String: Any] = [
                AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
                AVSampleRateKey: 44100,
                AVNumberOfChannelsKey: 1,
                AVEncoderAudioQualityKey:
                    AVAudioQuality.high.rawValue
            ]

            recorder = try AVAudioRecorder(
                url: url,
                settings: settings
            )

            recorder?.isMeteringEnabled = true
            recorder?.record()

            recordingURL = url
            recordingStartDate = Date()
            recordingHasSound = false
            hasRecordedVoice = false
            recordedVoiceDetected = false

            isRecording = true

            recordingTimer?.invalidate()

            recordingTimer = Timer.scheduledTimer(
                withTimeInterval: 0.1,
                repeats: true
            ) { _ in

                recorder?.updateMeters()

                let power =
                    recorder?.averagePower(
                        forChannel: 0
                    ) ?? -160

                if power > -35 {
                    recordingHasSound = true
                }
            }

        } catch {
            print("Error al grabar: \(error)")
        }
    }

    private func finishRecording() {

        recordingTimer?.invalidate()
        recordingTimer = nil

        recorder?.stop()
        recorder = nil

        isRecording = false

        let duration = Date().timeIntervalSince(
            recordingStartDate ?? Date()
        )

        hasRecordedVoice = duration >= 0.5

        recordedVoiceDetected =
            duration >= 0.5 && recordingHasSound
    }

    private func stopRecordingIfNeeded() {

        recordingTimer?.invalidate()
        recordingTimer = nil

        recorder?.stop()
        recorder = nil

        isRecording = false
    }

    // MARK: - AUDIO DE REFERENCIA

    private func playReferenceAudio() {

        guard let url = Bundle.main.url(
            forResource: "cuantosAniosTienes",
            withExtension: "m4a"
        ) else {
            print("No se encontró cuantosAniosTienes.m4a")
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
            print("Error al reproducir audio: \(error)")
        }
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
}

// MARK: - CONFETI

private struct ThirdPhraseConfettiView: View {

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
