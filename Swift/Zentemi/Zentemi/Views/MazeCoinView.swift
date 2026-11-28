import SwiftUI

struct MazeFloatingCoinView: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @Environment(\.scenePhase) private var scenePhase
    let size: CGFloat

    var body: some View {
        TimelineView(.animation(minimumInterval: 1.0 / 30, paused: reduceMotion || scenePhase != .active)) { context in
            let time = context.date.timeIntervalSinceReferenceDate
            let angle = reduceMotion ? 0 : time.truncatingRemainder(dividingBy: 2) * 180
            let float = reduceMotion ? 0 : CGFloat(sin(time * 3)) * size * 0.14

            ZStack {
                Ellipse()
                    .fill(.black.opacity(0.18))
                    .frame(width: size * 0.7, height: size * 0.2)
                    .offset(y: size * 0.55)

                ZStack {
                    Circle()
                        .fill(.yellow.gradient)
                    Circle()
                        .stroke(Color.orange, lineWidth: max(1, size * 0.10))
                        .padding(size * 0.05)
                    Image(systemName: "star.fill")
                        .font(.system(size: size * 0.48))
                        .foregroundStyle(Color(red: 0.75, green: 0.39, blue: 0.03))
                }
                .frame(width: size, height: size)
                .rotation3DEffect(.degrees(angle), axis: (x: 0, y: 1, z: 0), perspective: 0.25)
                .offset(y: float)
            }
        }
        .frame(width: size, height: size)
        .accessibilityLabel("Moneda de 10")
        .allowsHitTesting(false)
    }
}

struct MazeCoinPickupView: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var floating = false
    @State private var visible = true

    var body: some View {
        Text("+10 monedas")
            .font(.caption.bold())
            .foregroundStyle(Color(red: 0.46, green: 0.24, blue: 0.02))
            .padding(.horizontal, 8)
            .padding(.vertical, 5)
            .background(.yellow, in: Capsule())
            .fixedSize()
            .shadow(color: .black.opacity(0.15), radius: 3, y: 2)
            .offset(y: reduceMotion ? -22 : floating ? -40 : -12)
            .opacity(visible ? 1 : 0)
            .task {
                withAnimation(reduceMotion ? nil : .easeOut(duration: 1)) {
                    floating = true
                }
                do {
                    try await Task.sleep(for: .milliseconds(850))
                    withAnimation(.easeOut(duration: 0.25)) {
                        visible = false
                    }
                } catch { }
            }
    }
}
