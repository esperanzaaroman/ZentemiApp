import SwiftUI

struct LessonCompleteView: View {

    @EnvironmentObject var appState: AppState

    let correctAnswers: Int
    let totalAnswers: Int
    let elapsedSeconds: Int
    let onContinue: () -> Void

    @State private var showMascot = false
    @State private var visibleStars = 0
    @State private var showCorrectCard = false
    @State private var showTimeCard = false
    @State private var showCoinsCard = false
    @State private var showXPCard = false
    @State private var showContinueButton = false

    @State private var displayedXP = 0
    @State private var displayedLevel = 1
    @State private var displayedXPRemaining = 100
    @State private var xpBarProgress: Double = 0

    @State private var rewardApplied = false

    private let rewardCoins = 700
    private let rewardXP = 140

    private let purple = Color(
        red: 0.48,
        green: 0.29,
        blue: 0.79
    )

    private let turquoise = Color(
        red: 0.16,
        green: 0.78,
        blue: 0.70
    )

    private let backgroundColor = Color(
        red: 0.97,
        green: 0.95,
        blue: 0.99
    )

    private var starsEarned: Int {

        guard totalAnswers > 0 else {
            return 1
        }

        let percentage =
            Double(correctAnswers)
            / Double(totalAnswers)

        if percentage >= 1.0 {
            return 4
        }

        if percentage >= 0.75 {
            return 3
        }

        if percentage >= 0.50 {
            return 2
        }

        return 1
    }

    private var formattedTime: String {

        let minutes = elapsedSeconds / 60
        let seconds = elapsedSeconds % 60

        return String(
            format: "%d:%02d",
            minutes,
            seconds
        )
    }

    var body: some View {

        ZStack {

            backgroundColor
                .ignoresSafeArea()

            ScrollView(
                showsIndicators: false
            ) {

                VStack(spacing: 18) {

                    Spacer()
                        .frame(height: 45)

                    // MARK: - MASCOT

                    if showMascot {

                        PetView(size: 105)
                            .environmentObject(appState)
                            .transition(
                                .scale.combined(
                                    with: .opacity
                                )
                            )
                    }

                    // MARK: - TITLE

                    if showMascot {

                        Text("¡Excelente!")
                            .font(
                                .system(
                                    size: 28,
                                    weight: .bold
                                )
                            )
                            .foregroundStyle(purple)
                            .transition(.opacity)
                    }

                    // MARK: - STARS

                    HStack(spacing: 18) {

                        ForEach(
                            0..<starsEarned,
                            id: \.self
                        ) { index in

                            Image(
                                systemName: "star.fill"
                            )
                            .font(
                                .system(size: 31)
                            )
                            .foregroundStyle(.yellow)
                            .scaleEffect(
                                index < visibleStars
                                ? 1
                                : 0
                            )
                            .opacity(
                                index < visibleStars
                                ? 1
                                : 0
                            )
                        }
                    }
                    .frame(height: 45)

                    // MARK: - RESULTS

                    HStack(spacing: 8) {

                        resultCard(
                            visible: showCorrectCard,
                            icon: "checkmark",
                            value:
                                "\(correctAnswers)/\(totalAnswers)",
                            title: "Aciertos",
                            color: turquoise
                        )

                        resultCard(
                            visible: showTimeCard,
                            icon: "clock.fill",
                            value: formattedTime,
                            title: "Tiempo",
                            color: purple
                        )

                        resultCard(
                            visible: showCoinsCard,
                            icon: "dollarsign.circle.fill",
                            value: "+\(rewardCoins)",
                            title: "Monedas",
                            color: .orange
                        )
                    }

                    // MARK: - EXPERIENCE

                    if showXPCard {

                        VStack(
                            alignment: .leading,
                            spacing: 12
                        ) {

                            HStack {

                                Text("Experiencia")
                                    .font(
                                        .system(
                                            size: 14,
                                            weight: .bold
                                        )
                                    )

                                Spacer()

                                Text(
                                    "Nivel \(displayedLevel) · \(displayedXP) XP"
                                )
                                .font(
                                    .system(
                                        size: 13,
                                        weight: .bold
                                    )
                                )
                                .foregroundStyle(turquoise)
                            }

                            GeometryReader { geometry in

                                ZStack(
                                    alignment: .leading
                                ) {

                                    Capsule()
                                        .fill(
                                            Color.gray.opacity(
                                                0.10
                                            )
                                        )

                                    Capsule()
                                        .fill(turquoise)
                                        .frame(
                                            width:
                                                geometry.size.width
                                                * xpBarProgress
                                        )
                                }
                            }
                            .frame(height: 10)

                            if displayedLevel >= appState.maxLevel {

                                Text("Nivel máximo alcanzado")
                                    .font(
                                        .system(size: 11)
                                    )
                                    .foregroundStyle(
                                        Color.gray.opacity(0.65)
                                    )

                            } else {

                                Text(
                                    "\(displayedXPRemaining) XP para el Nivel \(displayedLevel + 1)"
                                )
                                .font(
                                    .system(size: 11)
                                )
                                .foregroundStyle(
                                    Color.gray.opacity(0.65)
                                )
                            }
                        }
                        .padding(16)
                        .background(.white)
                        .clipShape(
                            RoundedRectangle(
                                cornerRadius: 16
                            )
                        )
                        .shadow(
                            color: .black.opacity(0.05),
                            radius: 8,
                            y: 4
                        )
                        .transition(
                            .move(edge: .bottom)
                                .combined(with: .opacity)
                        )
                    }

                    Spacer()
                        .frame(height: 80)
                }
                .padding(.horizontal, 22)
            }

            // MARK: - CONTINUE

            VStack {

                Spacer()

                if showContinueButton {

                    Button {

                        onContinue()

                    } label: {

                        Text("Continuar")
                            .font(
                                .system(
                                    size: 16,
                                    weight: .bold
                                )
                            )
                            .foregroundStyle(.white)
                            .frame(
                                maxWidth: .infinity
                            )
                            .frame(height: 54)
                            .background(purple)
                            .clipShape(
                                RoundedRectangle(
                                    cornerRadius: 14
                                )
                            )
                            .shadow(
                                color:
                                    purple.opacity(0.25),
                                radius: 8,
                                y: 5
                            )
                    }
                    .transition(
                        .move(edge: .bottom)
                            .combined(with: .opacity)
                    )
                }
            }
            .padding(.horizontal, 22)
            .padding(.bottom, 18)
        }
        .onAppear {

            runSequence()
        }
    }

    // MARK: - RESULT CARD

    private func resultCard(
        visible: Bool,
        icon: String,
        value: String,
        title: String,
        color: Color
    ) -> some View {

        VStack(spacing: 7) {

            Image(systemName: icon)
                .font(
                    .system(
                        size: 16,
                        weight: .bold
                    )
                )
                .foregroundStyle(color)

            Text(value)
                .font(
                    .system(
                        size: 15,
                        weight: .bold
                    )
                )

            Text(title)
                .font(
                    .system(size: 11)
                )
                .foregroundStyle(.secondary)
        }
        .frame(
            maxWidth: .infinity
        )
        .frame(height: 88)
        .background(.white)
        .clipShape(
            RoundedRectangle(
                cornerRadius: 15
            )
        )
        .shadow(
            color: .black.opacity(0.04),
            radius: 7,
            y: 3
        )
        .scaleEffect(
            visible ? 1 : 0.7
        )
        .opacity(
            visible ? 1 : 0
        )
    }

    // MARK: - SEQUENCE

    private func runSequence() {

        let startingXP = appState.totalXP

        displayedXP = startingXP
        updateDisplayedLevel(for: startingXP)

        withAnimation(
            .spring(
                response: 0.55,
                dampingFraction: 0.70
            )
        ) {
            showMascot = true
        }

        for index in 1...starsEarned {

            DispatchQueue.main.asyncAfter(
                deadline:
                    .now()
                    + 0.45
                    + Double(index) * 0.18
            ) {

                withAnimation(
                    .spring(
                        response: 0.35,
                        dampingFraction: 0.55
                    )
                ) {
                    visibleStars = index
                }
            }
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 1.35
        ) {

            withAnimation(
                .spring(
                    response: 0.45,
                    dampingFraction: 0.75
                )
            ) {
                showCorrectCard = true
            }
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 1.60
        ) {

            withAnimation(
                .spring(
                    response: 0.45,
                    dampingFraction: 0.75
                )
            ) {
                showTimeCard = true
            }
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 1.85
        ) {

            withAnimation(
                .spring(
                    response: 0.45,
                    dampingFraction: 0.75
                )
            ) {
                showCoinsCard = true
            }
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 2.15
        ) {

            withAnimation(
                .easeOut(duration: 0.45)
            ) {
                showXPCard = true
            }
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 2.65
        ) {

            applyRewardAndAnimateXP(
                startingXP: startingXP
            )
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 4.35
        ) {

            withAnimation(
                .easeOut(duration: 0.40)
            ) {
                showContinueButton = true
            }
        }
    }

    // MARK: - REWARD

    private func applyRewardAndAnimateXP(
        startingXP: Int
    ) {

        guard !rewardApplied else {
            return
        }

        rewardApplied = true

        let wasAlreadyClaimed =
            appState.greetingsRewardClaimed

        appState.completeGreetingsLesson()

        if wasAlreadyClaimed {

            displayedXP = appState.totalXP
            updateDisplayedLevel(
                for: appState.totalXP
            )

            return
        }

        animateXP(
            from: startingXP,
            amount: rewardXP
        )
    }

    // MARK: - XP ANIMATION

    private func animateXP(
        from startingXP: Int,
        amount: Int
    ) {

        let steps = 70

        for step in 0...steps {

            DispatchQueue.main.asyncAfter(
                deadline:
                    .now()
                    + Double(step) * 0.02
            ) {

                let animatedAmount =
                    Int(
                        Double(amount)
                        * Double(step)
                        / Double(steps)
                    )

                let total =
                    startingXP + animatedAmount

                displayedXP = total

                updateDisplayedLevel(
                    for: total
                )
            }
        }

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 1.5
        ) {

            displayedXP =
                appState.totalXP

            updateDisplayedLevel(
                for: appState.totalXP
            )
        }
    }

    // MARK: - DISPLAYED LEVEL

    private func updateDisplayedLevel(
        for totalXP: Int
    ) {

        let calculatedLevel =
            min(
                (totalXP / appState.xpRequiredPerLevel) + 1,
                appState.maxLevel
            )

        displayedLevel = calculatedLevel

        if calculatedLevel >= appState.maxLevel {

            displayedXPRemaining = 0

            withAnimation(
                .linear(duration: 0.05)
            ) {
                xpBarProgress = 1
            }

            return
        }

        let levelXP =
            totalXP % appState.xpRequiredPerLevel

        displayedXPRemaining =
            appState.xpRequiredPerLevel - levelXP

        withAnimation(
            .linear(duration: 0.05)
        ) {

            xpBarProgress =
                Double(levelXP)
                / Double(appState.xpRequiredPerLevel)
        }
    }
}


// MARK: - PREVIEW

#Preview {

    LessonCompleteView(
        correctAnswers: 4,
        totalAnswers: 4,
        elapsedSeconds: 150
    ) {
        print("Continuar")
    }
    .environmentObject(
        AppState()
    )
}
