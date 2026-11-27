import SwiftUI

struct MemoryLevelsView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss
    @State private var selectedLevel: MemoryLevel?

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                HStack {
                    Button { dismiss() } label: {
                        Image(systemName: "chevron.left").padding(14).background(.white, in: Circle())
                    }
                    .accessibilityLabel("Volver a juegos")
                    Spacer()
                    Text("Memorama").font(.title.bold())
                    Spacer()
                }
                Image("memoramaFoto").resizable().scaledToFit().frame(height: 125)
                    .accessibilityHidden(true)
                Text("Une cada imagen con su palabra en Yuhmu.")
                    .font(.headline).multilineTextAlignment(.center)

                ForEach(MemoryLevel.allCases) { level in
                    MemoryLevelCard(
                        level: level,
                        completed: appState.memoryCompleted(level),
                        unlocked: appState.memoryUnlocked(level),
                        bestSeconds: appState.memoryBestSeconds(level)
                    ) {
                        guard appState.memoryUnlocked(level) else { return }
                        selectedLevel = level
                    }
                }
            }
            .padding(22).frame(maxWidth: 600).frame(maxWidth: .infinity)
        }
        .foregroundStyle(Color(red: 0.35, green: 0.17, blue: 0.65))
        .background(Color(red: 0.97, green: 0.95, blue: 0.99).ignoresSafeArea())
        .fullScreenCover(item: $selectedLevel) { level in
            MemoryGameView(level: level).environmentObject(appState)
        }
    }
}

private struct MemoryLevelCard: View {
    let level: MemoryLevel
    let completed: Bool
    let unlocked: Bool
    let bestSeconds: Double
    let onPlay: () -> Void

    var body: some View {
        VStack(spacing: 18) {
            HStack {
                Text("Nivel \(level.id)").font(.title.bold())
                Spacer()
                if completed {
                    Label("Completado", systemImage: "checkmark.seal.fill")
                        .font(.caption.bold()).foregroundStyle(.teal)
                } else if !unlocked {
                    Image(systemName: "lock.fill").accessibilityLabel("Bloqueado")
                }
            }
            Text("\(level.words.count) pares · \(level.theme)")
                .font(.headline).multilineTextAlignment(.center)
            Text("Sin límite de tiempo. ¡Aprende a tu ritmo!")
                .font(.subheadline).foregroundStyle(.secondary)
            HStack {
                Label("\(level.coins) monedas", systemImage: "circle.fill")
                Label("\(level.points.formatted()) puntos", systemImage: "star.fill")
            }.font(.subheadline.bold())
            Text(unlocked
                 ? "Premios solo en la primera victoria."
                 : "Completa el nivel \(level.id - 1) para desbloquearlo.")
                .font(.caption).foregroundStyle(.secondary)
            if bestSeconds > 0 {
                Text("Mejor tiempo: \(MazeLevel.timeText(bestSeconds))")
            }
            Button(unlocked ? (completed ? "Volver a jugar" : "Jugar") : "Bloqueado", action: onPlay)
                .buttonStyle(MazePrimaryButtonStyle())
                .disabled(!unlocked).opacity(unlocked ? 1 : 0.45)
        }
        .padding(22).background(.white, in: RoundedRectangle(cornerRadius: 24))
    }
}
