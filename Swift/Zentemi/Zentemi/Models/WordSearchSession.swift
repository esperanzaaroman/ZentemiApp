import Foundation
import Observation

struct WordSearchCell: Identifiable {
    let id: Int
    let letter: String
}

struct WordSearchWord: Identifiable {
    let id: String
    let yuhmu: String
    let spanish: String
    var letters: [String] { yuhmu.uppercased().map { String($0) } }
}

@MainActor
@Observable
final class WordSearchSession {
    let level: WordSearchLevel
    let words: [WordSearchWord]
    var side: Int { level.side }
    private(set) var cells: [WordSearchCell] = []
    private(set) var found: Set<String> = []
    private(set) var marked: Set<Int> = []
    private(set) var selection: [Int] = []
    private(set) var elapsedSeconds: Double = 0
    private(set) var feedback = "Desliza desde la primera hasta la última letra."
    private var lastTick: Double?
    var finished: Bool { found.count == words.count }

    init(level: WordSearchLevel = .one) {
        self.level = level
        words = level.words
        let fillers = Array("AEIOUMNLPST").map { String($0) }
        var letters = (0..<(level.side * level.side)).map { _ in fillers.randomElement() ?? "A" }
        // Recorridos definidos por nivel; relleno nuevo en cada partida.
        let paths = level.paths
        for (word, path) in zip(words, paths) {
            for (letter, index) in zip(word.letters, path) { letters[index] = letter }
        }
        cells = letters.enumerated().map { WordSearchCell(id: $0.offset, letter: $0.element) }
    }

    func resumeClock() { lastTick = ProcessInfo.processInfo.systemUptime }
    func pauseClock() {
        tick()
        lastTick = nil
        selection = []
    }
    func tick() {
        guard let lastTick, !finished else { return }
        let now = ProcessInfo.processInfo.systemUptime
        elapsedSeconds += max(0, now - lastTick)
        self.lastTick = now
    }

    func select(from start: Int, to end: Int) {
        guard !finished, cells.indices.contains(start), cells.indices.contains(end) else { return }
        let rowDelta = end / side - start / side
        let columnDelta = end % side - start % side
        guard rowDelta == 0 || columnDelta == 0 || (level == .three && abs(rowDelta) == abs(columnDelta)) else {
            selection = []
            return
        }
        let distance = max(abs(rowDelta), abs(columnDelta))
        let step = rowDelta.signum() * side + columnDelta.signum()
        selection = (0...distance).map { start + $0 * step }
    }

    func cancelSelection() { selection = [] }

    enum Answer { case ignored, correct, incorrect }
    func submit() -> Answer {
        guard !finished, selection.count > 1 else {
            selection = []
            return .ignored
        }
        tick()
        let letters = selection.map { cells[$0].letter }
        let candidate = words.first {
            $0.letters == letters || $0.letters == Array(letters.reversed())
        }
        defer { selection = [] }
        guard let word = candidate else {
            feedback = "Busca una palabra completa en línea recta. ¡Inténtalo otra vez!"
            return .incorrect
        }
        guard !found.contains(word.id) else {
            feedback = "Ya encontraste esa palabra. ¡Busca las demás!"
            return .ignored
        }
        found.insert(word.id)
        marked.formUnion(selection)
        feedback = finished ? "¡Encontraste las \(words.count) palabras!" : "¡Muy bien! \(word.yuhmu) · \(word.spanish)"
        return .correct
    }
}
