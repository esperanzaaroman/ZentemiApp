import SwiftUI

struct LearningPathView: View {

    @EnvironmentObject var appState: AppState
    @Environment(\.dismiss) private var dismiss

    @State private var animateBackground = false
    @State private var lockedLessonName: String? = nil
    @State private var showLessonIntro = false
    @State private var selectedLesson = 1



    private let turquoise = Color(
        red: 0.16,
        green: 0.78,
        blue: 0.70
    )

    private let purple = Color(
        red: 0.48,
        green: 0.29,
        blue: 0.79
    )

    private let backgroundColor = Color(
        red: 0.97,
        green: 0.95,
        blue: 0.99
    )

    private let lessons = [
        "Saludos",
        "Familia",
        "Números",
        "Colores",
        "Animales",
        "Alimentos",
        "Frases básicas"
    ]


    var body: some View {

        ZStack {

            background

            VStack(spacing: 0) {

                // MARK: - ENCABEZADO

                header

                // MARK: - CAMINO

                ScrollView(
                    showsIndicators: false
                ) {

                    VStack(spacing: 0) {

                        lessonNode(
                            number: 1,
                            name: "Saludos",
                            icon: "checkmark",
                            xOffset: -85
                        )

                        pathDots(
                            direction: .right
                        )

                        lessonNode(
                            number: 2,
                            name: "Familia",
                            icon: "person.2.fill",
                            xOffset: 75
                        )

                        pathDots(
                            direction: .left
                        )

                        lessonNode(
                            number: 3,
                            name: "Números",
                            icon: "number",
                            xOffset: -70
                        )

                        pathDots(
                            direction: .right
                        )

                        lessonNode(
                            number: 4,
                            name: "Colores",
                            icon: "paintpalette.fill",
                            xOffset: 80
                        )

                        pathDots(
                            direction: .left
                        )

                        lessonNode(
                            number: 5,
                            name: "Animales",
                            icon: "pawprint.fill",
                            xOffset: -75
                        )

                        pathDots(
                            direction: .right
                        )

                        lessonNode(
                            number: 6,
                            name: "Alimentos",
                            icon: "fork.knife",
                            xOffset: 75
                        )

                        pathDots(
                            direction: .left
                        )

                        lessonNode(
                            number: 7,
                            name: "Frases básicas",
                            icon: "text.bubble.fill",
                            xOffset: -65
                        )
                    }
                    .padding(.top, 24)
                    .padding(.bottom, 70)
                }
            }


            // MARK: - MENSAJE BLOQUEADO

            if let lessonName = lockedLessonName {

                lockedOverlay(
                    lessonName: lessonName
                )
            }
        }
        .onAppear {
            if appState.musicVolume > 0 {

                MusicManager.shared.playMusic(
                    named: "musicaCamino",
                    volume: Float(appState.musicVolume)
                )
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
        .onDisappear {

            if !showLessonIntro,
               appState.musicVolume > 0 {

                MusicManager.shared.playMusic(
                    named: "audiolobby",
                    volume: Float(appState.musicVolume)
                )
            }
        }
        // MARK: - ABRIR LECCIÓN

        .fullScreenCover(
            isPresented: $showLessonIntro
        ) {

            if selectedLesson == 1 {

                LessonIntroView(
                    lessonNumber: 1,
                    title: "Saludos básicos",
                    description:
                        "Aprende a saludar y despedirte en Yuhmu como lo hacen en San Juan Ixtenco.",
                    exercises: appState.greetingsExerciseCount,
                    estimatedMinutes: 3,
                    reward: 700
                ) {
                    var transaction = Transaction()
                    transaction.disablesAnimations = true

                    withTransaction(transaction) {
                        showLessonIntro = false
                    }

                    if appState.musicVolume > 0 {
                        MusicManager.shared.playMusic(
                            named: "musicaCamino",
                            volume: Float(appState.musicVolume)
                        )
                    }
                }
                .environmentObject(appState)

            } else if selectedLesson == 2 {

                LessonIntroView(
                    lessonNumber: 2,
                    title: "La familia",
                    description:
                        "Conoce las palabras en Yuhmu para identificar y hablar sobre los integrantes de la familia.",
                    exercises: appState.familyExerciseCount,
                    estimatedMinutes: 5,
                    reward: 900
                ) {
                    var transaction = Transaction()
                    transaction.disablesAnimations = true

                    withTransaction(transaction) {
                        showLessonIntro = false
                    }
                }
                .environmentObject(appState)
            }
        }
    }


    // MARK: - ENCABEZADO

    private var header: some View {

        VStack(spacing: 14) {

            HStack {

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

                Text("Tu camino")
                    .font(
                        .system(
                            size: 25,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(
                        Color(
                            red: 0.22,
                            green: 0.20,
                            blue: 0.28
                        )
                    )

                Spacer()

                Text("\(appState.pathLessonsCompleted) / 7")
                    .font(
                        .system(
                            size: 14,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(turquoise)
            }


            // MARK: Barra de progreso

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
                                * CGFloat(appState.pathLessonsCompleted)
                                / 7
                        )
                }
            }
            .frame(height: 8)
        }
        .padding(.horizontal, 22)
        .padding(.top, 16)
        .padding(.bottom, 12)
        .background(
            backgroundColor.opacity(0.96)
        )
    }


    // MARK: - NODO DE LECCIÓN

    private func lessonNode(
        number: Int,
        name: String,
        icon: String,
        xOffset: CGFloat
    ) -> some View {

        let isUnlocked =
            number <= appState.unlockedPathLesson

        let isCompleted =
            number <= appState.pathLessonsCompleted

        return Button {

            if isUnlocked {

                if number == 1 || number == 2 {
                    selectedLesson = number
                    showLessonIntro = true
                }

            } else {

                lockedLessonName = name
            }

        } label: {

            VStack(spacing: 7) {

                ZStack {

                    Circle()
                        .fill(
                            isCompleted
                            ? turquoise
                            : isUnlocked
                                ? purple
                                : Color(
                                    red: 0.88,
                                    green: 0.86,
                                    blue: 0.94
                                )
                        )
                        .frame(
                            width: 62,
                            height: 62
                        )
                        .shadow(
                            color:
                                isCompleted
                                ? turquoise.opacity(0.25)
                                : isUnlocked
                                    ? purple.opacity(0.25)
                                    : .clear,
                            radius: 8,
                            y: 4
                        )

                    Image(
                        systemName:
                            isUnlocked
                            ? icon
                            : "lock.fill"
                    )
                    .font(
                        .system(
                            size: 20,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(
                        isUnlocked
                        ? .white
                        : Color.purple.opacity(0.28)
                    )
                }


                // MARK: Título

                Text(name)
                    .font(
                        .system(
                            size: 13,
                            weight: .semibold
                        )
                    )
                    .foregroundStyle(
                        Color(
                            red: 0.30,
                            green: 0.27,
                            blue: 0.36
                        )
                    )
                    .padding(
                        .horizontal,
                        11
                    )
                    .padding(
                        .vertical,
                        5
                    )
                    .background(
                        Color.white.opacity(
                            isUnlocked
                            ? 0.95
                            : 0.50
                        )
                    )
                    .clipShape(Capsule())
            }

            // IMPORTANTE:
            // ocupa todo el ancho para evitar
            // que los nodos se recorten.

            .frame(
                maxWidth: .infinity
            )
            .offset(x: xOffset)
        }
        .buttonStyle(.plain)
    }


    // MARK: - PUNTOS DEL CAMINO

    private enum PathDirection {

        case left
        case right
    }


    private func pathDots(
        direction: PathDirection
    ) -> some View {

        ZStack {

            ForEach(
                0..<6,
                id: \.self
            ) { index in

                let progress =
                    CGFloat(index) / 5

                let startX: CGFloat =
                    direction == .right
                    ? -70
                    : 70

                let endX: CGFloat =
                    direction == .right
                    ? 70
                    : -70

                Circle()
                    .fill(
                        Color.purple.opacity(0.18)
                    )
                    .frame(
                        width: 7,
                        height: 7
                    )
                    .offset(
                        x:
                            startX
                            + (
                                endX - startX
                            ) * progress,
                        y:
                            CGFloat(index) * 12
                            - 30
                    )
            }
        }
        .frame(
            maxWidth: .infinity
        )
        .frame(
            height: 72
        )
    }


    // MARK: - AVISO LECCIÓN BLOQUEADA

    private func lockedOverlay(
        lessonName: String
    ) -> some View {

        ZStack {

            Color.black.opacity(0.20)
                .ignoresSafeArea()
                .onTapGesture {
                    lockedLessonName = nil
                }

            VStack(spacing: 13) {

                Image(
                    systemName: "lock.fill"
                )
                .font(
                    .system(size: 27)
                )
                .foregroundStyle(purple)

                Text("Lección bloqueada")
                    .font(
                        .system(
                            size: 19,
                            weight: .bold
                        )
                    )

                Text(
                    lockedMessage(
                        for: lessonName
                    )
                )
                .font(
                    .system(size: 14)
                )
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)

                Button {

                    lockedLessonName = nil

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
            .frame(maxWidth: 290)
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


    // MARK: - MENSAJE DE BLOQUEO

    private func lockedMessage(
        for lessonName: String
    ) -> String {

        guard
            let index =
                lessons.firstIndex(
                    of: lessonName
                ),
            index > 0
        else {

            return
                "Completa la lección anterior para continuar."
        }

        return
            "Completa \(lessons[index - 1]) para desbloquear esta lección."
    }


    // MARK: - FONDO ANIMADO

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
                    width: 260,
                    height: 260
                )
                .offset(
                    x:
                        animateBackground
                        ? 155
                        : 195,
                    y:
                        animateBackground
                        ? 80
                        : 130
                )

            Circle()
                .fill(
                    Color.purple.opacity(0.04)
                )
                .frame(
                    width: 180,
                    height: 180
                )
                .offset(
                    x:
                        animateBackground
                        ? -145
                        : -180,
                    y:
                        animateBackground
                        ? 350
                        : 400
                )

            Circle()
                .fill(
                    Color.cyan.opacity(0.045)
                )
                .frame(
                    width: 210,
                    height: 210
                )
                .offset(
                    x:
                        animateBackground
                        ? 145
                        : 180,
                    y:
                        animateBackground
                        ? 520
                        : 570
                )
        }
        .ignoresSafeArea()
    }
}


// MARK: - PREVIEW

#Preview {

    LearningPathView()
        .environmentObject(
            AppState()
        )
}
