
import SwiftUI
import AVFoundation

struct FirstPhraseSceneView: View {

    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var appState: AppState

    let onLessonCompleted: () -> Void

    @State private var breathing = false
    @State private var dialogueStep = 0
    @State private var showExercise = false
    @State private var player: AVAudioPlayer?

    private let purple = Color(
        red: 0.48,
        green: 0.29,
        blue: 0.79
    )

    private let dialogues = [
        "¡Hola!",
        "¡Hola!",
        "¿Sabes cómo se dice buenos días en Yuhmu?",
        "¡Sí! Se dice «Té xki hats’i».",
        "¡Muchas gracias!"
    ]

    private var isMaicitoSpeaking: Bool {
        dialogueStep % 2 == 0
    }

    private var speakingCharacter: String {
        isMaicitoSpeaking ? "mascota" : "personaje00"
    }

    var body: some View {
        GeometryReader { geometry in
            ZStack {

                // MARK: - FONDO

                Image("fondoPF_01")
                    .resizable()
                    .scaledToFill()
                    .frame(
                        width: geometry.size.width,
                        height: geometry.size.height
                    )
                    .clipped()
                    .ignoresSafeArea()

                VStack(spacing: 18) {

                    // MARK: - ENCABEZADO

                    HStack {
                        Button {
                            player?.stop()
                            dismiss()
                        } label: {
                            Image(systemName: "xmark")
                                .font(.headline)
                                .foregroundStyle(purple)
                                .frame(width: 44, height: 44)
                                .background(.white, in: Circle())
                        }

                        Spacer()

                        Text("Primeras frases")
                            .font(.headline)
                            .foregroundStyle(purple)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 10)
                            .background(
                                .white.opacity(0.92),
                                in: Capsule()
                            )
                    }

                    Spacer(minLength: 10)

                    // MARK: - DIÁLOGO ANIMADO

                    HStack {
                        if !isMaicitoSpeaking {
                            Spacer(minLength: 45)
                        }

                        VStack(spacing: 12) {

                            Text(dialogues[dialogueStep])
                                .font(
                                    .system(
                                        size: 23,
                                        weight: .bold
                                    )
                                )
                                .multilineTextAlignment(.center)
                                .foregroundStyle(
                                    Color(
                                        red: 0.24,
                                        green: 0.22,
                                        blue: 0.29
                                    )
                                )
                                .frame(maxWidth: .infinity)

                            HStack {
                                if !isMaicitoSpeaking {
                                    Spacer()
                                }

                                Group {
                                    if isMaicitoSpeaking {
                                        PetView(size: 48)
                                    } else {
                                        Image("personaje00")
                                            .resizable()
                                            .scaledToFit()
                                            .frame(width: 48, height: 48)
                                    }
                                }

                                if isMaicitoSpeaking {
                                    Spacer()
                                }
                            }
                        }
                        .padding(20)
                        .background(
                            .white.opacity(0.96),
                            in: RoundedRectangle(
                                cornerRadius: 24
                            )
                        )
                        .id(dialogueStep)
                        .transition(
                            .asymmetric(
                                insertion:
                                    .offset(
                                        x: isMaicitoSpeaking
                                        ? -70
                                        : 70
                                    )
                                    .combined(
                                        with: .opacity
                                    )
                                    .combined(
                                        with: .scale(
                                            scale: 0.9
                                        )
                                    ),
                                removal: .opacity
                            )
                        )

                        if isMaicitoSpeaking {
                            Spacer(minLength: 45)
                        }
                    }
                    .animation(
                        .easeInOut(duration: 0.4),
                        value: dialogueStep
                    )

                    Spacer()

                    // MARK: - PERSONAJES

                    HStack(
                        alignment: .bottom,
                        spacing: 12
                    ) {

                        PetView(size: min(geometry.size.width * 0.42, 190))
                            .frame(maxWidth: .infinity)
                            .scaleEffect(
                                x: 1,
                                y: breathing
                                ? 1.045
                                : 0.985,
                                anchor: .bottom
                            )

                        Image("personaje00")
                            .resizable()
                            .scaledToFit()
                            .frame(maxWidth: .infinity)
                            .scaleEffect(
                                x: 1,
                                y: breathing
                                ? 0.985
                                : 1.045,
                                anchor: .bottom
                            )
                    }
                    .frame(
                        height: min(
                            geometry.size.height * 0.28,
                            245
                        ),
                        alignment: .bottom
                    )

                    Spacer(minLength: 8)

                    // MARK: - ESCUCHAR AUDIO

                    if dialogueStep == 3 {
                        Button {
                            playAudio()
                        } label: {
                            Label(
                                "Escuchar audio",
                                systemImage:
                                    "speaker.wave.2.fill"
                            )
                            .font(
                                .system(
                                    size: 18,
                                    weight: .bold
                                )
                            )
                            .frame(maxWidth: .infinity)
                            .frame(height: 54)
                            .foregroundStyle(.white)
                            .background(
                                purple,
                                in: RoundedRectangle(
                                    cornerRadius: 15
                                )
                            )
                        }
                    }

                    // MARK: - SIGUIENTE / CONTINUAR

                    Button {
                        player?.stop()

                        if dialogueStep < dialogues.count - 1 {
                            withAnimation(
                                .easeInOut(duration: 0.4)
                            ) {
                                dialogueStep += 1
                            }
                        } else {
                            showExercise = true
                        }
                    } label: {
                        Text(
                            dialogueStep == dialogues.count - 1
                            ? "Continuar"
                            : "Siguiente"
                        )
                        .font(
                            .system(
                                size: 18,
                                weight: .bold
                            )
                        )
                        .frame(maxWidth: .infinity)
                        .frame(height: 54)
                        .foregroundStyle(.white)
                        .background(
                            purple,
                            in: RoundedRectangle(
                                cornerRadius: 15
                            )
                        )
                    }
                }
                .padding(.horizontal, 22)
                .padding(.top, 12)
                .padding(.bottom, 20)
            }
        }
        .onAppear {
            withAnimation(
                .easeInOut(duration: 1.6)
                    .repeatForever(
                        autoreverses: true
                    )
            ) {
                breathing = true
            }
        }
        .onDisappear {
            player?.stop()
        }
        .fullScreenCover(
            isPresented: $showExercise
        ) {
            GreetingExerciseView(
                exerciseNumber: 1,
                totalExercises:
                    appState.greetingsExerciseCount
            ) {
                onLessonCompleted()
            }
            .environmentObject(appState)
        }
    }

    // MARK: - AUDIO

    private func playAudio() {

        guard let url = Bundle.main.url(
            forResource: "buenosDias",
            withExtension: "m4a"
        ) else {
            print(
                "No se encontró buenosDias.m4a"
            )
            return
        }

        do {
            player?.stop()

            player = try AVAudioPlayer(
                contentsOf: url
            )
            player?.volume = Float(
                appState.soundVolume
            )
            player?.prepareToPlay()
            player?.play()

        } catch {
            print(
                "Error al reproducir audio: \(error)"
            )
        }
    }
}
