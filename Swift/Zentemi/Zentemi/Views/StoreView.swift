import SwiftUI

struct StoreView: View {
    
    @EnvironmentObject var appState: AppState
    
    // Objeto que está esperando confirmación
    @State private var confirmingItem: StoreItem?
    @State private var selectedCategory: ItemCategory = .all
    
    // Alerta monedas insuficientes
    @State private var showInsufficientCoins = false
    
    
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
        red: 0.975,
        green: 0.97,
        blue: 0.99
    )
    
    
    var body: some View {
        
        ZStack {
            
            backgroundColor
                .ignoresSafeArea()
            
            
            VStack(spacing: 0) {
                
                // MARK: - ENCABEZADO FIJO
                
                header

                ItemCategoryBar(selection: selectedCategory) { category in
                    confirmingItem = nil
                    selectedCategory = category
                }
                
                
                // MARK: - OBJETOS
                
                ScrollView(
                    showsIndicators: false
                ) {
                    
                    LazyVGrid(
                        columns: [
                            
                            GridItem(
                                .flexible(),
                                spacing: 12
                            ),
                            
                            GridItem(
                                .flexible(),
                                spacing: 12
                            )
                        ],
                        spacing: 12
                    ) {
                        
                        if selectedCategory.includes(.clothing) && !appState.redShirtPurchased {
                            storeCard(item: StoreItem(
                                id: "redShirt",
                                image: "playeritaRoja",
                                name: "Playerita roja",
                                price: appState.redShirtPrice,
                                rarity: "NUEVO",
                                rarityColor: turquoise
                            ))
                        }

                        // MARK: Objetos NO comprados
                        
                        if selectedCategory.includes(.accessories) && !appState.sunglassesPurchased {
                            
                            storeCard(
                                item: StoreItem(
                                    id: "sunglasses",
                                    image: "lentesdesol",
                                    name: "Lentes de sol",
                                    price: 120,
                                    rarity: "NUEVO",
                                    rarityColor: turquoise
                                )
                            )
                        }
                        
                        
                        if selectedCategory.includes(.hair) && !appState.pinkHairPurchased {
                            
                            storeCard(
                                item: StoreItem(
                                    id: "pinkHair",
                                    image: "cabellorosa",
                                    name: "Cabello rosa",
                                    price: 250,
                                    rarity: "RARO",
                                    rarityColor: .yellow
                                )
                            )
                        }
                        
                        
                        if selectedCategory.includes(.accessories) && !appState.swordPurchased {
                            
                            storeCard(
                                item: StoreItem(
                                    id: "sword",
                                    image: "espada",
                                    name: "Espada real",
                                    price: 500,
                                    rarity: "ÉPICO",
                                    rarityColor: .purple
                                )
                            )
                        }
                        if selectedCategory.includes(.clothing) && !appState.suitPurchased {
                            storeCard(
                                item: StoreItem(
                                    id: "suit",
                                    image: "traje1k",
                                    name: "Traje elegante",
                                    price: 1000,
                                    rarity: "LEGENDARIO",
                                    rarityColor: .orange
                                )
                            )
                        }
                        
                        
                        ForEach(HomeBackground.allCases) { background in
                            if selectedCategory.includes(.backgrounds) && !appState.purchasedHomeBackgrounds.contains(background) {
                                storeCard(item: StoreItem(
                                    id: background.id,
                                    image: background.imageName,
                                    name: background.title,
                                    price: background.price,
                                    rarity: "FONDO",
                                    rarityColor: turquoise
                                ))
                            }
                        }

                        // MARK: Objetos comprados
                        // Siempre aparecen hasta abajo

                        ForEach(HomeBackground.allCases) { background in
                            if selectedCategory.includes(.backgrounds) && appState.purchasedHomeBackgrounds.contains(background) {
                                purchasedCard(
                                    image: background.imageName,
                                    name: background.title,
                                    price: background.price,
                                    rarity: "FONDO",
                                    rarityColor: turquoise
                                )
                            }
                        }

                        if selectedCategory.includes(.clothing) && appState.redShirtPurchased {
                            purchasedCard(
                                image: "playeritaRoja",
                                name: "Playerita roja",
                                price: appState.redShirtPrice,
                                rarity: "NUEVO",
                                rarityColor: turquoise
                            )
                        }
                        
                        if selectedCategory.includes(.accessories) && appState.sunglassesPurchased {
                            
                            purchasedCard(
                                image: "lentesdesol",
                                name: "Lentes de sol",
                                price: 120,
                                rarity: "NUEVO",
                                rarityColor: turquoise
                            )
                        }
                        
                        
                        if selectedCategory.includes(.hair) && appState.pinkHairPurchased {
                            
                            purchasedCard(
                                image: "cabellorosa",
                                name: "Cabello rosa",
                                price: 250,
                                rarity: "RARO",
                                rarityColor: .yellow
                            )
                        }
                        
                        
                        if selectedCategory.includes(.accessories) && appState.swordPurchased {
                            
                            purchasedCard(
                                image: "espada",
                                name: "Espada real",
                                price: 500,
                                rarity: "ÉPICO",
                                rarityColor: .purple
                            )
                        }
                        if selectedCategory.includes(.clothing) && appState.suitPurchased {
                            purchasedCard(
                                image: "traje1k",
                                name: "Traje elegante",
                                price: 1000,
                                rarity: "LEGENDARIO",
                                rarityColor: .orange
                            )
                        }
                    }
                    .padding(.horizontal, 18)
                    .padding(.top, 14)
                    .padding(.bottom, 30)
                }
            }
        }
        .alert(
            "Monedas insuficientes",
            isPresented: $showInsufficientCoins
        ) {
            
            Button(
                "Entendido",
                role: .cancel
            ) { }
            
        } message: {
            
            Text(
                "No tienes suficientes monedas para comprar este objeto."
            )
        }
    }
    
    
    // MARK: - ENCABEZADO
    
    private var header: some View {
        
        ZStack {
            
            LinearGradient(
                colors: [
                    
                    Color(
                        red: 0.43,
                        green: 0.26,
                        blue: 0.77
                    ),
                    
                    Color(
                        red: 0.56,
                        green: 0.36,
                        blue: 0.85
                    )
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            
            
            Circle()
                .fill(
                    Color.white.opacity(0.07)
                )
                .frame(
                    width: 160,
                    height: 160
                )
                .offset(
                    x: -130,
                    y: -75
                )
            
            
            Circle()
                .fill(
                    Color.white.opacity(0.035)
                )
                .frame(
                    width: 210,
                    height: 210
                )
                .offset(
                    x: 150,
                    y: 80
                )
            
            
            HStack {
                
                VStack(
                    alignment: .leading,
                    spacing: 5
                ) {
                    
                    Text("Tienda")
                        .font(
                            .system(
                                size: 24,
                                weight: .bold
                            )
                        )
                        .foregroundStyle(.white)
                    
                    
                    Text(
                        "Viste a \(appState.petName)"
                    )
                    .font(
                        .system(
                            size: 12,
                            weight: .medium
                        )
                    )
                    .foregroundStyle(
                        Color.white.opacity(0.75)
                    )
                }
                
                
                Spacer()
                
                
                // Monedas
                
                HStack(spacing: 5) {
                    
                    Image(
                        systemName:
                            "dollarsign.circle.fill"
                    )
                    .font(
                        .system(size: 15)
                    )
                    
                    
                    Text(
                        "\(appState.coins)"
                    )
                    .font(
                        .system(
                            size: 13,
                            weight: .bold
                        )
                    )
                }
                .foregroundStyle(
                    Color(
                        red: 0.40,
                        green: 0.30,
                        blue: 0.05
                    )
                )
                .padding(
                    .horizontal,
                    11
                )
                .padding(
                    .vertical,
                    7
                )
                .background(
                    Color.yellow
                )
                .clipShape(Capsule())
            }
            .padding(.horizontal, 20)
            .padding(.top, 18)
        }
        .frame(height: 105)
    }
    
    
    // MARK: - TARJETA DE TIENDA
    
    private func storeCard(
        item: StoreItem
    ) -> some View {
        
        let isConfirming =
            confirmingItem?.id == item.id
        
        
        return VStack(spacing: 8) {
            
            // Rareza
            
            HStack {
                
                Text(item.rarity)
                    .font(
                        .system(
                            size: 8,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(
                        item.rarityColor
                    )
                    .padding(
                        .horizontal,
                        7
                    )
                    .padding(
                        .vertical,
                        3
                    )
                    .background(
                        item.rarityColor
                            .opacity(0.12)
                    )
                    .clipShape(Capsule())
                
                
                Spacer()
            }
            .frame(height: 18)
            
            
            // Imagen
            
            ZStack {
                
                RoundedRectangle(
                    cornerRadius: 13
                )
                .fill(
                    Color.gray.opacity(0.055)
                )
                .frame(height: 85)
                
                
                Image(item.image)
                    .resizable()
                    .scaledToFit()
                    .frame(
                        width: 78,
                        height: 70
                    )
            }
            
            
            Text(item.name)
                .font(
                    .system(
                        size: 14,
                        weight: .semibold
                    )
                )
            
            
            HStack(spacing: 4) {
                
                Image(
                    systemName:
                        "dollarsign.circle.fill"
                )
                .font(
                    .system(size: 11)
                )
                
                
                Text("\(item.price)")
                    .font(
                        .system(
                            size: 12,
                            weight: .bold
                        )
                    )
            }
            .foregroundStyle(
                Color(
                    red: 0.78,
                    green: 0.61,
                    blue: 0.05
                )
            )
            
            
            // MARK: Confirmación
            
            if isConfirming {
                
                VStack(spacing: 8) {
                    
                    Divider()
                    
                    
                    Text("¿Confirmar compra?")
                        .font(
                            .system(
                                size: 12,
                                weight: .bold
                            )
                        )
                    
                    
                    Text(
                        "Te quedarán \(max(0, appState.coins - item.price)) monedas"
                    )
                    .font(
                        .system(size: 10)
                    )
                    .foregroundStyle(
                        .secondary
                    )
                    
                    
                    Button {
                        
                        purchase(item)
                        
                    } label: {
                        
                        Text("Confirmar")
                            .font(
                                .system(
                                    size: 12,
                                    weight: .bold
                                )
                            )
                            .foregroundStyle(.white)
                            .frame(
                                maxWidth: .infinity
                            )
                            .frame(height: 34)
                            .background(turquoise)
                            .clipShape(
                                RoundedRectangle(
                                    cornerRadius: 9
                                )
                            )
                    }
                    
                    
                    Button {
                        
                        withAnimation(
                            .easeInOut(
                                duration: 0.2
                            )
                        ) {
                            confirmingItem = nil
                        }
                        
                    } label: {
                        
                        HStack(spacing: 4) {
                            
                            Image(
                                systemName:
                                    "chevron.left"
                            )
                            
                            Text("Regresar")
                        }
                        .font(
                            .system(
                                size: 11,
                                weight: .semibold
                            )
                        )
                        .foregroundStyle(purple)
                    }
                }
                
            } else {
                
                Button {
                    
                    withAnimation(
                        .easeInOut(
                            duration: 0.2
                        )
                    ) {
                        confirmingItem = item
                    }
                    
                } label: {
                    
                    Text("Comprar")
                        .font(
                            .system(
                                size: 13,
                                weight: .bold
                            )
                        )
                        .foregroundStyle(.white)
                        .frame(
                            maxWidth: .infinity
                        )
                        .frame(height: 36)
                        .background(purple)
                        .clipShape(
                            RoundedRectangle(
                                cornerRadius: 10
                            )
                        )
                }
            }
        }
        .padding(10)
        .background(.white)
        .clipShape(
            RoundedRectangle(
                cornerRadius: 16
            )
        )
        .shadow(
            color: .black.opacity(0.04),
            radius: 5,
            x: 0,
            y: 2
        )
    }
    
    
    // MARK: - TARJETA COMPRADA
    
    private func purchasedCard(
        image: String,
        name: String,
        price: Int,
        rarity: String,
        rarityColor: Color
    ) -> some View {
        
        VStack(spacing: 8) {
            
            HStack {
                
                Text(rarity)
                    .font(
                        .system(
                            size: 8,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(
                        rarityColor
                    )
                    .padding(
                        .horizontal,
                        7
                    )
                    .padding(
                        .vertical,
                        3
                    )
                    .background(
                        rarityColor.opacity(0.12)
                    )
                    .clipShape(Capsule())
                
                
                Spacer()
            }
            .frame(height: 18)
            
            
            ZStack {
                
                RoundedRectangle(
                    cornerRadius: 13
                )
                .fill(
                    Color.gray.opacity(0.055)
                )
                .frame(height: 85)
                
                
                Image(image)
                    .resizable()
                    .scaledToFit()
                    .frame(
                        width: 78,
                        height: 70
                    )
            }
            
            
            Text(name)
                .font(
                    .system(
                        size: 14,
                        weight: .semibold
                    )
                )
            
            
            HStack(spacing: 4) {
                
                Image(
                    systemName:
                        "dollarsign.circle.fill"
                )
                .font(
                    .system(size: 11)
                )
                
                
                Text("\(price)")
                    .font(
                        .system(
                            size: 12,
                            weight: .bold
                        )
                    )
            }
            .foregroundStyle(.secondary)
            
            
            HStack(spacing: 5) {
                
                Image(
                    systemName:
                        "checkmark.circle.fill"
                )
                
                
                Text("Comprado")
            }
            .font(
                .system(
                    size: 12,
                    weight: .bold
                )
            )
            .foregroundStyle(turquoise)
            .frame(
                maxWidth: .infinity
            )
            .frame(height: 36)
            .background(
                turquoise.opacity(0.10)
            )
            .clipShape(
                RoundedRectangle(
                    cornerRadius: 10
                )
            )
        }
        .padding(10)
        .background(.white)
        .clipShape(
            RoundedRectangle(
                cornerRadius: 16
            )
        )
        .overlay {
            
            RoundedRectangle(
                cornerRadius: 16
            )
            .stroke(
                turquoise.opacity(0.45),
                lineWidth: 1.5
            )
        }
        .shadow(
            color: .black.opacity(0.035),
            radius: 5,
            x: 0,
            y: 2
        )
    }
    
    
    // MARK: - COMPRAR
    
    private func purchase(
        _ item: StoreItem
    ) {
        
        // Ignore a repeated confirmation after this item has already been bought.
        if item.id == "redShirt", appState.redShirtPurchased {
            confirmingItem = nil
            return
        }

        if let background = HomeBackground(rawValue: item.id),
           appState.purchasedHomeBackgrounds.contains(background) {
            confirmingItem = nil
            return
        }

        // No tiene suficientes monedas
        
        guard appState.coins >= item.price
        else {
            
            confirmingItem = nil
            showInsufficientCoins = true
            return
        }
        
        
        // Restar monedas
        
        if let background = HomeBackground(rawValue: item.id) {
            guard appState.purchaseHomeBackground(background) else {
                confirmingItem = nil
                return
            }
        } else if item.id == "redShirt" {
            guard appState.purchaseRedShirt() else {
                confirmingItem = nil
                return
            }
        } else {
            appState.coins -= item.price
        }
        
        
        // Marcar objeto comprado
        
        switch item.id {

        case "sunglasses":
            appState.sunglassesPurchased = true

        case "pinkHair":
            appState.pinkHairPurchased = true

        case "sword":
            appState.swordPurchased = true
            
        case "suit":
            appState.suitPurchased = true

        default:
            break
        }
        
        // Sonido de compra

        SoundManager.shared.play(
            "audioComprado",
            volume: Float(appState.soundVolume)
        )


        // Iniciar animación hacia Mochila

        appState.purchasedItemAnimation = item.image
        appState.purchaseAnimationID = UUID()


        confirmingItem = nil
    }
}



// MARK: - Categorías compartidas de tienda y mochila

enum ItemCategory: String, CaseIterable, Identifiable {
    case all, clothing, accessories, hair, backgrounds

    var id: String { rawValue }

    var title: String {
        switch self {
        case .all: return "Todo"
        case .clothing: return "Ropa"
        case .accessories: return "Accesorios"
        case .hair: return "Cabellos"
        case .backgrounds: return "Fondos"
        }
    }

    var icon: String {
        switch self {
        case .all: return "square.grid.2x2.fill"
        case .clothing: return "tshirt.fill"
        case .accessories: return "eyeglasses"
        case .hair: return "comb.fill"
        case .backgrounds: return "photo"
        }
    }

    func includes(_ category: ItemCategory) -> Bool {
        self == .all || self == category
    }
}

struct ItemCategoryBar: View {
    let selection: ItemCategory
    let select: (ItemCategory) -> Void

    private let purple = Color(red: 0.48, green: 0.29, blue: 0.79)

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(ItemCategory.allCases) { category in
                    Button {
                        select(category)
                    } label: {
                        Label(category.title, systemImage: category.icon)
                            .font(.subheadline.weight(.semibold))
                            .padding(.horizontal, 14)
                            .frame(minHeight: 44)
                            .foregroundStyle(selection == category ? Color.white : purple)
                            .background(selection == category ? purple : purple.opacity(0.08), in: Capsule())
                    }
                    .buttonStyle(.plain)
                    .accessibilityAddTraits(selection == category ? .isSelected : [])
                }
            }
            .padding(.horizontal, 18)
        }
        .padding(.vertical, 10)
        .background(.white)
    }
}

// MARK: - MODELO ITEM TIENDA

private struct StoreItem: Identifiable {
    
    let id: String
    let image: String
    let name: String
    let price: Int
    let rarity: String
    let rarityColor: Color
}


// MARK: - PREVIEW

#Preview {
    
    StoreView()
        .environmentObject(
            AppState()
        )
}
