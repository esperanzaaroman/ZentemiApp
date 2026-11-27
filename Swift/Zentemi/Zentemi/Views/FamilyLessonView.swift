import SwiftUI

struct FamilyLessonView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    let onLessonCompleted: () -> Void
    @State private var session = FamilyLessonSession()
    @State private var reward: FamilyLessonReward?
    @State private var showExit = false

    var body: some View {
        ZStack {
            LinearGradient(colors: [Color(red: 0.97, green: 0.95, blue: 0.99), .teal.opacity(0.09)],
                           startPoint: .top, endPoint: .bottom).ignoresSafeArea()
            if let reward {
                LessonCompleteView(
                    correctAnswers: session.correctAnswers,
                    totalAnswers: 5,
                    elapsedSeconds: Int(session.elapsedSeconds),
                    familyResult: reward,
                    onContinue: onLessonCompleted
                )
            } else if appState.greetingsRewardClaimed {
                ScrollViewReader { proxy in
                    ScrollView {
                        VStack(spacing: 20) {
                            HStack {
                                Button { showExit = true } label: {
                                    Image(systemName: "xmark")
                                        .padding(14).background(.white, in: Circle())
                                }
                                .accessibilityLabel("Salir de Familia")
                                ProgressView(value: Double(session.step + 1), total: 5).tint(.teal)
                                Text("\(session.step + 1)/5").font(.headline).foregroundStyle(.teal)
                            }
                            .id("familyTop")
                            Text("Familia · \(session.title)")
                                .font(.title2.bold()).multilineTextAlignment(.center)
                                .foregroundStyle(FamilyPalette.purple)
                            if session.teaching {
                                FamilyTeachingCard(session: session) {
                                    withAnimation(reduceMotion ? nil : .easeInOut(duration: 0.25)) {
                                        session.startExercise()
                                    }
                                    proxy.scrollTo("familyTop", anchor: .top)
                                }
                                .id(session.step)
                            } else {
                                Text(session.question).font(.title3.bold())
                                    .multilineTextAlignment(.center)
                                if session.step == 4 {
                                    FamilyReviewGameView(isPaused: showExit) { mistakes in
                                        session.completeMiniGame(mistakes: mistakes)
                                    }
                                } else {
                                    FamilyExerciseContent(session: session)
                                        .disabled(session.checked || scenePhase != .active || showExit)
                                }
                                if session.checked {
                                    FamilyAnswerFeedback(
                                        correct: session.lastAnswerCorrect,
                                        explanation: session.explanation
                                    )
                                    Button(session.step == 4 ? "Ver resultados" : "Siguiente") {
                                        session.advance()
                                        if session.finished {
                                            reward = appState.completeFamilyLesson(
                                                correctAnswers: session.correctAnswers,
                                                seconds: session.elapsedSeconds
                                            )
                                        }
                                        proxy.scrollTo("familyTop", anchor: .top)
                                    }
                                    .buttonStyle(MazePrimaryButtonStyle())
                                } else if session.step != 4 {
                                    Button("Verificar") {
                                        guard scenePhase == .active, session.verify() else { return }
                                        SoundManager.shared.play(
                                            session.lastAnswerCorrect ? "audioBien" : "audioMalaSeleccion",
                                            volume: Float(appState.soundVolume)
                                        )
                                    }
                                    .buttonStyle(MazePrimaryButtonStyle())
                                    .disabled(!session.canVerify)
                                    .opacity(session.canVerify ? 1 : 0.45)
                                }
                            }
                        }
                        .padding(22).frame(maxWidth: 620).frame(maxWidth: .infinity)
                    }
                }
            } else {
                VStack(spacing: 20) {
                    Text("Completa Primeras frases para comenzar Familia.")
                    Button("Volver") { dismiss() }.buttonStyle(MazePrimaryButtonStyle())
                }.padding(24)
            }
        }
        .confirmationDialog("¿Salir de la lección?", isPresented: $showExit, titleVisibility: .visible) {
            Button("Salir sin terminar", role: .destructive) { dismiss() }
            Button("Seguir aprendiendo", role: .cancel) { }
        } message: {
            Text("Las respuestas de esta partida no se guardarán.")
        }
        .task(id: scenePhase) {
            guard scenePhase == .active, appState.greetingsRewardClaimed else { return }
            session.resumeClock()
            do {
                while !Task.isCancelled && !session.finished {
                    try await Task.sleep(for: .seconds(1))
                    guard scenePhase == .active else { return }
                    session.tick()
                }
            } catch { }
        }
        .onChange(of: scenePhase) { _, phase in
            if phase != .active { session.pauseClock() }
        }
        .onDisappear { session.pauseClock() }
    }
}

private enum FamilyPalette {
    static let purple = Color(red: 0.48, green: 0.29, blue: 0.79)
}

private struct FamilyPortrait: View {
    let name: String
    let asset: String
    var height: CGFloat = 120

    var body: some View {
        VStack(spacing: 8) {
            Image(asset).resizable().scaledToFit().frame(height: height)
                .clipShape(RoundedRectangle(cornerRadius: 18))
                .accessibilityHidden(true)
            Text(name).font(.headline).foregroundStyle(FamilyPalette.purple)
        }
        .frame(maxWidth: .infinity)
    }
}

private struct FamilyTeachingCard: View {
    let session: FamilyLessonSession
    let onContinue: () -> Void
    @EnvironmentObject private var appState: AppState
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var revealed: Set<FamilyTerm> = []

    var body: some View {
        VStack(spacing: 20) {
            HStack(alignment: .top, spacing: 14) {
                PetView(size: 75).accessibilityHidden(true)
                Text(session.teachingText).frame(maxWidth: .infinity, alignment: .leading)
            }
            Text("Toca a cada personaje para descubrir su palabra.")
                .font(.subheadline.bold()).foregroundStyle(.secondary)
            LazyVGrid(columns: [GridItem(.adaptive(minimum: 130))], spacing: 16) {
                ForEach(session.teachingTerms) { term in
                    Button {
                        withAnimation(reduceMotion ? nil : .spring(response: 0.35)) {
                            _ = revealed.insert(term)
                        }
                        SoundManager.shared.play("audioSeleccion", volume: Float(appState.soundVolume))
                    } label: {
                        VStack(spacing: 10) {
                            FamilyPortrait(name: term.meaning, asset: term.imageName, height: 125)
                            Text(revealed.contains(term) ? term.yuhmu : "Toca para descubrir")
                                .font(revealed.contains(term) ? .title2.bold() : .subheadline.bold())
                                .foregroundStyle(FamilyPalette.purple)
                                .frame(minHeight: 38)
                            Image(systemName: revealed.contains(term) ? "checkmark.circle.fill" : "hand.tap.fill")
                                .foregroundStyle(.teal)
                        }
                        .padding(12).frame(maxWidth: .infinity)
                        .background(revealed.contains(term) ? Color.mint.opacity(0.16) : .white,
                                    in: RoundedRectangle(cornerRadius: 22))
                        .overlay { RoundedRectangle(cornerRadius: 22).stroke(.teal.opacity(0.3), lineWidth: 2) }
                        .shadow(color: .purple.opacity(0.16), radius: 1, y: 5)
                        .scaleEffect(revealed.contains(term) && !reduceMotion ? 1.02 : 1)
                    }.buttonStyle(.plain)
                }
            }
            Text("\(revealed.count) de \(session.teachingTerms.count) palabras descubiertas")
                .font(.caption).foregroundStyle(.secondary)
            Button(session.step == 4 ? "¡Vamos a jugar!" : "¡Vamos a practicar!", action: onContinue)
                .buttonStyle(MazePrimaryButtonStyle())
                .disabled(revealed.count != session.teachingTerms.count)
                .opacity(revealed.count == session.teachingTerms.count ? 1 : 0.45)
        }
        .padding(16).background(.white.opacity(0.7), in: RoundedRectangle(cornerRadius: 26))
    }
}

private struct FamilyExerciseContent: View {
    @Bindable var session: FamilyLessonSession
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack(spacing: 16) {
            switch session.step {
            case 0:
                FamilyPortrait(name: "Leo · hermano de Ana", asset: "familyLeo", height: 180)
                ForEach([FamilyTerm.sisterByMan, .brother, .sisterByWoman]) { term in
                    Button { session.choice = term } label: {
                        FamilyChoiceLabel(text: term.yuhmu, selected: session.choice == term)
                    }.buttonStyle(.plain)
                }
            case 1:
                FamilyMatchingBoard(session: session)
            case 2:
                FamilyPortrait(name: "Una visita del tío", asset: "familyUncle", height: 170)
                Text("’W" + (session.missingLetter ?? "＿"))
                    .font(.largeTitle.bold()).foregroundStyle(FamilyPalette.purple)
                    .padding(24).frame(maxWidth: .infinity)
                    .background(.white, in: RoundedRectangle(cornerRadius: 22))
                Text("Mira la marca sobre la vocal y toca la que falta.").font(.subheadline)
                HStack {
                    ForEach(["é", "ě", "è"], id: \.self) { letter in
                        Button { session.missingLetter = letter } label: {
                            FamilyChoiceLabel(text: letter, selected: session.missingLetter == letter)
                        }.buttonStyle(.plain)
                    }
                }
            case 3:
                FamilyPortrait(name: "La madrina", asset: "familyGodmother", height: 170)
                HStack(spacing: 8) {
                    ForEach(0..<5) { index in
                        Text(index < session.orderedLetters.count ? session.orderedLetters[index] : "＿")
                            .font(.title.bold()).frame(maxWidth: .infinity).padding(.vertical, 14)
                            .background(.white, in: RoundedRectangle(cornerRadius: 12))
                    }
                }
                HStack(spacing: 8) {
                    ForEach(session.letterBank, id: \.self) { letter in
                        Button {
                            withAnimation(reduceMotion ? nil : .spring(response: 0.3)) {
                                session.appendLetter(letter)
                            }
                        } label: {
                            Text(letter).font(.title2.bold())
                                .frame(maxWidth: .infinity).padding(.vertical, 14)
                                .background(.purple.opacity(0.12), in: RoundedRectangle(cornerRadius: 12))
                                .shadow(color: .purple.opacity(0.2), radius: 1, y: 4)
                        }
                        .buttonStyle(.plain)
                        .disabled(session.orderedLetters.contains(letter))
                        .opacity(session.orderedLetters.contains(letter) ? 0.25 : 1)
                    }
                }.foregroundStyle(FamilyPalette.purple)
                Button("Quitar última letra", systemImage: "delete.left") { session.undoLetter() }
                    .disabled(session.orderedLetters.isEmpty)
            default:
                EmptyView()
            }
        }
    }
}

private struct FamilyChoiceLabel: View {
    let text: String
    let selected: Bool
    var body: some View {
        HStack {
            Text(text).font(.title3.bold())
            Spacer(minLength: 8)
            Image(systemName: selected ? "checkmark.circle.fill" : "circle")
        }
        .foregroundStyle(FamilyPalette.purple)
        .padding(18).frame(maxWidth: .infinity)
        .background(selected ? Color.purple.opacity(0.12) : .white, in: RoundedRectangle(cornerRadius: 18))
        .overlay { RoundedRectangle(cornerRadius: 18).stroke(selected ? FamilyPalette.purple : .purple.opacity(0.15), lineWidth: 2) }
        .shadow(color: .purple.opacity(0.10), radius: 2, y: 4)
        .accessibilityAddTraits(selected ? .isSelected : [])
    }
}

private struct FamilyMatchingBoard: View {
    let session: FamilyLessonSession
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack(spacing: 18) {
            Text("Toca una palabra y después su personaje. También puedes arrastrarla. Puedes cambiar las parejas antes de verificar.")
                .font(.subheadline).foregroundStyle(.secondary).multilineTextAlignment(.center)
            ForEach([FamilyTerm.grandfather, .grandmother]) { label in
                Button { session.selectedLabel = label } label: {
                    FamilyChoiceLabel(text: label.yuhmu, selected: session.selectedLabel == label)
                }
                .buttonStyle(.plain)
                .draggable(label.rawValue)
            }
            HStack(alignment: .top, spacing: 12) {
                ForEach(FamilyTerm.grandparents) { target in
                    Button {
                        if let label = session.selectedLabel { assign(label, to: target) }
                    } label: {
                        VStack(spacing: 10) {
                            FamilyPortrait(name: target.meaning, asset: target.imageName, height: 120)
                            Text(session.assignments[target]?.yuhmu ?? "Coloca aquí")
                                .font(.title3.bold()).foregroundStyle(FamilyPalette.purple)
                        }
                        .padding(12).frame(maxWidth: .infinity)
                        .background(.white, in: RoundedRectangle(cornerRadius: 20))
                        .overlay {
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(.teal.opacity(0.6), style: StrokeStyle(lineWidth: 2, dash: [6]))
                        }
                        .shadow(color: .teal.opacity(0.15), radius: 2, y: 5)
                    }
                    .buttonStyle(.plain)
                    .dropDestination(for: String.self) { items, _ in
                        guard let raw = items.first, let term = FamilyTerm(rawValue: raw),
                              FamilyTerm.grandparents.contains(term), !session.checked else { return false }
                        assign(term, to: target)
                        return true
                    }
                    .accessibilityHint("Selecciona primero una palabra y después activa este destino.")
                }
            }
        }
    }

    private func assign(_ label: FamilyTerm, to target: FamilyTerm) {
        withAnimation(reduceMotion ? nil : .spring(response: 0.3)) {
            session.assign(label, to: target)
        }
    }
}

private struct FamilyAnswerFeedback: View {
    let correct: Bool
    let explanation: String
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label(correct ? "¡Muy bien!" : "Aprendamos de esta respuesta",
                  systemImage: correct ? "checkmark.circle.fill" : "lightbulb.fill")
                .font(.headline).foregroundStyle(correct ? .teal : .orange)
            Text(explanation).font(.body)
        }
        .padding(20).frame(maxWidth: .infinity, alignment: .leading)
        .background(.white, in: RoundedRectangle(cornerRadius: 22))
        .accessibilityElement(children: .combine)
    }
}
