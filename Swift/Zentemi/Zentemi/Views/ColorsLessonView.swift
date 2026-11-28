import SwiftUI

struct ColorsLessonView: View {
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
    @State private var discovered: Set<LessonPaint> = []
    @State private var choice: LessonPaint?
    @State private var painted: LessonPaint?
    @State private var reward: FamilyLessonReward?
    @State private var elapsed: Double = 0
    @State private var lastTick: Double?
    @State private var showExit = false

    private let titles = ["Descubre la paleta", "Busca el color", "Pinta la figura", "Completa el patrón", "El taller de Maicito"]
    private let explanations = [
        "K’áxt’i: amarillo · Nk’àngi: verde · T’àxi: blanco · Mbòi: negro.",
        "Nk’àngi significa verde.",
        "T’àxi significa blanco: ese era el color del pedido.",
        "El patrón alterna amarillo y negro. Faltaba Mbòi: negro.",
        "¡Terminaste los cuatro pedidos del taller!"
    ]
    private var canVerify: Bool {
        switch step {
        case 0: discovered.count == LessonPaint.allCases.count
        case 1, 3: choice != nil
        case 2: painted != nil
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
            } else if appState.numbersRewardClaimed {
                ScrollViewReader { proxy in
                    ScrollView {
                        VStack(spacing: 22) {
                            HStack {
                                Button { showExit = true } label: {
                                    Image(systemName: "xmark").padding(14).background(.white, in: Circle())
                                }.accessibilityLabel("Salir de Colores")
                                ProgressView(value: Double(step + 1), total: 5).tint(.teal)
                                Text("\(step + 1)/5").font(.headline).foregroundStyle(.teal)
                            }.id("colorsTop")
                            Text("Colores · \(titles[step])").font(.title2.bold())
                                .foregroundStyle(.purple).multilineTextAlignment(.center)
                            if teaching {
                                VStack(spacing: 18) {
                                    PetView(size: 90)
                                    Text("¡Bienvenido al taller! Descubre cuatro colores, practica con ellos y ayuda a Maicito a pintar sus pedidos.")
                                        .multilineTextAlignment(.center)
                                    Button("¡Comenzar!") { teaching = false }
                                        .buttonStyle(MazePrimaryButtonStyle())
                                }
                            } else {
                                ColorsActivityView(step: step, discovered: $discovered, choice: $choice, painted: $painted)
                                    .disabled(checked || showExit || scenePhase != .active)
                                if step == 4 {
                                    ColorsWorkshopView { mistakes in finishAnswer(mistakes == 0) }
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
                                            reward = appState.completeColorsLesson(correctAnswers: score, seconds: elapsed)
                                            lastTick = nil
                                        } else {
                                            step += 1
                                            checked = false
                                            choice = nil
                                            painted = nil
                                        }
                                        proxy.scrollTo("colorsTop", anchor: .top)
                                    }.buttonStyle(MazePrimaryButtonStyle())
                                } else if step < 4 {
                                    Button("Verificar") {
                                        let answer: Bool
                                        switch step {
                                        case 0: answer = discovered.count == 4
                                        case 1: answer = choice == .green
                                        case 2: answer = painted == .white
                                        default: answer = choice == .black
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
                    Text("Completa Números para comenzar Colores.")
                    Button("Volver") { dismiss() }.buttonStyle(MazePrimaryButtonStyle())
                }.padding()
            }
        }
        .confirmationDialog("¿Salir de Colores?", isPresented: $showExit, titleVisibility: .visible) {
            Button("Salir sin terminar", role: .destructive) { dismiss() }
            Button("Continuar", role: .cancel) { }
        } message: { Text("Las respuestas de esta partida no se guardarán.") }
        .task(id: scenePhase) {
            guard scenePhase == .active, appState.numbersRewardClaimed, reward == nil else { return }
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


// Manual para la escritura de los sonidos del yuhmu: pp. 74, 75, 81 y 85.
// El léxico consultado confirma estos cuatro colores; no se infieren otros.
private enum LessonPaint: String, CaseIterable, Identifiable {
    case yellow, green, white, black
    var id: String { rawValue }
    var word: String {
        switch self {
        case .yellow: "K’áxt’i"
        case .green: "Nk’àngi"
        case .white: "T’àxi"
        case .black: "Mbòi"
        }
    }
    var spanish: String {
        switch self {
        case .yellow: "Amarillo"
        case .green: "Verde"
        case .white: "Blanco"
        case .black: "Negro"
        }
    }
    var color: Color {
        switch self {
        case .yellow: .yellow
        case .green: .green
        case .white: .white
        case .black: .black
        }
    }
}

private struct PaintPaletteView: View {
    @Binding var selection: LessonPaint?
    var body: some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
            ForEach(LessonPaint.allCases) { paint in
                Button { selection = paint } label: {
                    VStack(spacing: 8) {
                        Image(systemName: "paintbrush.pointed.fill")
                            .font(.system(size: 38)).foregroundStyle(paint.color)
                            .padding(10).background(Color.gray.opacity(0.25), in: Circle())
                        Text(paint.spanish).font(.headline).foregroundStyle(.purple)
                        Image(systemName: selection == paint ? "checkmark.circle.fill" : "circle")
                            .foregroundStyle(.teal)
                    }
                    .padding(14).frame(maxWidth: .infinity)
                    .background(.white, in: RoundedRectangle(cornerRadius: 20))
                    .overlay {
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(selection == paint ? .purple : .purple.opacity(0.15), lineWidth: 2)
                    }
                    .shadow(color: .purple.opacity(0.15), radius: 1, y: 4)
                }.buttonStyle(.plain)
                    .accessibilityLabel("Pintura \(paint.spanish)")
                    .accessibilityAddTraits(selection == paint ? .isSelected : [])
            }
        }
    }
}

private struct PaintedDrawingView: View {
    let symbol: String
    let paint: LessonPaint?
    var body: some View {
        Image(systemName: symbol)
            .resizable().scaledToFit().frame(width: 140, height: 140)
            .foregroundStyle(paint?.color ?? Color.gray.opacity(0.5))
            .padding(24)
            .background(Color(red: 0.79, green: 0.82, blue: 0.89), in: RoundedRectangle(cornerRadius: 24))
            .overlay {
                RoundedRectangle(cornerRadius: 24).stroke(.white, lineWidth: 5)
            }
            .shadow(color: .purple.opacity(0.12), radius: 6, y: 5)
            .accessibilityLabel(paint.map { "Dibujo pintado: \($0.spanish)" } ?? "Dibujo sin pintar")
    }
}

private struct ColorsActivityView: View {
    let step: Int
    @Binding var discovered: Set<LessonPaint>
    @Binding var choice: LessonPaint?
    @Binding var painted: LessonPaint?
    @EnvironmentObject private var appState: AppState
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack(spacing: 20) {
            switch step {
            case 0:
                Text("Toca cada mancha para descubrir su nombre en Yuhmu.")
                ForEach(LessonPaint.allCases) { paint in
                    Button {
                        withAnimation(reduceMotion ? nil : .spring(response: 0.3)) { _ = discovered.insert(paint) }
                        SoundManager.shared.play("audioSeleccion", volume: Float(appState.soundVolume))
                    } label: {
                        HStack(spacing: 18) {
                            Image(systemName: "drop.fill").font(.system(size: 44)).foregroundStyle(paint.color)
                                .padding(10).background(.gray.opacity(0.25), in: Circle())
                            VStack(alignment: .leading) {
                                Text(discovered.contains(paint) ? paint.word : "Descubrir").font(.title2.bold())
                                Text(paint.spanish).font(.subheadline)
                            }
                            Spacer()
                            if discovered.contains(paint) { Image(systemName: "checkmark.circle.fill") }
                        }.foregroundStyle(.purple).padding(14)
                            .background(.white, in: RoundedRectangle(cornerRadius: 22))
                            .shadow(color: .purple.opacity(0.15), radius: 1, y: 4)
                    }.buttonStyle(.plain)
                }
                Text("\(discovered.count) de 4 colores descubiertos").font(.caption)
            case 1:
                Text("Busca el objeto de color").font(.headline)
                Text(LessonPaint.green.word).font(.largeTitle.bold()).foregroundStyle(.purple)
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                    ForEach(LessonPaint.allCases) { paint in
                        Button { choice = paint } label: {
                            VStack(spacing: 10) {
                                Image(systemName: "leaf.fill").font(.system(size: 50)).foregroundStyle(paint.color)
                                    .padding(12).background(.gray.opacity(0.25), in: Circle())
                                Text(paint.spanish).foregroundStyle(.purple)
                                Image(systemName: choice == paint ? "checkmark.circle.fill" : "circle")
                                    .foregroundStyle(.teal)
                            }.padding(16).frame(maxWidth: .infinity)
                                .background(.white, in: RoundedRectangle(cornerRadius: 22))
                                .shadow(color: .purple.opacity(0.14), radius: 1, y: 4)
                        }.buttonStyle(.plain)
                    }
                }
            case 2:
                Text("Pedido: \(LessonPaint.white.word)").font(.title2.bold()).foregroundStyle(.purple)
                Text("Elige una pintura y toca la nube. Puedes cambiarla antes de verificar.")
                Button {
                    guard let choice else { return }
                    withAnimation(reduceMotion ? nil : .easeInOut(duration: 0.35)) { painted = choice }
                    SoundManager.shared.play("audioSeleccion", volume: Float(appState.soundVolume))
                } label: { PaintedDrawingView(symbol: "cloud.fill", paint: painted) }
                    .buttonStyle(.plain).disabled(choice == nil)
                    .accessibilityHint("Aplica la pintura seleccionada")
                PaintPaletteView(selection: $choice)
            case 3:
                Text("Amarillo, negro, amarillo… ¿qué color sigue?")
                HStack(spacing: 12) {
                    ForEach(0..<4) { index in
                        VStack {
                            Circle().fill(patternPaint(index)?.color ?? .gray.opacity(0.3))
                                .frame(height: 52)
                                .overlay { Circle().stroke(.gray.opacity(0.5), lineWidth: 1) }
                            Text(patternPaint(index)?.word ?? "¿…?").font(.caption.bold())
                        }.frame(maxWidth: .infinity)
                    }
                }
                PaintPaletteView(selection: $choice)
            default:
                EmptyView()
            }
        }.multilineTextAlignment(.center)
    }

    private func patternPaint(_ index: Int) -> LessonPaint? {
        index == 3 ? choice : (index == 1 ? .black : .yellow)
    }
}

private struct ColorsWorkshopView: View {
    let onCompleted: (Int) -> Void
    private let orders: [LessonPaint] = [.yellow, .green, .black, .white]
    private let symbols = ["sun.max.fill", "tree.fill", "cat.fill", "cloud.fill"]
    @State private var order = 0
    @State private var selection: LessonPaint?
    @State private var painted: LessonPaint?
    @State private var delivered = false
    @State private var finished = false
    @State private var mistakes = 0
    @State private var feedback = "Elige una pintura y toca el dibujo para pintarlo."
    @EnvironmentObject private var appState: AppState
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack(spacing: 20) {
            HStack {
                PetView(size: 75).accessibilityHidden(true)
                Text("El taller de Maicito").font(.title2.bold()).foregroundStyle(.purple)
            }
            Text("Completa los cuatro pedidos. Sin prisas: no hay límite de tiempo.")
            Text("El ejercicio cuenta como acierto si entregas todos los pedidos sin equivocarte.")
                .font(.caption).foregroundStyle(.secondary)
            ProgressView(value: Double(finished ? 4 : order), total: 4).tint(.teal)
            Text("Pedido \(order + 1) de 4: \(orders[order].word)")
                .font(.title2.bold()).foregroundStyle(.purple)
            Button {
                guard let selection, !delivered else { return }
                withAnimation(reduceMotion ? nil : .easeInOut(duration: 0.35)) { painted = selection }
                SoundManager.shared.play("audioSeleccion", volume: Float(appState.soundVolume))
            } label: {
                PaintedDrawingView(symbol: symbols[order], paint: painted)
                    .scaleEffect(delivered && !reduceMotion ? 1.05 : 1)
                    .overlay(alignment: .topTrailing) {
                        if delivered {
                            Image(systemName: "checkmark.seal.fill").font(.largeTitle)
                                .foregroundStyle(.teal).background(.white, in: Circle())
                        }
                    }
            }.buttonStyle(.plain).disabled(selection == nil || delivered)
            Text(feedback).font(.headline).multilineTextAlignment(.center)
                .foregroundStyle(delivered ? .teal : .purple)
            if !delivered {
                PaintPaletteView(selection: $selection)
                Button("Entregar dibujo") { deliver() }
                    .buttonStyle(MazePrimaryButtonStyle()).disabled(painted == nil)
            } else if !finished {
                Button("Siguiente pedido") {
                    order += 1
                    painted = nil
                    selection = nil
                    delivered = false
                    feedback = "¡Vamos con otro dibujo!"
                }.buttonStyle(MazePrimaryButtonStyle())
            }
        }
        .sensoryFeedback(.success, trigger: delivered)
    }

    private func deliver() {
        guard !delivered, let painted else { return }
        if painted == orders[order] {
            withAnimation(reduceMotion ? nil : .spring(response: 0.4)) { delivered = true }
            feedback = "¡Pedido listo! \(orders[order].word): \(orders[order].spanish)."
            if order == orders.count - 1 {
                finished = true
                onCompleted(mistakes)
            } else {
                SoundManager.shared.play("audioBien", volume: Float(appState.soundVolume))
            }
        } else {
            mistakes += 1
            feedback = "El pedido dice \(orders[order].word), que significa \(orders[order].spanish). ¡Vuelve a pintarlo!"
            self.painted = nil
            selection = nil
            SoundManager.shared.play("audioMalaSeleccion", volume: Float(appState.soundVolume))
        }
    }
}
