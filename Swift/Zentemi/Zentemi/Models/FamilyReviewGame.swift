import Foundation
import CoreGraphics
import Observation

@MainActor
@Observable
final class FamilyReviewGame {
    enum Event { case none, pickup, correct, wrong, finished }
    let words = FamilyTerm.review
    let targets: [FamilyTerm] = [.uncle, .brother, .godmother, .grandmother]
    private(set) var position = CGPoint(x: 0.49, y: 0.5)
    private(set) var carried: FamilyTerm?
    private(set) var delivered: Set<FamilyTerm> = []
    private(set) var mistakes = 0
    private(set) var feedback = "Acércate a una palabra para recogerla."
    private var lastTarget: FamilyTerm?
    var finished: Bool { delivered.count == words.count }

    func wordPosition(_ term: FamilyTerm) -> CGPoint {
        let index = words.firstIndex(of: term) ?? 0
        return CGPoint(x: 0.19, y: 0.14 + CGFloat(index) * 0.24)
    }
    func targetPosition(_ term: FamilyTerm) -> CGPoint {
        let index = targets.firstIndex(of: term) ?? 0
        return CGPoint(x: 0.80, y: 0.14 + CGFloat(index) * 0.24)
    }
    func move(_ direction: CGVector, delta: Double) -> Event {
        guard !finished else { return .none }
        let dt = CGFloat(min(max(delta, 0), 0.06))
        position.x = min(0.94, max(0.06, position.x + direction.dx * dt * 0.42))
        position.y = min(0.94, max(0.06, position.y + direction.dy * dt * 0.32))
        return interact()
    }
    // Alternativa accesible: visitar un punto mediante una acción de VoiceOver.
    func visit(_ point: CGPoint) -> Event {
        guard !finished else { return .none }
        position = point
        lastTarget = nil
        return interact()
    }
    private func near(_ point: CGPoint) -> Bool {
        hypot(position.x - point.x, position.y - point.y) < 0.105
    }
    private func interact() -> Event {
        if carried == nil {
            lastTarget = nil
            if let term = words.first(where: { !delivered.contains($0) && near(wordPosition($0)) }) {
                carried = term
                feedback = "Llevas \(term.yuhmu). Busca su traducción."
                return .pickup
            }
            return .none
        }
        guard let target = targets.first(where: { near(targetPosition($0)) }) else {
            lastTarget = nil
            return .none
        }
        guard lastTarget != target else { return .none }
        lastTarget = target
        guard let carried else { return .none }
        if target == carried {
            delivered.insert(carried)
            self.carried = nil
            feedback = finished ? "¡La reunión está lista! Entregaste las cuatro palabras." : "¡Correcto! \(carried.yuhmu) · \(carried.meaning)"
            return finished ? .finished : .correct
        }
        mistakes += 1
        feedback = "Aquí dice \(target.meaning). Sigue buscando dónde llevar \(carried.yuhmu)."
        return .wrong
    }
}
