import SwiftUI

struct AnimalsLessonView: View {
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
    @State private var discovered: Set<LessonAnimal> = []
    @State private var choice: LessonAnimal?
    @State private var selectedAnimals: Set<LessonAnimal> = []
    @State private var reward: FamilyLessonReward?
    @State private var elapsed: Double = 0
    @State private var lastTick: Double?
    @State private var showExit = false

    private let titles = ["Álbum de exploración", "¿Quién se esconde?", "La palabra partida", "Encuentra a los dos", "El sendero de los animales"]
    private let explanations = [
        "’Yó: perro · Bàdu: pato · Khwá: conejo · Gáne: abeja.",
        "La silueta era un conejo: Khwá.",
        "Bà + du forman Bàdu: pato.",
        "’Yó es perro y Gáne es abeja.",
        "¡Reuniste a los cuatro animales con Maicito!"
    ]
    private var canVerify: Bool {
        switch step {
        case 0: discovered.count == LessonAnimal.allCases.count
        case 1, 2: choice != nil
        case 3: selectedAnimals.count == 2
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
            } else if appState.colorsRewardClaimed {
                ScrollViewReader { proxy in
                    ScrollView {
                        VStack(spacing: 22) {
                            HStack {
                                Button { showExit = true } label: {
                                    Image(systemName: "xmark").padding(14).background(.white, in: Circle())
                                }.accessibilityLabel("Salir de Animales")
                                ProgressView(value: Double(step + 1), total: 5).tint(.teal)
                                Text("\(step + 1)/5").font(.headline).foregroundStyle(.teal)
                            }.id("animalsTop")
                            Text("Animales · \(titles[step])").font(.title2.bold())
                                .foregroundStyle(.purple).multilineTextAlignment(.center)
                            if teaching {
                                VStack(spacing: 18) {
                                    PetView(size: 90)
                                    Text("¡Prepara tu mochila de explorador! Descubre cuatro animales y ayuda a Maicito a encontrarlos.")
                                        .multilineTextAlignment(.center)
                                    Button("¡Comenzar!") { teaching = false }
                                        .buttonStyle(MazePrimaryButtonStyle())
                                }
                            } else {
                                AnimalsActivityView(step: step, discovered: $discovered, choice: $choice, selectedAnimals: $selectedAnimals)
                                    .disabled(checked || showExit || scenePhase != .active)
                                if step == 4 {
                                    AnimalsTrailView { mistakes in finishAnswer(mistakes == 0) }
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
                                            reward = appState.completeAnimalsLesson(correctAnswers: score, seconds: elapsed)
                                            lastTick = nil
                                        } else {
                                            step += 1
                                            checked = false
                                            choice = nil
                                            selectedAnimals = []
                                        }
                                        proxy.scrollTo("animalsTop", anchor: .top)
                                    }.buttonStyle(MazePrimaryButtonStyle())
                                } else if step < 4 {
                                    Button("Verificar") {
                                        let answer: Bool
                                        switch step {
                                        case 0: answer = discovered.count == 4
                                        case 1: answer = choice == .rabbit
                                        case 2: answer = choice == .duck
                                        default: answer = selectedAnimals == Set([.dog, .bee])
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
                    Text("Completa Colores para comenzar Animales.")
                    Button("Volver") { dismiss() }.buttonStyle(MazePrimaryButtonStyle())
                }.padding()
            }
        }
        .confirmationDialog("¿Salir de Animales?", isPresented: $showExit, titleVisibility: .visible) {
            Button("Salir sin terminar", role: .destructive) { dismiss() }
            Button("Continuar", role: .cancel) { }
        } message: { Text("Las respuestas de esta partida no se guardarán.") }
        .task(id: scenePhase) {
            guard scenePhase == .active, appState.colorsRewardClaimed, reward == nil else { return }
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



// Manual para la escritura de los sonidos del yuhmu, páginas 74, 76 y 82.
private enum LessonAnimal: String, CaseIterable, Identifiable {
    case dog, duck, rabbit, bee
    var id: String { rawValue }
    var word: String {
        switch self {
        case .dog: "’Yó"
        case .duck: "Bàdu"
        case .rabbit: "Khwá"
        case .bee: "Gáne"
        }
    }
    var spanish: String {
        switch self {
        case .dog: "Perro"
        case .duck: "Pato"
        case .rabbit: "Conejo"
        case .bee: "Abeja"
        }
    }
    var asset: String {
        switch self {
        case .dog: "animalLessonDog"
        case .duck: "animalLessonDuck"
        case .rabbit: "animalLessonRabbit"
        case .bee: "animalLessonBee"
        }
    }
    var ending: String {
        switch self {
        case .dog: "yó"
        case .duck: "du"
        case .rabbit: "wá"
        case .bee: "ne"
        }
    }
}

private struct AnimalPortraitView: View {
    let animal: LessonAnimal
    var size: CGFloat = 120
    var body: some View {
        Image(animal.asset).resizable().scaledToFit().frame(height: size)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .accessibilityLabel(animal.spanish)
    }
}

private struct AnimalsActivityView: View {
    let step: Int
    @Binding var discovered: Set<LessonAnimal>
    @Binding var choice: LessonAnimal?
    @Binding var selectedAnimals: Set<LessonAnimal>
    @EnvironmentObject private var appState: AppState
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack(spacing: 20) {
            switch step {
            case 0:
                Text("Toca los retratos y completa tu álbum de explorador.")
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                    ForEach(LessonAnimal.allCases) { animal in
                        Button {
                            withAnimation(reduceMotion ? nil : .spring(response: 0.3)) {
                                _ = discovered.insert(animal)
                            }
                            SoundManager.shared.play("audioSeleccion", volume: Float(appState.soundVolume))
                        } label: {
                            VStack(spacing: 10) {
                                AnimalPortraitView(animal: animal)
                                Text(discovered.contains(animal) ? animal.word : "Descubrir").font(.title3.bold())
                                Text(animal.spanish).font(.subheadline)
                                Image(systemName: discovered.contains(animal) ? "checkmark.seal.fill" : "magnifyingglass")
                            }
                            .padding(12).frame(maxWidth: .infinity)
                            .background(.white, in: RoundedRectangle(cornerRadius: 22))
                            .shadow(color: .purple.opacity(0.15), radius: 1, y: 5)
                            .scaleEffect(discovered.contains(animal) && !reduceMotion ? 1.02 : 1)
                        }.buttonStyle(.plain).foregroundStyle(.purple)
                    }
                }
                Text("\(discovered.count) de 4 animales descubiertos").font(.caption)
            case 1:
                Text("Reconoce al animal escondido. ¿Cómo se llama en Yuhmu?")
                // Silueta vectorial: el retrato generado tiene fondo opaco.
                RabbitSilhouetteView().frame(width: 160, height: 190).accessibilityLabel("Silueta de conejo")
                ForEach([LessonAnimal.bee, .rabbit, .dog, .duck]) { animal in
                    animalChoice(animal, text: animal.word)
                }
            case 2:
                Text("¡A esta palabra le falta una pieza! Elige cómo termina.")
                AnimalPortraitView(animal: .duck, size: 180)
                Text("Bà" + (choice?.ending ?? "＿")).font(.largeTitle.bold()).foregroundStyle(.purple)
                HStack {
                    ForEach([LessonAnimal.rabbit, .duck, .bee]) { animal in
                        animalChoice(animal, text: animal.ending)
                    }
                }
            case 3:
                Text("Encuentra a estos dos amigos").font(.headline)
                Text("’Yó  +  Gáne").font(.largeTitle.bold()).foregroundStyle(.purple)
                Text("Toca dos retratos. Toca de nuevo para quitar una selección.").font(.subheadline)
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                    ForEach([LessonAnimal.duck, .bee, .dog, .rabbit]) { animal in
                        Button {
                            if selectedAnimals.contains(animal) { selectedAnimals.remove(animal) }
                            else if selectedAnimals.count < 2 { selectedAnimals.insert(animal) }
                        } label: {
                            VStack {
                                AnimalPortraitView(animal: animal)
                                Image(systemName: selectedAnimals.contains(animal) ? "checkmark.circle.fill" : "circle")
                                    .font(.title2).foregroundStyle(.teal)
                            }.padding(12).frame(maxWidth: .infinity)
                                .background(selectedAnimals.contains(animal) ? Color.mint.opacity(0.3) : .white,
                                            in: RoundedRectangle(cornerRadius: 20))
                                .shadow(color: .purple.opacity(0.15), radius: 1, y: 4)
                        }.buttonStyle(.plain)
                            .accessibilityAddTraits(selectedAnimals.contains(animal) ? .isSelected : [])
                    }
                }
            default: EmptyView()
            }
        }.multilineTextAlignment(.center)
    }

    private func animalChoice(_ animal: LessonAnimal, text: String) -> some View {
        Button { choice = animal } label: {
            HStack {
                Text(text).font(.title3.bold())
                Spacer(minLength: 4)
                Image(systemName: choice == animal ? "checkmark.circle.fill" : "circle")
            }.foregroundStyle(.purple).padding(16).frame(maxWidth: .infinity)
                .background(choice == animal ? Color.purple.opacity(0.12) : .white,
                            in: RoundedRectangle(cornerRadius: 18))
                .shadow(color: .purple.opacity(0.15), radius: 1, y: 4)
        }.buttonStyle(.plain)
    }
}

private struct RabbitSilhouetteView: View {
    var body: some View {
        ZStack {
            Ellipse().frame(width: 90, height: 100).offset(y: 35)
            Circle().frame(width: 78).offset(y: -15)
            Capsule().frame(width: 22, height: 70).rotationEffect(.degrees(-12)).offset(x: -22, y: -65)
            Capsule().frame(width: 22, height: 70).rotationEffect(.degrees(12)).offset(x: 22, y: -65)
            Circle().frame(width: 30).offset(x: 48, y: 52)
            Ellipse().frame(width: 52, height: 23).offset(x: -18, y: 80)
        }.foregroundStyle(.purple.opacity(0.8))
    }
}

private struct AnimalsTrailView: View {
    let onCompleted: (Int) -> Void
    private let trail: [LessonAnimal] = [.duck, .bee, .rabbit, .dog]
    private let candidates: [[LessonAnimal]] = [
        [.rabbit, .duck, .dog], [.bee, .duck, .rabbit],
        [.dog, .bee, .rabbit], [.duck, .dog, .bee]
    ]
    @State private var round = 0
    @State private var gathered: [LessonAnimal] = []
    @State private var mistakes = 0
    @State private var arrived = false
    @State private var finished = false
    @State private var dragging: CGPoint?
    @State private var chosenIndex: Int?
    @State private var feedback = "Arrastra a Maicito hacia el animal que indica el letrero."
    @EnvironmentObject private var appState: AppState
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack(spacing: 18) {
            Text("El sendero de los animales").font(.title2.bold()).foregroundStyle(.purple)
            Text("Lee el letrero y lleva a Maicito hasta su nuevo amigo. También puedes tocar el retrato.")
                .multilineTextAlignment(.center)
            Text("Encuentra: \(trail[round].word)").font(.largeTitle.bold()).foregroundStyle(.purple)
            Text("\(gathered.count) de 4 amigos reunidos").font(.headline).foregroundStyle(.teal)
            GeometryReader { geometry in
                ZStack {
                    RoundedRectangle(cornerRadius: 28)
                        .fill(LinearGradient(colors: [.mint.opacity(0.4), .green.opacity(0.13)],
                                             startPoint: .top, endPoint: .bottom))
                    ForEach(0..<3) { index in
                        Path { path in
                            path.move(to: start(geometry.size))
                            path.addQuadCurve(to: destination(index, geometry.size),
                                              control: CGPoint(x: geometry.size.width / 2, y: 160))
                        }.stroke(.orange.opacity(0.35), style: StrokeStyle(lineWidth: 22, lineCap: .round))
                            .allowsHitTesting(false)
                    }
                    ForEach(0..<3) { index in
                        Button { visit(index) } label: {
                            AnimalPortraitView(animal: candidates[round][index], size: 80)
                                .padding(4).background(.white, in: RoundedRectangle(cornerRadius: 18))
                                .overlay {
                                    if arrived && chosenIndex == index {
                                        RoundedRectangle(cornerRadius: 18).stroke(.teal, lineWidth: 4)
                                    }
                                }
                        }.buttonStyle(.plain).disabled(arrived)
                            .position(destination(index, geometry.size))
                    }
                    ForEach(0..<3) { index in
                        Image(systemName: "pawprint.fill").foregroundStyle(.brown.opacity(0.4))
                            .position(x: geometry.size.width * CGFloat(index + 1) / 4, y: 170)
                            .accessibilityHidden(true)
                    }
                    PetView(size: 72)
                        .position(petPosition(geometry.size))
                        .gesture(
                            DragGesture(minimumDistance: 0, coordinateSpace: .named("animalTrail"))
                                .onChanged { value in
                                    guard !arrived else { return }
                                    dragging = CGPoint(x: min(max(value.location.x, 30), geometry.size.width - 30),
                                                       y: min(max(value.location.y, 35), 285))
                                }
                                .onEnded { value in
                                    guard !arrived else { return }
                                    if let index = (0..<3).first(where: {
                                        let point = destination($0, geometry.size)
                                        return hypot(value.location.x - point.x, value.location.y - point.y) < 48
                                    }) { visit(index) }
                                    withAnimation(reduceMotion ? nil : .spring(response: 0.4)) { dragging = nil }
                                }
                        )
                        .accessibilityLabel("Maicito")
                        .accessibilityHint("Toca el retrato del animal para elegir un sendero.")
                }.coordinateSpace(name: "animalTrail")
            }.frame(height: 320)
            Text(feedback).font(.headline).foregroundStyle(arrived ? .teal : .purple)
                .multilineTextAlignment(.center)
            HStack {
                ForEach(gathered) { animal in
                    AnimalPortraitView(animal: animal, size: 52)
                        .transition(.scale.combined(with: .opacity))
                }
            }
            if arrived && !finished {
                Button("Seguir explorando") {
                    round += 1
                    arrived = false
                    chosenIndex = nil
                    dragging = nil
                    feedback = "¡Lee el siguiente letrero!"
                }.buttonStyle(MazePrimaryButtonStyle())
            }
            Text("Sin límite de tiempo. Reúne a los cuatro sin equivocarte para ganar el acierto.")
                .font(.caption).foregroundStyle(.secondary).multilineTextAlignment(.center)
        }
        .sensoryFeedback(.success, trigger: gathered.count)
    }

    private func start(_ size: CGSize) -> CGPoint { CGPoint(x: size.width / 2, y: 270) }
    private func destination(_ index: Int, _ size: CGSize) -> CGPoint {
        CGPoint(x: size.width * (CGFloat(index) + 0.5) / 3, y: 65)
    }
    private func petPosition(_ size: CGSize) -> CGPoint {
        if let dragging { return dragging }
        if arrived, let chosenIndex {
            let target = destination(chosenIndex, size)
            return CGPoint(x: target.x, y: target.y + 60)
        }
        return start(size)
    }
    private func visit(_ index: Int) {
        guard !arrived, !finished else { return }
        let animal = candidates[round][index]
        if animal == trail[round] {
            withAnimation(reduceMotion ? nil : .spring(response: 0.5)) {
                dragging = nil
                chosenIndex = index
                arrived = true
                gathered.append(animal)
            }
            feedback = "¡Encontraste a \(animal.word)! Significa \(animal.spanish.lowercased())."
            if gathered.count == 4 {
                finished = true
                onCompleted(mistakes)
            } else {
                SoundManager.shared.play("audioBien", volume: Float(appState.soundVolume))
            }
        } else {
            mistakes += 1
            feedback = "Ese es \(animal.word). El letrero busca \(trail[round].word), \(trail[round].spanish.lowercased())."
            SoundManager.shared.play("audioMalaSeleccion", volume: Float(appState.soundVolume))
        }
    }
}
