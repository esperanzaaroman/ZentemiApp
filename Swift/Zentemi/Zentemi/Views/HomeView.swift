import SwiftUI

struct HomeView: View {

    @EnvironmentObject var appState: AppState

    @State private var showCoinsInfo = false
    @State private var showStreakInfo = false
    @State private var mascotScaleY: CGFloat = 1.0
    @State private var animateBackground = false
    @State private var showLearningPath = false

    var body: some View {

        ZStack {

            background

            VStack(spacing: 18) {

                // MARK: - Indicadores superiores

                HStack {

                    statBadge(
                        icon: "dollarsign.circle.fill",
                        value: "\(appState.coins)",
                        foreground: .yellow
                    )
                    .onTapGesture {
                        showCoinsInfo = true
                    }

                    Spacer()

                    statBadge(
                        icon: "flame.fill",
                        value: "\(appState.streak)",
                        foreground: .red
                    )
                    .onTapGesture {
                        showStreakInfo = true
                    }
                }
                .padding(.horizontal, 22)
                .padding(.top, 12)

                Spacer()

                // MARK: - Mascota

                VStack(spacing: 5) {

                    Text(appState.petName)
                        .font(.system(size: 24, weight: .bold))
                        .foregroundStyle(
                            Color(
                                red: 0.35,
                                green: 0.17,
                                blue: 0.65
                            )
                        )
                        .offset(y: -15)

                    ZStack {

                        Circle()
                            .fill(.white.opacity(0.22))
                            .frame(width: 220, height: 220)
                            .blur(radius: 25)

                        Circle()
                            .fill(.yellow.opacity(0.15))
                            .frame(width: 170, height: 170)
                            .blur(radius: 25)

                        // ÚNICO CAMBIO:
                        // Ahora usa la mascota con accesorios.

                        PetView(size: 190)
                            .scaleEffect(
                                x: 1.0,
                                y: mascotScaleY,
                                anchor: .bottom
                            )
                    }
                }

                // MARK: - Felicidad

                VStack(alignment: .leading, spacing: 14) {

                    HStack {

                        Text("Felicidad de \(appState.petName)")
                            .font(.system(size: 16, weight: .semibold))

                        Spacer()

                        Text("\(appState.happiness)%")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundStyle(.teal)
                    }

                    ProgressView(
                        value: Double(appState.happiness),
                        total: 100
                    )
                    .tint(.teal)
                    .scaleEffect(x: 1, y: 1.8)

                    HStack(spacing: 12) {

                        Button {

                            print("Alimentar a \(appState.petName)")

                        } label: {

                            Text("Alimentar a \(appState.petName)")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundStyle(
                                    Color(
                                        red: 0.35,
                                        green: 0.17,
                                        blue: 0.65
                                    )
                                )
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .background(
                                    Color.purple.opacity(0.10)
                                )
                                .clipShape(
                                    RoundedRectangle(cornerRadius: 12)
                                )
                        }

                        Button {

                            print("Jugar con \(appState.petName)")

                        } label: {

                            Text("Jugar con \(appState.petName)")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundStyle(
                                    Color(
                                        red: 0.35,
                                        green: 0.17,
                                        blue: 0.65
                                    )
                                )
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .background(
                                    Color.purple.opacity(0.10)
                                )
                                .clipShape(
                                    RoundedRectangle(cornerRadius: 12)
                                )
                        }
                    }
                }
                .padding(18)
                .background(
                    RoundedRectangle(cornerRadius: 22)
                        .fill(.white.opacity(0.96))
                        .shadow(
                            color: .black.opacity(0.06),
                            radius: 10,
                            x: 0,
                            y: 5
                        )
                )
                .padding(.horizontal, 22)

                // MARK: - Continuar lección

                Button {

                    showLearningPath = true

                } label: {

                    Text("Jugar")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(
                            Color(
                                red: 0.43,
                                green: 0.27,
                                blue: 0.78
                            )
                        )
                        .clipShape(
                            RoundedRectangle(cornerRadius: 16)
                        )
                        .shadow(
                            color: Color.purple.opacity(0.20),
                            radius: 7,
                            x: 0,
                            y: 5
                        )
                }
                .padding(.horizontal, 30)

                Spacer()
            }

            // MARK: - Información de monedas

            if showCoinsInfo {

                infoOverlay(
                    title: "Monedas",
                    icon: "dollarsign.circle.fill",
                    text: """
                    Consigue monedas completando lecciones, retos y actividades.

                    Puedes utilizarlas para obtener objetos y recompensas dentro de Zentemi.
                    """,
                    color: .yellow
                ) {
                    showCoinsInfo = false
                }
            }

            // MARK: - Información de racha

            if showStreakInfo {

                infoOverlay(
                    title: "Racha",
                    icon: "flame.fill",
                    text: """
                    Tu racha representa los días consecutivos que has practicado Yuhmu.

                    Completa actividades cada día para aumentarla y mantenerla.
                    """,
                    color: .red
                ) {
                    showStreakInfo = false
                }
            }
        }
        .onAppear {

            withAnimation(
                .easeInOut(duration: 1.6)
                    .repeatForever(autoreverses: true)
            ) {
                mascotScaleY = 1.06
            }

            withAnimation(
                .easeInOut(duration: 6)
                    .repeatForever(autoreverses: true)
            ) {
                animateBackground = true
            }
        }
        .fullScreenCover(
            isPresented: $showLearningPath
        ) {
            LearningPathView()
                .environmentObject(appState)
        }
    }

    // MARK: - Indicadores

    private func statBadge(
        icon: String,
        value: String,
        foreground: Color
    ) -> some View {

        HStack(spacing: 6) {

            Image(systemName: icon)
                .foregroundStyle(foreground)

            Text(value)
                .font(.system(size: 14, weight: .bold))
                .foregroundStyle(.primary)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(.white.opacity(0.85))
        .clipShape(Capsule())
        .overlay {

            Capsule()
                .stroke(
                    foreground.opacity(0.35),
                    lineWidth: 1
                )
        }
    }

    // MARK: - Ventana informativa

    private func infoOverlay(
        title: String,
        icon: String,
        text: String,
        color: Color,
        close: @escaping () -> Void
    ) -> some View {

        ZStack {

            Color.black.opacity(0.28)
                .ignoresSafeArea()
                .onTapGesture {
                    close()
                }

            VStack(spacing: 14) {

                HStack(spacing: 8) {

                    Image(systemName: icon)
                        .font(.system(size: 22))
                        .foregroundStyle(color)

                    Text(title)
                        .font(.system(size: 23, weight: .bold))
                        .foregroundStyle(color)
                }

                Text(text)
                    .font(.system(size: 15))
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)

                Button {

                    close()

                } label: {

                    Text("Entendido")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundStyle(.black)
                        .padding(.horizontal, 25)
                        .padding(.vertical, 10)
                        .background(
                            color.opacity(0.12)
                        )
                        .clipShape(
                            RoundedRectangle(cornerRadius: 12)
                        )
                }
            }
            .padding(25)
            .frame(maxWidth: 300)
            .background(
                color.opacity(0.12)
            )
            .background(.white)
            .clipShape(
                RoundedRectangle(cornerRadius: 22)
            )
            .overlay {

                RoundedRectangle(cornerRadius: 22)
                    .stroke(
                        color.opacity(0.40),
                        lineWidth: 2
                    )
            }
            .shadow(
                color: .black.opacity(0.15),
                radius: 15
            )
        }
    }

    // MARK: - Fondo

    private var background: some View {

        ZStack {

            Color(
                red: 0.97,
                green: 0.95,
                blue: 0.99
            )

            Circle()
                .fill(Color.purple.opacity(0.06))
                .frame(width: 230, height: 230)
                .offset(
                    x: animateBackground ? -125 : -165,
                    y: animateBackground ? -250 : -290
                )

            Circle()
                .fill(Color.purple.opacity(0.05))
                .frame(width: 190, height: 190)
                .offset(
                    x: animateBackground ? 145 : 180,
                    y: animateBackground ? -110 : -65
                )

            Circle()
                .fill(Color.cyan.opacity(0.06))
                .frame(width: 200, height: 200)
                .offset(
                    x: animateBackground ? -125 : -165,
                    y: animateBackground ? 285 : 330
                )

            Circle()
                .fill(Color.yellow.opacity(0.08))
                .frame(width: 140, height: 140)
                .offset(
                    x: animateBackground ? 135 : 165,
                    y: animateBackground ? 300 : 340
                )
        }
        .ignoresSafeArea()
    }
}

#Preview {

    HomeView()
        .environmentObject(AppState())
}
