import SwiftUI

struct MaicitoGamesView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss

    @State private var showMazeLevels = false
    @State private var showMemoryLevels = false
    @State private var showWordSearchLevels = false

    // Sustituir por el nombre del nuevo audio (sin extensión) cuando esté disponible.
    private let backgroundMusicName = "audiolobby"

    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 20) {
                    HStack(spacing: 12) {
                        Button { dismiss() } label: {
                            Image(systemName: "chevron.left")
                                .font(.headline)
                                .foregroundStyle(.purple)
                                .frame(width: 44, height: 44)
                                .background(.white, in: Circle())
                        }
                        .accessibilityLabel("Volver al inicio")

                        Text("Jugar con \(appState.petName)")
                            .font(.title2.bold())
                            .foregroundStyle(Color(red: 0.35, green: 0.17, blue: 0.65))
                        Spacer(minLength: 0)
                    }

                    MaicitoGamePointsView(
                        points: appState.gamePoints,
                        goal: appState.gamePointsGoal
                    )

                    let cardHeight = max(190, (geometry.size.height - 240) / 2)
                    VStack(spacing: 16) {
                        MaicitoGameCard(title: "Memorama", symbol: "square.grid.2x2.fill",
                                        color: .purple, height: cardHeight, imageName: "memoramaFoto",
                                        isCompleted: appState.memoryLevelThreeCompleted) {
                            showMemoryLevels = true
                        }
                        HStack(spacing: 16) {
                            MaicitoGameCard(title: "Laberinto", symbol: "point.topleft.down.to.point.bottomright.curvepath",
                                            color: .teal, height: cardHeight, imageName: "laberintoFoto",
                                            isCompleted: appState.mazeLevelFiveCompleted) {
                                showMazeLevels = true
                            }
                            MaicitoGameCard(title: "Sopa de letras", symbol: "character.book.closed.fill",
                                            color: .pink, height: cardHeight, imageName: "sopaLetrasFoto",
                                            isCompleted: appState.wordSearchLevelThreeCompleted) {
                                showWordSearchLevels = true
                            }
                        }
                    }
                }
                .padding(22)
                .frame(minHeight: geometry.size.height, alignment: .top)
            }
        }
        .background(MaicitoGamesBackground())
        .fullScreenCover(isPresented: $showMazeLevels) {
            MazeLevelsView()
                .environmentObject(appState)
        }
        .fullScreenCover(isPresented: $showMemoryLevels) {
            MemoryLevelsView().environmentObject(appState)
        }
        .fullScreenCover(isPresented: $showWordSearchLevels) {
            WordSearchLevelsView().environmentObject(appState)
        }
        .onAppear {
            if appState.musicVolume > 0 {
                MusicManager.shared.playMusic(named: backgroundMusicName, volume: Float(appState.musicVolume))
            }
        }
        .onDisappear {
            if appState.musicVolume > 0 {
                MusicManager.shared.playMusic(named: "audiolobby", volume: Float(appState.musicVolume))
            }
        }
    }
}

private struct MaicitoGamePointsView: View {
    let points: Int
    let goal: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Label("Puntos de juego", systemImage: "star.fill")
                    .font(.headline)
                    .foregroundStyle(.purple)
                Spacer(minLength: 8)
                Text("\(points.formatted()) / \(goal.formatted())")
                    .font(.subheadline.bold())
                    .monospacedDigit()
            }
            ProgressView(value: Double(min(max(points, 0), goal)), total: Double(goal))
                .tint(.teal)
                .accessibilityLabel("Objetivo de puntos de juego")
                .accessibilityValue("\(points) de \(goal) puntos")
            Text("¡Juega y alcanza tu objetivo!")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(18)
        .background(.white.opacity(0.96), in: RoundedRectangle(cornerRadius: 22))
    }
}

private struct MaicitoGameCard: View {
    let title: LocalizedStringKey
    let symbol: String
    let color: Color
    let height: CGFloat
    // Para usar una imagen de Assets, pasar su nombre en lugar de nil.
    let imageName: String?
    var isCompleted = false
    var action: () -> Void = {}

    var body: some View {
        Button(action: action) {
            VStack(spacing: isCompleted ? 10 : 18) {
                if let imageName {
                    Image(imageName)
                        .resizable()
                        .scaledToFit()
                        .frame(height: max(80, height - (isCompleted ? 130 : 105)))
                        .accessibilityHidden(true)
                } else {
                    Image(systemName: symbol)
                        .font(.system(size: 44, weight: .semibold))
                        .foregroundStyle(color)
                        .frame(width: 100, height: 100)
                        .background(color.opacity(0.12), in: RoundedRectangle(cornerRadius: 28))
                        .accessibilityHidden(true)
                }
                Text(title)
                    .font(.title3.bold())
                    .foregroundStyle(isCompleted ? Color(red: 0.10, green: 0.38, blue: 0.20) : Color(red: 0.35, green: 0.17, blue: 0.65))
                    .multilineTextAlignment(.center)
                if isCompleted {
                    Label("Completado", systemImage: "checkmark.seal.fill")
                        .font(.caption.bold())
                        .foregroundStyle(Color(red: 0.10, green: 0.38, blue: 0.20))
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, minHeight: height)
            .background(isCompleted ? Color(red: 0.78, green: 0.96, blue: 0.80) : .white.opacity(0.96),
                        in: RoundedRectangle(cornerRadius: 24))
            .overlay {
                RoundedRectangle(cornerRadius: 24)
                    .stroke(isCompleted ? Color.green : color.opacity(0.18), lineWidth: isCompleted ? 3 : 1)
            }
            .shadow(color: color.opacity(0.10), radius: 8, y: 4)
        }
        .buttonStyle(MaicitoGamePressStyle())
        .accessibilityValue(isCompleted ? "Completado. Puedes volver a jugar." : "")
    }
}

private struct MaicitoGamePressStyle: ButtonStyle {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed && !reduceMotion ? 0.95 : 1)
            .opacity(configuration.isPressed ? 0.8 : 1)
            .animation(.easeOut(duration: 0.16), value: configuration.isPressed)
    }
}

private struct MaicitoGamesBackground: View {
    var body: some View {
        ZStack {
            Color(red: 0.97, green: 0.95, blue: 0.99)
            Circle()
                .fill(Color.purple.opacity(0.06))
                .frame(width: 230, height: 230)
                .offset(x: -165, y: -290)
            Circle()
                .fill(Color.purple.opacity(0.05))
                .frame(width: 190, height: 190)
                .offset(x: 180, y: -65)
            Circle()
                .fill(Color.cyan.opacity(0.06))
                .frame(width: 200, height: 200)
                .offset(x: -165, y: 330)
            Circle()
                .fill(Color.yellow.opacity(0.08))
                .frame(width: 140, height: 140)
                .offset(x: 165, y: 340)
        }
        .ignoresSafeArea()
    }
}
