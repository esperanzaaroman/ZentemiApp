import SwiftUI

struct FamilyReviewGameView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    var isPaused = false
    let onCompleted: (Int) -> Void
    @State private var game = FamilyReviewGame()
    @State private var direction = CGVector.zero
    @State private var instructions = true
    @State private var successPulse = false
    @State private var errorPulse = false
    @State private var floating = false

    var body: some View {
        VStack(spacing: 14) {
            HStack {
                Label("\(game.delivered.count) / 4 entregas", systemImage: "gift.fill")
                    .font(.headline).foregroundStyle(.teal)
                Spacer()
                if let carried = game.carried {
                    Text("Llevas: \(carried.yuhmu)").font(.headline).foregroundStyle(.purple)
                }
            }
            if instructions {
                VStack(spacing: 12) {
                    PetView(size: 85).accessibilityHidden(true)
                    Text("¡Vamos a reunir a la familia!").font(.headline)
                    Text("Usa la palanca para mover a Maicito. Recoge una palabra de la izquierda y llévala al personaje con su traducción, a la derecha.")
                    Text("Completa las cuatro entregas. El ejercicio cuenta como acierto si no haces entregas equivocadas.")
                        .font(.caption).foregroundStyle(.secondary)
                    Button("¡A jugar!") { instructions = false }
                        .buttonStyle(MazePrimaryButtonStyle())
                }
                .multilineTextAlignment(.center).padding(20)
                .background(.white, in: RoundedRectangle(cornerRadius: 24))
            } else {
                arena
                Text(game.feedback).font(.subheadline.bold())
                    .foregroundStyle(errorPulse ? .orange : .purple)
                    .multilineTextAlignment(.center).frame(minHeight: 48)
                if !game.finished {
                    MaicitoJoystick(direction: $direction)
                        .accessibilityLabel("Palanca para mover a Maicito")
                    Text("Recoge → lleva → entrega")
                        .font(.caption.bold()).foregroundStyle(.secondary)
                } else {
                    Label("¡Cuatro palabras aprendidas!", systemImage: "checkmark.seal.fill")
                        .font(.headline).foregroundStyle(.teal)
                }
            }
        }
        .task(id: instructions || isPaused || scenePhase != .active) {
            guard !instructions, !isPaused, scenePhase == .active else { return }
            var previous = ProcessInfo.processInfo.systemUptime
            do {
                while !Task.isCancelled && !game.finished {
                    try await Task.sleep(for: .milliseconds(20))
                    guard !isPaused, scenePhase == .active else { return }
                    let now = ProcessInfo.processInfo.systemUptime
                    let event = game.move(direction, delta: now - previous)
                    previous = now
                    handle(event)
                }
            } catch { }
        }
        .onChange(of: scenePhase) { _, phase in
            if phase != .active { direction = .zero }
        }
        .onChange(of: isPaused) { _, paused in
            if paused { direction = .zero }
        }
        .onDisappear { direction = .zero }
        .sensoryFeedback(.success, trigger: game.delivered.count)
        .sensoryFeedback(.warning, trigger: game.mistakes)
    }

    private var arena: some View {
        GeometryReader { geometry in
            ZStack {
                RoundedRectangle(cornerRadius: 25)
                    .fill(LinearGradient(colors: [.mint.opacity(0.25), .green.opacity(0.16)],
                                         startPoint: .top, endPoint: .bottom))
                RoundedRectangle(cornerRadius: 40)
                    .fill(.yellow.opacity(0.18))
                    .frame(width: geometry.size.width * 0.36)
                    .padding(.vertical, 12)
                ForEach(game.words) { term in
                    if !game.delivered.contains(term) && game.carried != term {
                        Text(term.yuhmu).font(.headline.bold())
                            .foregroundStyle(.purple)
                            .padding(10).background(.white, in: Capsule())
                            .shadow(color: .purple.opacity(0.18), radius: 3, y: 4)
                            .offset(y: floating && !reduceMotion ? -4 : 0)
                            .position(point(game.wordPosition(term), size: geometry.size))
                            .accessibilityLabel("Recoger \(term.yuhmu)")
                            .accessibilityAddTraits(.isButton)
                            .accessibilityAction { handle(game.visit(game.wordPosition(term))) }
                    }
                }
                ForEach(game.targets) { term in
                    VStack(spacing: 3) {
                        Image(term.imageName).resizable().scaledToFit().frame(height: 56)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                        Text(term.meaning).font(.caption.bold())
                        if game.delivered.contains(term) {
                            Image(systemName: "checkmark.circle.fill").foregroundStyle(.teal)
                        }
                    }
                    .padding(5).frame(width: geometry.size.width * 0.29)
                    .background(game.delivered.contains(term) ? Color.mint.opacity(0.7) : .white,
                                in: RoundedRectangle(cornerRadius: 15))
                    .scaleEffect(game.delivered.contains(term) ? 1.04 : 1)
                    .animation(reduceMotion ? nil : .spring(response: 0.3), value: game.delivered)
                    .position(point(game.targetPosition(term), size: geometry.size))
                    .accessibilityElement(children: .ignore)
                    .accessibilityLabel("Entregar a \(term.meaning)")
                    .accessibilityValue(game.delivered.contains(term) ? "Completado" : "")
                    .accessibilityAddTraits(.isButton)
                    .accessibilityAction { handle(game.visit(game.targetPosition(term))) }
                }
                VStack(spacing: 0) {
                    if let carried = game.carried {
                        Text(carried.yuhmu).font(.caption.bold()).padding(5)
                            .background(.white, in: Capsule())
                    }
                    PetView(size: 60)
                        .scaleEffect(successPulse && !reduceMotion ? 1.15 : 1)
                        .rotationEffect(.degrees(errorPulse && !reduceMotion ? -8 : 0))
                }
                .position(point(game.position, size: geometry.size))
                .allowsHitTesting(false).accessibilityHidden(true)
                if successPulse {
                    Image(systemName: "sparkles").font(.system(size: 48))
                        .foregroundStyle(.yellow).allowsHitTesting(false)
                        .transition(.scale.combined(with: .opacity))
                }
            }
            .clipped()
        }
        .frame(height: 400)
        .task {
            guard !reduceMotion else { return }
            withAnimation(.easeInOut(duration: 1.1).repeatForever(autoreverses: true)) { floating = true }
        }
        .task(id: game.delivered.count + game.mistakes) {
            guard successPulse || errorPulse else { return }
            try? await Task.sleep(for: .milliseconds(850))
            guard !Task.isCancelled else { return }
            withAnimation(.easeOut(duration: 0.2)) {
                successPulse = false
                errorPulse = false
            }
        }
    }

    private func point(_ point: CGPoint, size: CGSize) -> CGPoint {
        CGPoint(x: point.x * size.width, y: point.y * size.height)
    }
    private func handle(_ event: FamilyReviewGame.Event) {
        switch event {
        case .none: break
        case .pickup:
            SoundManager.shared.play("audioSeleccion", volume: Float(appState.soundVolume))
        case .correct, .finished:
            withAnimation(reduceMotion ? nil : .spring(response: 0.3)) { successPulse = true; errorPulse = false }
            SoundManager.shared.play("audioBien", volume: Float(appState.soundVolume))
            if game.finished { direction = .zero; onCompleted(game.mistakes) }
        case .wrong:
            withAnimation(reduceMotion ? nil : .spring(response: 0.25)) { errorPulse = true; successPulse = false }
            SoundManager.shared.play("audioMalaSeleccion", volume: Float(appState.soundVolume) * 0.65)
        }
    }
}
