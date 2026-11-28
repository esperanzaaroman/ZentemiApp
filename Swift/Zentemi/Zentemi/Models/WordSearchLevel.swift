import Foundation

enum WordSearchLevel: Int, CaseIterable, Identifiable {
    case one = 1, two, three
    var id: Int { rawValue }
    var side: Int { rawValue * 2 + 4 }
    var coins: Int {
        switch self { case .one: 300; case .two: 450; case .three: 700 }
    }
    var points: Int {
        switch self { case .one: 500; case .two: 1_400; case .three: 2_500 }
    }
    var instructions: String {
        self == .three
            ? "Busca en horizontal, vertical y diagonal. Algunas palabras están al revés."
            : "Busca en horizontal y vertical."
    }
    var words: [WordSearchWord] {
        if self == .one {
            return [
                WordSearchWord(id: "water", yuhmu: "Déhe", spanish: "Agua"),
                WordSearchWord(id: "flower", yuhmu: "Dò̱ni", spanish: "Flor"),
                WordSearchWord(id: "moon", yuhmu: "Zä̀nä", spanish: "Luna")
            ]
        }
        // Reutiliza las grafías del manual ya revisadas para Memorama.
        let ids = self == .two
            ? ["bee", "spider", "eagle", "horse", "sheep"]
            : ["pillow", "ring", "key", "atole", "pumpkin", "donkey", "rainbow"]
        let vocabulary = MemoryLevel.two.words + MemoryLevel.three.words
        return ids.compactMap { id in
            guard let word = vocabulary.first(where: { $0.id == id }) else { return nil }
            return WordSearchWord(id: word.id, yuhmu: word.yuhmu, spanish: word.spanish)
        }
    }

    // Recorridos diseñados sin colisiones; cada vocal con sus marcas ocupa una casilla.
    var paths: [[Int]] {
        switch self {
        case .one:
            [[7, 8, 9, 10], [5, 11, 17, 23], [25, 26, 27, 28]]
        case .two:
            [[1, 2, 3, 4], [15, 23, 31, 39], [25, 26, 27, 28, 29],
             [24, 32, 40, 48], [58, 59, 60, 61]]
        case .three:
            [[0, 11, 22, 33, 44], [9, 8, 7, 6, 5, 4],
             [19, 29, 39, 49, 59, 69, 79], [90, 81, 72, 63],
             [96, 97], [66, 56, 46, 36, 26], [89, 88, 87, 86, 85, 84, 83, 82]]
        }
    }
}
