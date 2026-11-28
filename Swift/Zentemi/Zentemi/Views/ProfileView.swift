import SwiftUI

struct ProfileView: View {

    @EnvironmentObject var appState: AppState
    @ObservedObject private var authService = AuthService.shared

    @State private var showAvatarEditor = false
    @State private var showSettings = false
    @State private var showLevels = false
    @State private var selectedInfo: ProfileInfo?
    @State private var editedUserName = ""

    // Gestión de cuenta (Modo Invitado / Nube)
    @State private var showCreateAccount = false
    @State private var showSignIn = false
    @State private var showEmailVerification = false
    @State private var showLogoutConfirmation = false

    private let purple = Color(
        red: 0.48,
        green: 0.29,
        blue: 0.79
    )

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

                        accountSection

                        statsSection

                        progressSection

                        achievementsSection

                        Spacer()
                            .frame(height: 95)
                    }
                    .padding(.horizontal, 18)
                    .padding(.top, 14)
                }
            }
        }
        .onAppear {
            if authService.isAuthenticated && !authService.isEmailVerified {
                Task {
                    _ = try? await authService.reloadAndCheckVerification()
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

        // MARK: - Crear Cuenta / Iniciar Sesión / Verificar Correo

        .sheet(isPresented: $showCreateAccount) {
            NavigationStack {
                CreateAccountView()
                    .environmentObject(appState)
            }
        }
        .sheet(isPresented: $showSignIn) {
            NavigationStack {
                SignInView()
                    .environmentObject(appState)
            }
        }
        .sheet(isPresented: $showEmailVerification) {
            NavigationStack {
                EmailVerificationView()
            }
        }
        .alert("¿Cerrar sesión?", isPresented: $showLogoutConfirmation) {
            Button("Cancelar", role: .cancel) { }
            Button("Cerrar sesión", role: .destructive) {
                try? authService.signOut()
            }
        } message: {
            Text("Tu progreso se queda en este teléfono y puedes seguir jugando. Para volver a guardarlo en la nube, inicia sesión otra vez.")
        }

        // MARK: - Información estadísticas

        .sheet(
            item: $selectedInfo
        ) { info in

            infoSheet(info)
        }
    }

    // MARK: - CUENTA (MODO INVITADO / NUBE)

    @ViewBuilder
    private var accountSection: some View {
        if appState.isGuest || appState.accountEmail == nil {
            saveProgressCard
        } else if let email = appState.accountEmail {
            accountCard(email: email)
        }
    }

    private var saveProgressCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 10) {
                Image(systemName: "icloud.and.arrow.up.fill")
                    .font(.system(size: 22))
                    .foregroundStyle(purple)

                Text("Guardar mi progreso")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(.primary)
            }

            Text("Ahora tu progreso está guardado solo en este teléfono. Pídele a un adulto que cree una cuenta para guardarlo en la nube.")
                .font(.system(size: 13))
                .foregroundStyle(.secondary)
                .fixedSize(horizontal: false, vertical: true)

            HStack(spacing: 10) {
                Button {
                    showCreateAccount = true
                } label: {
                    Text("Crear cuenta")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 11)
                        .background(purple)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                }

                Button {
                    showSignIn = true
                } label: {
                    Text("Iniciar sesión")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(purple)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 11)
                        .background(Color.clear)
                        .overlay {
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(purple.opacity(0.6), lineWidth: 1.5)
                        }
                }
            }
            .padding(.top, 2)
        }
        .padding(16)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .shadow(color: Color.black.opacity(0.03), radius: 6, y: 2)
    }

    private func accountCard(email: String) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 10) {
                Image(systemName: "checkmark.icloud.fill")
                    .font(.system(size: 24))
                    .foregroundStyle(turquoise)

                VStack(alignment: .leading, spacing: 2) {
                    Text("Progreso guardado en la nube")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundStyle(.primary)

                    Text(email)
                        .font(.system(size: 13))
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }

                Spacer()
            }

            if authService.isEmailVerified {
                HStack(spacing: 6) {
                    Image(systemName: "checkmark.seal.fill")
                        .font(.system(size: 15))
                        .foregroundStyle(.green)

                    Text("Correo verificado")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundStyle(.secondary)
                }
            } else {
                Button {
                    showEmailVerification = true
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: "envelope.badge.fill")
                            .font(.system(size: 17))
                            .foregroundStyle(.orange)

                        Text("Falta confirmar el correo del adulto. Toca aquí para verificarlo.")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundStyle(.primary)
                            .multilineTextAlignment(.leading)

                        Spacer()
                    }
                    .padding(11)
                    .background(Color.yellow.opacity(0.18))
                    .clipShape(RoundedRectangle(cornerRadius: 13))
                }
                .buttonStyle(.plain)
            }

            Button {
                showLogoutConfirmation = true
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: "rectangle.portrait.and.arrow.right")
                        .font(.system(size: 14, weight: .semibold))
                    Text("Cerrar sesión")
                        .font(.system(size: 14, weight: .bold))
                }
                .foregroundStyle(.red)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .overlay {
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(Color.red.opacity(0.45), lineWidth: 1.2)
                }
            }
            .padding(.top, 2)
        }
        .padding(16)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .shadow(color: Color.black.opacity(0.03), radius: 6, y: 2)
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

                    Button {
                        showAvatarEditor = true
                    } label: {
                        ZStack {
                            Circle()
                                .fill(
                                    Color(
                                        hue: appState.avatarBackgroundHue,
                                        saturation: 0.22,
                                        brightness: 1
                                    )
                                )

                            if let backdrop = appState.equippedBackdrop?.backdrop {
                                MaicitoBackdropView(backdrop: backdrop)
                            }

                            PetView(size: 78)
                        }
                        .frame(width: 100, height: 100)
                        .clipShape(Circle())
                        .overlay(
                            Circle()
                                .stroke(Color.white.opacity(0.75), lineWidth: 3)
                        )
                    }
                    .buttonStyle(.plain)

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
        HStack(spacing: 10) {
            statCard(
                icon: "ic_racha",
                value: "\(appState.streak)",
                title: "Racha"
            ) {
                selectedInfo = .streak
            }

            statCard(
                icon: "ic_primera_leccion",
                value: "\(appState.lessonsCompleted)",
                title: "Lecciones"
            ) {
                selectedInfo = .lessons
            }

            statCard(
                icon: "ic_moneda",
                value: "\(appState.coins)",
                title: "Monedas"
            ) {
                selectedInfo = .coins
            }
        }
    }

    private func statCard(
        icon: String,
        value: String,
        title: String,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            VStack(spacing: 4) {
                ZentemiIconView(name: icon, size: 30)

                Text(value)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(.primary)

                Text(title)
                    .font(.system(size: 11))
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .background(.white)
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .shadow(color: .black.opacity(0.03), radius: 5, y: 2)
        }
        .buttonStyle(.plain)
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
                    "\(appState.currentLevelXP) / \(appState.xpRequiredPerLevel) XP"
                )
                .font(
                    .system(
                        size: 12,
                        weight: .semibold
                    )
                )
                .foregroundStyle(.secondary)
            }

            ProgressView(
                value: appState.levelProgress
            )
            .tint(purple)
            .scaleEffect(
                x: 1,
                y: 1.6,
                anchor: .center
            )

            Text(
                "Faltan \(appState.xpRemaining) XP para el siguiente nivel"
            )
            .font(
                .system(size: 11)
            )
            .foregroundStyle(
                .secondary
            )
        }
        .padding(16)
        .background(.white)
        .clipShape(
            RoundedRectangle(
                cornerRadius: 20
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
        ProfileAchievementsSection(achievements: appState.profileAchievements)
    }

    // MARK: - NIVELES

    private var currentLevelName: String {

        guard
            appState.currentLevel > 0,
            appState.currentLevel <= levelNames.count
        else {

            return "Semilla"
        }

        return levelNames[
            appState.currentLevel - 1
        ]
    }

    private let levelNames = [
        "Semilla",
        "Brote",
        "Raíz",
        "Tallo",
        "Hoja",
        "Flor",
        "Fruto",
        "Árbol",
        "Guardián del Yuhmu",
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
                                Circle()
                                    .fill(
                                        Color(
                                            hue: appState.avatarBackgroundHue,
                                            saturation: 0.22,
                                            brightness: 1
                                        )
                                    )

                                if let backdrop = appState.equippedBackdrop?.backdrop {
                                    MaicitoBackdropView(backdrop: backdrop)
                                }

                                PetView(size: 74)
                            }
                            .frame(width: 96, height: 96)
                            .clipShape(Circle())
                            .overlay(
                                Circle()
                                    .stroke(Color.white.opacity(0.75), lineWidth: 3)
                            )

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

                        if appState.equippedBackdrop != nil {
                            Text("Tienes un fondo de la tienda puesto: se ve encima del color de fondo de la mascota.")
                                .font(.system(size: 12))
                                .foregroundStyle(.secondary)
                        }
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
