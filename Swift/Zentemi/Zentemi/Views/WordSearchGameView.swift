import SwiftUI

struct WordSearchGameView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var session: WordSearchSession
    @State private var showCelebration = false
    @State private var firstWin = false
    @State private var showResults = false
    @State private var sounds = LessonResultSounds()

    init(level: WordSearchLevel = .one) {
        _session = State(initialValue: WordSearchSession(level: level))
    }

    var body: some View {
        ZStack {
            Color(red: 0.97, green: 0.95, blue: 0.99).ignoresSafeArea()
            if showCelebration {
                WordSearchCompletionCelebrationView {
                    showCelebration = false
                    showResults = true
                }
            } else if showResults {
                WordSearchResultsView(
                    level: session.level,
                    elapsedSeconds: session.elapsedSeconds,
                    earnedCoins: firstWin ? session.level.coins : 0,
                    earnedPoints: firstWin ? session.level.points : 0
                ) { dismiss() }
            } else {
                ScrollView {
                    VStack(spacing: 20) {
                        HStack {
                            Button { dismiss() } label: {
                                Image(systemName: "xmark").padding(14).background(.white, in: Circle())
                            }
                            .accessibilityLabel("Salir de sopa de letras")
                            Spacer()
                            Text("Sopa de letras").font(.title2.bold())
                            Spacer()
                            Label(MazeLevel.timeText(session.elapsedSeconds), systemImage: "stopwatch")
                                .font(.caption.bold()).monospacedDigit()
                        }
                        .foregroundStyle(.purple)
                        Text("Nivel \(session.level.id) · Encuentra las palabras").font(.headline)
                        WordSearchVocabulary(words: session.words, found: session.found)
                        HStack {
                            ProgressView(value: Double(session.found.count), total: Double(session.words.count)).tint(.teal)
                            Text("\(session.found.count) de \(session.words.count)").font(.headline).monospacedDigit()
                        }
                        Text(session.level.instructions)
                            .font(.subheadline).foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                        WordSearchBoard(session: session, onSubmit: submit)
                            .disabled(session.finished || scenePhase != .active)
                        Text(session.feedback)
                            .font(.headline).foregroundStyle(session.finished ? .teal : .purple)
                            .multilineTextAlignment(.center)
                            .frame(minHeight: 48)
                            .accessibilityAddTraits(.updatesFrequently)
                        if session.finished {
                            Button(session.level == .three && firstWin ? "Siguiente" : "Ver resultados") {
                                if session.level == .three && firstWin {
                                    showCelebration = true
                                } else {
                                    showResults = true
                                }
                            }
                                .buttonStyle(MazePrimaryButtonStyle())
                        }
                    }
                    .padding(20)
                    .frame(maxWidth: 560).frame(maxWidth: .infinity)
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
                    session.tick()
                }
            } catch { }
        }
        .onChange(of: session.selection) { previous, current in
            if scenePhase == .active && !current.isEmpty && current != previous {
                sounds.playCard(volume: Float(appState.soundVolume) * 0.35)
            }
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

    private func submit() {
        guard scenePhase == .active else { return }
        let result = withAnimation(reduceMotion ? nil : .easeOut(duration: 0.2)) {
            session.submit()
        }
        switch result {
        case .correct:
            SoundManager.shared.play("audioBien", volume: Float(appState.soundVolume))
            if session.finished {
                firstWin = appState.completeWordSearch(level: session.level, seconds: session.elapsedSeconds)
            }
        case .incorrect:
            SoundManager.shared.play("audioMalaSeleccion", volume: Float(appState.soundVolume) * 0.4)
        case .ignored:
            break
        }
    }
}

private struct WordSearchVocabulary: View {
    let words: [WordSearchWord]
    let found: Set<String>

    var body: some View {
        LazyVGrid(columns: [GridItem(.adaptive(minimum: 110), spacing: 10)], spacing: 10) {
            ForEach(words) { word in
                WordSearchWordBadge(word: word, found: found.contains(word.id))
            }
        }
    }
}

private struct WordSearchWordBadge: View {
    let word: WordSearchWord
    let found: Bool

    var body: some View {
        VStack(spacing: 5) {
            Text(word.yuhmu).font(.title3.bold()).foregroundStyle(.purple)
            Text(word.spanish).font(.subheadline).foregroundStyle(.secondary)
            Image(systemName: found ? "checkmark.circle.fill" : "circle")
                .foregroundStyle(found ? .teal : .gray)
        }
        .multilineTextAlignment(.center)
        .padding(12).frame(maxWidth: .infinity)
        .background(found ? Color.teal.opacity(0.12) : .white, in: RoundedRectangle(cornerRadius: 18))
        .overlay { RoundedRectangle(cornerRadius: 18).stroke(found ? Color.teal : .purple.opacity(0.15)) }
        .accessibilityElement(children: .combine)
        .accessibilityValue(found ? "Encontrada" : "Por encontrar")
    }
}

private struct WordSearchBoard: View {
    let session: WordSearchSession
    let onSubmit: () -> Void
    @State private var accessibleStart: Int?
    @GestureState private var dragging = false

    var body: some View {
        GeometryReader { geometry in
            let cellSize = geometry.size.width / CGFloat(session.side)
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 0), count: session.side), spacing: 0) {
                ForEach(session.cells) { cell in
                    let selected = session.selection.contains(cell.id)
                    let found = session.marked.contains(cell.id)
                    Text(cell.letter)
                        .font(.title2.bold()).minimumScaleFactor(0.5).lineLimit(1)
                        .foregroundStyle(selected || found ? .white : Color.purple)
                        .frame(maxWidth: .infinity).frame(height: cellSize)
                        .background {
                            RoundedRectangle(cornerRadius: 10)
                                .fill(selected ? Color.purple : found ? .teal : .white)
                                .padding(2)
                                .shadow(color: .purple.opacity(0.12), radius: 1, y: 3)
                        }
                        .accessibilityLabel("\(cell.letter), fila \(cell.id / session.side + 1), columna \(cell.id % session.side + 1)")
                        .accessibilityValue(found ? "Encontrada" : selected ? "Seleccionada" : "")
                        .accessibilityAddTraits(.isButton)
                        .accessibilityHint("Activa la primera letra y después la última para seleccionar una palabra.")
                        .accessibilityAction {
                            if let start = accessibleStart {
                                session.select(from: start, to: cell.id)
                                accessibleStart = nil
                                onSubmit()
                            } else {
                                accessibleStart = cell.id
                                session.select(from: cell.id, to: cell.id)
                            }
                        }
                }
            }
            .contentShape(Rectangle())
            .highPriorityGesture(
                DragGesture(minimumDistance: 0, coordinateSpace: .local)
                    .updating($dragging) { _, state, _ in state = true }
                    .onChanged { value in
                        accessibleStart = nil
                        updateSelection(start: value.startLocation, end: value.location, width: geometry.size.width)
                    }
                    .onEnded { value in
                        updateSelection(start: value.startLocation, end: value.location, width: geometry.size.width)
                        onSubmit()
                    }
            )
            .onChange(of: dragging) { _, active in
                if !active { session.cancelSelection() }
            }
        }
        .aspectRatio(1, contentMode: .fit)
        .padding(8)
        .background(Color.purple.opacity(0.08), in: RoundedRectangle(cornerRadius: 22))
    }

    private func updateSelection(start: CGPoint, end: CGPoint, width: CGFloat) {
        guard let first = index(at: start, width: width),
              let last = index(at: end, width: width) else {
            session.cancelSelection()
            return
        }
        session.select(from: first, to: last)
    }

    private func index(at point: CGPoint, width: CGFloat) -> Int? {
        guard width > 0, point.x >= 0, point.y >= 0,
              point.x < width, point.y < width else { return nil }
        let size = width / CGFloat(session.side)
        return Int(point.y / size) * session.side + Int(point.x / size)
    }
}
