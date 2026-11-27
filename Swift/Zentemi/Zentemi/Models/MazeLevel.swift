import Foundation
import CoreGraphics

enum MazeExitSide: Equatable {
    case top, left, right, bottom
}

struct MazeExit: Identifiable {
    let id: Int
    let column: Int
    var row: Int = 0
    var side: MazeExitSide = .top
    let translation: String
    let isCorrect: Bool

    var center: CGPoint {
        CGPoint(x: CGFloat(column) + 0.5, y: CGFloat(row) + 0.5)
    }

    var returnPosition: CGPoint {
        switch side {
        case .top: return CGPoint(x: center.x, y: center.y + 1)
        case .bottom: return CGPoint(x: center.x, y: center.y - 1)
        case .left: return CGPoint(x: center.x + 1, y: center.y)
        case .right: return CGPoint(x: center.x - 1, y: center.y)
        }
    }
}

struct MazeCoin: Identifiable {
    let id: Int
    let position: CGPoint
}

struct MazeLevel: Identifiable {
    let id: Int
    let phrase: String
    let backgroundName: String
    let usesStone: Bool
    let map: [[Character]]
    let start: CGPoint
    let exits: [MazeExit]
    let coins: [MazeCoin]
    var usesBrick = false
    var followsPlayerVertically = false
    var usesIce = false
    var startsInside = false
    var usesLava = false

    var columns: Int { map.first?.count ?? 0 }
    var rows: Int { map.count }
    static let petRadius: CGFloat = 0.35

    static let one = MazeLevel(
        id: 1, phrase: "Buenos días", backgroundName: "mazeGrassBackground", usesStone: false,
        map: [
            "#.###.###.#",
            "#...#.....#",
            "###.#.###.#",
            "#...#...#.#",
            "#.#####.#.#",
            "#.....#...#",
            "#.###.###.#",
            "#...#.....#",
            "###.#####.#",
            "#...#.....#",
            "#.###.###.#",
            "#.....#...#",
            "#####.#####"
        ].map { Array($0) },
        start: CGPoint(x: 5.5, y: 12.5),
        exits: [
            MazeExit(id: 0, column: 1, translation: "Nzengwa", isCorrect: false),
            MazeExit(id: 1, column: 5, translation: "Té xki hats’i", isCorrect: true),
            MazeExit(id: 2, column: 9, translation: "Ra hyadi", isCorrect: false)
        ],
        coins: []
    )

    static let two = MazeLevel(
        id: 2, phrase: "Necesito ayuda", backgroundName: "mazeStoneBackground", usesStone: true,
        map: [
            "###.#######.###",
            "#...#.....#...#",
            "#.#.#.###.#.#.#",
            "#.#...#...#.#.#",
            "#.#####.###.#.#",
            "....#...#...#.#",
            "###.#.#.#.###.#",
            "#...#.#...#...#",
            "#.###.#.###.#.#",
            "#.....#.....#.#",
            "#.#####.###.#.#",
            "#...#...#......",
            "###.#.###.###.#",
            "#...#.....#...#",
            "#.###.#####.#.#",
            "#.......#.....#",
            "#######.#######"
        ].map { Array($0) },
        start: CGPoint(x: 7.5, y: 16.5),
        exits: [
            MazeExit(id: 0, column: 3, translation: "Nzengwa", isCorrect: false),
            MazeExit(id: 1, column: 11, translation: "Té xki hats’i", isCorrect: false),
            MazeExit(id: 2, column: 0, row: 5, side: .left, translation: "Ra hyadi", isCorrect: false),
            // Texto provisional solicitado por el usuario, pendiente de traducción definitiva.
            MazeExit(id: 3, column: 14, row: 11, side: .right, translation: "*yuhmu*", isCorrect: true)
        ],
        coins: [
            MazeCoin(id: 0, position: CGPoint(x: 1.5, y: 1.5)),
            MazeCoin(id: 1, position: CGPoint(x: 13.5, y: 3.5)),
            MazeCoin(id: 2, position: CGPoint(x: 1.5, y: 13.5)),
            MazeCoin(id: 3, position: CGPoint(x: 9.5, y: 15.5)),
            MazeCoin(id: 4, position: CGPoint(x: 7.5, y: 9.5))
        ]
    )

    static let three = MazeLevel(
        id: 3, phrase: "palabraX", backgroundName: "mazeStoneBackground", usesStone: false,
        map: [
            "#.#######.#",
            "#...#.....#",
            "###.#.###.#",
            "#...#...#.#",
            "#.#####.#.#",
            "#.....#...#",
            "#.###.###.#",
            "#...#.....#",
            "###.#####.#",
            "....#.....#",
            "#.###.###.#",
            "#.....#...#",
            "#.###.###.#",
            "#...#.....#",
            "###.#.###.#",
            "#...#...#.#",
            "#.#####.#.#",
            "#.....#...#",
            "#.###.###.#",
            "#...#.....#",
            "###.#####.#",
            "#...#.....#",
            "#.###.###.#",
            "#.....#....",
            "#.###.###.#",
            "#...#.....#",
            "###.#.###.#",
            "#...#...#.#",
            "#.#####.#.#",
            "#.....#...#",
            "#.###.###.#",
            "#.........#",
            "#####.#####"
        ].map { Array($0) },
        start: CGPoint(x: 5.5, y: 32.5),
        exits: [
            MazeExit(id: 0, column: 1, translation: "salidaMal1", isCorrect: false),
            MazeExit(id: 1, column: 9, translation: "salidaBuena", isCorrect: true),
            MazeExit(id: 2, column: 0, row: 9, side: .left, translation: "salidaMal2", isCorrect: false),
            MazeExit(id: 3, column: 10, row: 23, side: .right, translation: "salidaMal3", isCorrect: false)
        ],
        coins: [
            MazeCoin(id: 0, position: CGPoint(x: 1.5, y: 31.5)),
            MazeCoin(id: 1, position: CGPoint(x: 9.5, y: 29.5)),
            MazeCoin(id: 2, position: CGPoint(x: 3.5, y: 25.5)),
            MazeCoin(id: 3, position: CGPoint(x: 7.5, y: 23.5)),
            MazeCoin(id: 4, position: CGPoint(x: 1.5, y: 21.5)),
            MazeCoin(id: 5, position: CGPoint(x: 5.5, y: 17.5)),
            MazeCoin(id: 6, position: CGPoint(x: 9.5, y: 13.5)),
            MazeCoin(id: 7, position: CGPoint(x: 1.5, y: 9.5)),
            MazeCoin(id: 8, position: CGPoint(x: 7.5, y: 5.5)),
            MazeCoin(id: 9, position: CGPoint(x: 3.5, y: 1.5))
        ],
        usesBrick: true,
        followsPlayerVertically: true
    )

    // Inicio central con tres direcciones. El recorrido hacia la salida inferior
    // atraviesa la zona superior antes de descender; los cruces permiten desvíos.
    static let four = MazeLevel(
        id: 4, phrase: "palabraHielo", backgroundName: "mazeIceBackground", usesStone: false,
        map: [
            "#.#######.#",
            "#.#.......#",
            "#...#.#.###",
            "#.#.#.#...#",
            "#.#.#.###.#",
            "#.#.#...#.#",
            "#.#.###.#.#",
            "#.#...#.#.#",
            "#.###.#.#.#",
            "#...#.#.#.#",
            "###.#.#.#.#",
            "....#...#.#",
            "#.#####.#.#",
            "#.#.....#.#",
            "#.###.###.#",
            "#...#...#.#",
            "###.#.###.#",
            "#.#.#.#...#",
            "#.#.#.#.###",
            "#...#.#...#",
            "#.###.###.#",
            "#.#.....#.#",
            "#.#.#####.#",
            "#.#.#.....#",
            "#.###.###.#",
            "#...#.#...#",
            "###.#.#.###",
            "#.#...#...#",
            "#.#######.#",
            "#.....#....",
            "#####.#.###",
            "#.....#...#",
            "#.#######.#",
            "#...#...#.#",
            "#.#.#.#.#.#",
            "#.#...#...#",
            "#.#########",
            "#.#.......#",
            "#.###.###.#",
            "#.....#...#",
            "#########.#"
        ].map { Array($0) },
        start: CGPoint(x: 5.5, y: 21.5),
        exits: [
            MazeExit(id: 0, column: 1, translation: "salidaMal1", isCorrect: false),
            MazeExit(id: 1, column: 9, translation: "salidaMal2", isCorrect: false),
            MazeExit(id: 2, column: 0, row: 11, side: .left, translation: "salidaMal3", isCorrect: false),
            MazeExit(id: 3, column: 10, row: 29, side: .right, translation: "salidaMal4", isCorrect: false),
            MazeExit(id: 4, column: 9, row: 40, side: .bottom, translation: "salidaBuena", isCorrect: true)
        ],
        coins: [
            MazeCoin(id: 0, position: CGPoint(x: 9.5, y: 3.5)),
            MazeCoin(id: 1, position: CGPoint(x: 9.5, y: 5.5)),
            MazeCoin(id: 2, position: CGPoint(x: 9.5, y: 7.5)),
            MazeCoin(id: 3, position: CGPoint(x: 3.5, y: 9.5)),
            MazeCoin(id: 4, position: CGPoint(x: 1.5, y: 13.5)),
            MazeCoin(id: 5, position: CGPoint(x: 3.5, y: 15.5)),
            MazeCoin(id: 6, position: CGPoint(x: 1.5, y: 17.5)),
            MazeCoin(id: 7, position: CGPoint(x: 1.5, y: 19.5)),
            MazeCoin(id: 8, position: CGPoint(x: 5.5, y: 23.5)),
            MazeCoin(id: 9, position: CGPoint(x: 3.5, y: 25.5)),
            MazeCoin(id: 10, position: CGPoint(x: 1.5, y: 27.5)),
            MazeCoin(id: 11, position: CGPoint(x: 5.5, y: 31.5)),
            MazeCoin(id: 12, position: CGPoint(x: 1.5, y: 33.5)),
            MazeCoin(id: 13, position: CGPoint(x: 1.5, y: 35.5)),
            MazeCoin(id: 14, position: CGPoint(x: 9.5, y: 37.5))
        ],
        followsPlayerVertically: true,
        usesIce: true,
        startsInside: true
    )

    // Reto final: inicio central, ramificaciones y seis salidas en cuatro bordes.
    static let five = MazeLevel(
        id: 5, phrase: "palabraLava", backgroundName: "mazeLavaBackground", usesStone: false,
        map: [
            "#.#######.#",
            "#.......#.#",
            "#######.#.#",
            "#...#.#...#",
            "#.#.#.###.#",
            "#.#.....#.#",
            "#.###.###.#",
            "#.#...#...#",
            "#.###.#.###",
            "#...#.#.#.#",
            "#.#.#.#.#.#",
            "#.#.#.#.#.#",
            "###.#.#.#.#",
            "....#.#...#",
            "#.###.###.#",
            "#.#...#.#.#",
            "#.#.###.#.#",
            "#.#.....#.#",
            "#.#####.#.#",
            "#...#.#.#.#",
            "#.#.#.#.#.#",
            "#.#.#...#.#",
            "###.#.###.#",
            "#...#.#...#",
            "#.###.#.###",
            "#.#...#...#",
            "#.###.###.#",
            "#...#...#.#",
            "###.###.#.#",
            "#...#.#.#.#",
            "#.###.#.#.#",
            "#.#.....#.#",
            "#.#####.#.#",
            "#.....#.#.#",
            "#.###.###.#",
            "#.#.#.....#",
            "#.#.#######",
            "#.#.....#.#",
            "#.#.###.#.#",
            "#...#.#...#",
            "#####.###.#",
            "#.#.....#.#",
            "#.#.###.#.#",
            "#...#.#....",
            "#.###.#####",
            "#.#.......#",
            "#.#.###.#.#",
            "#.#.#...#.#",
            "#.#.#.###.#",
            "#.#.#...#.#",
            "#.#####.#.#",
            "#.......#.#",
            "#########.#",
            "#.......#.#",
            "#.###.#.#.#",
            "#.#...#.#.#",
            "###.#.###.#",
            "#...#.#...#",
            "#.#####.###",
            "#.........#",
            "#.#######.#"
        ].map { Array($0) },
        start: CGPoint(x: 5.5, y: 31.5),
        exits: [
            MazeExit(id: 0, column: 1, translation: "salidaMal1", isCorrect: false),
            MazeExit(id: 1, column: 9, translation: "salidaMal2", isCorrect: false),
            MazeExit(id: 2, column: 0, row: 13, side: .left, translation: "salidaMal3", isCorrect: false),
            MazeExit(id: 3, column: 10, row: 43, side: .right, translation: "salidaMal4", isCorrect: false),
            MazeExit(id: 4, column: 1, row: 60, side: .bottom, translation: "salidaMal5", isCorrect: false),
            MazeExit(id: 5, column: 9, row: 60, side: .bottom, translation: "salidaBuena", isCorrect: true)
        ],
        coins: [
            MazeCoin(id: 0, position: CGPoint(x: 7.5, y: 3.5)),
            MazeCoin(id: 1, position: CGPoint(x: 9.5, y: 5.5)),
            MazeCoin(id: 2, position: CGPoint(x: 7.5, y: 9.5)),
            MazeCoin(id: 3, position: CGPoint(x: 7.5, y: 11.5)),
            MazeCoin(id: 4, position: CGPoint(x: 9.5, y: 15.5)),
            MazeCoin(id: 5, position: CGPoint(x: 9.5, y: 17.5)),
            MazeCoin(id: 6, position: CGPoint(x: 9.5, y: 21.5)),
            MazeCoin(id: 7, position: CGPoint(x: 9.5, y: 23.5)),
            MazeCoin(id: 8, position: CGPoint(x: 9.5, y: 27.5)),
            MazeCoin(id: 9, position: CGPoint(x: 9.5, y: 29.5)),
            MazeCoin(id: 10, position: CGPoint(x: 9.5, y: 33.5)),
            MazeCoin(id: 11, position: CGPoint(x: 3.5, y: 35.5)),
            MazeCoin(id: 12, position: CGPoint(x: 5.5, y: 39.5)),
            MazeCoin(id: 13, position: CGPoint(x: 1.5, y: 41.5)),
            MazeCoin(id: 14, position: CGPoint(x: 3.5, y: 45.5)),
            MazeCoin(id: 15, position: CGPoint(x: 3.5, y: 47.5)),
            MazeCoin(id: 16, position: CGPoint(x: 9.5, y: 51.5)),
            MazeCoin(id: 17, position: CGPoint(x: 1.5, y: 53.5)),
            MazeCoin(id: 18, position: CGPoint(x: 1.5, y: 55.5)),
            MazeCoin(id: 19, position: CGPoint(x: 5.5, y: 57.5))
        ],
        followsPlayerVertically: true,
        startsInside: true,
        usesLava: true
    )

    func isWall(column: Int, row: Int) -> Bool {
        guard (0..<columns).contains(column), (0..<rows).contains(row) else { return true }
        return map[row][column] == "#"
    }

    func canOccupy(_ point: CGPoint) -> Bool {
        let radius = Self.petRadius
        guard point.x >= radius, point.y >= radius,
              point.x <= CGFloat(columns) - radius,
              point.y <= CGFloat(rows) - radius else { return false }
        for row in Int(floor(point.y - radius))...Int(floor(point.y + radius)) {
            for column in Int(floor(point.x - radius))...Int(floor(point.x + radius)) {
                guard isWall(column: column, row: row) else { continue }
                let closestX = min(max(point.x, CGFloat(column)), CGFloat(column + 1))
                let closestY = min(max(point.y, CGFloat(row)), CGFloat(row + 1))
                let dx = point.x - closestX
                let dy = point.y - closestY
                if dx * dx + dy * dy < radius * radius { return false }
            }
        }
        return true
    }

    static func timeText(_ seconds: Double) -> String {
        let value = max(0, Int(seconds.rounded(.up)))
        return String(format: "%02d:%02d", value / 60, value % 60)
    }
}

struct MazeReward {
    let baseCoins: Int
    let collectedCoins: Int
    let points: Int
    var totalCoins: Int { baseCoins + collectedCoins }

    static let none = MazeReward(baseCoins: 0, collectedCoins: 0, points: 0)
}
