import Foundation
import CoreGraphics
import Observation

@MainActor
@Observable
final class MazeGameSession {
    let level: MazeLevel
    private(set) var position: CGPoint
    private(set) var remainingCoins: [MazeCoin]
    private(set) var collectedCoinIDs: Set<Int> = []
    private(set) var lastCollectedCoin: MazeCoin?

    init(level: MazeLevel = .one, claimedCoinMask: Int = 0) {
        self.level = level
        position = level.start
        remainingCoins = level.coins.filter { claimedCoinMask & (1 << $0.id) == 0 }
    }
    var direction: CGVector = .zero
    private(set) var elapsedSeconds: Double = 0
    private(set) var finished = false
    private(set) var wrongExit: MazeExit?

    /// Devuelve una salida solo en el instante de alcanzarla.
    func advance(seconds: Double) -> MazeExit? {
        guard !finished else { return nil }
        elapsedSeconds += max(0, seconds)
        guard wrongExit == nil else { return nil }

        let length = hypot(direction.dx, direction.dy)
        guard length > 0.08 else { return nil }
        let normalization = max(1, length)
        let distance = CGFloat(min(max(seconds, 0), 0.05)) * 2.7
        let dx = direction.dx / normalization * distance
        let dy = direction.dy / normalization * distance

        // Pasos pequeños y ejes separados: deslizarse por una pared es posible,
        // saltarla por una caída de fotogramas o movimiento diagonal no lo es.
        let steps = max(1, Int(ceil(max(abs(dx), abs(dy)) / 0.08)))
        for _ in 0..<steps {
            let horizontal = CGPoint(x: position.x + dx / CGFloat(steps), y: position.y)
            if level.canOccupy(horizontal) { position = horizontal }
            let vertical = CGPoint(x: position.x, y: position.y + dy / CGFloat(steps))
            if level.canOccupy(vertical) { position = vertical }
        }

        // Se recogen solo las monedas pendientes; el premio se confirma al ganar.
        if let coin = remainingCoins.first(where: {
            hypot(position.x - $0.position.x, position.y - $0.position.y) < 0.42
        }) {
            collectedCoinIDs.insert(coin.id)
            lastCollectedCoin = coin
            remainingCoins.removeAll { $0.id == coin.id }
        }

        guard let exit = level.exits.first(where: { exit in
            switch exit.side {
            case .top:
                return position.y < 0.65 && abs(position.x - exit.center.x) < 0.4
            case .bottom:
                return position.y > CGFloat(level.rows) - 0.65
                    && abs(position.x - exit.center.x) < 0.4
            case .left:
                return position.x < 0.65 && abs(position.y - exit.center.y) < 0.4
            case .right:
                return position.x > CGFloat(level.columns) - 0.65
                    && abs(position.y - exit.center.y) < 0.4
            }
        }) else { return nil }

        direction = .zero
        if exit.isCorrect {
            finished = true
        } else {
            wrongExit = exit
        }
        return exit
    }

    func continueAfterWrongExit() {
        guard let wrongExit else { return }
        position = wrongExit.returnPosition
        direction = .zero
        self.wrongExit = nil
    }
}
