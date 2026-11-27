import SwiftUI

// MARK: - Paleta y Constantes de Juegos (Espejo de GameCommon.kt y Color.kt)

enum GameTheme {
    static let purple = Color(red: 0.478, green: 0.290, blue: 0.792)           // #7A4ACA
    static let purpleDark = Color(red: 0.341, green: 0.169, blue: 0.639)       // #572BA3
    static let purpleGradStart = Color(red: 0.471, green: 0.278, blue: 0.773)  // #7847C5
    static let purpleGradEnd = Color(red: 0.341, green: 0.169, blue: 0.639)    // #572BA3
    static let headerTop = Color(red: 0.353, green: 0.243, blue: 0.596)        // #5A3E98
    static let headerBottom = Color(red: 0.455, green: 0.345, blue: 0.690)     // #7458B0
    static let turquoise = Color(red: 0.161, green: 0.780, blue: 0.702)        // #29C7B3
    static let successGreen = Color(red: 0.298, green: 0.769, blue: 0.416)     // #4CC46A
    static let successGreenDark = Color(red: 0.180, green: 0.620, blue: 0.302) // #2E9E4D
    static let errorRed = Color(red: 1.0, green: 0.420, blue: 0.420)           // #FF6B6B
    static let backgroundLight = Color(red: 0.973, green: 0.953, blue: 0.988)  // #F8F3FC
    static let darkText = Color(red: 0.220, green: 0.200, blue: 0.275)         // #383346
    static let subText = Color(red: 0.302, green: 0.271, blue: 0.361)          // #4D455C
    static let lockedNode = Color(red: 0.878, green: 0.859, blue: 0.941)       // #E0DBF0
    static let bubbleLilac = Color(red: 0.929, green: 0.894, blue: 0.980)      // #EDE4FA
    static let marketOrange = Color(red: 1.0, green: 0.584, blue: 0.0)         // #FF9500

    static let happinessReward = 5
    static let coinsPerStar = 5

    /// Estrellas (0-3) según la proporción de aciertos, igual que en Android.
    static func starsFor(correct: Int, total: Int) -> Int {
        guard total > 0 else { return 0 }
        let ratio = Double(correct) / Double(total)
        if ratio >= 0.8 { return 3 }
        if ratio >= 0.5 { return 2 }
        if correct > 0 { return 1 }
        return 0
    }
}

// MARK: - Ícono Ilustrado de Zentemi (Espejo de ZentemiIcon.kt)

struct ZentemiIconView: View {
    let name: String
    var size: CGFloat = 24
    var muted: Bool = false

    var body: some View {
        Image(name)
            .resizable()
            .scaledToFit()
            .frame(width: size, height: size)
            .saturation(muted ? 0 : 1)
            .opacity(muted ? 0.45 : 1.0)
    }
}

// MARK: - Botón 3D estilo Juego (Espejo de ChunkyButton en ZentemiComponents.kt)

struct GameChunkyButtonStyle: ButtonStyle {
    let color: Color
    let lipColor: Color
    let textColor: Color
    let enabled: Bool
    let height: CGFloat

    func makeBody(configuration: Configuration) -> some View {
        let lip: CGFloat = 4
        let isPressed = configuration.isPressed && enabled
        let face = enabled ? color : Color(red: 0.812, green: 0.796, blue: 0.847)
        let under = enabled ? lipColor : Color(red: 0.706, green: 0.686, blue: 0.753)

        ZStack(alignment: .top) {
            RoundedRectangle(cornerRadius: 14)
                .fill(under)
                .frame(height: height)
                .offset(y: lip)

            configuration.label
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(textColor)
                .frame(maxWidth: .infinity)
                .frame(height: height)
                .background(
                    LinearGradient(
                        colors: [face.opacity(0.90), face],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .offset(y: isPressed ? lip : 0)
                .animation(.easeOut(duration: 0.09), value: isPressed)
        }
        .frame(height: height + lip)
    }
}

struct GameChunkyButton: View {
    let text: String
    var color: Color = GameTheme.purple
    var lipColor: Color = GameTheme.purpleDark
    var textColor: Color = .white
    var enabled: Bool = true
    var height: CGFloat = 50
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(text)
        }
        .buttonStyle(
            GameChunkyButtonStyle(
                color: color,
                lipColor: lipColor,
                textColor: textColor,
                enabled: enabled,
                height: height
            )
        )
        .disabled(!enabled)
    }
}

// MARK: - Etiqueta de Categoría (Espejo de TagChip)

struct GameTagChip: View {
    let text: String
    let color: Color

    var body: some View {
        Text(text.uppercased())
            .font(.system(size: 10, weight: .bold))
            .foregroundStyle(color)
            .padding(.horizontal, 9)
            .padding(.vertical, 3)
            .background(color.opacity(0.16))
            .clipShape(Capsule())
    }
}

// MARK: - Encabezado Morado (Espejo de WaveHeader)

struct GameWaveHeader<Content: View>: View {
    @ViewBuilder let content: () -> Content

    var body: some View {
        ZStack(alignment: .topTrailing) {
            LinearGradient(
                colors: [GameTheme.headerTop, GameTheme.headerBottom],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea(edges: .top)

            Circle()
                .fill(Color.white.opacity(0.09))
                .frame(width: 190, height: 190)
                .offset(x: 50, y: -40)

            HStack {
                content()
                Spacer(minLength: 0)
            }
            .padding(.horizontal, 20)
            .padding(.top, 10)
            .padding(.bottom, 16)
        }
        .fixedSize(horizontal: false, vertical: true)
    }
}

// MARK: - Barra Superior de Juegos (Espejo de GameTopBar y FunGameTitle)

struct GameTopBar: View {
    let title: String
    let progress: String
    var titleIcon: String? = nil
    var titleColors: [Color] = [
        Color(red: 1.0, green: 0.624, blue: 0.110),
        Color(red: 1.0, green: 0.310, blue: 0.596)
    ]
    let onClose: () -> Void

    @State private var titleScale: CGFloat = 0.6

    var body: some View {
        HStack {
            Button(action: onClose) {
                Image(systemName: "xmark")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(GameTheme.purple)
                    .frame(width: 42, height: 42)
                    .background(Color.white)
                    .clipShape(Circle())
                    .shadow(color: .black.opacity(0.12), radius: 3, y: 2)
            }

            Spacer()

            if let titleIcon {
                HStack(spacing: 8) {
                    ZStack {
                        Circle()
                            .fill(Color.white)
                            .frame(width: 30, height: 30)
                        ZentemiIconView(name: titleIcon, size: 24)
                    }

                    Text(title)
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(.white)
                        .shadow(color: .black.opacity(0.25), radius: 2, y: 1)
                }
                .padding(.leading, 5)
                .padding(.trailing, 16)
                .padding(.vertical, 5)
                .background(
                    LinearGradient(
                        colors: titleColors,
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .clipShape(Capsule())
                .overlay(
                    Capsule()
                        .stroke(Color.white, lineWidth: 2.5)
                )
                .shadow(color: (titleColors.last ?? .pink).opacity(0.45), radius: 6, y: 3)
                .rotationEffect(.degrees(-3))
                .scaleEffect(titleScale)
                .onAppear {
                    withAnimation(.spring(response: 0.45, dampingFraction: 0.55)) {
                        titleScale = 1.0
                    }
                }
            } else {
                Text(title)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(GameTheme.purple)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 9)
                    .background(Color.white.opacity(0.95))
                    .clipShape(Capsule())
            }

            Spacer()

            Text(progress)
                .font(.system(size: 14, weight: .bold))
                .foregroundStyle(GameTheme.turquoise)
                .padding(.horizontal, 12)
                .padding(.vertical, 9)
                .background(Color.white.opacity(0.95))
                .clipShape(Capsule())
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 10)
    }
}

// MARK: - Botón "Escuchar audio" (Espejo de ListenButton)

struct GameListenButton: View {
    let isPlaying: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                ZStack {
                    Circle()
                        .fill(Color.white)
                        .frame(width: 34, height: 34)
                    ZentemiIconView(name: "ic_escuchar_audio", size: 26)
                }

                Text(isPlaying ? "Reproduciendo..." : "Escuchar audio")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(.white)
            }
            .frame(height: 48)
            .padding(.horizontal, 20)
            .background(GameTheme.purple)
            .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Tarjeta de Consejo con Maicito (Espejo de MaicitoTipCard)

struct MaicitoTipCardView: View {
    let text: String

    var body: some View {
        HStack(spacing: 14) {
            PetView(size: 72)

            Text(text)
                .font(.system(size: 14, weight: .medium))
                .foregroundStyle(GameTheme.subText)
                .fixedSize(horizontal: false, vertical: true)

            Spacer(minLength: 0)
        }
        .padding(14)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .shadow(color: .black.opacity(0.04), radius: 4, y: 2)
    }
}

// MARK: - Confeti Animado (Espejo de ConfettiOverlay)

struct GameConfettiOverlay: View {
    private let colors: [Color] = [
        Color(red: 0.61, green: 0.15, blue: 0.69),
        Color(red: 1.00, green: 0.92, blue: 0.23),
        Color(red: 0.30, green: 0.69, blue: 0.31),
        Color(red: 0.91, green: 0.12, blue: 0.39),
        Color(red: 0.00, green: 0.74, blue: 0.83),
        Color(red: 1.00, green: 0.60, blue: 0.00)
    ]
    private let shapes = ["●", "■", "▲", "★"]

    @State private var progress: CGFloat = 1.0
    @State private var seeds: [CGFloat] = (0..<30).map { _ in CGFloat.random(in: 0.06...0.94) }

    var body: some View {
        GeometryReader { geo in
            ZStack {
                ForEach(0..<30, id: \.self) { index in
                    Text(shapes[index % shapes.count])
                        .font(.system(size: CGFloat(10 + (index % 7))))
                        .foregroundStyle(colors[index % colors.count])
                        .position(
                            x: seeds[index] * geo.size.width,
                            y: progress * geo.size.height + CGFloat((index % 5) * 18)
                        )
                        .animation(
                            .easeOut(duration: 0.8 + Double(index % 5) * 0.12)
                                .delay(Double(index % 8) * 0.025),
                            value: progress
                        )
                }
            }
            .onAppear {
                progress = -0.1
            }
        }
        .allowsHitTesting(false)
    }
}

// MARK: - Ventana Final de Juego (Espejo exacto de GameResultOverlay en GameCommon.kt)

struct GameResultOverlay: View {
    @EnvironmentObject var appState: AppState

    let title: String
    let message: String
    let stars: Int
    let onPlayAgain: () -> Void
    let onExit: () -> Void

    @State private var rewardGiven: Bool? = nil
    @State private var visibleStars: Int = 0

    private var coinsEarned: Int {
        stars * GameTheme.coinsPerStar
    }

    var body: some View {
        ZStack {
            Color.black.opacity(0.55)
                .ignoresSafeArea()

            VStack(spacing: 14) {
                PetView(size: 110)

                Text(title)
                    .font(.system(size: 25, weight: .bold))
                    .foregroundStyle(GameTheme.purple)

                Text(message)
                    .font(.system(size: 16))
                    .foregroundStyle(GameTheme.darkText)
                    .multilineTextAlignment(.center)

                // Estrellas animadas una por una
                HStack(spacing: 14) {
                    ForEach(0..<3, id: \.self) { index in
                        if index < visibleStars {
                            ZentemiIconView(
                                name: "ic_estrella",
                                size: 46,
                                muted: index >= stars
                            )
                            .transition(.scale(scale: 0.3).combined(with: .opacity))
                        } else {
                            Color.clear
                                .frame(width: 46, height: 46)
                        }
                    }
                }
                .frame(height: 48)

                if let rewardGiven {
                    if rewardGiven {
                        HStack(spacing: 16) {
                            if coinsEarned > 0 {
                                rewardChip(
                                    icon: "ic_moneda",
                                    text: "+\(coinsEarned)",
                                    color: Color(red: 1.0, green: 0.584, blue: 0.0)
                                )
                            }
                            rewardChip(
                                icon: "ic_felicidad",
                                text: "+\(GameTheme.happinessReward)%",
                                color: Color(red: 1.0, green: 0.176, blue: 0.333)
                            )
                        }
                    } else {
                        Text("Ya usaste las \(appState.maxGameRewardsPerDay) recompensas de hoy. ¡Puedes seguir practicando!")
                            .font(.system(size: 13))
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                    }
                }

                GameChunkyButton(
                    text: "Jugar otra vez",
                    height: 50,
                    action: onPlayAgain
                )
                .padding(.top, 4)

                Button(action: onExit) {
                    Text("Salir")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(.gray)
                        .padding(.vertical, 4)
                }
            }
            .padding(25)
            .frame(maxWidth: 360)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 25))
            .shadow(color: .black.opacity(0.25), radius: 20, y: 10)
            .padding(.horizontal, 22)
        }
        .onAppear {
            rewardGiven = appState.claimGameReward(
                coinsEarned: coinsEarned,
                happinessEarned: GameTheme.happinessReward
            )
            SoundManager.shared.play(
                "audioBien",
                volume: Float(appState.soundVolume)
            )
            for step in 1...3 {
                DispatchQueue.main.asyncAfter(deadline: .now() + Double(step) * 0.22) {
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.55)) {
                        visibleStars = step
                    }
                }
            }
        }
    }

    private func rewardChip(icon: String, text: String, color: Color) -> some View {
        HStack(spacing: 6) {
            ZentemiIconView(name: icon, size: 26)
            Text(text)
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(color)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 8)
        .background(color.opacity(0.12))
        .clipShape(Capsule())
    }
}
