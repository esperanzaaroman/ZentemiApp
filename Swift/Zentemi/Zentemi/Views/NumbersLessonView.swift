import SwiftUI

// Grafías del documento Zentemi4.pdf, tabla «Números del 1 al 10».
private enum NumberVocabulary {
    static let words = ["N’á", "Yóho", "Hyǘ", "Gǒho", "Kú̱t’a"]
    static let spanish = ["Uno", "Dos", "Tres", "Cuatro", "Cinco"]
    static func word(_ value: Int) -> String { words[value - 1] }
}

struct NumbersLessonView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    let onLessonCompleted: () -> Void
    @State private var step = 0
    @State private var teaching = true
    @State private var checked = false
    @State private var correct = false
    @State private var score = 0
    @State private var tapped: Set<Int> = []
    @State private var choice: Int?
    @State private var basket = 0
    @State private var reward: FamilyLessonReward?
    @State private var elapsed: Double = 0
    @State private var lastTick: Double?
    @State private var showExit = false

    private let titles = ["Toca y cuenta", "¿Cuántos hay?", "Llena la canasta", "El número perdido", "Conecta las cantidades"]
    private let explanations = [
        "Contaste cinco objetos: Kú̱t’a significa cinco.",
        "Hay tres flores. Tres se escribe Hyǘ.",
        "Yóho significa dos: la canasta necesita dos objetos.",
        "Después de Hyǘ (tres) viene Gǒho (cuatro).",
        "N’á: uno · Yóho: dos · Hyǘ: tres · Gǒho: cuatro · Kú̱t’a: cinco."
    ]
    private var canVerify: Bool {
        switch step {
        case 0: tapped.count == 5
        case 1, 3: choice != nil
        case 2: true
        default: false
        }
    }

    var body: some View {
        ZStack {
            LinearGradient(colors: [.purple.opacity(0.07), .orange.opacity(0.09)],
                           startPoint: .top, endPoint: .bottom).ignoresSafeArea()
            if let reward {
                LessonCompleteView(correctAnswers: score, totalAnswers: 5,
                                   elapsedSeconds: Int(elapsed), familyResult: reward,
                                   onContinue: onLessonCompleted)
            } else if appState.familyRewardClaimed {
                ScrollViewReader { proxy in
                    ScrollView {
                        VStack(spacing: 22) {
                            HStack {
                                Button { showExit = true } label: {
                                    Image(systemName: "xmark").padding(14).background(.white, in: Circle())
                                }.accessibilityLabel("Salir de Números")
                                ProgressView(value: Double(step + 1), total: 5).tint(.teal)
                                Text("\(step + 1)/5").font(.headline).foregroundStyle(.teal)
                            }.id("numbersTop")
                            Text("Números · \(titles[step])").font(.title2.bold())
                                .foregroundStyle(.purple).multilineTextAlignment(.center)
                            if teaching {
                                NumbersDiscoveryView {
                                    teaching = false
                                    proxy.scrollTo("numbersTop", anchor: .top)
                                }
                            } else {
                                NumbersActivityView(step: step, tapped: $tapped, choice: $choice, basket: $basket)
                                    .disabled(checked || showExit || scenePhase != .active)
                                if step == 4 {
                                    NumbersConnectionsView { mistakes in finishAnswer(mistakes == 0) }
                                        .disabled(checked || showExit || scenePhase != .active)
                                }
                                if checked {
                                    VStack(spacing: 12) {
                                        Label(correct ? "¡Muy bien!" : "Sigamos aprendiendo",
                                              systemImage: correct ? "checkmark.circle.fill" : "lightbulb.fill")
                                            .font(.headline).foregroundStyle(correct ? .teal : .orange)
                                        Text(explanations[step])
                                    }.padding(20).frame(maxWidth: .infinity)
                                        .background(.white, in: RoundedRectangle(cornerRadius: 22))
                                    Button(step == 4 ? "Ver resultados" : "Siguiente") {
                                        tick()
                                        if step == 4 {
                                            reward = appState.completeNumbersLesson(correctAnswers: score, seconds: elapsed)
                                            lastTick = nil
                                        } else {
                                            step += 1
                                            checked = false
                                            choice = nil
                                        }
                                        proxy.scrollTo("numbersTop", anchor: .top)
                                    }.buttonStyle(MazePrimaryButtonStyle())
                                } else if step < 4 {
                                    Button("Verificar") {
                                        let answer: Bool
                                        switch step {
                                        case 0: answer = tapped.count == 5
                                        case 1: answer = choice == 3
                                        case 2: answer = basket == 2
                                        default: answer = choice == 4
                                        }
                                        finishAnswer(answer)
                                    }.buttonStyle(MazePrimaryButtonStyle())
                                        .disabled(!canVerify).opacity(canVerify ? 1 : 0.45)
                                }
                            }
                        }.padding(22).frame(maxWidth: 620).frame(maxWidth: .infinity)
                    }
                }
            } else {
                VStack(spacing: 20) {
                    Text("Completa Familia para comenzar Números.")
                    Button("Volver") { dismiss() }.buttonStyle(MazePrimaryButtonStyle())
                }.padding()
            }
        }
        .confirmationDialog("¿Salir de Números?", isPresented: $showExit, titleVisibility: .visible) {
            Button("Salir sin terminar", role: .destructive) { dismiss() }
            Button("Continuar", role: .cancel) { }
        } message: { Text("Las respuestas de esta partida no se guardarán.") }
        .task(id: scenePhase) {
            guard scenePhase == .active, appState.familyRewardClaimed, reward == nil else { return }
            lastTick = ProcessInfo.processInfo.systemUptime
            do {
                while !Task.isCancelled && reward == nil {
                    try await Task.sleep(for: .seconds(1))
                    tick()
                }
            } catch { }
        }
        .onChange(of: scenePhase) { _, phase in
            if phase != .active { tick(); lastTick = nil }
        }
        .onDisappear { tick(); lastTick = nil }
    }

    private func tick() {
        guard let lastTick else { return }
        let now = ProcessInfo.processInfo.systemUptime
        elapsed += max(0, now - lastTick)
        self.lastTick = now
    }

    private func finishAnswer(_ answer: Bool) {
        guard !checked, scenePhase == .active else { return }
        withAnimation(reduceMotion ? nil : .spring(response: 0.35)) {
            correct = answer
            checked = true
            if answer { score += 1 }
        }
        SoundManager.shared.play(answer ? "audioBien" : "audioMalaSeleccion", volume: Float(appState.soundVolume))
    }
}

private struct NumbersDiscoveryView: View {
    let onContinue: () -> Void
    @EnvironmentObject private var appState: AppState
    @State private var discovered: Set<Int> = []

    var body: some View {
        VStack(spacing: 18) {
            PetView(size: 90)
            Text("¡Contemos juntos! Toca cada cantidad para descubrir cómo se escribe en Yuhmu.")
                .multilineTextAlignment(.center)
            ForEach(1...5, id: \.self) { value in
                Button {
                    discovered.insert(value)
                    SoundManager.shared.play("audioSeleccion", volume: Float(appState.soundVolume))
                } label: {
                    HStack {
                        NumberObjects(count: value)
                        Spacer()
                        VStack {
                            Text(discovered.contains(value) ? NumberVocabulary.word(value) : "Descubrir")
                                .font(.title3.bold())
                            Text("\(value) · \(NumberVocabulary.spanish[value - 1])").font(.caption)
                        }
                    }.padding(18).background(.white, in: RoundedRectangle(cornerRadius: 20))
                        .shadow(color: .purple.opacity(0.15), radius: 1, y: 5)
                }.buttonStyle(.plain).foregroundStyle(.purple)
            }
            Button("¡A practicar!", action: onContinue).buttonStyle(MazePrimaryButtonStyle())
                .disabled(discovered.count < 5).opacity(discovered.count == 5 ? 1 : 0.45)
        }
    }
}

private struct NumberObjects: View {
    let count: Int
    var body: some View {
        HStack(spacing: 3) {
            ForEach(0..<count, id: \.self) { _ in
                Image(systemName: "leaf.fill").foregroundStyle(.green)
            }
        }.font(.title3).accessibilityLabel("\(count) hojas")
    }
}

private struct NumbersActivityView: View {
    let step: Int
    @Binding var tapped: Set<Int>
    @Binding var choice: Int?
    @Binding var basket: Int
    @EnvironmentObject private var appState: AppState
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack(spacing: 20) {
            switch step {
            case 0:
                Text("Toca cada hoja una sola vez para contar hasta cinco.")
                ForEach(1...5, id: \.self) { value in
                    Button {
                        withAnimation(reduceMotion ? nil : .spring(response: 0.3)) { _ = tapped.insert(value) }
                        SoundManager.shared.play("audioSeleccion", volume: Float(appState.soundVolume))
                    } label: {
                        Image(systemName: tapped.contains(value) ? "checkmark.circle.fill" : "leaf.fill")
                            .font(.system(size: 42)).foregroundStyle(tapped.contains(value) ? .teal : .green)
                            .frame(maxWidth: .infinity).padding(10)
                            .background(.white, in: RoundedRectangle(cornerRadius: 18))
                    }.disabled(tapped.contains(value)).accessibilityLabel("Hoja \(value)")
                }
                Text(tapped.isEmpty ? "¡Empieza a contar!" : "\(tapped.count) · \(NumberVocabulary.word(tapped.count))")
                    .font(.title2.bold()).foregroundStyle(.purple)
            case 1:
                Text("Cuenta las flores. ¿Qué palabra corresponde?")
                HStack {
                    ForEach(0..<3) { _ in Image(systemName: "camera.macro").font(.system(size: 52)).foregroundStyle(.pink) }
                }.accessibilityLabel("Tres flores")
                NumberChoices(values: [2, 4, 3], choice: $choice)
            case 2:
                Text("Pon Yóho objetos en la canasta. Puedes agregar o quitar.")
                Image(systemName: "basket.fill").font(.system(size: 75)).foregroundStyle(.orange)
                NumberObjects(count: basket)
                Text("\(basket) objetos").font(.headline)
                HStack {
                    Button("Quitar", systemImage: "minus.circle.fill") { basket -= 1 }
                        .disabled(basket == 0)
                    Spacer()
                    Button("Agregar", systemImage: "plus.circle.fill") { basket += 1 }
                        .disabled(basket == 5)
                }.buttonStyle(.borderedProminent).tint(.purple)
            case 3:
                Text("Sigue el orden del uno al cinco. ¿Qué palabra falta?")
                ForEach(1...5, id: \.self) { value in
                    Text(value == 4 ? (choice.map(NumberVocabulary.word) ?? "¿…?") : NumberVocabulary.word(value))
                        .font(.title2.bold()).foregroundStyle(value == 4 ? .orange : .purple)
                        .padding(12).frame(maxWidth: .infinity)
                        .background(.white, in: RoundedRectangle(cornerRadius: 18))
                }
                NumberChoices(values: [5, 1, 4], choice: $choice)
            default:
                Text("Une cada palabra con su cantidad. Desliza desde la palabra hasta las hojas o toca ambos cuadros.")
                Text("Completa las cinco conexiones sin equivocarte para ganar el acierto.")
                    .font(.caption).foregroundStyle(.secondary)
            }
        }.multilineTextAlignment(.center)
    }
}

private struct NumberChoices: View {
    let values: [Int]
    @Binding var choice: Int?
    var body: some View {
        ForEach(values, id: \.self) { value in
            Button { choice = value } label: {
                HStack {
                    Text(NumberVocabulary.word(value)).font(.title3.bold())
                    Spacer()
                    Image(systemName: choice == value ? "checkmark.circle.fill" : "circle")
                }.padding(18).foregroundStyle(.purple)
                    .background(choice == value ? Color.purple.opacity(0.15) : .white,
                                in: RoundedRectangle(cornerRadius: 18))
                    .shadow(color: .purple.opacity(0.14), radius: 1, y: 4)
            }.buttonStyle(.plain)
        }
    }
}

// Los puntos se calculan en el mismo espacio de coordenadas que el gesto.
private struct NumbersConnectionsView: View {
    let onCompleted: (Int) -> Void
    private let targets = [3, 5, 1, 4, 2]
    @State private var matched: Set<Int> = []
    @State private var selected: Int?
    @State private var dragging: CGPoint?
    @State private var mistakes = 0
    @State private var feedback = "0 de 5 conexiones"
    @EnvironmentObject private var appState: AppState
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack {
            GeometryReader { geometry in
                ZStack {
                    ForEach(1...5, id: \.self) { value in
                        if matched.contains(value), let row = targets.firstIndex(of: value) {
                            Path { path in
                                path.move(to: point(row: value - 1, right: false, size: geometry.size))
                                path.addLine(to: point(row: row, right: true, size: geometry.size))
                            }.stroke(.teal, style: StrokeStyle(lineWidth: 4, lineCap: .round))
                                .allowsHitTesting(false)
                        }
                    }
                    if let selected, let dragging {
                        Path { path in
                            path.move(to: point(row: selected - 1, right: false, size: geometry.size))
                            path.addLine(to: dragging)
                        }.stroke(.purple.opacity(0.5), lineWidth: 3).allowsHitTesting(false)
                    }
                    ForEach(1...5, id: \.self) { value in
                        Button { selected = value } label: {
                            Text(NumberVocabulary.word(value)).font(.headline)
                                .frame(width: geometry.size.width * 0.39, height: 62)
                                .background(matched.contains(value) ? Color.mint : .white,
                                            in: RoundedRectangle(cornerRadius: 16))
                                .overlay { RoundedRectangle(cornerRadius: 16).stroke(selected == value ? .purple : .teal.opacity(0.3), lineWidth: 2) }
                        }
                        .buttonStyle(.plain).disabled(matched.contains(value))
                        .position(point(row: value - 1, right: false, size: geometry.size))
                        .highPriorityGesture(
                            DragGesture(minimumDistance: 8, coordinateSpace: .named("numberLinks"))
                                .onChanged { gesture in
                                    guard !matched.contains(value) else { return }
                                    selected = value
                                    dragging = gesture.location
                                }
                                .onEnded { gesture in
                                    dragging = nil
                                    guard !matched.contains(value) else { return }
                                    let width = geometry.size.width
                                    if gesture.location.x >= width * 0.58 && gesture.location.x <= width {
                                        let row = Int(gesture.location.y / 84)
                                        if targets.indices.contains(row), gesture.location.y >= 0,
                                           abs(gesture.location.y - (CGFloat(row) * 84 + 42)) <= 31 {
                                            connect(value, to: targets[row])
                                        }
                                    }
                                }
                        )
                    }
                    ForEach(0..<5) { row in
                        let value = targets[row]
                        Button {
                            if let selected { connect(selected, to: value) }
                        } label: {
                            NumberObjects(count: value)
                                .frame(width: geometry.size.width * 0.39, height: 62)
                                .background(matched.contains(value) ? Color.mint : .white,
                                            in: RoundedRectangle(cornerRadius: 16))
                        }.buttonStyle(.plain).disabled(matched.contains(value))
                            .position(point(row: row, right: true, size: geometry.size))
                    }
                }.coordinateSpace(name: "numberLinks")
            }.frame(height: 420)
            Text(feedback).font(.headline).foregroundStyle(.purple)
        }
    }

    private func point(row: Int, right: Bool, size: CGSize) -> CGPoint {
        CGPoint(x: size.width * (right ? 0.79 : 0.21), y: CGFloat(row) * 84 + 42)
    }

    private func connect(_ word: Int, to target: Int) {
        guard !matched.contains(word), !matched.contains(target) else { return }
        if word == target {
            withAnimation(reduceMotion ? nil : .spring(response: 0.3)) { _ = matched.insert(word) }
            feedback = "¡Bien! \(matched.count) de 5 conexiones"
            SoundManager.shared.play("audioBien", volume: Float(appState.soundVolume))
            if matched.count == 5 { onCompleted(mistakes) }
        } else {
            mistakes += 1
            feedback = "Cuenta las hojas y vuelve a intentarlo."
            SoundManager.shared.play("audioMalaSeleccion", volume: Float(appState.soundVolume))
        }
        selected = nil
    }
}
