import Foundation

enum MemoryLevel: Int, CaseIterable, Identifiable {
    case one = 1, two, three

    var id: Int { rawValue }
    var coins: Int {
        switch self {
        case .one: 300
        case .two: 450
        case .three: 700
        }
    }
    var points: Int {
        switch self {
        case .one: 500
        case .two: 1_400
        case .three: 2_500
        }
    }
    var theme: String {
        switch self {
        case .one: "Agua, casa, flor, luna y árbol"
        case .two: "Conoce a los animales"
        case .three: "Objetos, alimentos y naturaleza"
        }
    }
    var words: [MemoryWord] {
        switch self {
        case .one: MemoryWord.levelOne
        case .two: Self.animals
        case .three: Self.everydayWords
        }
    }

    // Manual de Yonca Gaspar y Alarcón Montero (2024), léxico pp. 74, 75 y 80.
    // Los signos combinantes son parte de la palabra, no decoración.
    private static let animals: [MemoryWord] = [
        MemoryWord(id: "bee", yuhmu: "Gáne", spanish: "Abeja", imageName: "memoryBee"),
        MemoryWord(id: "spider", yuhmu: "Méxe", spanish: "Araña", imageName: "memorySpider"),
        MemoryWord(id: "eagle", yuhmu: "Nxù̱ni", spanish: "Águila", imageName: "memoryEagle"),
        MemoryWord(id: "hare", yuhmu: "Dé", spanish: "Liebre", imageName: "memoryHare"),
        MemoryWord(id: "horse", yuhmu: "Tó̱ge", spanish: "Caballo", imageName: "memoryHorse"),
        MemoryWord(id: "donkey", yuhmu: "’Ǎxnu", spanish: "Asno", imageName: "memoryDonkey"),
        MemoryWord(id: "sheep", yuhmu: "Dé̱ti", spanish: "Borrego", imageName: "memorySheep")
    ]

    // Se conserva Rà en las dos entradas que el léxico da con artículo.
    private static let everydayWords: [MemoryWord] = [
        MemoryWord(id: "pillow", yuhmu: "Nhǚni", spanish: "Almohada", imageName: "memoryPillow"),
        MemoryWord(id: "ring", yuhmu: "Mphòxo", spanish: "Anillo", imageName: "memoryRing"),
        MemoryWord(id: "earring", yuhmu: "Ts’ú̱ngü", spanish: "Arete", imageName: "memoryEarring"),
        MemoryWord(id: "pumpkin", yuhmu: "Mǚ", spanish: "Calabaza", imageName: "memoryPumpkin"),
        MemoryWord(id: "sweetPotato", yuhmu: "Bó̱’kw’ä", spanish: "Camote", imageName: "memorySweetPotato"),
        MemoryWord(id: "atole", yuhmu: "T’èi", spanish: "Atole", imageName: "memoryAtole"),
        MemoryWord(id: "star", yuhmu: "Rà tsó̱", spanish: "La estrella", imageName: "memoryStar"),
        MemoryWord(id: "broom", yuhmu: "Rà ’màxi", spanish: "La escoba", imageName: "memoryBroom"),
        MemoryWord(id: "key", yuhmu: "Ntshóki", spanish: "Llave", imageName: "memoryKey"),
        MemoryWord(id: "rainbow", yuhmu: "’Mè̱khu̱ni", spanish: "Arcoíris", imageName: "memoryRainbow")
    ]
}
