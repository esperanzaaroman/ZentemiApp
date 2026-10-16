import SwiftUI

struct ProfileView: View {

    @EnvironmentObject var appState: AppState

    @State private var showAvatarEditor = false
    @State private var showSettings = false
    @State private var showLevels = false
    @State private var selectedInfo: ProfileInfo?
    @State private var editedUserName = ""

    private let turquoise = Color(
        red: 0.28,
        green: 0.78,
        blue: 0.75
    )

    var body: some View {

        ZStack {

            // Fondo original claro

            Color(
                red: 0.975,
                green: 0.97,
                blue: 0.99
            )
            .ignoresSafeArea()

            ScrollView(
                showsIndicators: false
            ) {

                VStack(spacing: 0) {

                    // MARK: - CABECERA

                    header

                    // MARK: - CONTENIDO

                    VStack(spacing: 14) {

                        statsSection

                        progressSection

                        achievementsSection

                        Spacer()
                            .frame(height: 25)
                    }
                    .padding(.horizontal, 18)
                    .padding(.top, 14)
                }
            }
        }

        // MARK: - Personalización

        .sheet(
            isPresented: $showAvatarEditor
        ) {

            avatarEditor
        }

        // MARK: - Ajustes

        .sheet(
            isPresented: $showSettings
        ) {

            NavigationStack {

                SettingsView()
                    .environmentObject(appState)
            }
        }

        // MARK: - Información estadísticas

        .sheet(
            item: $selectedInfo
        ) { info in

            infoSheet(info)
        }
    }

    // MARK: - HEADER

    private var header: some View {

        VStack(spacing: 0) {

            ZStack {

                // Fondo personalizable

                LinearGradient(
                    colors: [

                        Color(
                            hue: appState.profileBackgroundHue,
                            saturation: 0.72,
                            brightness: 0.72
                        ),

                        Color(
                            hue: appState.profileBackgroundHue,
                            saturation: 0.64,
                            brightness: 0.84
                        )
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )

                // Círculos decorativos

                Circle()
                    .fill(
                        Color.white.opacity(0.06)
                    )
                    .frame(
                        width: 180,
                        height: 180
                    )
                    .offset(
                        x: -190,
                        y: -75
                    )

                Circle()
                    .fill(
                        Color.white.opacity(0.035)
                    )
                    .frame(
                        width: 230,
                        height: 230
                    )
                    .offset(
                        x: 145,
                        y: 95
                    )

                // MARK: Botones superiores

                VStack {

                    HStack {

                        // Editar perfil

                        Button {

                            showAvatarEditor = true

                        } label: {

                            Image(
                                systemName: "pencil"
                            )
                            .font(
                                .system(
                                    size: 16,
                                    weight: .semibold
                                )
                            )
                            .foregroundStyle(.white)
                            .frame(
                                width: 36,
                                height: 36
                            )
                            .background(
                                Color.white.opacity(0.16)
                            )
                            .clipShape(Circle())
                        }

                        Spacer()

                        // Ajustes

                        Button {

                            showSettings = true

                        } label: {

                            Image(
                                systemName: "gearshape.fill"
                            )
                            .font(
                                .system(size: 16)
                            )
                            .foregroundStyle(.white)
                            .frame(
                                width: 36,
                                height: 36
                            )
                            .background(
                                Color.white.opacity(0.16)
                            )
                            .clipShape(Circle())
                        }
                    }
                    .padding(.horizontal, 17)
                    .padding(.top, 16)

                    Spacer()
                }

                // MARK: Perfil

                VStack(spacing: 9) {

                    Spacer()
                        .frame(height: 52)

                    // Avatar

                    ZStack {

                        RoundedRectangle(
                            cornerRadius: 16
                        )
                        .fill(
                            Color(
                                hue: appState.avatarBackgroundHue,
                                saturation: 0.22,
                                brightness: 1
                            )
                        )
                        .frame(
                            width: 82,
                            height: 82
                        )

                        // CAMBIO: mascota + accesorios
                        PetView(size: 67)
                    }

                    // Nombre

                    Text(appState.userName)
                        .font(
                            .system(
                                size: 22,
                                weight: .bold
                            )
                        )
                        .foregroundStyle(.white)

                    // Nivel

                    Button {

                        withAnimation(
                            .easeInOut(
                                duration: 0.25
                            )
                        ) {

                            showLevels.toggle()
                        }

                    } label: {

                        HStack(spacing: 6) {

                            Text(
                                "Nivel \(appState.currentLevel) — \(currentLevelName)"
                            )

                            Image(
                                systemName:
                                    showLevels
                                    ? "chevron.up"
                                    : "chevron.down"
                            )
                            .font(
                                .system(
                                    size: 9,
                                    weight: .bold
                                )
                            )
                        }
                        .font(
                            .system(
                                size: 12,
                                weight: .semibold
                            )
                        )
                        .foregroundStyle(.white)
                        .padding(
                            .horizontal,
                            14
                        )
                        .padding(
                            .vertical,
                            7
                        )
                        .background(
                            Color.white.opacity(0.18)
                        )
                        .clipShape(Capsule())
                    }

                    // MARK: - Lista de niveles

                    if showLevels {

                        VStack(spacing: 6) {

                            ScrollView(
                                .vertical,
                                showsIndicators: true
                            ) {

                                VStack(spacing: 7) {

                                    ForEach(
                                        Array(
                                            levelNames.enumerated()
                                        ),
                                        id: \.offset
                                    ) { index, name in

                                        let level = index + 1

                                        HStack {

                                            ZStack {

                                                Circle()
                                                    .fill(
                                                        level ==
                                                        appState.currentLevel
                                                        ? Color.white.opacity(0.25)
                                                        : Color.white.opacity(0.10)
                                                    )
                                                    .frame(
                                                        width: 28,
                                                        height: 28
                                                    )

                                                Text(
                                                    "\(level)"
                                                )
                                                .font(
                                                    .system(
                                                        size: 11,
                                                        weight: .bold
                                                    )
                                                )
                                                .foregroundStyle(
                                                    .white
                                                )
                                            }

                                            Text(name)
                                                .font(
                                                    .system(
                                                        size: 12,
                                                        weight:
                                                            level ==
                                                            appState.currentLevel
                                                            ? .bold
                                                            : .medium
                                                    )
                                                )
                                                .foregroundStyle(
                                                    .white
                                                )

                                            Spacer()

                                            if level ==
                                                appState.currentLevel {

                                                Image(
                                                    systemName: "checkmark"
                                                )
                                                .font(
                                                    .system(
                                                        size: 11,
                                                        weight: .bold
                                                    )
                                                )
                                                .foregroundStyle(
                                                    .white
                                                )
                                            }
                                        }
                                        .padding(
                                            .horizontal,
                                            10
                                        )
                                        .padding(
                                            .vertical,
                                            4
                                        )
                                    }
                                }
                            }
                            .frame(maxHeight: 175)

                            // Indicador de scroll

                            HStack(spacing: 5) {

                                Text(
                                    "Desliza para ver más"
                                )
                                .font(
                                    .system(
                                        size: 10,
                                        weight: .medium
                                    )
                                )

                                Image(
                                    systemName: "chevron.down"
                                )
                                .font(
                                    .system(
                                        size: 9,
                                        weight: .bold
                                    )
                                )
                            }
                            .foregroundStyle(
                                Color.white.opacity(0.75)
                            )
                        }
                        .padding(10)
                        .background(
                            Color.black.opacity(0.10)
                        )
                        .clipShape(
                            RoundedRectangle(
                                cornerRadius: 15
                            )
                        )
                        .padding(
                            .horizontal,
                            40
                        )
                    }

                    Spacer()
                        .frame(height: 15)
                }
                .frame(maxWidth: .infinity)
            }
            .frame(
                minHeight:
                    showLevels
                    ? 450
                    : 270
            )
        }
    }

    // MARK: - ESTADÍSTICAS

    private var statsSection: some View {

        HStack(spacing: 9) {

            statCard(
                icon: "flame.fill",
                value: "\(appState.streak)",
                title: "Racha",
                color: .red
            ) {

                selectedInfo = .streak
            }

            statCard(
                icon: "checkmark",
                value: "\(appState.lessonsCompleted)",
                title: "Lecciones",
                color: turquoise
            ) {

                selectedInfo = .lessons
            }

            statCard(
                icon: "dollarsign.circle.fill",
                value: "\(appState.coins)",
                title: "Monedas",
                color: .yellow
            ) {

                selectedInfo = .coins
            }
        }
    }

    private func statCard(
        icon: String,
        value: String,
        title: String,
        color: Color,
        action: @escaping () -> Void
    ) -> some View {

        Button {

            action()

        } label: {

            VStack(spacing: 5) {

                Image(systemName: icon)
                    .font(
                        .system(
                            size: 16,
                            weight: .semibold
                        )
                    )
                    .foregroundStyle(color)

                Text(value)
                    .font(
                        .system(
                            size: 16,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(.primary)

                Text(title)
                    .font(
                        .system(size: 10)
                    )
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 82)
            .background(.white)
            .clipShape(
                RoundedRectangle(
                    cornerRadius: 15
                )
            )
            .shadow(
                color: .black.opacity(0.03),
                radius: 5,
                x: 0,
                y: 2
            )
        }
    }

    // MARK: - PROGRESO

    private var progressSection: some View {

        VStack(
            alignment: .leading,
            spacing: 9
        ) {

            HStack {

                Text(
                    "Nivel \(appState.currentLevel)"
                )
                .font(
                    .system(
                        size: 14,
                        weight: .bold
                    )
                )

                Spacer()

                Text(
                    "\(Int(appState.levelProgress * 100))%"
                )
                .font(
                    .system(
                        size: 13,
                        weight: .bold
                    )
                )
                .foregroundStyle(turquoise)
            }

            ProgressView(
                value: appState.levelProgress
            )
            .tint(turquoise)
            .scaleEffect(
                x: 1,
                y: 1.6,
                anchor: .center
            )

            Text(
                "\(appState.xpRemaining) XP para el Nivel \(appState.currentLevel + 1)"
            )
            .font(
                .system(size: 11)
            )
            .foregroundStyle(
                .secondary
            )
        }
        .padding(14)
        .background(.white)
        .clipShape(
            RoundedRectangle(
                cornerRadius: 15
            )
        )
        .shadow(
            color: .black.opacity(0.03),
            radius: 5,
            x: 0,
            y: 2
        )
    }

    // MARK: - LOGROS

    private var achievementsSection: some View {

        VStack(
            alignment: .leading,
            spacing: 10
        ) {

            Text("Logros")
                .font(
                    .system(
                        size: 15,
                        weight: .bold
                    )
                )

            ScrollView(
                .horizontal,
                showsIndicators: false
            ) {

                HStack(spacing: 10) {

                    achievementCard(
                        icon: "star.fill",
                        title: "Primeros pasos",
                        color: .yellow,
                        completed: true
                    )

                    achievementCard(
                        icon: "flame.fill",
                        title: "5 días seguidos",
                        color: .red,
                        completed: true
                    )

                    achievementCard(
                        icon: "lock.fill",
                        title: "Por descubrir",
                        color: .gray,
                        completed: false
                    )

                    achievementCard(
                        icon: "flame.fill",
                        title: "10 días seguidos",
                        color: .orange,
                        completed: false
                    )

                    achievementCard(
                        icon: "book.fill",
                        title: "10 lecciones",
                        color: .purple,
                        completed: false
                    )

                    achievementCard(
                        icon: "trophy.fill",
                        title: "Todas las lecciones",
                        color: .yellow,
                        completed: false
                    )
                }
            }
        }
    }

    private func achievementCard(
        icon: String,
        title: String,
        color: Color,
        completed: Bool
    ) -> some View {

        VStack(spacing: 7) {

            ZStack {

                Circle()
                    .fill(
                        completed
                        ? color.opacity(0.13)
                        : Color.gray.opacity(0.08)
                    )
                    .frame(
                        width: 45,
                        height: 45
                    )

                Image(systemName: icon)
                    .font(
                        .system(size: 17)
                    )
                    .foregroundStyle(
                        completed
                        ? color
                        : Color.gray.opacity(0.45)
                    )
            }

            Text(title)
                .font(
                    .system(
                        size: 9,
                        weight: .medium
                    )
                )
                .foregroundStyle(
                    completed
                    ? Color.primary
                    : Color.secondary
                )
                .multilineTextAlignment(
                    .center
                )
                .lineLimit(2)
        }
        .frame(
            width: 94,
            height: 85
        )
        .background(.white)
        .clipShape(
            RoundedRectangle(
                cornerRadius: 15
            )
        )
        .opacity(
            completed ? 1 : 0.65
        )
    }

    // MARK: - NIVELES

    private var currentLevelName: String {

        guard
            appState.currentLevel > 0,
            appState.currentLevel <= levelNames.count
        else {

            return "Explorador"
        }

        return levelNames[
            appState.currentLevel - 1
        ]
    }

    private let levelNames = [

        "Semilla",
        "Brote",
        "Explorador",
        "Guardián de Palabras",
        "Caminante Yuhmu",
        "Voz del Maíz",
        "Tejedor de Historias",
        "Sabio de Ixtenco",
        "Guardián Yuhmu",
        "Maestro Zentemi"
    ]

    // MARK: - EDITOR DE PERFIL

    private var avatarEditor: some View {

        NavigationStack {

            ScrollView {

                VStack(spacing: 25) {

                    // MARK: - VISTA PREVIA COMPLETA

                    ZStack {

                        // Fondo del perfil

                        LinearGradient(
                            colors: [

                                Color(
                                    hue: appState.profileBackgroundHue,
                                    saturation: 0.72,
                                    brightness: 0.72
                                ),

                                Color(
                                    hue: appState.profileBackgroundHue,
                                    saturation: 0.64,
                                    brightness: 0.84
                                )
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )

                        // Círculos decorativos

                        Circle()
                            .fill(
                                Color.white.opacity(0.06)
                            )
                            .frame(
                                width: 140,
                                height: 140
                            )
                            .offset(
                                x: -130,
                                y: -70
                            )

                        Circle()
                            .fill(
                                Color.white.opacity(0.035)
                            )
                            .frame(
                                width: 180,
                                height: 180
                            )
                            .offset(
                                x: 120,
                                y: 70
                            )

                        // Avatar + usuario

                        VStack(spacing: 10) {

                            ZStack {

                                RoundedRectangle(
                                    cornerRadius: 16
                                )
                                .fill(
                                    Color(
                                        hue: appState.avatarBackgroundHue,
                                        saturation: 0.22,
                                        brightness: 1
                                    )
                                )
                                .frame(
                                    width: 95,
                                    height: 95
                                )

                                // CAMBIO: mascota + accesorios
                                PetView(size: 78)
                            }

                            Text(appState.userName)
                                .font(
                                    .system(
                                        size: 20,
                                        weight: .bold
                                    )
                                )
                                .foregroundStyle(.white)

                            Text(
                                "Nivel \(appState.currentLevel) — \(currentLevelName)"
                            )
                            .font(
                                .system(
                                    size: 11,
                                    weight: .semibold
                                )
                            )
                            .foregroundStyle(.white)
                            .padding(
                                .horizontal,
                                12
                            )
                            .padding(
                                .vertical,
                                6
                            )
                            .background(
                                Color.white.opacity(0.18)
                            )
                            .clipShape(Capsule())
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 260)
                    .clipShape(
                        RoundedRectangle(
                            cornerRadius: 24
                        )
                    )
                    .padding(.top, 15)

                    // MARK: - Color fondo perfil

                    VStack(
                        alignment: .leading,
                        spacing: 8
                    ) {

                        Text(
                            "Color del fondo del perfil"
                        )
                        .font(
                            .system(
                                size: 15,
                                weight: .semibold
                            )
                        )

                        Slider(
                            value:
                                $appState.profileBackgroundHue,
                            in: 0...1
                        )
                    }

                    // MARK: - Fondo de la mascota

                    VStack(
                        alignment: .leading,
                        spacing: 8
                    ) {

                        Text(
                            "Color del fondo de la mascota"
                        )
                        .font(
                            .system(
                                size: 15,
                                weight: .semibold
                            )
                        )

                        Slider(
                            value:
                                $appState.avatarBackgroundHue,
                            in: 0...1
                        )
                    }

                    // MARK: - Color mascota

                    VStack(
                        alignment: .leading,
                        spacing: 8
                    ) {

                        Text(
                            "Color de Maicito"
                        )
                        .font(
                            .system(
                                size: 15,
                                weight: .semibold
                            )
                        )

                        Slider(
                            value:
                                $appState.mascotHue,
                            in: 0...1
                        )
                    }

                    // MARK: - Nombre mascota

                    VStack(
                        alignment: .leading,
                        spacing: 8
                    ) {

                        Text(
                            "Nombre de tu mascota"
                        )
                        .font(
                            .system(
                                size: 15,
                                weight: .semibold
                            )
                        )

                        TextField(
                            "Nombre",
                            text: $appState.petName
                        )
                        .padding()
                        .background(
                            Color.gray.opacity(0.08)
                        )
                        .clipShape(
                            RoundedRectangle(
                                cornerRadius: 14
                            )
                        )
                    }
                    // MARK: - Nombre usuario

                    VStack(
                        alignment: .leading,
                        spacing: 8
                    ) {

                        Text("Nombre de usuario")
                            .font(
                                .system(
                                    size: 15,
                                    weight: .semibold
                                )
                            )

                        TextField(
                            "Nombre",
                            text: $editedUserName
                        )
                        .padding()
                        .background(
                            Color.gray.opacity(0.08)
                        )
                        .clipShape(
                            RoundedRectangle(
                                cornerRadius: 14
                            )
                        )
                        .disabled(!appState.canChangeUserName)

                        if appState.canChangeUserName {

                            Button("Guardar nombre") {

                                appState.changeUserName(
                                    to: editedUserName
                                )
                            }
                            .font(
                                .system(
                                    size: 14,
                                    weight: .semibold
                                )
                            )

                        } else {

                            Text(
                                "Podrás cambiar tu nombre nuevamente en \(appState.daysUntilUserNameChange) días."
                            )
                            .font(.system(size: 12))
                            .foregroundStyle(.secondary)
                        }
                    }
                    Spacer()
                        .frame(height: 20)
                }
                .padding(.horizontal, 25)
            }
            .onAppear {
                editedUserName = appState.userName
            }
            .navigationTitle(
                "Personalizar perfil"
            )
            .navigationBarTitleDisplayMode(
                .inline
            )
            .toolbar {

                ToolbarItem(
                    placement: .confirmationAction
                ) {

                    Button("Listo") {

                        showAvatarEditor = false
                    }
                }
            }
        }
    }

    // MARK: - INFO ESTADÍSTICAS

    private func infoSheet(
        _ info: ProfileInfo
    ) -> some View {

        VStack(spacing: 18) {

            Spacer()

            Image(
                systemName: info.icon
            )
            .font(.system(size: 48))
            .foregroundStyle(info.color)

            Text(info.title)
                .font(
                    .system(
                        size: 26,
                        weight: .bold
                    )
                )

            Text(
                info.description(appState)
            )
            .font(
                .system(size: 15)
            )
            .foregroundStyle(.secondary)
            .multilineTextAlignment(
                .center
            )
            .padding(.horizontal, 30)

            Spacer()
        }
        .presentationDetents([.medium])
    }
}

// MARK: - INFO

enum ProfileInfo: Identifiable {

    case streak
    case lessons
    case coins

    var id: String {

        String(describing: self)
    }

    var title: String {

        switch self {

        case .streak:
            return "Racha"

        case .lessons:
            return "Lecciones"

        case .coins:
            return "Monedas"
        }
    }

    var icon: String {

        switch self {

        case .streak:
            return "flame.fill"

        case .lessons:
            return "checkmark.circle.fill"

        case .coins:
            return "dollarsign.circle.fill"
        }
    }

    var color: Color {

        switch self {

        case .streak:
            return .red

        case .lessons:
            return Color(
                red: 0.28,
                green: 0.78,
                blue: 0.75
            )

        case .coins:
            return .yellow
        }
    }

    func description(
        _ appState: AppState
    ) -> String {

        switch self {

        case .streak:

            return """
            Llevas \(appState.streak) días consecutivos practicando Yuhmu.

            Practica cada día para mantener tu racha.
            """

        case .lessons:

            return """
            Has completado \(appState.lessonsCompleted) de \(appState.totalLessons) lecciones disponibles.
            """

        case .coins:

            return """
            Actualmente tienes \(appState.coins) monedas.

            Puedes conseguir más completando lecciones y actividades.
            """
        }
    }
}

#Preview {

    ProfileView()
        .environmentObject(AppState())
}
