import Foundation
import Observation

struct MemoryWord: Identifiable {
    let id: String
    let yuhmu: String
    let spanish: String
    let imageName: String

    // Manual de Yonca Gaspar y Alarcón Montero (2024), léxico pp. 74 y 80.
    // Se conserva la grafía del manual, incluidos tonos y nasalización.
    // Casa y flor se presentan sin el artículo Rà.
    static let levelOne: [MemoryWord] = [
        MemoryWord(id: "water", yuhmu: "Déhe", spanish: "Agua", imageName: "memoryWater"),
        MemoryWord(id: "house", yuhmu: "Ngǚ", spanish: "Casa", imageName: "memoryHouse"),
        MemoryWord(id: "flower", yuhmu: "Dò̱ni", spanish: "Flor", imageName: "memoryFlower"),
        MemoryWord(id: "moon", yuhmu: "Zä̀nä", spanish: "Luna", imageName: "memoryMoon"),
        MemoryWord(id: "tree", yuhmu: "Zä̌", spanish: "Árbol", imageName: "memoryTree")
    ]
}

struct MemoryCard: Identifiable {
    let word: MemoryWord
    let isPicture: Bool
    var id: String { word.id + (isPicture ? "-image" : "-text") }
}

@MainActor
@Observable
final class MemoryGameSession {
    let level: MemoryLevel
    let cards: [MemoryCard]

    init(level: MemoryLevel = .one) {
        self.level = level
        cards = level.words.flatMap {
            [MemoryCard(word: $0, isPicture: true), MemoryCard(word: $0, isPicture: false)]
        }.shuffled()
    }
    private(set) var selected: [String] = []
    private(set) var matched: Set<String> = []
    private(set) var elapsedSeconds: Double = 0
    private(set) var feedback = "Encuentra cada imagen y su palabra."
    private var mismatchRemaining: Double = 0
    private var lastTick: Double?
    var finished: Bool { matched.count == level.words.count }
    var resolving: Bool { mismatchRemaining > 0 }

    enum Selection { case ignored, revealed, matched, missed }

    func resumeClock() { lastTick = ProcessInfo.processInfo.systemUptime }
    func pauseClock() {
        tick()
        lastTick = nil
    }

    func tick() {
        guard let lastTick, !finished else { return }
        let now = ProcessInfo.processInfo.systemUptime
        let delta = max(0, now - lastTick)
        self.lastTick = now
        elapsedSeconds += delta
        if mismatchRemaining > 0 {
            mismatchRemaining = max(0, mismatchRemaining - delta)
            if mismatchRemaining == 0 { selected.removeAll() }
        }
    }

    func select(_ card: MemoryCard) -> Selection {
        guard !finished, !resolving, !matched.contains(card.word.id),
              !selected.contains(card.id) else { return .ignored }
        tick()
        selected.append(card.id)
        guard selected.count == 2,
              let first = cards.first(where: { $0.id == selected[0] }) else {
            feedback = "Ahora busca su pareja."
            return .revealed
        }
        if first.word.id == card.word.id {
            matched.insert(card.word.id)
            selected.removeAll()
            feedback = finished ? "¡Encontraste los \(level.words.count) pares!" : "¡Muy bien! \(card.word.yuhmu) · \(card.word.spanish)"
            return .matched
        }
        mismatchRemaining = 1.5
        feedback = "Observa las tarjetas e inténtalo otra vez."
        return .missed
    }
}
