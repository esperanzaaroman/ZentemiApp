import SwiftUI

struct WordSearchLevelsView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss
    @State private var selectedLevel: WordSearchLevel?

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                HStack {
                    Button { dismiss() } label: {
                        Image(systemName: "chevron.left")
                            .font(.headline).padding(14).background(.white, in: Circle())
                    }
                    .accessibilityLabel("Volver a juegos")
                    Spacer()
                    Text("Sopa de letras").font(.title2.bold())
                    Spacer()
                }
                .foregroundStyle(.purple)
                Image("sopaLetrasFoto")
                    .resizable().scaledToFit().frame(height: 170)
                    .accessibilityHidden(true)
                Text("Encuentra palabras en Yuhmu")
                    .font(.title2.bold()).multilineTextAlignment(.center)
                ForEach(WordSearchLevel.allCases) { level in
                    VStack(spacing: 18) {
                        HStack {
                            Text("Nivel \(level.id)").font(.title.bold())
                            Spacer()
                            if appState.wordSearchCompleted(level) {
                                Label("Completado", systemImage: "checkmark.seal.fill")
                                    .font(.subheadline.bold()).foregroundStyle(.teal)
                            }
                        }
                        Text(level.words.map(\.spanish).joined(separator: ", "))
                            .font(.title3.bold()).frame(maxWidth: .infinity, alignment: .leading)
                        Text("\(level.words.count) palabras · \(level.side) × \(level.side)")
                            .foregroundStyle(.secondary)
                        Text(level.instructions)
                            .multilineTextAlignment(.center)
                        HStack(spacing: 24) {
                            Label("+\(level.coins)", systemImage: "circle.fill")
                                .foregroundStyle(.orange)
                            Label("+\(level.points)", systemImage: "star.fill")
                                .foregroundStyle(.purple)
                        }.font(.headline)
                        Text("Premio por completar el nivel por primera vez.")
                            .font(.caption).foregroundStyle(.secondary)
                        if appState.wordSearchBestSeconds(level) > 0 {
                            Label("Mejor tiempo: \(MazeLevel.timeText(appState.wordSearchBestSeconds(level)))", systemImage: "stopwatch")
                                .font(.subheadline.bold()).foregroundStyle(.teal)
                        }
                        Button(appState.wordSearchCompleted(level) ? "Volver a jugar" : "Comenzar") {
                            if appState.wordSearchUnlocked(level) { selectedLevel = level }
                        }
                        .buttonStyle(MazePrimaryButtonStyle())
                        .disabled(!appState.wordSearchUnlocked(level))
                        if !appState.wordSearchUnlocked(level) {
                            Label("Completa el nivel \(level.id - 1) para desbloquear", systemImage: "lock.fill")
                                .font(.caption).foregroundStyle(.secondary)
                        }
                    }
                    .padding(24)
                    .background(.white, in: RoundedRectangle(cornerRadius: 26))
                    .overlay { RoundedRectangle(cornerRadius: 26).stroke(.purple.opacity(0.15)) }
                    .shadow(color: .purple.opacity(0.10), radius: 8, y: 5)
                }
            }
            .padding(22).frame(maxWidth: 560).frame(maxWidth: .infinity)
        }
        .background(Color(red: 0.97, green: 0.95, blue: 0.99).ignoresSafeArea())
        .fullScreenCover(item: $selectedLevel) { level in
            WordSearchGameView(level: level).environmentObject(appState)
        }
    }
}
