import SwiftUI

/// Pantalla de Inicio renovada: saludo personalizado, paisaje suave con milpa (`HomeSceneView`),
/// Maicito con su burbuja de nombre y fondo comprado, tarjeta de felicidad y botón "Comenzar lección".
/// Espejo exacto de `HomeScreen.kt` en Android v1.1.
struct HomeView: View {

    @EnvironmentObject var appState: AppState

    @State private var showCoinsInfo = false
    @State private var showStreakInfo = false
    @State private var mascotScaleY: CGFloat = 1.0
    @State private var glowScale: CGFloat = 0.94
    @State private var animateBackground = false
    @State private var showLearningPath = false
    @State private var showFeedingScreen = false
    @State private var showGamesHub = false
    @State private var showStreakCelebration = false

    @AppStorage("zentemi.lastCelebratedStreakDate")
    private var lastCelebratedStreakDate: Double = 0

    private let coinYellow = Color(red: 0.949, green: 0.718, blue: 0.020)
    private let streakRed = Color(red: 0.941, green: 0.282, blue: 0.243)

    var body: some View {
        ZStack {
            background

            VStack(spacing: 0) {
                // MARK: - Encabezado: Saludo (izquierda) + Monedas y Racha (derecha)
                HStack(alignment: .center, spacing: 8) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("¡Hola, \(appState.userName)!")
                            .font(.system(size: 22, weight: .bold))
                            .foregroundStyle(GameTheme.purpleDark)
                            .lineLimit(1)

                        Text("Qué gusto verte hoy")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundStyle(GameTheme.subText.opacity(0.85))
                    }

                    Spacer(minLength: 6)

                    statBadge(
                        icon: "ic_moneda",
                        value: "\(appState.coins)",
                        tint: Color(red: 1.0, green: 0.945, blue: 0.780),
                        textColor: Color(red: 0.400, green: 0.302, blue: 0.051)
                    ) {
                        showCoinsInfo = true
                    }

                    statBadge(
                        icon: "ic_racha",
                        value: "\(appState.streak)",
                        tint: Color(red: 1.0, green: 0.886, blue: 0.867),
                        textColor: Color(red: 0.761, green: 0.212, blue: 0.173)
                    ) {
                        showStreakInfo = true
                    }
                }
                .padding(.leading, 22)
                .padding(.trailing, 18)
                .padding(.top, 12)
                .padding(.bottom, 4)

                // MARK: - Paisaje + Maicito + Burbuja de nombre
                ZStack {
                    if appState.equippedBackdrop == nil {
                        HomeSceneView()
                    }

                    VStack(spacing: 4) {
                        nameBubble(name: appState.petName)

                        ZStack {
                            Circle()
                                .fill(
                                    RadialGradient(
                                        colors: [
                                            Color(red: 0.957, green: 0.769, blue: 0.416).opacity(0.35),
                                            GameTheme.purple.opacity(0.06),
                                            Color.clear
                                        ],
                                        center: .center,
                                        startRadius: 10,
                                        endRadius: 125
                                    )
                                )
                                .frame(width: 250, height: 250)
                                .scaleEffect(glowScale)

                            if let backdrop = appState.equippedBackdrop?.backdrop {
                                MaicitoBackdropView(backdrop: backdrop)
                                    .frame(width: 215, height: 215)
                                    .clipShape(Circle())
                                    .overlay(
                                        Circle()
                                            .stroke(Color.white, lineWidth: 4)
                                    )
                            }

                            Ellipse()
                                .fill(GameTheme.purple.opacity(0.13))
                                .frame(width: 120, height: 16)
                                .offset(y: 92)

                            PetView(size: 190)
                                .scaleEffect(x: 1.0, y: mascotScaleY, anchor: .bottom)
                        }
                        .frame(width: 250, height: 250)
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)

                // MARK: - Tarjeta de Felicidad
                VStack(alignment: .leading, spacing: 14) {
                    HStack {
                        Text("Felicidad de \(appState.petName)")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(GameTheme.darkText)

                        Spacer()

                        Text("\(appState.happiness)%")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundStyle(GameTheme.turquoise)
                    }

                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            RoundedRectangle(cornerRadius: 4)
                                .fill(GameTheme.turquoise.opacity(0.15))

                            RoundedRectangle(cornerRadius: 4)
                                .fill(GameTheme.turquoise)
                                .frame(width: geo.size.width * CGFloat(min(max(appState.happiness, 0), 100)) / 100)
                        }
                    }
                    .frame(height: 8)

                    HStack(spacing: 12) {
                        Button {
                            showFeedingScreen = true
                        } label: {
                            Text("Alimentar a \(appState.petName)")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundStyle(GameTheme.purpleDark)
                                .multilineTextAlignment(.center)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .padding(.horizontal, 6)
                                .background(GameTheme.purple.opacity(0.10))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        }

                        Button {
                            showGamesHub = true
                        } label: {
                            Text("Jugar con \(appState.petName)")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundStyle(GameTheme.purpleDark)
                                .multilineTextAlignment(.center)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .padding(.horizontal, 6)
                                .background(GameTheme.purple.opacity(0.10))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                    }
                }
                .padding(18)
                .background(
                    RoundedRectangle(cornerRadius: 22)
                        .fill(Color.white.opacity(0.97))
                        .shadow(color: GameTheme.purple.opacity(0.14), radius: 10, y: 5)
                )
                .padding(.horizontal, 22)

                Spacer().frame(height: 16)

                // MARK: - Botón Comenzar Lección
                Button {
                    showLearningPath = true
                } label: {
                    HStack(spacing: 10) {
                        ZentemiIconView(name: "ic_comenzar_leccion", size: 30)
                        Text("Comenzar lección")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(.white)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .background(GameTheme.purple)
                    .clipShape(RoundedRectangle(cornerRadius: 28))
                    .shadow(color: GameTheme.purple.opacity(0.32), radius: 8, y: 4)
                }
                .padding(.horizontal, 30)

                Spacer().frame(height: 92)
            }

            // MARK: - Ventanas Informativas
            if showCoinsInfo {
                infoOverlay(
                    title: "Monedas",
                    icon: "ic_moneda",
                    text: "Consigue monedas completando lecciones, retos y actividades.\n\nPuedes utilizarlas para obtener objetos y recompensas dentro de Zentemi.",
                    color: coinYellow
                ) {
                    showCoinsInfo = false
                }
            }

            if showStreakInfo {
                infoOverlay(
                    title: "Racha",
                    icon: "ic_racha",
                    text: "Tu racha representa los días consecutivos que has practicado Yuhmu.\n\nCompleta actividades cada día para aumentarla y mantenerla.",
                    color: streakRed
                ) {
                    showStreakInfo = false
                }
            }

            if showStreakCelebration {
                streakCelebrationOverlay
                    .zIndex(100)
                    .transition(.opacity.combined(with: .scale(scale: 0.85)))
            }
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 1.6).repeatForever(autoreverses: true)) {
                mascotScaleY = 1.06
            }
            withAnimation(.easeInOut(duration: 2.6).repeatForever(autoreverses: true)) {
                glowScale = 1.06
            }
            withAnimation(.easeInOut(duration: 6.0).repeatForever(autoreverses: true)) {
                animateBackground = true
            }
            checkStreakCelebration()
        }
        .onChange(of: appState.streak) { _, _ in
            checkStreakCelebration()
        }
        .fullScreenCover(isPresented: $showLearningPath) {
            LearningPathView()
                .environmentObject(appState)
        }
        .fullScreenCover(isPresented: $showFeedingScreen) {
            FeedMaicitoView()
                .environmentObject(appState)
        }
        .fullScreenCover(isPresented: $showGamesHub) {
            GamesHubView()
                .environmentObject(appState)
        }
    }

    // MARK: - Componentes de Inicio

    private func nameBubble(name: String) -> some View {
        let border = GameTheme.purple.opacity(0.30)
        return ZStack(alignment: .bottom) {
            Rectangle()
                .fill(Color.white)
                .frame(width: 14, height: 14)
                .overlay(Rectangle().stroke(border, lineWidth: 2))
                .rotationEffect(.degrees(45))
                .offset(y: 6)

            Text(name)
                .font(.system(size: 24, weight: .bold))
                .foregroundStyle(GameTheme.purpleDark)
                .padding(.horizontal, 26)
                .padding(.vertical, 6)
                .background(Color.white)
                .clipShape(Capsule())
                .overlay(Capsule().stroke(border, lineWidth: 2))
                .shadow(color: GameTheme.purple.opacity(0.16), radius: 6, y: 2)
        }
    }

    private func statBadge(
        icon: String,
        value: String,
        tint: Color,
        textColor: Color,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            HStack(spacing: 5) {
                ZentemiIconView(name: icon, size: 26)
                Text(value)
                    .font(.system(size: 16, weight: .heavy))
                    .foregroundStyle(textColor)
            }
            .padding(.leading, 8)
            .padding(.trailing, 12)
            .padding(.vertical, 6)
            .background(tint)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .shadow(color: textColor.opacity(0.18), radius: 3, y: 2)
        }
        .buttonStyle(.plain)
    }

    private func infoOverlay(
        title: String,
        icon: String,
        text: String,
        color: Color,
        close: @escaping () -> Void
    ) -> some View {
        ZStack {
            Color.black.opacity(0.28)
                .ignoresSafeArea()
                .onTapGesture { close() }

            VStack(spacing: 14) {
                HStack(spacing: 8) {
                    ZentemiIconView(name: icon, size: 34)
                    Text(title)
                        .font(.system(size: 23, weight: .bold))
                        .foregroundStyle(color)
                }

                Text(text)
                    .font(.system(size: 15, weight: .medium))
                    .foregroundStyle(GameTheme.subText)
                    .multilineTextAlignment(.center)

                Button(action: close) {
                    Text("Entendido")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundStyle(GameTheme.darkText)
                        .padding(.horizontal, 25)
                        .padding(.vertical, 10)
                        .background(color.opacity(0.15))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .padding(25)
            .frame(maxWidth: 320)
            .background(color.opacity(0.12))
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 22))
            .overlay(
                RoundedRectangle(cornerRadius: 22)
                    .stroke(color.opacity(0.40), lineWidth: 2)
            )
            .shadow(color: .black.opacity(0.15), radius: 15)
        }
    }

    private func checkStreakCelebration() {
        guard appState.streak > 0 else { return }
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "America/Mexico_City")!
        let today = calendar.startOfDay(for: Date())
        let lastDate = Date(timeIntervalSince1970: lastCelebratedStreakDate)
        guard !calendar.isDate(lastDate, inSameDayAs: today) else { return }

        withAnimation(.spring(response: 0.5, dampingFraction: 0.75)) {
            showStreakCelebration = true
        }
    }

    private var streakCelebrationOverlay: some View {
        ZStack {
            Color.black.opacity(0.65)
                .ignoresSafeArea()

            VStack(spacing: 18) {
                ZentemiIconView(name: "ic_racha", size: 58)

                Text("¡Felicidades!")
                    .font(.system(size: 30, weight: .heavy))
                    .foregroundStyle(GameTheme.purple)

                Text("¡Llevas \(appState.streak) días de racha!")
                    .font(.system(size: 21, weight: .bold))
                    .multilineTextAlignment(.center)

                ZStack {
                    ForEach(0..<12, id: \.self) { index in
                        Image(systemName: "sparkle")
                            .foregroundStyle(index.isMultiple(of: 2) ? .yellow : .orange)
                            .offset(
                                x: CGFloat((index % 4) * 65 - 95),
                                y: CGFloat((index / 4) * 70 - 70)
                            )
                    }

                    PetView(size: 175)
                        .environmentObject(appState)
                }
                .frame(height: 210)

                Text("¡Sigue así! \(appState.petName) está orgulloso de ti.")
                    .font(.system(size: 16, weight: .medium))
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.secondary)

                Button {
                    var calendar = Calendar(identifier: .gregorian)
                    calendar.timeZone = TimeZone(identifier: "America/Mexico_City")!
                    lastCelebratedStreakDate = calendar.startOfDay(for: Date()).timeIntervalSince1970
                    withAnimation(.easeOut(duration: 0.25)) {
                        showStreakCelebration = false
                    }
                } label: {
                    Text("¡Continuar!")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 15)
                        .background(GameTheme.purple)
                        .clipShape(RoundedRectangle(cornerRadius: 15))
                }
            }
            .padding(25)
            .frame(maxWidth: 340)
            .background(.white)
            .clipShape(RoundedRectangle(cornerRadius: 25))
            .shadow(radius: 20)
            .padding(.horizontal, 20)
        }
    }

    @ViewBuilder
    private var background: some View {
        if let backdrop = appState.equippedBackdrop?.backdrop {
            MaicitoBackdropView(backdrop: backdrop)
                .ignoresSafeArea()
                .allowsHitTesting(false)
                .accessibilityHidden(true)
        } else if let background = appState.equippedHomeBackground {
            GeometryReader { geometry in
                Image(background.imageName)
                    .resizable()
                    .scaledToFill()
                    .frame(width: geometry.size.width, height: geometry.size.height)
                    .clipped()
            }
            .ignoresSafeArea()
            .allowsHitTesting(false)
            .accessibilityHidden(true)
        } else {
            defaultBackground
        }
    }

    private var defaultBackground: some View {

        ZStack {
            GameTheme.backgroundLight

            Circle()
                .fill(GameTheme.purple.opacity(0.06))
                .frame(width: 220, height: 220)
                .offset(
                    x: animateBackground ? 140 : 170,
                    y: animateBackground ? -280 : -310
                )

            Circle()
                .fill(GameTheme.turquoise.opacity(0.07))
                .frame(width: 150, height: 150)
                .offset(
                    x: animateBackground ? -145 : -175,
                    y: animateBackground ? -40 : -80
                )
        }
        .ignoresSafeArea()
    }
}

#Preview {
    HomeView()
        .environmentObject(AppState())
}
