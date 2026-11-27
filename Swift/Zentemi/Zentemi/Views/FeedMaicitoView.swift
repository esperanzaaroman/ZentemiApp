import SwiftUI

/// Alimentar a Maicito: se elige entre las 11 comidas tradicionales, aparece en el plato
/// y se arrastra hacia Maicito (máximo 3 comidas cada 4 horas).
/// Espejo exacto de `FeedMaicitoScreen.kt` en Android v1.1.
struct FeedMaicitoView: View {

    @State private var mascotScaleY: CGFloat = 1.0
    @State private var tutorialShownThisSession = false

    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss

    private let purple = GameTheme.purple
    private let turquoise = GameTheme.turquoise
    private let coinOrange = Color(red: 1.0, green: 0.584, blue: 0.0)

    private let maxMeals = 3
    private let feedingInterval: TimeInterval = 4 * 60 * 60

    private struct Food: Identifiable {
        let id: String
        let image: String
        let name: String
        let price: Int
        let happiness: Int
    }

    private let foods: [Food] = [
        Food(id: "manzana", image: "manzana", name: "Manzana", price: 20, happiness: 5),
        Food(id: "platano", image: "food_platano", name: "Plátano", price: 25, happiness: 6),
        Food(id: "uvas", image: "food_uvas", name: "Uvas", price: 30, happiness: 7),
        Food(id: "tortillas", image: "tortillas", name: "Tortillas", price: 30, happiness: 8),
        Food(id: "sandia", image: "food_sandia", name: "Sandía", price: 35, happiness: 9),
        Food(id: "concha", image: "food_concha", name: "Concha", price: 40, happiness: 10),
        Food(id: "chocolate", image: "acc_taza_chocolate", name: "Chocolate", price: 40, happiness: 11),
        Food(id: "quesadilla", image: "food_quesadilla", name: "Quesadilla", price: 45, happiness: 12),
        Food(id: "tamal", image: "tamal", name: "Tamal", price: 50, happiness: 15),
        Food(id: "taco", image: "food_taco", name: "Taco", price: 55, happiness: 16),
        Food(id: "nieve", image: "food_nieve", name: "Nieve", price: 60, happiness: 18)
    ]

    @State private var selectedFood: Food?
    @State private var foodDragOffset: CGSize = .zero
    @State private var isFeeding = false

    @State private var showHearts = false
    @State private var showTutorial = false
    @State private var tutorialHandOffset: CGFloat = 0

    @State private var message: String?
    @State private var messageTask: Task<Void, Never>?

    @State private var spentAmount: Int? = nil
    @State private var spentRise: CGFloat = 0

    @State private var mealDates: [Date] = []

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
        !isFull && availableMeals > 0 && !isFeeding
    }

    var body: some View {
        ZStack {
            VStack(spacing: 0) {
                header

                feedingArea

                foodSection

                feedingStatus
            }
            .padding(.bottom, 10)

            if let message {
                VStack {
                    Spacer()

                    Text(message)
                        .font(.system(size: 14, weight: .bold))
                        .multilineTextAlignment(.center)
                        .foregroundStyle(.white)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 12)
                        .background(purple.opacity(0.95), in: Capsule())
                        .shadow(radius: 5)
                        .padding(.bottom, 195)
                }
                .allowsHitTesting(false)
                .transition(.opacity)
            }
        }
        .background {
            GeometryReader { geo in
                ZStack {
                    Image("fondoCocina")
                        .resizable()
                        .scaledToFill()
                        .frame(width: geo.size.width, height: geo.size.height)
                        .clipped()

                    Color.black.opacity(0.08)
                }
            }
            .ignoresSafeArea()
        }
        .onAppear {
            loadFeedingHistory()
        }
        .onDisappear {
            messageTask?.cancel()
        }
    }

    // MARK: - Encabezado

    private var header: some View {
        HStack(alignment: .top, spacing: 12) {
            Button {
                dismiss()
            } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(purple)
                    .frame(width: 44, height: 44)
                    .background(.white, in: RoundedRectangle(cornerRadius: 14))
            }

            VStack(spacing: 7) {
                HStack {
                    Text("Felicidad de \(appState.petName)")
                        .font(.system(size: 13, weight: .bold))

                    Spacer()

                    Text("\(happiness)%")
                        .font(.system(size: 14, weight: .heavy))
                }
                .foregroundStyle(purple)

                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        Capsule()
                            .fill(Color.gray.opacity(0.18))

                        Capsule()
                            .fill(turquoise)
                            .frame(width: geo.size.width * CGFloat(happiness) / 100)
                    }
                }
                .frame(height: 12)
            }
            .padding(13)
            .background(.white.opacity(0.96), in: RoundedRectangle(cornerRadius: 15))
        }
        .padding(.horizontal, 16)
        .padding(.top, 12)
    }

    // MARK: - Área de Alimentación

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
                PetView(size: 165)
                    .environmentObject(appState)
                    .scaleEffect(x: 1.0, y: mascotScaleY, anchor: .bottom)
                    .position(mascotCenter)
                    .onAppear {
                        withAnimation(.easeInOut(duration: 1.6).repeatForever(autoreverses: true)) {
                            mascotScaleY = 1.06
                        }
                    }

                if showHearts {
                    HeartAnimationView()
                        .frame(width: 180, height: 150)
                        .position(x: mascotCenter.x, y: mascotCenter.y - 85)
                        .allowsHitTesting(false)
                }

                Image("plato")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 190, height: 130)
                    .position(plateCenter)

                if let selectedFood {
                    Image(selectedFood.image)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 100, height: 100)
                        .offset(foodDragOffset)
                        .position(x: plateCenter.x, y: plateCenter.y - 12)
                        .zIndex(5)
                        .gesture(
                            DragGesture(minimumDistance: 0)
                                .onChanged { value in
                                    guard !isFeeding else { return }
                                    foodDragOffset = value.translation
                                    if showTutorial {
                                        showTutorial = false
                                    }
                                }
                                .onEnded { value in
                                    guard !isFeeding else { return }
                                    let finalPosition = CGPoint(
                                        x: plateCenter.x + value.translation.width,
                                        y: plateCenter.y - 12 + value.translation.height
                                    )
                                    let distance = hypot(
                                        finalPosition.x - mascotCenter.x,
                                        finalPosition.y - mascotCenter.y
                                    )
                                    if distance < 105 {
                                        feedMaicito(with: selectedFood)
                                    } else {
                                        withAnimation(.spring(response: 0.35, dampingFraction: 0.65)) {
                                            foodDragOffset = .zero
                                        }
                                    }
                                }
                        )
                }

                if showTutorial && selectedFood != nil {
                    VStack(spacing: 8) {
                        Text("¡Desliza la comida hacia \(appState.petName)!")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 15)
                            .padding(.vertical, 10)
                            .background(purple, in: Capsule())

                        Image(systemName: "hand.point.up.fill")
                            .font(.system(size: 27))
                            .foregroundStyle(.white)
                            .shadow(color: .black.opacity(0.4), radius: 4)
                            .offset(y: tutorialHandOffset)
                    }
                    .position(x: plateCenter.x, y: plateCenter.y - 115)
                    .allowsHitTesting(false)
                    .onAppear {
                        withAnimation(.easeInOut(duration: 0.75).repeatForever(autoreverses: true)) {
                            tutorialHandOffset = -20
                        }
                    }
                }
            }
        }
        .padding(.vertical, 8)
    }

    // MARK: - Sección de Alimentos (11 comidas en cuadrícula 3 columnas)

    private var foodSection: some View {
        VStack(spacing: 10) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Alimentos")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(purple)

                    Text(
                        selectedFood.map { "Comprando: \($0.name) · \($0.price) monedas" }
                            ?? "Elige uno y arrástralo a \(appState.petName)"
                    )
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(selectedFood != nil ? coinOrange : .gray)
                    .lineLimit(1)
                }

                Spacer()

                ZStack {
                    HStack(spacing: 6) {
                        ZentemiIconView(name: "ic_moneda", size: 28)
                        Text("\(appState.coins)")
                            .font(.system(size: 20, weight: .heavy))
                            .foregroundStyle(coinOrange)
                    }
                    .padding(.leading, 8)
                    .padding(.trailing, 14)
                    .padding(.vertical, 6)
                    .background(Color(red: 1.0, green: 0.945, blue: 0.780), in: Capsule())

                    if let spentAmount {
                        Text("−\(spentAmount)")
                            .font(.system(size: 17, weight: .heavy))
                            .foregroundStyle(Color(red: 0.878, green: 0.271, blue: 0.227))
                            .offset(y: -26 - 36 * spentRise)
                            .opacity(1.0 - Double(spentRise))
                    }
                }
            }
            .padding(.leading, 18)
            .padding(.trailing, 12)
            .padding(.vertical, 10)
            .background(.white.opacity(0.96), in: RoundedRectangle(cornerRadius: 16))
            .padding(.horizontal, 16)

            ScrollView(showsIndicators: false) {
                LazyVGrid(
                    columns: [
                        GridItem(.flexible(), spacing: 10),
                        GridItem(.flexible(), spacing: 10),
                        GridItem(.flexible(), spacing: 10)
                    ],
                    spacing: 10
                ) {
                    ForEach(foods) { food in
                        foodCard(food)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 4)
                .padding(.bottom, 10)
            }
            .frame(height: 168)
        }
    }

    private func foodCard(_ food: Food) -> some View {
        let canAfford = appState.coins >= food.price
        let enabled = canChooseFood && canAfford
        let isSelected = (selectedFood?.id == food.id)

        return Button {
            selectFood(food)
        } label: {
            ZStack(alignment: .topTrailing) {
                VStack(spacing: 3) {
                    Image(food.image)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 56, height: 56)

                    Text(food.name)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(purple)
                        .lineLimit(1)

                    Text("+\(food.happiness)%")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundStyle(turquoise)

                    HStack(spacing: 4) {
                        ZentemiIconView(name: "ic_moneda", size: 19)
                        Text("\(food.price)")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundStyle(coinOrange)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 138)
                .background(
                    isSelected
                        ? Color(red: 0.945, green: 0.914, blue: 0.988)
                        : Color.white.opacity(0.96),
                    in: RoundedRectangle(cornerRadius: 17)
                )
                .overlay {
                    RoundedRectangle(cornerRadius: 17)
                        .stroke(isSelected ? purple : .clear, lineWidth: 3)
                }
                .opacity((enabled || isSelected) ? 1.0 : 0.55)

                if isSelected {
                    ZStack {
                        Circle()
                            .fill(purple)
                            .frame(width: 22, height: 22)
                        Image(systemName: "checkmark")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundStyle(.white)
                    }
                    .padding(6)
                }
            }
        }
        .buttonStyle(.plain)
        .disabled(!canChooseFood)
    }

    // MARK: - Estado de Alimentación

    private var feedingStatus: some View {
        Group {
            if isFull {
                Text("¡\(appState.petName) está muy feliz!")
                    .foregroundStyle(Color(red: 0.118, green: 0.620, blue: 0.278))
            } else if availableMeals == 0 {
                Text("\(appState.petName) está lleno. Vuelve más tarde.")
                    .foregroundStyle(purple)
            } else {
                Text("Comidas disponibles: \(availableMeals)/\(maxMeals)")
                    .foregroundStyle(purple)
            }
        }
        .font(.system(size: 13, weight: .bold))
        .padding(.vertical, 8)
        .padding(.horizontal, 15)
        .background(.white.opacity(0.95), in: Capsule())
        .padding(.top, 5)
    }

    // MARK: - Lógica

    private func selectFood(_ food: Food) {
        guard canChooseFood else { return }
        guard appState.coins >= food.price else {
            showMessage("No tienes suficientes monedas.")
            return
        }

        selectedFood = food
        foodDragOffset = .zero
        SoundManager.shared.play("audioSeleccion", volume: Float(appState.soundVolume))

        if !tutorialShownThisSession {
            showTutorial = true
            tutorialShownThisSession = true
        }
    }

    // MARK: - ALIMENTAR

    private func feedMaicito(
        with food: Food
    ) {

        appState.updateHappinessDecay()
        guard !isFeeding,
              happiness < 100,
              availableMeals > 0,
              appState.coins >= food.price else {
            withAnimation { foodDragOffset = .zero }
            return
        }

        isFeeding = true
        appState.coins -= food.price

        spentAmount = food.price
        spentRise = 0
        withAnimation(.easeOut(duration: 1.2)) {
            spentRise = 1.0
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
            spentAmount = nil
            spentRise = 0
        }

        // Registrar comida
        appState.recordFeeding()
        appState.happiness = min(100, appState.happiness + food.happiness)
        mealDates.append(Date())
        saveFeedingHistory()

        selectedFood = nil
        foodDragOffset = .zero
        showTutorial = false
        showHearts = true

        SoundManager.shared.play("audioBien", volume: Float(appState.soundVolume))
        showMessage("¡\(appState.petName) ganó +\(food.happiness)% de felicidad!")

        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            showHearts = false
            isFeeding = false
        }
    }

    private func showMessage(_ text: String) {
        messageTask?.cancel()
        withAnimation { message = text }

        messageTask = Task {
            try? await Task.sleep(for: .seconds(2))
            guard !Task.isCancelled else { return }
            withAnimation { message = nil }
        }
    }

    private func loadFeedingHistory() {
        let now = Date()
        let saved = UserDefaults.standard.array(forKey: "zentemi.mealDates") as? [Date] ?? []
        mealDates = saved.filter {
            let elapsed = now.timeIntervalSince($0)
            return elapsed >= 0 && elapsed < feedingInterval
        }
        saveFeedingHistory()
    }

    private func saveFeedingHistory() {
        UserDefaults.standard.set(mealDates, forKey: "zentemi.mealDates")
    }
}

private struct HeartAnimationView: View {
    @State private var animate = false

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                ForEach(0..<8, id: \.self) { index in
                    ZentemiIconView(
                        name: "ic_felicidad",
                        size: CGFloat(20 + (index % 4) * 6)
                    )
                    .position(
                        x: geometry.size.width / 2 + CGFloat((index % 4) * 25 - 37),
                        y: animate ? -25 : geometry.size.height / 2
                    )
                    .opacity(animate ? 0 : 1)
                    .scaleEffect(animate ? 1.4 : 0.6)
                    .animation(
                        .easeOut(duration: 0.9 + Double(index % 3) * 0.15)
                            .delay(Double(index) * 0.06),
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

#Preview {
    FeedMaicitoView()
        .environmentObject(AppState())
}
