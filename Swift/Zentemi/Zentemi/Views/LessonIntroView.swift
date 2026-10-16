import SwiftUI

struct LessonIntroView: View {

    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) private var dismiss

    let lessonNumber: Int
    let title: String
    let description: String
    let exercises: Int
    let estimatedMinutes: Int
    let reward: Int
    let onLessonCompleted: () -> Void

    @State private var mascotScaleY: CGFloat = 1.0
    @State private var animateBackground = false
    @State private var selectedInfo: LessonInfo? = nil
    @State private var showGreetingExercise = false

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

            background

            VStack(spacing: 0) {

                // MARK: - ENCABEZADO

                header

                ScrollView(
                    showsIndicators: false
                ) {

                    VStack(spacing: 14) {

                        lessonCard

                        // MARK: - COMENZAR

                        Button {

                            if exercises > 0 {

                                MusicManager.shared.pauseMusic()

                                showGreetingExercise = true
                            }

                        } label: {

                            Text("Comenzar")
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
                                .background(purple)
                                .clipShape(
                                    RoundedRectangle(
                                        cornerRadius: 14
                                    )
                                )
                                .shadow(
                                    color:
                                        purple.opacity(0.25),
                                    radius: 8,
                                    y: 5
                                )
                        }
                    }
                    .disabled(exercises == 0)
                    .opacity(exercises == 0 ? 0.45 : 1.0)
                    .padding(.horizontal, 20)
                    .padding(.top, 18)
                    .padding(.bottom, 30)
                }
            }

            // MARK: - INFORMACIÓN

            if let selectedInfo {

                infoOverlay(
                    info: selectedInfo
                )
            }
        }
        .onAppear {

            mascotScaleY = 1.0

            DispatchQueue.main.asyncAfter(
                deadline: .now() + 0.1
            ) {

                withAnimation(
                    .easeInOut(duration: 1.6)
                        .repeatForever(
                            autoreverses: true
                        )
                ) {
                    mascotScaleY = 1.06
                }
            }
            
            withAnimation(
                .easeInOut(duration: 7)
                    .repeatForever(
                        autoreverses: true
                    )
            ) {
                animateBackground = true
            }
        }
        .onChange(of: showGreetingExercise) { oldValue, newValue in

            if oldValue == true,
               newValue == false,
               appState.musicVolume > 0 {

                MusicManager.shared.playMusic(
                    named: "musicaCamino",
                    volume: Float(appState.musicVolume)
                )
            }
        }
        .fullScreenCover(
            isPresented: $showGreetingExercise
        ) {
            GreetingExerciseView(
                exerciseNumber: 1,
                totalExercises: appState.greetingsExerciseCount
            ) {
                onLessonCompleted()
            }
            .environmentObject(appState)
        }
    }


    // MARK: - ENCABEZADO

    private var header: some View {

        HStack(spacing: 10) {

            Button {

                dismiss()

            } label: {

                Image(
                    systemName: "chevron.left"
                )
                .font(
                    .system(
                        size: 17,
                        weight: .bold
                    )
                )
                .foregroundStyle(purple)
                .frame(
                    width: 38,
                    height: 38
                )
                .background(.white)
                .clipShape(Circle())
                .shadow(
                    color: .black.opacity(0.05),
                    radius: 5,
                    y: 2
                )
            }

            Text(
                "Lección \(lessonNumber) de 7"
            )
            .font(
                .system(
                    size: 15,
                    weight: .medium
                )
            )
            .foregroundStyle(.secondary)

            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.top, 15)
        .padding(.bottom, 10)
    }


    // MARK: - TARJETA DE LECCIÓN

    private var lessonCard: some View {

        VStack(spacing: 0) {

            // MARK: Nueva sección

            Text("NUEVA SECCIÓN")
                .font(
                    .system(
                        size: 9,
                        weight: .bold
                    )
                )
                .foregroundStyle(purple)
                .padding(
                    .horizontal,
                    12
                )
                .padding(
                    .vertical,
                    5
                )
                .background(
                    purple.opacity(0.09)
                )
                .clipShape(Capsule())
                .padding(.top, 18)


            // MARK: Mascota

            PetView(size: 125)
                .scaleEffect(
                    x: 1.0,
                    y: mascotScaleY,
                    anchor: .bottom
                )
                .padding(.top, 15)


            // MARK: Título

            Text(title)
                .font(
                    .system(
                        size: 24,
                        weight: .bold
                    )
                )
                .foregroundStyle(
                    Color(
                        red: 0.24,
                        green: 0.22,
                        blue: 0.29
                    )
                )
                .padding(.top, 13)


            // MARK: Descripción

            Text(description)
                .font(
                    .system(size: 14)
                )
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .lineSpacing(3)
                .padding(
                    .horizontal,
                    25
                )
                .padding(.top, 8)
                .padding(.bottom, 20)


            Divider()
                .opacity(0.6)


            // MARK: Información

            VStack(spacing: 14) {

                infoRow(
                    icon: "checkmark",
                    text:
                        "\(exercises) ejercicios",
                    color: turquoise
                ) {
                    selectedInfo = .exercises
                }

                infoRow(
                    icon: "clock.fill",
                    text:
                        "~\(estimatedMinutes) min",
                    color: purple
                ) {
                    selectedInfo = .time
                }

                infoRow(
                    icon:
                        "dollarsign.circle.fill",
                    text:
                        "+\(reward) monedas",
                    color: .yellow
                ) {
                    selectedInfo = .coins
                }
            }
            .padding(.horizontal, 17)
            .padding(.vertical, 18)
        }
        .frame(
            maxWidth: .infinity
        )
        .background(.white)
        .clipShape(
            RoundedRectangle(
                cornerRadius: 20
            )
        )
        .shadow(
            color: .black.opacity(0.055),
            radius: 9,
            y: 4
        )
    }


    // MARK: - FILA INFORMATIVA

    private func infoRow(
        icon: String,
        text: String,
        color: Color,
        action: @escaping () -> Void
    ) -> some View {

        Button {

            action()

        } label: {

            HStack(spacing: 11) {

                ZStack {

                    RoundedRectangle(
                        cornerRadius: 9
                    )
                    .fill(
                        color.opacity(0.12)
                    )
                    .frame(
                        width: 34,
                        height: 34
                    )

                    Image(
                        systemName: icon
                    )
                    .font(
                        .system(
                            size: 14,
                            weight: .semibold
                        )
                    )
                    .foregroundStyle(color)
                }

                Text(text)
                    .font(
                        .system(
                            size: 14,
                            weight: .semibold
                        )
                    )
                    .foregroundStyle(
                        Color(
                            red: 0.30,
                            green: 0.28,
                            blue: 0.34
                        )
                    )

                Spacer()

                Image(
                    systemName:
                        "info.circle"
                )
                .font(
                    .system(size: 14)
                )
                .foregroundStyle(
                    Color.gray.opacity(0.45)
                )
            }
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }


    // MARK: - CUADRO INFORMATIVO

    private func infoOverlay(
        info: LessonInfo
    ) -> some View {

        ZStack {

            Color.black.opacity(0.25)
                .ignoresSafeArea()
                .onTapGesture {
                    selectedInfo = nil
                }

            VStack(spacing: 14) {

                Image(
                    systemName:
                        info.icon
                )
                .font(
                    .system(size: 27)
                )
                .foregroundStyle(
                    info.color(
                        purple: purple,
                        turquoise: turquoise
                    )
                )

                Text(info.title)
                    .font(
                        .system(
                            size: 20,
                            weight: .bold
                        )
                    )

                Text(
                    info.message(
                        exercises: exercises,
                        minutes:
                            estimatedMinutes,
                        reward: reward
                    )
                )
                .font(
                    .system(size: 14)
                )
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .lineSpacing(3)

                Button {

                    selectedInfo = nil

                } label: {

                    Text("Entendido")
                        .font(
                            .system(
                                size: 14,
                                weight: .bold
                            )
                        )
                        .foregroundStyle(.white)
                        .padding(
                            .horizontal,
                            25
                        )
                        .padding(
                            .vertical,
                            10
                        )
                        .background(purple)
                        .clipShape(
                            RoundedRectangle(
                                cornerRadius: 11
                            )
                        )
                }
            }
            .padding(24)
            .frame(maxWidth: 300)
            .background(.white)
            .clipShape(
                RoundedRectangle(
                    cornerRadius: 22
                )
            )
            .shadow(
                color: .black.opacity(0.15),
                radius: 15
            )
        }
    }


    // MARK: - FONDO

    private var background: some View {

        ZStack {

            backgroundColor

            Circle()
                .fill(
                    Color.purple.opacity(0.045)
                )
                .frame(
                    width: 230,
                    height: 230
                )
                .offset(
                    x:
                        animateBackground
                        ? -145
                        : -185,
                    y:
                        animateBackground
                        ? -270
                        : -320
                )

            Circle()
                .fill(
                    turquoise.opacity(0.055)
                )
                .frame(
                    width: 220,
                    height: 220
                )
                .offset(
                    x:
                        animateBackground
                        ? 150
                        : 185,
                    y:
                        animateBackground
                        ? -100
                        : -55
                )

            Circle()
                .fill(
                    Color.yellow.opacity(0.055)
                )
                .frame(
                    width: 170,
                    height: 170
                )
                .offset(
                    x:
                        animateBackground
                        ? 145
                        : 175,
                    y:
                        animateBackground
                        ? 350
                        : 390
                )
        }
        .ignoresSafeArea()
    }
}


// MARK: - INFORMACIÓN

private enum LessonInfo {

    case exercises
    case time
    case coins

    var title: String {

        switch self {

        case .exercises:
            return "Ejercicios"

        case .time:
            return "Tiempo estimado"

        case .coins:
            return "Recompensa"
        }
    }


    var icon: String {

        switch self {

        case .exercises:
            return "checkmark"

        case .time:
            return "clock.fill"

        case .coins:
            return "dollarsign.circle.fill"
        }
    }


    func color(
        purple: Color,
        turquoise: Color
    ) -> Color {

        switch self {

        case .exercises:
            return turquoise

        case .time:
            return purple

        case .coins:
            return .yellow
        }
    }


    func message(
        exercises: Int,
        minutes: Int,
        reward: Int
    ) -> String {

        switch self {

        case .exercises:

            return
                "Esta sección contiene \(exercises) ejercicios para practicar lo aprendido."

        case .time:

            return
                "Los ~\(minutes) minutos son solo un tiempo estimado. Puedes avanzar a tu propio ritmo y tomarte el tiempo que necesites."

        case .coins:

            return
                "Al completar esta sección recibirás \(reward) monedas para utilizar dentro de Zentemi."
        }
    }
}


// MARK: - PREVIEW

#Preview {

    LessonIntroView(
        lessonNumber: 1,
        title: "Saludos básicos",
        description:
            "Aprende a saludar y despedirte en Yuhmu como lo hacen en San Juan Ixtenco.",
        exercises: 1,
        estimatedMinutes: 3,
        reward: 700
        ) {
            print("Lección terminada")
        }
        .environmentObject(
            AppState()
        )
}
