import SwiftUI

struct WordSearchResultsView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @Environment(\.scenePhase) private var scenePhase

    let level: WordSearchLevel
    let elapsedSeconds: Double
    let earnedCoins: Int
    let earnedPoints: Int
    let onContinue: () -> Void

    @State private var visibleCards = 0
    @State private var showMascot = false
    @State private var showContinue = false
    @State private var displayedPoints = 0
    @State private var sounds = LessonResultSounds()

    var body: some View {
        ScrollView {
            VStack(spacing: 22) {
                Text("¡Nivel \(level.id) completado!")
                    .font(.largeTitle.bold())
                    .multilineTextAlignment(.center)
                    .foregroundStyle(Color(red: 0.48, green: 0.29, blue: 0.79))

                PetView(size: 140)
                    .scaleEffect(showMascot ? 1 : 0.6)
                    .opacity(showMascot ? 1 : 0)

                WordSearchResultCard(
                    symbol: "stopwatch.fill",
                    title: "Tiempo",
                    value: MazeLevel.timeText(elapsedSeconds),
                    detail: "Mejor tiempo: \(MazeLevel.timeText(appState.wordSearchBestSeconds(level)))",
                    color: .teal,
                    visible: visibleCards >= 1
                )

                WordSearchResultCard(
                    symbol: "circle.fill",
                    title: "Monedas ganadas",
                    value: "+\(earnedCoins)",
                    detail: "Monedas totales: \(appState.coins.formatted())",
                    color: .orange,
                    visible: visibleCards >= 2
                )

                VStack(spacing: 12) {
                    WordSearchResultCard(
                        symbol: "star.fill",
                        title: "Puntos ganados",
                        value: "+\(earnedPoints)",
                        detail: "\(displayedPoints.formatted()) / \(appState.gamePointsGoal.formatted()) puntos totales",
                        color: .purple,
                        visible: true
                    )
                    ProgressView(
                        value: Double(min(displayedPoints, appState.gamePointsGoal)),
                        total: Double(appState.gamePointsGoal)
                    )
                    .tint(.purple)
                    .padding(.horizontal, 18)
                    .padding(.bottom, 16)
                    .accessibilityLabel("Puntos de juego")
                    .accessibilityValue("\(displayedPoints) de \(appState.gamePointsGoal)")
                }
                .background(.white, in: RoundedRectangle(cornerRadius: 22))
                .scaleEffect(visibleCards >= 3 ? 1 : 0.75)
                .opacity(visibleCards >= 3 ? 1 : 0)

                if earnedCoins == 0 && earnedPoints == 0 {
                    Text("Ya recibiste el premio de este nivel. Puedes seguir jugando para mejorar tu tiempo.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                }

                Button("Volver a niveles", action: onContinue)
                    .buttonStyle(MazePrimaryButtonStyle())
                    .opacity(showContinue ? 1 : 0)
                    .disabled(!showContinue)
            }
            .padding(24)
            .frame(maxWidth: 520)
            .frame(maxWidth: .infinity)
        }
        .background(Color(red: 0.97, green: 0.95, blue: 0.99).ignoresSafeArea())
        .task(id: scenePhase) {
            guard scenePhase == .active else {
                sounds.stop()
                return
            }
            await animateResults()
        }
        .onDisappear {
            sounds.stop()
        }
    }

    @MainActor
    private func animateResults() async {
        displayedPoints = max(0, appState.gamePoints - earnedPoints)
        withAnimation(reduceMotion ? nil : .spring(response: 0.5, dampingFraction: 0.7)) {
            showMascot = true
        }

        do {
            try await Task.sleep(for: .milliseconds(450))
            for card in 1...3 {
                try Task.checkCancellation()
                withAnimation(reduceMotion ? nil : .spring(response: 0.45, dampingFraction: 0.75)) {
                    visibleCards = card
                }
                sounds.playCard(volume: Float(appState.soundVolume))
                try await Task.sleep(for: .milliseconds(350))
            }

            if earnedPoints > 0 {
                sounds.playExperienceFill(volume: Float(appState.soundVolume))
                let startingPoints = max(0, appState.gamePoints - earnedPoints)
                for step in 1...70 {
                    try await Task.sleep(for: .milliseconds(20))
                    withAnimation(reduceMotion ? nil : .linear(duration: 0.02)) {
                        displayedPoints = startingPoints + earnedPoints * step / 70
                    }
                }
            }
            displayedPoints = appState.gamePoints
            withAnimation(reduceMotion ? nil : .easeOut(duration: 0.3)) {
                showContinue = true
            }
        } catch {
            sounds.stop()
        }
    }
}

private struct WordSearchResultCard: View {
    let symbol: String
    let title: LocalizedStringKey
    let value: String
    let detail: String
    let color: Color
    let visible: Bool

    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: symbol)
                .font(.title2.bold())
                .foregroundStyle(color)
                .frame(width: 48, height: 48)
                .background(color.opacity(0.12), in: Circle())
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(.secondary)
                Text(value)
                    .font(.title.bold())
                    .monospacedDigit()
                Text(detail)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer(minLength: 0)
        }
        .padding(18)
        .frame(maxWidth: .infinity)
        .background(.white, in: RoundedRectangle(cornerRadius: 22))
        .scaleEffect(visible ? 1 : 0.75)
        .opacity(visible ? 1 : 0)
    }
}
