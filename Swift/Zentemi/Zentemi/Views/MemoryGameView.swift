import SwiftUI

struct MemoryGameView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var session: MemoryGameSession
    @State private var sounds = LessonResultSounds()
    @State private var firstWin = false
    @State private var showResults = false
    @State private var showCompletionCelebration = false

    init(level: MemoryLevel = .one) {
        _session = State(initialValue: MemoryGameSession(level: level))
    }

    var body: some View {
        ZStack {
            Color(red: 0.97, green: 0.95, blue: 0.99).ignoresSafeArea()
            if showCompletionCelebration {
                MemoryCompletionCelebrationView {
                    showCompletionCelebration = false
                    showResults = true
                }
            } else if showResults {
                MemoryResultsView(
                    level: session.level,
                    elapsedSeconds: session.elapsedSeconds,
                    earnedCoins: firstWin ? session.level.coins : 0,
                    earnedPoints: firstWin ? session.level.points : 0
                ) { dismiss() }
            } else {
                GeometryReader { geometry in
                    let layout = MemoryGridLayout(
                        size: geometry.size, level: session.level,
                        accessibilitySize: dynamicTypeSize.isAccessibilitySize
                    )
                    ScrollView {
                        VStack(spacing: 14) {
                            MemoryHeader(
                                level: session.level,
                                seconds: session.elapsedSeconds,
                                pairs: session.matched.count,
                                onClose: { dismiss() }
                            )
                            Text("Une la imagen con su palabra")
                                .font(.headline).multilineTextAlignment(.center)
                            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 12), count: layout.columns),
                                      spacing: 12) {
                                ForEach(session.cards) { card in
                                    MemoryCardView(
                                        card: card,
                                        revealed: session.selected.contains(card.id) || session.matched.contains(card.word.id),
                                        matched: session.matched.contains(card.word.id),
                                        height: layout.cardHeight
                                    ) { select(card) }
                                    .disabled(session.resolving || session.finished || scenePhase != .active)
                                }
                            }
                            Text(session.feedback)
                                .font(.subheadline.bold())
                                .foregroundStyle(session.finished ? .teal : .purple)
                                .multilineTextAlignment(.center)
                                .frame(minHeight: 38)
                            if session.finished {
                                Button(session.level == .three && firstWin ? "Siguiente" : "Ver resultados") {
                                    if session.level == .three && firstWin {
                                        showCompletionCelebration = true
                                    } else {
                                        showResults = true
                                    }
                                }
                                    .buttonStyle(MazePrimaryButtonStyle())
                            }
                        }
                        .padding(18).frame(maxWidth: 900).frame(maxWidth: .infinity)
                    }
                }
            }
        }
        .task(id: scenePhase) {
            guard scenePhase == .active else { return }
            session.resumeClock()
            do {
                while !Task.isCancelled && !session.finished {
                    try await Task.sleep(for: .milliseconds(100))
                    guard scenePhase == .active else { return }
                    withAnimation(reduceMotion ? nil : .easeInOut(duration: 0.2)) {
                        session.tick()
                    }
                }
            } catch { }
        }
        .onChange(of: scenePhase) { _, phase in
            if phase != .active {
                session.pauseClock()
                sounds.stop()
            }
        }
        .onDisappear {
            session.pauseClock()
            sounds.stop()
        }
    }

    private func select(_ card: MemoryCard) {
        guard scenePhase == .active else { return }
        let result = withAnimation(reduceMotion ? nil : .easeInOut(duration: 0.22)) {
            session.select(card)
        }
        switch result {
        case .ignored:
            return
        case .revealed:
            sounds.playCard(volume: Float(appState.soundVolume))
        case .matched:
            sounds.playCard(volume: Float(appState.soundVolume))
            SoundManager.shared.play("audioBien", volume: Float(appState.soundVolume))
            if session.finished {
                firstWin = appState.completeMemory(level: session.level, seconds: session.elapsedSeconds)
            }
        case .missed:
            sounds.playCard(volume: Float(appState.soundVolume))
            SoundManager.shared.play("audioMalaSeleccion", volume: Float(appState.soundVolume) * 0.4)
        }
    }
}

private struct MemoryHeader: View {
    let level: MemoryLevel
    let seconds: Double
    let pairs: Int
    let onClose: () -> Void

    var body: some View {
        VStack(spacing: 12) {
            HStack {
                Button(action: onClose) {
                    Image(systemName: "xmark").padding(12).background(.white, in: Circle())
                }
                .accessibilityLabel("Salir del memorama")
                Spacer()
                Text("Memorama · Nivel \(level.id)").font(.headline)
                Spacer()
                Label(MazeLevel.timeText(seconds), systemImage: "stopwatch")
                    .font(.caption.bold()).monospacedDigit()
            }
            HStack {
                ProgressView(value: Double(pairs), total: Double(level.words.count)).tint(.teal)
                Text("\(pairs)/\(level.words.count) pares").font(.subheadline.bold())
            }
        }.foregroundStyle(.purple)
    }
}

private struct MemoryCardView: View {
    let card: MemoryCard
    let revealed: Bool
    let matched: Bool
    let height: CGFloat
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            ZStack {
                Color.clear
                if revealed {
                    if card.isPicture {
                        Image(card.word.imageName)
                            .resizable().scaledToFit()
                            .padding(12)
                    } else {
                        VStack(spacing: 8) {
                            Text(card.word.yuhmu)
                                .font(.title2.bold()).foregroundStyle(.purple)
                                .lineLimit(2).minimumScaleFactor(0.75)
                            Text(card.word.spanish)
                                .font(.subheadline).foregroundStyle(.secondary)
                                .lineLimit(2).minimumScaleFactor(0.85)
                        }
                        .multilineTextAlignment(.center).padding(12)
                    }
                } else {
                    Image(systemName: "sparkles")
                        .font(.largeTitle).foregroundStyle(.white)
                }
            }
            .frame(height: height)
            .frame(maxWidth: .infinity)

            .overlay(alignment: .topTrailing) {
                if matched {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundStyle(.white, .teal).padding(6)
                }
            }

        }
        .buttonStyle(MemoryRaisedCardStyle(revealed: revealed, matched: matched))
        .disabled(revealed)
        .accessibilityLabel(revealed
            ? (card.isPicture ? "Imagen: \(card.word.spanish)" : "\(card.word.yuhmu), \(card.word.spanish)")
            : "Tarjeta oculta")
        .accessibilityValue(matched ? "Par encontrado" : "")
    }
}

/// La base permanece fija; la cara baja hacia ella al presionar.
private struct MemoryRaisedCardStyle: ButtonStyle {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    let revealed: Bool
    let matched: Bool

    func makeBody(configuration: Configuration) -> some View {
        let edge = matched ? Color.teal : Color(red: 0.48, green: 0.29, blue: 0.79)
        let depth: CGFloat = configuration.isPressed ? 2 : 7
        let faceColors: [Color] = revealed
            ? [.white, Color(red: 0.96, green: 0.94, blue: 1)]
            : [Color(red: 0.65, green: 0.45, blue: 0.92),
               Color(red: 0.44, green: 0.24, blue: 0.74)]

        configuration.label
            .background {
                RoundedRectangle(cornerRadius: 24)
                    .fill(LinearGradient(colors: faceColors, startPoint: .topLeading, endPoint: .bottomTrailing))
            }
            .clipShape(RoundedRectangle(cornerRadius: 24))
            .overlay {
                RoundedRectangle(cornerRadius: 24)
                    .strokeBorder(revealed ? edge.opacity(0.35) : .white.opacity(0.4), lineWidth: 2)
                RoundedRectangle(cornerRadius: 20)
                    .inset(by: 4)
                    .strokeBorder(
                        LinearGradient(colors: [.white.opacity(0.75), .clear],
                                       startPoint: .top, endPoint: .center),
                        lineWidth: 2
                    )
            }
            .offset(y: configuration.isPressed ? 5 : 0)
            .background {
                RoundedRectangle(cornerRadius: 24)
                    .fill(revealed ? edge.opacity(0.5) : Color(red: 0.30, green: 0.14, blue: 0.53))
                    .offset(y: 7)
                    .shadow(color: edge.opacity(0.22), radius: configuration.isPressed ? 3 : 7,
                            x: 0, y: depth)
            }
            .padding(.bottom, 7)
            .animation(reduceMotion ? nil : .easeOut(duration: 0.12), value: configuration.isPressed)
    }
}

private struct MemoryGridLayout {
    let columns: Int
    let cardHeight: CGFloat

    init(size: CGSize, level: MemoryLevel, accessibilitySize: Bool) {
        // Limita el ancho al mismo máximo del contenido. Mantiene las tarjetas
        // legibles y permite desplazar el tablero en pantallas pequeñas.
        let width = min(size.width, 900)
        if accessibilitySize {
            columns = width >= 600 ? 2 : 1
        } else if level == .one {
            columns = width > 650 ? 5 : 2
        } else {
            columns = width >= 700 ? 5 : width >= 500 ? 4 : width >= 360 ? 3 : 2
        }
        let rows = (level.words.count * 2 + columns - 1) / columns
        let availableHeight = (size.height - 235 - CGFloat(rows - 1) * 19) / CGFloat(rows)
        cardHeight = accessibilitySize ? 190 : max(level == .one ? 90 : 110, min(160, availableHeight))
    }
}
