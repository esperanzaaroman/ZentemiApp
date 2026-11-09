
import SwiftUI

struct FeedMaicitoView: View {

    @State private var mascotScaleY: CGFloat = 1.0
    @State private var tutorialShownThisSession = false

    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss

    // MARK: - CONFIGURACIÓN

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

    private let maxMeals = 3
    private let feedingInterval: TimeInterval = 4 * 60 * 60

    // MARK: - ALIMENTOS

    private struct Food: Identifiable {
        let id: String
        let name: String
        let price: Int
        let happiness: Int
    }

    private let foods: [Food] = [
        Food(
            id: "manzana",
            name: "Manzana",
            price: 20,
            happiness: 5
        ),
        Food(
            id: "tortillas",
            name: "Tortillas",
            price: 30,
            happiness: 8
        ),
        Food(
            id: "tamal",
            name: "Tamal",
            price: 50,
            happiness: 15
        )
    ]

    // MARK: - ESTADO

    @State private var selectedFood: Food?
    @State private var foodDragOffset: CGSize = .zero
    @State private var isFeeding = false

    @State private var showHearts = false
    @State private var showTutorial = false
    @State private var tutorialHandOffset: CGFloat = 0

    @State private var message: String?
    @State private var messageTask: Task<Void, Never>?

    @State private var mealDates: [Date] = []
    @State private var currentDate = Date()

    // MARK: - GUARDADO

    @AppStorage("maicitoFeedingTutorialSeen")
    private var tutorialSeen = false

    private var happiness: Int {
        min(max(appState.happiness, 0), 100)
    }

    private var availableMeals: Int {
        max(0, maxMeals - mealDates.count)
    }

    private var isFull: Bool {
        happiness >= 100
    }

    private var canChooseFood: Bool {
        !isFull
        && availableMeals > 0
        && !isFeeding
    }

    var body: some View {

        GeometryReader { geometry in

            ZStack {

                // MARK: - FONDO

                Image("fondoCocina")
                    .resizable()
                    .scaledToFill()
                    .frame(
                        width: geometry.size.width,
                        height: geometry.size.height
                    )
                    .clipped()
                    .ignoresSafeArea()

                // Oscurecimiento ligero para legibilidad
                Color.black.opacity(0.08)
                    .ignoresSafeArea()

                // MARK: - CONTENIDO

                VStack(spacing: 0) {

                    header

                    Spacer(minLength: 12)

                    feedingArea

                    Spacer(minLength: 12)

                    foodCarousel

                    feedingStatus
                }
                .padding(.bottom, 12)

                // MARK: - MENSAJES

                if let message {

                    VStack {

                        Spacer()

                        Text(message)
                            .font(
                                .system(
                                    size: 14,
                                    weight: .bold
                                )
                            )
                            .multilineTextAlignment(.center)
                            .foregroundStyle(.white)
                            .padding(.horizontal, 20)
                            .padding(.vertical, 12)
                            .background(
                                purple.opacity(0.95),
                                in: Capsule()
                            )
                            .shadow(radius: 5)
                            .padding(.bottom, 185)
                    }
                    .allowsHitTesting(false)
                    .transition(.opacity)
                }
            }
        }
        .onAppear {
            loadFeedingHistory()
        }
        .onDisappear {
            messageTask?.cancel()
        }
    }

    // MARK: - ENCABEZADO

    private var header: some View {

        HStack(alignment: .top, spacing: 12) {

            Button {
                dismiss()
            } label: {

                Image(systemName: "chevron.left")
                    .font(
                        .system(
                            size: 18,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(purple)
                    .frame(width: 44, height: 44)
                    .background(
                        .white,
                        in: RoundedRectangle(
                            cornerRadius: 14
                        )
                    )
            }

            VStack(spacing: 7) {

                HStack {

                    Text("Felicidad de Maicito")
                        .font(
                            .system(
                                size: 13,
                                weight: .bold
                            )
                        )

                    Spacer()

                    Text("\(happiness)%")
                        .font(
                            .system(
                                size: 14,
                                weight: .heavy
                            )
                        )
                }
                .foregroundStyle(purple)

                GeometryReader { geometry in

                    ZStack(alignment: .leading) {

                        Capsule()
                            .fill(
                                Color.gray.opacity(0.18)
                            )

                        Capsule()
                            .fill(turquoise)
                            .frame(
                                width:
                                    geometry.size.width
                                    * CGFloat(happiness)
                                    / 100
                            )
                    }
                }
                .frame(height: 12)
            }
            .padding(13)
            .background(
                .white.opacity(0.96),
                in: RoundedRectangle(
                    cornerRadius: 15
                )
            )

            Spacer(minLength: 0)
        }
        .padding(.horizontal, 16)
        .padding(.top, 12)
    }

    // MARK: - ÁREA DE ALIMENTACIÓN

    private var feedingArea: some View {

        GeometryReader { geometry in

            let mascotCenter = CGPoint(
                x: geometry.size.width / 2,
                y: geometry.size.height * 0.32
            )

            let plateCenter = CGPoint(
                x: geometry.size.width / 2,
                y: geometry.size.height * 0.73
            )

            ZStack {

                // MARK: - MAICITO

                PetView(size: 165)
                    .environmentObject(appState)
                    .scaleEffect(
                        x: 1.0,
                        y: mascotScaleY,
                        anchor: .bottom
                    )
                    .position(mascotCenter)
                    .onAppear {
                        withAnimation(
                            .easeInOut(duration: 1.6)
                                .repeatForever(autoreverses: true)
                        ) {
                            mascotScaleY = 1.06
                        }
                    }

                // MARK: - CORAZONES

                if showHearts {

                    HeartAnimationView()
                        .frame(width: 180, height: 150)
                        .position(
                            x: mascotCenter.x,
                            y: mascotCenter.y - 85
                        )
                        .allowsHitTesting(false)
                }

                // MARK: - PLATO

                Image("plato")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 190, height: 130)
                    .position(plateCenter)

                // MARK: - COMIDA SELECCIONADA

                if let selectedFood {

                    Image(selectedFood.id)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 100, height: 100)
                        .offset(foodDragOffset)
                        .position(
                            x: plateCenter.x,
                            y: plateCenter.y - 12
                        )
                        .zIndex(5)
                        .gesture(

                            DragGesture(minimumDistance: 0)
                                .onChanged { value in

                                    guard !isFeeding else {
                                        return
                                    }

                                    foodDragOffset =
                                        value.translation

                                    if showTutorial {
                                        showTutorial = false
                                        tutorialSeen = true
                                    }
                                }
                                .onEnded { value in

                                    guard !isFeeding else {
                                        return
                                    }

                                    let finalPosition = CGPoint(
                                        x:
                                            plateCenter.x
                                            + value.translation.width,
                                        y:
                                            plateCenter.y - 12
                                            + value.translation.height
                                    )

                                    let distance = sqrt(
                                        pow(
                                            finalPosition.x
                                            - mascotCenter.x,
                                            2
                                        )
                                        +
                                        pow(
                                            finalPosition.y
                                            - mascotCenter.y,
                                            2
                                        )
                                    )

                                    if distance < 105 {

                                        feedMaicito(
                                            with: selectedFood
                                        )

                                    } else {

                                        withAnimation(
                                            .spring(
                                                response: 0.35,
                                                dampingFraction: 0.65
                                            )
                                        ) {
                                            foodDragOffset = .zero
                                        }
                                    }
                                }
                        )
                }

                // MARK: - TUTORIAL

                if showTutorial && selectedFood != nil {

                    VStack(spacing: 8) {

                        Text(
                            "¡Desliza la comida hacia Maicito!"
                        )
                        .font(
                            .system(
                                size: 13,
                                weight: .bold
                            )
                        )
                        .foregroundStyle(.white)
                        .padding(.horizontal, 15)
                        .padding(.vertical, 10)
                        .background(
                            purple,
                            in: Capsule()
                        )

                        Image(
                            systemName: "hand.point.up.fill"
                        )
                        .font(.system(size: 27))
                        .foregroundStyle(.white)
                        .shadow(
                            color: .black.opacity(0.4),
                            radius: 4
                        )
                        .offset(y: tutorialHandOffset)
                    }
                    .position(
                        x: plateCenter.x,
                        y: plateCenter.y - 115
                    )
                    .allowsHitTesting(false)
                    .onAppear {

                        withAnimation(
                            .easeInOut(duration: 0.75)
                                .repeatForever(
                                    autoreverses: true
                                )
                        ) {
                            tutorialHandOffset = -20
                        }
                    }
                }
            }
        }
    }

    // MARK: - CARRUSEL DE COMIDA

    private var foodCarousel: some View {

        VStack(alignment: .leading, spacing: 10) {

            HStack {

                Text("Alimentos")
                    .font(
                        .system(
                            size: 18,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(purple)

                Spacer()

                Label(
                    "\(appState.coins)",
                    systemImage: "dollarsign.circle.fill"
                )
                .font(
                    .system(
                        size: 14,
                        weight: .bold
                    )
                )
                .foregroundStyle(.orange)
            }
            .padding(.horizontal, 18)
            .padding(.vertical, 12)
            .background(
                .white.opacity(0.96),
                in: RoundedRectangle(cornerRadius: 16)
            )
            .padding(.horizontal, 16)

            ScrollView(
                .horizontal,
                showsIndicators: false
            ) {

                HStack(spacing: 12) {

                    ForEach(foods) { food in

                        foodCard(food)
                    }
                }
                .padding(.horizontal, 18)
                .padding(.vertical, 5)
            }
        }
    }

    // MARK: - TARJETA DE ALIMENTO

    private func foodCard(
        _ food: Food
    ) -> some View {

        let canAfford = appState.coins >= food.price

        let isSelected =
            selectedFood?.id == food.id

        return Button {

            selectFood(food)

        } label: {

            VStack(spacing: 6) {

                Image(food.id)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 75, height: 75)

                Text(food.name)
                    .font(
                        .system(
                            size: 13,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(purple)

                Text("+\(food.happiness)% felicidad")
                    .font(
                        .system(
                            size: 11,
                            weight: .semibold
                        )
                    )
                    .foregroundStyle(turquoise)

                Label(
                    "\(food.price)",
                    systemImage: "dollarsign.circle.fill"
                )
                .font(
                    .system(
                        size: 12,
                        weight: .bold
                    )
                )
                .foregroundStyle(.orange)
            }
            .frame(width: 120, height: 157)
            .background(
                .white.opacity(0.96),
                in: RoundedRectangle(
                    cornerRadius: 17
                )
            )
            .overlay {

                RoundedRectangle(
                    cornerRadius: 17
                )
                .stroke(
                    isSelected ? purple : .clear,
                    lineWidth: 3
                )
            }
            .opacity(
                canChooseFood && canAfford
                    ? 1
                    : 0.55
            )
        }
        .buttonStyle(.plain)
        .disabled(
            !canChooseFood || !canAfford
        )
    }

    // MARK: - ESTADO DE ALIMENTACIÓN

    private var feedingStatus: some View {

        VStack(spacing: 6) {

            if isFull {

                Text("¡Maicito está muy feliz!")
                    .foregroundStyle(.green)

            } else if availableMeals == 0 {

                Text("Maicito está lleno. Vuelve más tarde.")
                    .foregroundStyle(purple)

            } else {

                Text(
                    "Comidas disponibles: \(availableMeals)/\(maxMeals)"
                )
                .foregroundStyle(purple)
            }
        }
        .font(
            .system(
                size: 13,
                weight: .bold
            )
        )
        .padding(.vertical, 8)
        .padding(.horizontal, 15)
        .background(
            .white.opacity(0.95),
            in: Capsule()
        )
        .padding(.top, 5)
    }

    // MARK: - SELECCIONAR ALIMENTO

    private func selectFood(
        _ food: Food
    ) {

        guard canChooseFood else {
            return
        }

        guard appState.coins >= food.price else {

            showMessage(
                "No tienes suficientes monedas."
            )

            return
        }

        selectedFood = food
        foodDragOffset = .zero

        SoundManager.shared.play(
            "audioSeleccion",
            volume: Float(appState.soundVolume)
        )

        if !tutorialShownThisSession {
            showTutorial = true
            tutorialShownThisSession = true
        }
    }

    // MARK: - ALIMENTAR

    private func feedMaicito(
        with food: Food
    ) {

        guard !isFeeding,
              happiness < 100,
              availableMeals > 0,
              appState.coins >= food.price
        else {

            withAnimation {
                foodDragOffset = .zero
            }

            return
        }

        isFeeding = true

        // Descontar monedas
        appState.coins -= food.price

        // Aumentar felicidad sin superar 100%
        appState.happiness = min(
            100,
            appState.happiness + food.happiness
        )

        // Registrar comida
        mealDates.append(Date())
        saveFeedingHistory()

        // Quitar alimento del plato
        selectedFood = nil
        foodDragOffset = .zero
        showTutorial = false

        // Animación
        showHearts = true

        SoundManager.shared.play(
            "audioBien",
            volume: Float(appState.soundVolume)
        )

        showMessage(
            "¡Maicito ganó +\(food.happiness)% de felicidad!"
        )

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 1.5
        ) {

            showHearts = false
            isFeeding = false
        }
    }

    // MARK: - MENSAJES

    private func showMessage(
        _ text: String
    ) {

        messageTask?.cancel()

        withAnimation {
            message = text
        }

        messageTask = Task {

            try? await Task.sleep(
                for: .seconds(2)
            )

            guard !Task.isCancelled else {
                return
            }

            withAnimation {
                message = nil
            }
        }
    }

    // MARK: - HISTORIAL DE COMIDAS

    private func loadFeedingHistory() {
        mealDates = UserDefaults.standard.array(
            forKey: "zentemi.mealDates"
        ) as? [Date] ?? []

        refreshFeedingHistory()
    }

    private func refreshFeedingHistory() {

        currentDate = Date()

        mealDates = mealDates.filter {
            let elapsed = currentDate.timeIntervalSince($0)

            return elapsed >= 0
                && elapsed < feedingInterval
        }

        saveFeedingHistory()
    }

    private func saveFeedingHistory() {
        UserDefaults.standard.set(
            mealDates,
            forKey: "zentemi.mealDates"
        )
    }
}

// MARK: - ANIMACIÓN DE CORAZONES

// MARK: - ANIMACIÓN DE CORAZONES

private struct HeartAnimationView: View {

    @State private var animate = false

    var body: some View {

        GeometryReader { geometry in

            ZStack {

                ForEach(0..<8, id: \.self) { index in

                    Image(
                        systemName: "heart.fill"
                    )
                    .font(
                        .system(
                            size: CGFloat(
                                17 + (index % 4) * 5
                            )
                        )
                    )
                    .foregroundStyle(
                        index.isMultiple(of: 2)
                            ? Color.pink
                            : Color.red
                    )
                    .position(
                        x:
                            geometry.size.width / 2
                            + CGFloat(
                                (index % 4) * 25 - 37
                            ),
                        y:
                            animate
                                ? -25
                                : geometry.size.height / 2
                    )
                    .opacity(
                        animate ? 0 : 1
                    )
                    .scaleEffect(
                        animate ? 1.4 : 0.6
                    )
                    .animation(
                        .easeOut(
                            duration:
                                0.9
                                + Double(index % 3) * 0.15
                        )
                        .delay(
                            Double(index) * 0.06
                        ),
                        value: animate
                    )
                }
            }
            .onAppear {
                animate = true
            }
        }
    }
}
