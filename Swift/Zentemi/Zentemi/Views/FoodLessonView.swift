import SwiftUI

struct FoodLessonView: View {
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
    @State private var discovered: Set<LessonFood> = []
    @State private var choice: LessonFood?
    @State private var selectedFood: Set<LessonFood> = []
    @State private var reward: FamilyLessonReward?
    @State private var elapsed: Double = 0
    @State private var lastTick: Double?
    @State private var showExit = false

    private let titles = ["El puesto del mercado", "Ponle nombre", "Sirve el pedido", "Una comida para llevar", "Las canastas de Maicito"]
    private let explanations = [
        "Déhe: agua · Ts’óni: aguacate · K’ápuxü: papa · Hmé: tortilla.",
        "La papa se llama K’ápuxü.",
        "Ts’óni significa aguacate.",
        "Hmé significa tortilla y Déhe significa agua.",
        "¡Preparaste las tres canastas siguiendo las listas en Yuhmu!"
    ]
    private var canVerify: Bool {
        switch step {
        case 0: discovered.count == LessonFood.allCases.count
        case 1, 2: choice != nil
        case 3: selectedFood.count == 2
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
            } else if appState.animalsRewardClaimed {
                ScrollViewReader { proxy in
                    ScrollView {
                        VStack(spacing: 22) {
                            HStack {
                                Button { showExit = true } label: {
                                    Image(systemName: "xmark").padding(14).background(.white, in: Circle())
                                }.accessibilityLabel("Salir de Alimentos")
                                ProgressView(value: Double(step + 1), total: 5).tint(.teal)
                                Text("\(step + 1)/5").font(.headline).foregroundStyle(.teal)
                            }.id("foodTop")
                            Text("Alimentos · \(titles[step])").font(.title2.bold())
                                .foregroundStyle(.purple).multilineTextAlignment(.center)
                            if teaching {
                                VStack(spacing: 18) {
                                    PetView(size: 90)
                                    Text("¡Vamos al mercado con Maicito! Descubre alimentos y una bebida, practica sus nombres y prepara las canastas.")
                                        .multilineTextAlignment(.center)
                                    Button("¡Comenzar!") { teaching = false }
                                        .buttonStyle(MazePrimaryButtonStyle())
                                }
                            } else {
                                FoodActivityView(step: step, discovered: $discovered, choice: $choice, selectedFood: $selectedFood)
                                    .disabled(checked || showExit || scenePhase != .active)
                                if step == 4 {
                                    FoodBasketGameView { mistakes in finishAnswer(mistakes == 0) }
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
                                            reward = appState.completeFoodLesson(correctAnswers: score, seconds: elapsed)
                                            lastTick = nil
                                        } else {
                                            step += 1
                                            checked = false
                                            choice = nil
                                            selectedFood = []
                                        }
                                        proxy.scrollTo("foodTop", anchor: .top)
                                    }.buttonStyle(MazePrimaryButtonStyle())
                                } else if step < 4 {
                                    Button("Verificar") {
                                        let answer: Bool
                                        switch step {
                                        case 0: answer = discovered.count == 4
                                        case 1: answer = choice == .potato
                                        case 2: answer = choice == .avocado
                                        default: answer = selectedFood == Set([.tortilla, .water])
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
                    Text("Completa Animales para comenzar Alimentos.")
                    Button("Volver") { dismiss() }.buttonStyle(MazePrimaryButtonStyle())
                }.padding()
            }
        }
        .confirmationDialog("¿Salir de Alimentos?", isPresented: $showExit, titleVisibility: .visible) {
            Button("Salir sin terminar", role: .destructive) { dismiss() }
            Button("Continuar", role: .cancel) { }
        } message: { Text("Las respuestas de esta partida no se guardarán.") }
        .task(id: scenePhase) {
            guard scenePhase == .active, appState.animalsRewardClaimed, reward == nil else { return }
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




// Manual para la escritura de los sonidos del yuhmu: pp. 74, 82 y 85.
private enum LessonFood: String, CaseIterable, Identifiable {
    case water, avocado, potato, tortilla
    var id: String { rawValue }
    var word: String {
        switch self {
        case .water: "Déhe"
        case .avocado: "Ts’óni"
        case .potato: "K’ápuxü"
        case .tortilla: "Hmé"
        }
    }
    var spanish: String {
        switch self {
        case .water: "Agua"
        case .avocado: "Aguacate"
        case .potato: "Papa"
        case .tortilla: "Tortilla"
        }
    }
    var asset: String {
        switch self {
        case .water: "foodLessonWater"
        case .avocado: "foodLessonAvocado"
        case .potato: "foodLessonPotato"
        case .tortilla: "foodLessonTortilla"
        }
    }
}

private struct FoodPortraitView: View {
    let food: LessonFood
    var size: CGFloat = 115
    var body: some View {
        Image(food.asset).resizable().scaledToFit().frame(height: size)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .accessibilityLabel(food.spanish)
    }
}

private struct FoodActivityView: View {
    let step: Int
    @Binding var discovered: Set<LessonFood>
    @Binding var choice: LessonFood?
    @Binding var selectedFood: Set<LessonFood>
    @EnvironmentObject private var appState: AppState
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack(spacing: 20) {
            switch step {
            case 0:
                Text("Explora el puesto: toca cada producto y descubre su nombre.")
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                    ForEach(LessonFood.allCases) { food in
                        Button {
                            withAnimation(reduceMotion ? nil : .spring(response: 0.35)) { _ = discovered.insert(food) }
                            SoundManager.shared.play("audioSeleccion", volume: Float(appState.soundVolume))
                        } label: {
                            VStack(spacing: 8) {
                                FoodPortraitView(food: food)
                                Text(discovered.contains(food) ? food.word : "Descubrir").font(.title3.bold())
                                Text(food.spanish).font(.subheadline)
                                Image(systemName: discovered.contains(food) ? "checkmark.seal.fill" : "hand.tap.fill")
                            }.padding(12).frame(maxWidth: .infinity)
                                .background(.white, in: RoundedRectangle(cornerRadius: 22))
                                .shadow(color: .orange.opacity(0.2), radius: 1, y: 5)
                        }.buttonStyle(.plain).foregroundStyle(.purple)
                    }
                }
                Text("\(discovered.count) de 4 productos descubiertos").font(.caption)
            case 1:
                Text("Pon el nombre correcto en la etiqueta de esta papa.")
                FoodPortraitView(food: .potato, size: 180)
                ForEach([LessonFood.tortilla, .water, .potato, .avocado]) { food in
                    Button { choice = food } label: {
                        HStack {
                            Text(food.word).font(.title3.bold())
                            Spacer()
                            Image(systemName: choice == food ? "checkmark.circle.fill" : "tag.fill")
                        }.padding(18).foregroundStyle(.purple)
                            .background(choice == food ? Color.purple.opacity(0.15) : .white,
                                        in: RoundedRectangle(cornerRadius: 18))
                            .shadow(color: .purple.opacity(0.12), radius: 1, y: 4)
                    }.buttonStyle(.plain)
                }
            case 2:
                Text("Maicito pide: Ts’óni").font(.title2.bold()).foregroundStyle(.purple)
                Text("Toca el producto para colocarlo en el plato.")
                ZStack {
                    Ellipse().fill(.white).frame(height: 135)
                        .overlay { Ellipse().stroke(.purple.opacity(0.2), lineWidth: 5) }
                    if let choice { FoodPortraitView(food: choice, size: 100) }
                    else { Image(systemName: "fork.knife").font(.largeTitle).foregroundStyle(.gray) }
                }
                foodGrid
            case 3:
                Text("Prepara una comida para llevar").font(.headline)
                Text("Hmé  +  Déhe").font(.largeTitle.bold()).foregroundStyle(.purple)
                Text("Selecciona los dos productos de la lista. Puedes cambiar antes de verificar.")
                foodGrid
                Text("\(selectedFood.count) de 2 seleccionados").font(.caption)
            default: EmptyView()
            }
        }.multilineTextAlignment(.center)
    }

    private var foodGrid: some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
            ForEach([LessonFood.avocado, .tortilla, .potato, .water]) { food in
                Button {
                    withAnimation(reduceMotion ? nil : .spring(response: 0.3)) {
                        if step == 2 { choice = food }
                        else if selectedFood.contains(food) { selectedFood.remove(food) }
                        else if selectedFood.count < 2 { selectedFood.insert(food) }
                    }
                    SoundManager.shared.play("audioSeleccion", volume: Float(appState.soundVolume))
                } label: {
                    VStack {
                        FoodPortraitView(food: food)
                        Image(systemName: isSelected(food) ? "checkmark.circle.fill" : "circle")
                            .foregroundStyle(.teal).font(.title2)
                    }.padding(12).frame(maxWidth: .infinity)
                        .background(isSelected(food) ? Color.mint.opacity(0.25) : .white,
                                    in: RoundedRectangle(cornerRadius: 20))
                        .shadow(color: .purple.opacity(0.13), radius: 1, y: 4)
                }.buttonStyle(.plain)
                    .accessibilityAddTraits(isSelected(food) ? .isSelected : [])
            }
        }
    }
    private func isSelected(_ food: LessonFood) -> Bool {
        step == 2 ? choice == food : selectedFood.contains(food)
    }
}

private struct FoodBasketGameView: View {
    let onCompleted: (Int) -> Void
    private let orders: [[LessonFood: Int]] = [
        [.tortilla: 2, .water: 1],
        [.avocado: 1, .potato: 2],
        [.water: 1, .avocado: 1, .potato: 1, .tortilla: 1]
    ]
    @State private var round = 0
    @State private var basket: [LessonFood: Int] = [:]
    @State private var mistakes = 0
    @State private var delivered = false
    @State private var finished = false
    @State private var feedback = "Toca un producto o arrástralo a la canasta."
    @EnvironmentObject private var appState: AppState
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack(spacing: 18) {
            HStack {
                PetView(size: 75).accessibilityHidden(true)
                Text("Las canastas de Maicito").font(.title2.bold()).foregroundStyle(.purple)
            }
            Text("Pedido \(round + 1) de 3").font(.headline)
            VStack(alignment: .leading, spacing: 8) {
                Label("Lista de compras", systemImage: "list.bullet.clipboard.fill").font(.headline)
                ForEach(LessonFood.allCases) { food in
                    if let quantity = orders[round][food] {
                        Text("\(quantity) × \(food.word)").font(.title3.bold())
                    }
                }
            }.padding(18).frame(maxWidth: .infinity, alignment: .leading)
                .background(.yellow.opacity(0.15), in: RoundedRectangle(cornerRadius: 20))
            Text("Cada toque agrega una unidad, aunque el dibujo muestre varias tortillas.")
                .font(.caption).foregroundStyle(.secondary).multilineTextAlignment(.center)
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 14) {
                ForEach(LessonFood.allCases) { food in
                    Button { add(food) } label: {
                        VStack {
                            FoodPortraitView(food: food, size: 85)
                            Label("Agregar", systemImage: "plus.circle.fill").font(.subheadline.bold())
                        }.padding(10).frame(maxWidth: .infinity)
                            .background(.white, in: RoundedRectangle(cornerRadius: 18))
                            .shadow(color: .orange.opacity(0.2), radius: 1, y: 4)
                    }.buttonStyle(.plain).foregroundStyle(.purple)
                        .disabled(delivered || basket[food, default: 0] >= 3)
                        .draggable(food.rawValue)
                        .accessibilityLabel("Agregar una unidad de \(food.spanish)")
                }
            }
            VStack(spacing: 10) {
                Label(delivered ? "¡Canasta lista!" : "Tu canasta", systemImage: "basket.fill")
                    .font(.title2.bold()).foregroundStyle(.orange)
                if basket.isEmpty {
                    Text("Arrastra aquí los productos").padding(12)
                }
                ForEach(LessonFood.allCases) { food in
                    if let count = basket[food], count > 0 {
                        HStack {
                            FoodPortraitView(food: food, size: 45)
                            Text("\(count) × \(food.spanish)").font(.headline)
                            Spacer()
                            Button {
                                withAnimation(reduceMotion ? nil : .spring(response: 0.3)) {
                                    if count == 1 { basket.removeValue(forKey: food) }
                                    else { basket[food] = count - 1 }
                                }
                            } label: { Image(systemName: "minus.circle.fill").font(.title2) }
                                .disabled(delivered).accessibilityLabel("Quitar una unidad de \(food.spanish)")
                        }
                    }
                }
            }.padding(16).frame(maxWidth: .infinity, minHeight: 110)
                .background(delivered ? Color.mint.opacity(0.3) : Color.orange.opacity(0.1),
                            in: RoundedRectangle(cornerRadius: 22))
                .overlay { RoundedRectangle(cornerRadius: 22).stroke(.orange.opacity(0.4), lineWidth: 2) }
                .dropDestination(for: String.self) { items, _ in
                    guard !delivered, let raw = items.first, let food = LessonFood(rawValue: raw),
                          basket[food, default: 0] < 3 else { return false }
                    add(food)
                    return true
                }
            Text(feedback).font(.headline).foregroundStyle(delivered ? .teal : .purple)
                .multilineTextAlignment(.center)
            if !delivered {
                Button("Entregar canasta") { deliver() }
                    .buttonStyle(MazePrimaryButtonStyle()).disabled(basket.isEmpty)
            } else if !finished {
                Button("Preparar siguiente canasta") {
                    round += 1
                    basket = [:]
                    delivered = false
                    feedback = "Lee la nueva lista y prepara sus cantidades."
                }.buttonStyle(MazePrimaryButtonStyle())
            }
            Text("Sin tiempo límite. Entrega las tres canastas sin errores para ganar el acierto.")
                .font(.caption).foregroundStyle(.secondary).multilineTextAlignment(.center)
        }.sensoryFeedback(.success, trigger: delivered)
    }

    private func add(_ food: LessonFood) {
        guard !delivered, basket[food, default: 0] < 3 else { return }
        withAnimation(reduceMotion ? nil : .spring(response: 0.35)) { basket[food, default: 0] += 1 }
        SoundManager.shared.play("audioSeleccion", volume: Float(appState.soundVolume))
    }

    private func deliver() {
        guard !delivered else { return }
        if basket == orders[round] {
            withAnimation(reduceMotion ? nil : .spring(response: 0.4)) { delivered = true }
            feedback = "¡Todo coincide con la lista!"
            if round == orders.count - 1 {
                finished = true
                onCompleted(mistakes)
            } else {
                SoundManager.shared.play("audioBien", volume: Float(appState.soundVolume))
            }
        } else {
            mistakes += 1
            feedback = "Revisa productos y cantidades. Puedes quitar o agregar antes de intentarlo otra vez."
            SoundManager.shared.play("audioMalaSeleccion", volume: Float(appState.soundVolume))
        }
    }
}
