import Foundation
import Observation

// Manual para la escritura de los sonidos del yuhmu (2024), pp. 74, 79, 81 y 84.
// Entradas de vocabulario: no se inventan frases posesivas.
enum FamilyTerm: String, CaseIterable, Identifiable {
    case brother, sisterByWoman, sisterByMan, grandmother, grandfather, uncle, godmother
    var id: String { rawValue }
    var yuhmu: String {
        switch self {
        case .brother: "Dó̱t’e"
        case .sisterByWoman: "Zíkü"
        case .sisterByMan: "Nkhǘ"
        case .grandmother: "Tsǘ"
        case .grandfather: "Dä̀ta"
        case .uncle: "’Wě"
        case .godmother: "Měkhä"
        }
    }
    var meaning: String {
        switch self {
        case .brother: "Hermano"
        case .sisterByWoman: "Hermana · habla una mujer"
        case .sisterByMan: "Hermana · habla un hombre"
        case .grandmother: "Abuela"
        case .grandfather: "Abuelo"
        case .uncle: "Tío"
        case .godmother: "Madrina"
        }
    }
    var imageName: String {
        switch self {
        case .brother: "familyLeo"
        case .sisterByWoman: "familyAna"
        case .sisterByMan: "familyLuz"
        case .grandmother: "familyGrandmother"
        case .grandfather: "familyGrandfather"
        case .uncle: "familyUncle"
        case .godmother: "familyGodmother"
        }
    }
    static let grandparents: [FamilyTerm] = [.grandmother, .grandfather]
    static let review: [FamilyTerm] = [.brother, .grandmother, .uncle, .godmother]
}

struct FamilyLessonReward {
    let coins: Int
    let xp: Int
    let startingXP: Int
}

@MainActor
@Observable
final class FamilyLessonSession {
    private(set) var step = 0
    private(set) var teaching = true
    private(set) var checked = false
    private(set) var correctAnswers = 0
    private(set) var lastAnswerCorrect = false
    private(set) var elapsedSeconds: Double = 0
    private(set) var finished = false
    private var lastTick: Double?
    var choice: FamilyTerm?
    var missingLetter: String?
    var selectedLabel: FamilyTerm?
    private(set) var assignments: [FamilyTerm: FamilyTerm] = [:]
    private(set) var orderedLetters: [String] = []
    let letterBank = ["ä", "k", "M", "h", "ě"]

    var teachingTerms: [FamilyTerm] {
        switch step {
        case 0: [.brother, .sisterByWoman, .sisterByMan]
        case 1: FamilyTerm.grandparents
        case 2: [.uncle]
        case 3: [.godmother]
        default: FamilyTerm.review
        }
    }
    var title: String {
        ["Hermanos y hermanas", "Conoce a los abuelos", "Una visita del tío",
         "La madrina", "La reunión familiar"][step]
    }
    var teachingText: String {
        switch step {
        case 0: "Leo es el hermano de Ana y Luz: hermano se escribe Dó̱t’e. Para hermana se usa Zíkü cuando habla una mujer y Nkhǘ cuando habla un hombre."
        case 1: "¡Llegaron los abuelos! Abuela se escribe Tsǘ y abuelo se escribe Dä̀ta. Toca cada tarjeta para descubrir su palabra."
        case 2: "¡El tío viene de visita! Tío se escribe ’Wě. Observa el signo inicial y la marca sobre la e. Después completarás esta palabra."
        case 3: "También nos acompaña la madrina: Měkhä. Mira el orden de sus letras y las marcas de las vocales. Luego formarás la palabra."
        default: "Ayuda a Maicito a preparar la reunión. Recoge cada palabra en Yuhmu y llévala a su traducción. Repasarás hermanos, abuelos, tío y madrina."
        }
    }
    var question: String {
        switch step {
        case 0: "Leo es el hermano de Ana. ¿Qué palabra significa hermano?"
        case 1: "Une cada palabra con su abuelo o abuela."
        case 2: "Completa la palabra para tío: ’W_"
        case 3: "Ordena las letras para escribir madrina."
        default: "Recoge y entrega las cuatro palabras."
        }
    }
    var explanation: String {
        switch step {
        case 0: "Dó̱t’e significa hermano. Las otras dos palabras significan hermana, según quién habla."
        case 1: "Tsǘ significa abuela; Dä̀ta significa abuelo."
        case 2: "La vocal es ě. La palabra completa es ’Wě: tío."
        case 3: "El orden correcto es M · ě · k · h · ä: Měkhä, madrina."
        default: "Dó̱t’e = hermano; Tsǘ = abuela; ’Wě = tío; Měkhä = madrina."
        }
    }
    var canVerify: Bool {
        guard !teaching, !checked, !finished else { return false }
        switch step {
        case 0: return choice != nil
        case 1: return assignments.count == 2
        case 2: return missingLetter != nil
        case 3: return orderedLetters.count == letterBank.count
        default: return false
        }
    }
    func assign(_ label: FamilyTerm, to target: FamilyTerm) {
        guard step == 1, !teaching, !checked,
              FamilyTerm.grandparents.contains(label), FamilyTerm.grandparents.contains(target) else { return }
        assignments = assignments.filter { $0.value != label }
        assignments[target] = label
        selectedLabel = nil
    }
    func appendLetter(_ letter: String) {
        guard step == 3, !checked, !orderedLetters.contains(letter), letterBank.contains(letter) else { return }
        orderedLetters.append(letter)
    }
    func undoLetter() {
        guard !checked, !orderedLetters.isEmpty else { return }
        orderedLetters.removeLast()
    }
    func startExercise() { teaching = false }

    @discardableResult
    func verify() -> Bool {
        guard canVerify else { return false }
        tick()
        switch step {
        case 0: record(choice == .brother)
        case 1: record(FamilyTerm.grandparents.allSatisfy { assignments[$0] == $0 })
        case 2: record(missingLetter == "ě")
        case 3: record(orderedLetters.joined() == FamilyTerm.godmother.yuhmu)
        default: return false
        }
        return true
    }
    func completeMiniGame(mistakes: Int) {
        guard step == 4, !teaching, !checked else { return }
        tick()
        record(mistakes == 0)
    }
    private func record(_ correct: Bool) {
        lastAnswerCorrect = correct
        if correct { correctAnswers += 1 }
        checked = true
    }
    func advance() {
        guard checked, !finished else { return }
        tick()
        if step == 4 {
            finished = true
            lastTick = nil
        } else {
            step += 1
            teaching = true
            checked = false
            choice = nil
            missingLetter = nil
            selectedLabel = nil
            assignments = [:]
            orderedLetters = []
        }
    }
    func resumeClock() { if !finished { lastTick = ProcessInfo.processInfo.systemUptime } }
    func pauseClock() { tick(); lastTick = nil }
    func tick() {
        guard let lastTick, !finished else { return }
        let now = ProcessInfo.processInfo.systemUptime
        elapsedSeconds += max(0, now - lastTick)
        self.lastTick = now
    }
}
