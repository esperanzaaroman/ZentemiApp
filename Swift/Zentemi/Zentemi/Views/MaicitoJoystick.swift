
import SwiftUI

struct MaicitoJoystick: View {

    @Binding var direction: CGVector

    @State private var knobOffset: CGSize = .zero

    private let joystickSize: CGFloat = 110
    private let knobSize: CGFloat = 46

    private var maxDistance: CGFloat {
        (joystickSize - knobSize) / 2
    }

    var body: some View {

        ZStack {

            Circle()
                .fill(.black.opacity(0.35))
                .frame(
                    width: joystickSize,
                    height: joystickSize
                )
                .overlay {

                    Circle()
                        .stroke(
                            .white.opacity(0.65),
                            lineWidth: 3
                        )
                }

            Circle()
                .fill(.white.opacity(0.9))
                .frame(
                    width: knobSize,
                    height: knobSize
                )
                .shadow(
                    color: .black.opacity(0.2),
                    radius: 5
                )
                .offset(knobOffset)
        }
        .frame(
            width: joystickSize, // volvar
            height: joystickSize
        )
        .contentShape(Circle())
        .gesture(

            DragGesture(minimumDistance: 0)
                .onChanged { value in

                    let dx = value.location.x
                        - joystickSize / 2

                    let dy = value.location.y
                        - joystickSize / 2

                    let distance = sqrt(
                        dx * dx + dy * dy
                    )

                    let scale = distance > maxDistance
                        ? maxDistance / distance
                        : 1

                    let x = dx * scale
                    let y = dy * scale

                    knobOffset = CGSize(
                        width: x,
                        height: y
                    )

                    direction = CGVector(
                        dx: x / maxDistance,
                        dy: y / maxDistance
                    )
                }
                .onEnded { _ in

                    withAnimation(
                        .easeOut(duration: 0.15)
                    ) {
                        knobOffset = .zero
                    }

                    direction = .zero
                }
        )
    }
}
