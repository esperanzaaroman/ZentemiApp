import SwiftUI

struct MazeLevelsView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss
    @State private var selectedLevel: MazeLevel?

    var body: some View {
        ScrollView {
            VStack(spacing: 26) {
                HStack {
                    Button { dismiss() } label: {
                        Image(systemName: "chevron.left")
                            .font(.headline)
                            .frame(width: 44, height: 44)
                            .background(.white, in: Circle())
                    }
                    .accessibilityLabel("Volver a juegos")
                    Spacer()
                    Text("Laberinto").font(.title2.bold())
                    Spacer()
                    Color.clear.frame(width: 44, height: 44)
                }

                Image("laberintoFoto")
                    .resizable()
                    .scaledToFit()
                    .frame(height: 125)
                    .accessibilityHidden(true)

                Text("Encuentra el camino y la traducción correcta.")
                    .font(.headline)
                    .multilineTextAlignment(.center)

                MazeLevelCard(
                    level: .one, completed: appState.mazeLevelOneCompleted, unlocked: true,
                    bestSeconds: appState.mazeLevelOneBestSeconds,
                    coins: appState.mazeLevelOneCoins, points: appState.mazeLevelOnePoints,
                    pendingCoins: 0
                ) {
                    selectedLevel = .one
                }

                MazeLevelCard(
                    level: .two, completed: appState.mazeLevelTwoCompleted,
                    unlocked: appState.mazeLevelOneCompleted,
                    bestSeconds: appState.mazeLevelTwoBestSeconds,
                    coins: appState.mazeLevelTwoCoins, points: appState.mazeLevelTwoPoints,
                    pendingCoins: 5 - appState.mazeLevelTwoClaimedCoinMask.nonzeroBitCount
                ) {
                    guard appState.mazeLevelOneCompleted else { return }
                    selectedLevel = .two
                }

                MazeLevelCard(
                    level: .three, completed: appState.mazeLevelThreeCompleted,
                    unlocked: appState.mazeLevelTwoCompleted,
                    bestSeconds: appState.mazeLevelThreeBestSeconds,
                    coins: appState.mazeLevelThreeCoins, points: appState.mazeLevelThreePoints,
                    pendingCoins: 10 - appState.mazeLevelThreeClaimedCoinMask.nonzeroBitCount
                ) {
                    guard appState.mazeLevelTwoCompleted else { return }
                    selectedLevel = .three
                }

                MazeLevelCard(
                    level: .four, completed: appState.mazeLevelFourCompleted,
                    unlocked: appState.mazeLevelThreeCompleted,
                    bestSeconds: appState.mazeLevelFourBestSeconds,
                    coins: appState.mazeLevelFourCoins, points: appState.mazeLevelFourPoints,
                    pendingCoins: 15 - appState.mazeLevelFourClaimedCoinMask.nonzeroBitCount
                ) {
                    guard appState.mazeLevelThreeCompleted else { return }
                    selectedLevel = .four
                }

                MazeLevelCard(
                    level: .five, completed: appState.mazeLevelFiveCompleted,
                    unlocked: appState.mazeLevelFourCompleted,
                    bestSeconds: appState.mazeLevelFiveBestSeconds,
                    coins: appState.mazeLevelFiveCoins, points: appState.mazeLevelFivePoints,
                    pendingCoins: 20 - appState.mazeLevelFiveClaimedCoinMask.nonzeroBitCount
                ) {
                    guard appState.mazeLevelFourCompleted else { return }
                    selectedLevel = .five
                }

                Text("Cada juego tendrá \(appState.plannedLevelsPerGame) niveles. ¡La dificultad irá aumentando!")
                    .font(.subheadline)
                    .multilineTextAlignment(.center)
                    .padding()
                    .background(.white.opacity(0.92), in: RoundedRectangle(cornerRadius: 18))

                Text("\(appState.gamePoints.formatted()) / \(appState.gamePointsGoal.formatted()) puntos")
                    .font(.headline)
                    .padding()
                    .background(.white.opacity(0.92), in: Capsule())
            }
            .padding(22)
        }
        .foregroundStyle(Color(red: 0.35, green: 0.17, blue: 0.65))
        .background(MazeGrassBackground())
        .fullScreenCover(item: $selectedLevel) { level in
            MazeGameView(
                level: level,
                claimedCoinMask: appState.mazeClaimedCoinMask(for: level.id)
            )
            .environmentObject(appState)
        }
    }
}

private struct MazeLevelCard: View {
    let level: MazeLevel
    let completed: Bool
    let unlocked: Bool
    let bestSeconds: Double
    let coins: Int
    let points: Int
    let pendingCoins: Int
    let onPlay: () -> Void

    var body: some View {
        VStack(spacing: 18) {
            HStack {
                Text("Nivel \(level.id)").font(.title.bold())
                Spacer()
                if completed {
                    Label("Completado", systemImage: "checkmark.seal.fill")
                        .font(.caption.bold())
                        .foregroundStyle(.teal)
                } else if !unlocked {
                    Image(systemName: "lock.fill").foregroundStyle(.secondary)
                }
            }

            Text(level.phrase)
                .font(.title2.bold())
                .frame(maxWidth: .infinity, alignment: .leading)
            Text("Dificultad \(level.id) de 5")
                .font(.caption.bold())
                .frame(maxWidth: .infinity, alignment: .leading)

            HStack(spacing: 20) {
                Label("\(coins) monedas", systemImage: "circle.fill")
                    .foregroundStyle(.orange)
                Label("\(points.formatted()) puntos", systemImage: "star.fill")
                    .foregroundStyle(.purple)
            }
            .font(.subheadline.bold())

            if !level.coins.isEmpty {
                Text(pendingCoins > 0
                     ? "\(pendingCoins) monedas del mapa pendientes · +10 cada una"
                     : "¡Ya recogiste todas las monedas del mapa!")
                    .font(.caption.bold())
                    .foregroundStyle(.orange)
                    .multilineTextAlignment(.center)
            }

            Text(!unlocked
                 ? "Completa el nivel \(level.id - 1) para desbloquearlo."
                 : completed
                    ? "Premio del nivel recibido. ¡Mejora tu tiempo!"
                    : "Premio por completar el nivel por primera vez.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)

            if bestSeconds > 0 {
                Label("Mejor tiempo: \(MazeLevel.timeText(bestSeconds))", systemImage: "stopwatch")
                    .font(.headline)
            }

            Button(unlocked ? (completed ? "Volver a jugar" : "Jugar") : "Bloqueado", action: onPlay)
                .buttonStyle(MazePrimaryButtonStyle())
                .disabled(!unlocked)
                .opacity(unlocked ? 1 : 0.45)
        }
        .padding(24)
        .background(.white.opacity(0.96), in: RoundedRectangle(cornerRadius: 26))
    }
}

struct MazeGrassBackground: View {
    var imageName = "mazeGrassBackground"
    var body: some View {
        GeometryReader { geometry in
            Image(imageName)
                .resizable()
                .scaledToFill()
                .frame(width: geometry.size.width, height: geometry.size.height)
                .clipped()
        }
        .ignoresSafeArea()
    }
}

struct MazePrimaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity)
            .frame(minHeight: 54)
            .background(Color(red: 0.48, green: 0.29, blue: 0.79),
                        in: RoundedRectangle(cornerRadius: 16))
            .opacity(configuration.isPressed ? 0.7 : 1)
    }
}
