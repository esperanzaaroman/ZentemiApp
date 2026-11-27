import SwiftUI

enum FirstPhrase: String, CaseIterable, Identifiable {
    case morning, name, age, help
    var id: String { rawValue }
    var translation: String {
        switch self {
        case .morning: "Buenos días"
        case .name: "¿Cómo te llamas?"
        case .age: "¿Cuántos años tienes?"
        case .help: "Ayúdame por favor"
        }
    }
    var audio: String {
        switch self {
        case .morning: "buenosDias"
        case .name: "comoTeLlamas"
        case .age: "cuantosAniosTienes"
        case .help: "ayudamePorfavor"
        }
    }
    var asset: String {
        switch self {
        case .morning: "phraseMorning"
        case .name: "phraseName"
        case .age: "phraseAge"
        case .help: "phraseHelp"
        }
    }
    var situation: String {
        switch self {
        case .morning: "Llegas a la escuela por la mañana. Saluda a tus amigos."
        case .name: "Conoces a alguien nuevo y quieres saber su nombre."
        case .age: "Tu amigo cumple años. Quieres preguntarle su edad."
        case .help: "Se te cayeron los libros y necesitas pedir ayuda."
        }
    }
}

struct FirstPhrasesLessonView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss
    @Environment(\.scenePhase) private var scenePhase
    let onLessonCompleted: () -> Void
    @State private var step = 0
    @State private var discovery: FirstPhrase = .morning
    @State private var heard: Set<FirstPhrase> = []
    @State private var exerciseListened = false
    @State private var choice: FirstPhrase?
    @State private var words: [String] = []
    @State private var checked = false
    @State private var correct = false
    @State private var score = 0
    @State private var showExit = false
    @State private var elapsed: Double = 0
    @State private var lastTick: Double?
    @State private var reward: FamilyLessonReward?
    private let wordBank = ["tienes", "Cuántos", "años"]
    private let titles = ["Descubre las frases", "Escucha y elige", "Ordena la traducción", "¿Qué dirías aquí?", "Un día con Maicito"]
    private var ready: Bool {
        switch step {
        case 0: heard.count == 4
        case 1: exerciseListened && choice != nil
        case 2: exerciseListened && words.count == 3
        case 3: exerciseListened && choice != nil
        default: false
        }
    }

    var body: some View {
        ZStack {
            LinearGradient(colors: [.purple.opacity(0.07), .mint.opacity(0.12)],
                           startPoint: .top, endPoint: .bottom).ignoresSafeArea()
            if let reward {
                LessonCompleteView(correctAnswers: score, totalAnswers: 5, elapsedSeconds: Int(elapsed),
                                   familyResult: reward, onContinue: onLessonCompleted)
            } else {
                ScrollViewReader { proxy in
                    ScrollView {
                        VStack(spacing: 20) {
                            HStack {
                                Button { showExit = true } label: {
                                    Image(systemName: "xmark").padding(14).background(.white, in: Circle())
                                }.accessibilityLabel("Salir de Primeras frases")
                                ProgressView(value: Double(step + 1), total: 5).tint(.teal)
                                Text("\(step + 1)/5").font(.headline).foregroundStyle(.teal)
                            }.id("phrasesTop")
                            Text(titles[step]).font(.title2.bold()).foregroundStyle(.purple)
                            activity.disabled(checked || showExit || scenePhase != .active)
                            if checked {
                                Label(correct ? "¡Muy bien!" : "Sigamos aprendiendo",
                                      systemImage: correct ? "checkmark.circle.fill" : "lightbulb.fill")
                                    .font(.headline).foregroundStyle(correct ? .teal : .orange)
                                Text(explanation).multilineTextAlignment(.center)
                                Button(step == 4 ? "Ver resultados" : "Siguiente") {
                                    tick()
                                    if step == 4 {
                                        let startingXP = appState.totalXP
                                        let first = !appState.greetingsRewardClaimed
                                        appState.completeGreetingsLesson()
                                        reward = FamilyLessonReward(coins: first ? 700 : 0, xp: first ? 140 : 0,
                                                                    startingXP: startingXP)
                                        lastTick = nil
                                    } else {
                                        step += 1
                                        checked = false
                                        choice = nil
                                        exerciseListened = false
                                        words = []
                                    }
                                    proxy.scrollTo("phrasesTop", anchor: .top)
                                }.buttonStyle(MazePrimaryButtonStyle())
                            } else if step < 4 {
                                Button(step == 0 ? "¡A practicar!" : "Verificar") {
                                    switch step {
                                    case 0: record(true)
                                    case 1: record(choice == .morning)
                                    case 2: record(words == ["Cuántos", "años", "tienes"])
                                    default: record(choice == .help)
                                    }
                                }.buttonStyle(MazePrimaryButtonStyle()).disabled(!ready)
                                    .opacity(ready ? 1 : 0.45)
                            }
                        }.padding(22).frame(maxWidth: 620).frame(maxWidth: .infinity)
                    }
                }
            }
        }
        .confirmationDialog("¿Salir de la lección?", isPresented: $showExit, titleVisibility: .visible) {
            Button("Salir sin terminar", role: .destructive) { dismiss() }
            Button("Continuar aprendiendo", role: .cancel) { }
        } message: { Text("Las respuestas de esta partida no se guardarán.") }
        .task(id: scenePhase) {
            guard scenePhase == .active, reward == nil else { return }
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
        .onChange(of: choice) { _, _ in
            if step == 3 { exerciseListened = false }
        }
        .onDisappear { tick(); lastTick = nil }
    }

    @ViewBuilder private var activity: some View {
        switch step {
        case 0:
            Text("Toca cada tarjeta y escucha su frase completa en Yuhmu.")
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 14) {
                ForEach(FirstPhrase.allCases) { phrase in
                    Button { discovery = phrase } label: {
                        VStack {
                            PhraseIllustration(phrase: phrase, height: 110)
                            Text(phrase.translation).font(.subheadline.bold())
                            Image(systemName: heard.contains(phrase) ? "checkmark.circle.fill" : "speaker.wave.2.fill")
                        }.padding(10).frame(maxWidth: .infinity)
                            .background(discovery == phrase ? Color.mint.opacity(0.25) : .white,
                                        in: RoundedRectangle(cornerRadius: 20))
                            .shadow(color: .purple.opacity(0.12), radius: 1, y: 4)
                    }.buttonStyle(.plain).foregroundStyle(.purple)
                }
            }
            Text(discovery.translation).font(.title3.bold())
            PhraseListeningPanel(phrase: discovery, paused: showExit) { heard.insert(discovery) }
                .id(discovery)
            Text("\(heard.count) de 4 frases escuchadas").font(.caption)
        case 1:
            Text("Escucha el audio y elige su traducción.")
            PhraseListeningPanel(phrase: .morning, paused: showExit) { exerciseListened = true }.id("listenMorning")
            PhraseTranslationChoices(choice: $choice)
        case 2:
            Text("Ordena las palabras para traducir lo que escuchas.")
            PhraseIllustration(phrase: .age, height: 180)
            PhraseListeningPanel(phrase: .age, paused: showExit) { exerciseListened = true }.id("listenAge")
            Text(words.isEmpty ? "¿…?" : "¿" + words.joined(separator: " ") + "?")
                .font(.title2.bold()).padding(18).frame(maxWidth: .infinity)
                .background(.white, in: RoundedRectangle(cornerRadius: 20))
            HStack {
                ForEach(wordBank, id: \.self) { word in
                    Button(word) { words.append(word) }
                        .buttonStyle(.borderedProminent).tint(.purple).disabled(words.contains(word))
                }
            }
            Button("Quitar última palabra", systemImage: "delete.left") {
                if !words.isEmpty { words.removeLast() }
            }.disabled(words.isEmpty)
        case 3:
            PhraseIllustration(phrase: .help, height: 200)
            Text(FirstPhrase.help.situation)
            Text("Elige la frase y escucha cómo se dice en Yuhmu.").font(.subheadline)
            PhraseTranslationChoices(choice: $choice)
            if let choice {
                PhraseListeningPanel(phrase: choice, paused: showExit) { exerciseListened = true }.id(choice)
            }
        default:
            PhraseDayGameView(paused: showExit) { mistakes in record(mistakes == 0) }
        }
    }

    private var explanation: String {
        switch step {
        case 0: "Ya conoces cuatro frases para saludar, preguntar y pedir ayuda."
        case 1: "El audio significa «Buenos días»."
        case 2: "La traducción es «¿Cuántos años tienes?»."
        case 3: "En esta situación usamos «Ayúdame por favor»."
        default: "¡Completaste las cuatro situaciones del día!"
        }
    }
    private func record(_ answer: Bool) {
        guard !checked, scenePhase == .active else { return }
        correct = answer
        checked = true
        if answer { score += 1 }
        SoundManager.shared.play(answer ? "audioBien" : "audioMalaSeleccion", volume: Float(appState.soundVolume))
    }
    private func tick() {
        guard let lastTick else { return }
        let now = ProcessInfo.processInfo.systemUptime
        elapsed += max(0, now - lastTick)
        self.lastTick = now
    }
}

private struct PhraseIllustration: View {
    let phrase: FirstPhrase
    var height: CGFloat = 180
    var body: some View {
        Image(phrase.asset).resizable().scaledToFit().frame(height: height)
            .clipShape(RoundedRectangle(cornerRadius: 20)).accessibilityHidden(true)
    }
}

private struct PhraseTranslationChoices: View {
    @Binding var choice: FirstPhrase?
    var body: some View {
        ForEach([FirstPhrase.name, .help, .morning, .age]) { phrase in
            Button { choice = phrase } label: {
                HStack {
                    Text(phrase.translation).font(.headline)
                    Spacer()
                    Image(systemName: choice == phrase ? "checkmark.circle.fill" : "circle")
                }.padding(16).foregroundStyle(.purple)
                    .background(choice == phrase ? Color.purple.opacity(0.12) : .white,
                                in: RoundedRectangle(cornerRadius: 18))
                    .shadow(color: .purple.opacity(0.12), radius: 1, y: 4)
            }.buttonStyle(.plain)
        }
    }
}

/// Cada panel se identifica por su frase: cambiarlo detiene el audio anterior.
private struct PhraseListeningPanel: View {
    let phrase: FirstPhrase
    var paused = false
    let onListened: () -> Void
    @EnvironmentObject private var appState: AppState
    @Environment(\.scenePhase) private var scenePhase
    @State private var player = PhraseSceneAudioPlayer()
    var body: some View {
        VStack(spacing: 8) {
            Button {
                player.play(named: phrase.audio, volume: Float(appState.soundVolume))
            } label: {
                Label(player.hasFinished ? "Escuchar otra vez" : "Escuchar en Yuhmu",
                      systemImage: "speaker.wave.2.fill")
                    .font(.headline).padding(14).frame(maxWidth: .infinity)
                    .background(.purple.opacity(0.12), in: Capsule())
            }.buttonStyle(.plain).foregroundStyle(.purple)
                .disabled(appState.soundVolume <= 0)
            if appState.soundVolume <= 0 {
                Text("Sube el volumen para escuchar la frase.")
                    .font(.caption).foregroundStyle(.orange)
            }
            Slider(value: $appState.soundVolume, in: 0...1)
                .accessibilityLabel("Volumen de las frases")
            if let error = player.errorMessage {
                Text(error).font(.caption).foregroundStyle(.orange)
            } else if player.hasFinished {
                Label("Audio escuchado completo", systemImage: "checkmark.circle.fill")
                    .font(.caption).foregroundStyle(.teal)
            } else {
                Text("Escucha hasta el final para continuar.").font(.caption).foregroundStyle(.secondary)
            }
        }
        .onChange(of: player.hasFinished) { _, finished in
            if finished { onListened() }
        }
        .onChange(of: scenePhase) { _, phase in if phase != .active { player.stop() } }
        .onChange(of: paused) { _, value in if value { player.stop() } }
        .onDisappear { player.stop() }
    }
}

private struct PhraseDayGameView: View {
    var paused = false
    let onCompleted: (Int) -> Void
    private let stops = FirstPhrase.allCases
    private let choices: [[FirstPhrase]] = [
        [.age, .morning, .help], [.help, .age, .name],
        [.age, .name, .morning], [.morning, .help, .name]
    ]
    @State private var stop = 0
    @State private var selected: FirstPhrase?
    @State private var listened: Set<FirstPhrase> = []
    @State private var mistakes = 0
    @State private var solved = false
    @State private var finished = false
    @State private var feedback = "Escucha las opciones y usa la frase adecuada."
    @EnvironmentObject private var appState: AppState
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack(spacing: 18) {
            Text("Un día con Maicito").font(.title2.bold()).foregroundStyle(.purple)
            HStack {
                ForEach(0..<4) { index in
                    VStack {
                        Image(systemName: index < stop || (index == stop && solved) ? "checkmark.seal.fill" : "mappin.circle.fill")
                            .font(.title).foregroundStyle(index <= stop ? .teal : .gray)
                        Text("\(index + 1)").font(.caption.bold())
                    }.frame(maxWidth: .infinity)
                }
            }
            HStack {
                PetView(size: 65)
                Text(stops[stop].situation).font(.headline)
            }
            PhraseIllustration(phrase: stops[stop], height: 220)
                .scaleEffect(solved && !reduceMotion ? 1.03 : 1)
            Text(feedback).multilineTextAlignment(.center).foregroundStyle(solved ? .teal : .purple)
            if !solved {
                ForEach(choices[stop]) { phrase in
                    Button { selected = phrase } label: {
                        Label("Audio \((choices[stop].firstIndex(of: phrase) ?? 0) + 1)",
                              systemImage: selected == phrase ? "checkmark.circle.fill" : "headphones")
                            .font(.headline).padding(14).frame(maxWidth: .infinity)
                            .background(selected == phrase ? Color.mint.opacity(0.3) : .white,
                                        in: RoundedRectangle(cornerRadius: 18))
                    }.buttonStyle(.plain).foregroundStyle(.purple)
                }
                if let selected {
                    PhraseListeningPanel(phrase: selected, paused: paused) { listened.insert(selected) }
                        .id("\(stop)-\(selected.rawValue)")
                }
                Button("Usar esta frase") { usePhrase() }
                    .buttonStyle(MazePrimaryButtonStyle())
                    .disabled(selected.map { !listened.contains($0) } ?? true)
            } else if !finished {
                Button("Ir a la siguiente situación") {
                    stop += 1
                    selected = nil
                    listened = []
                    solved = false
                    feedback = "Escucha las opciones y usa la frase adecuada."
                }.buttonStyle(MazePrimaryButtonStyle())
            }
            Text("Sin tiempo límite. Completa las cuatro situaciones sin errores para ganar el acierto.")
                .font(.caption).foregroundStyle(.secondary).multilineTextAlignment(.center)
        }.sensoryFeedback(.success, trigger: solved)
    }
    private func usePhrase() {
        guard !solved, let selected, listened.contains(selected) else { return }
        if selected == stops[stop] {
            withAnimation(reduceMotion ? nil : .spring(response: 0.4)) { solved = true }
            feedback = "¡Bien! Usaste «\(selected.translation)»."
            if stop == 3 {
                finished = true
                onCompleted(mistakes)
            } else {
                SoundManager.shared.play("audioBien", volume: Float(appState.soundVolume))
            }
        } else {
            mistakes += 1
            feedback = "Ese audio dice «\(selected.translation)». Aquí necesitas «\(stops[stop].translation)». Prueba otra opción."
            SoundManager.shared.play("audioMalaSeleccion", volume: Float(appState.soundVolume))
            self.selected = nil
        }
    }
}
