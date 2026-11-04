
import SwiftUI
import AVFoundation

struct FifthPhraseExerciseView: View {

    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var appState: AppState

    let exerciseNumber: Int
    let totalExercises: Int
    let previousCorrectAnswers: Int
    let lessonStartDate: Date
    let onExerciseCompleted: (Int) -> Void

    // MARK: - ESTADO DEL JUEGO

    @State private var joystickDirection: CGVector = .zero
    @State private var maicitoPosition = CGPoint(x: 0.5, y: 0.5)

    @State private var carriedPhrase: Int? = nil
    @State private var completedPairs: Set<Int> = []

    @State private var audioPlayer: AVAudioPlayer?
    @State private var lastUpdate = Date()

    @State private var showError = false
    @State private var showSuccess = false
    @State private var gameFinished = false

    @State private var feedbackTask: Task<Void, Never>?

    private let movementSpeed: CGFloat = 0.22
    private let interactionDistance: CGFloat = 0.085

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

    // MARK: - FRASES

    private struct PhrasePair: Identifiable {

        let id: Int
        let spanish: String
        let audio: String

        let yuhmuPosition: CGPoint
        let spanishPosition: CGPoint
    }

    private let phrases: [PhrasePair] = [

        PhrasePair(
            id: 0,
            spanish: "Buenos días",
            audio: "buenosDias",
            yuhmuPosition: CGPoint(x: 0.17, y: 0.24),
            spanishPosition: CGPoint(x: 0.79, y: 0.71)
        ),

        PhrasePair(
            id: 1,
            spanish: "¿Cómo te llamas?",
            audio: "comoTeLlamas",
            yuhmuPosition: CGPoint(x: 0.80, y: 0.23),
            spanishPosition: CGPoint(x: 0.22, y: 0.73)
        ),

        PhrasePair(
            id: 2,
            spanish: "¿Cuántos años tienes?",
            audio: "cuantosAniosTienes",
            yuhmuPosition: CGPoint(x: 0.19, y: 0.48),
            spanishPosition: CGPoint(x: 0.79, y: 0.45)
        ),

        PhrasePair(
            id: 3,
            spanish: "Ayúdame por favor",
            audio: "ayudamePorfavor",
            yuhmuPosition: CGPoint(x: 0.78, y: 0.58),
            spanishPosition: CGPoint(x: 0.22, y: 0.59)
        )
    ]

    var body: some View {

        GeometryReader { geometry in

            ZStack {

                // MARK: - FONDO

                Image("fondoPF_05")
                    .resizable()
                    .scaledToFill()
                    .frame(
                        width: geometry.size.width,
                        height: geometry.size.height
                    )
                    .clipped()
                    .ignoresSafeArea()

                // MARK: - JUEGO

                TimelineView(.animation) { timeline in

                    Canvas { _, _ in
                        // El movimiento se actualiza
                        // mediante el ciclo de animación.
                    }
                    .hidden()
                    .onChange(of: timeline.date) { _, newDate in
                        updateMovement(
                            date: newDate,
                            size: geometry.size
                        )
                    }
                }

                // MARK: - FRASES Y TRADUCCIONES

                ForEach(phrases) { phrase in

                    if !completedPairs.contains(phrase.id) {

                        // FRASE EN YUHmu

                        if carriedPhrase != phrase.id {

                            phraseLabel(
                                title: "Yuhmu \(phrase.id + 1)",
                                subtitle: "Recoger",
                                color: purple
                            )
                            .position(
                                x: phrase.yuhmuPosition.x
                                    * geometry.size.width,
                                y: phrase.yuhmuPosition.y
                                    * geometry.size.height
                            )
                        }

                        // TRADUCCIÓN

                        phraseLabel(
                            title: phrase.spanish,
                            subtitle: "Traducción",
                            color: turquoise
                        )
                        .position(
                            x: phrase.spanishPosition.x
                                * geometry.size.width,
                            y: phrase.spanishPosition.y
                                * geometry.size.height
                        )

                    } else {

                        // PAREJA COMPLETADA

                        phraseLabel(
                            title: phrase.spanish,
                            subtitle: "✓ Completado",
                            color: .green
                        )
                        .position(
                            x: phrase.spanishPosition.x
                                * geometry.size.width,
                            y: phrase.spanishPosition.y
                                * geometry.size.height
                        )
                    }
                }

                // MARK: - MAICITO

                VStack(spacing: 2) {

                    if let carriedPhrase {

                        Text("Yuhmu \(carriedPhrase + 1)")
                            .font(
                                .system(
                                    size: 11,
                                    weight: .bold
                                )
                            )
                            .foregroundStyle(.white)
                            .padding(.horizontal, 9)
                            .padding(.vertical, 5)
                            .background(
                                purple,
                                in: Capsule()
                            )
                    }

                    PetView(size: 65)
                }
                .position(
                    x: maicitoPosition.x
                        * geometry.size.width,
                    y: maicitoPosition.y
                        * geometry.size.height
                )

                // MARK: - ENCABEZADO

                VStack {

                    HStack(spacing: 12) {

                        Button {
                            audioPlayer?.stop()
                            dismiss()
                        } label: {

                            Image(systemName: "xmark")
                                .font(.headline)
                                .foregroundStyle(purple)
                                .frame(width: 40, height: 40)
                                .background(
                                    .white,
                                    in: Circle()
                                )
                        }

                        Spacer()

                        VStack(spacing: 3) {

                            Text("Repaso final")
                                .font(
                                    .system(
                                        size: 16,
                                        weight: .bold
                                    )
                                )

                            Text(
                                "\(completedPairs.count) / 4 parejas"
                            )
                            .font(
                                .system(
                                    size: 12,
                                    weight: .semibold
                                )
                            )
                        }
                        .foregroundStyle(purple)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 9)
                        .background(
                            .white.opacity(0.95),
                            in: Capsule()
                        )

                        Spacer()

                        Text(
                            "\(exerciseNumber)/\(totalExercises)"
                        )
                        .font(
                            .system(
                                size: 13,
                                weight: .bold
                            )
                        )
                        .foregroundStyle(purple)
                        .padding(10)
                        .background(
                            .white,
                            in: Capsule()
                        )
                    }

                    Spacer()

                    // MARK: - CONTROLES

                    HStack(alignment: .bottom) {

                        MaicitoJoystick(
                            direction: $joystickDirection
                        )

                        Spacer()

                        VStack(spacing: 10) {

                            if showError {

                                Text("¡Intenta otra traducción!")
                                    .foregroundStyle(.red)
                                    .font(
                                        .system(
                                            size: 12,
                                            weight: .bold
                                        )
                                    )
                            }

                            if showSuccess {

                                Text("¡Pareja correcta!")
                                    .foregroundStyle(.green)
                                    .font(
                                        .system(
                                            size: 12,
                                            weight: .bold
                                        )
                                    )
                            }

                            Button {
                                interact()
                            } label: {

                                Image(
                                    systemName: "hand.tap.fill"
                                )
                                .font(.system(size: 27))
                                .foregroundStyle(.white)
                                .frame(
                                    width: 70,
                                    height: 70
                                )
                                .background(
                                    purple,
                                    in: Circle()
                                )
                            }

                            Text("Interactuar")
                                .font(
                                    .system(
                                        size: 12,
                                        weight: .bold
                                    )
                                )
                                .foregroundStyle(.white)
                                .shadow(
                                    color: .black,
                                    radius: 3
                                )
                        }
                    }
                    .padding(.horizontal, 22)
                    .padding(.bottom, 20)
                }

                // MARK: - FINAL

                if gameFinished {

                    Color.black.opacity(0.55)
                        .ignoresSafeArea()

                    VStack(spacing: 20) {

                        PetView(size: 110)

                        Text("¡Repaso completado!")
                            .font(
                                .system(
                                    size: 25,
                                    weight: .bold
                                )
                            )
                            .foregroundStyle(purple)

                        Text(
                            "¡Encontraste las cuatro parejas!"
                        )
                        .font(.system(size: 16))
                        .multilineTextAlignment(.center)

                        Button {

                            audioPlayer?.stop()

                            onExerciseCompleted(
                                previousCorrectAnswers + 1
                            )

                        } label: {

                            Text("Ver resultados")
                                .font(
                                    .system(
                                        size: 17,
                                        weight: .bold
                                    )
                                )
                                .foregroundStyle(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 54)
                                .background(
                                    purple,
                                    in: RoundedRectangle(
                                        cornerRadius: 15
                                    )
                                )
                        }
                    }
                    .padding(25)
                    .frame(
                        maxWidth: geometry.size.width - 44
                    )
                    .background(
                        .white,
                        in: RoundedRectangle(
                            cornerRadius: 25
                        )
                    )
                }
            }
        }
        .onDisappear {

            audioPlayer?.stop()
            feedbackTask?.cancel()
            joystickDirection = .zero
        }
    }

    // MARK: - ETIQUETAS

    private func phraseLabel(
        title: String,
        subtitle: String,
        color: Color
    ) -> some View {

        VStack(spacing: 4) {

            Text(title)
                .font(
                    .system(
                        size: 12,
                        weight: .bold
                    )
                )
                .multilineTextAlignment(.center)
                .lineLimit(2)
                .minimumScaleFactor(0.75)

            Text(subtitle)
                .font(.system(size: 10))
                .opacity(0.75)
        }
        .foregroundStyle(.white)
        .padding(.horizontal, 9)
        .padding(.vertical, 8)
        .frame(width: 125)
        .background(
            color.opacity(0.95),
            in: RoundedRectangle(
                cornerRadius: 12
            )
        )
        .shadow(
            color: .black.opacity(0.18),
            radius: 4,
            y: 2
        )
    }

    // MARK: - MOVIMIENTO

    private func updateMovement(
        date: Date,
        size: CGSize
    ) {

        let delta = min(
            date.timeIntervalSince(lastUpdate),
            0.05
        )

        lastUpdate = date

        guard !gameFinished else {
            return
        }

        guard size.width > 0,
              size.height > 0 else {
            return
        }

        let aspectCorrection =
            size.height / size.width

        let nextX =
            maicitoPosition.x
            + joystickDirection.dx
            * movementSpeed
            * CGFloat(delta)
            * aspectCorrection

        let nextY =
            maicitoPosition.y
            + joystickDirection.dy
            * movementSpeed
            * CGFloat(delta)

        maicitoPosition.x = min(
            max(nextX, 0.10),
            0.90
        )

        maicitoPosition.y = min(
            max(nextY, 0.13),
            0.84
        )
    }

    // MARK: - INTERACCIÓN

    private func interact() {

        guard !gameFinished else {
            return
        }

        if let carriedPhrase {

            guard let nearest = phrases
                .filter({
                    !completedPairs.contains($0.id)
                })
                .min(by: {
                    distance(
                        from: maicitoPosition,
                        to: $0.spanishPosition
                    )
                    <
                    distance(
                        from: maicitoPosition,
                        to: $1.spanishPosition
                    )
                })
            else {
                return
            }

            let proximity = distance(
                from: maicitoPosition,
                to: nearest.spanishPosition
            )

            guard proximity < interactionDistance else {
                return
            }

            if nearest.id == carriedPhrase {

                completedPairs.insert(
                    carriedPhrase
                )

                self.carriedPhrase = nil

                SoundManager.shared.play(
                    "audioBien",
                    volume: Float(appState.soundVolume)
                )

                showFeedback(success: true)

                if completedPairs.count == 4 {

                    joystickDirection = .zero

                    DispatchQueue.main.asyncAfter(
                        deadline: .now() + 0.8
                    ) {
                        gameFinished = true
                    }
                }

            } else {

                SoundManager.shared.play(
                    "audioMalaSeleccion",
                    volume: Float(appState.soundVolume)
                )

                showFeedback(success: false)
            }

        } else {

            guard let nearest = phrases
                .filter({
                    !completedPairs.contains($0.id)
                })
                .min(by: {
                    distance(
                        from: maicitoPosition,
                        to: $0.yuhmuPosition
                    )
                    <
                    distance(
                        from: maicitoPosition,
                        to: $1.yuhmuPosition
                    )
                })
            else {
                return
            }

            let proximity = distance(
                from: maicitoPosition,
                to: nearest.yuhmuPosition
            )

            guard proximity < interactionDistance else {
                return
            }

            carriedPhrase = nearest.id

            playAudio(
                named: nearest.audio
            )
        }
    }

    // MARK: - DISTANCIA

    private func distance(
        from first: CGPoint,
        to second: CGPoint
    ) -> CGFloat {

        let dx = first.x - second.x
        let dy = first.y - second.y

        return sqrt(
            dx * dx + dy * dy
        )
    }

    // MARK: - AUDIO

    private func playAudio(
        named name: String
    ) {

        guard let url = Bundle.main.url(
            forResource: name,
            withExtension: "m4a"
        ) else {

            print(
                "No se encontró \(name).m4a"
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

    // MARK: - FEEDBACK

    private func showFeedback(
        success: Bool
    ) {

        feedbackTask?.cancel()

        showSuccess = success
        showError = !success

        feedbackTask = Task {

            try? await Task.sleep(
                for: .seconds(1.2)
            )

            guard !Task.isCancelled else {
                return
            }

            showSuccess = false
            showError = false
        }
    }
}
