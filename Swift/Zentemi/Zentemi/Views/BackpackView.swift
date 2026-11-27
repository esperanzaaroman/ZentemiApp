import SwiftUI

struct BackpackView: View {
    
    @EnvironmentObject var appState: AppState
    
    // MARK: - Animación mascota
    
    @State private var mascotScaleY: CGFloat = 1.0
    @State private var selectedCategory: ItemCategory = .all
    
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
    
    
    // MARK: - Cantidades
    
    private var totalItems: Int {
        
        3
        + (appState.sunglassesPurchased ? 1 : 0)
        + (appState.pinkHairPurchased ? 1 : 0)
        + (appState.swordPurchased ? 1 : 0)
        + (appState.suitPurchased ? 1 : 0)
        + (appState.redShirtPurchased ? 1 : 0)
        + appState.purchasedHomeBackgrounds.count
    }
    
    
    private var equippedItems: Int {
        
        [
            appState.glassesEquipped,
            appState.hairEquipped,
            appState.scarfEquipped,
            appState.sunglassesEquipped,
            appState.pinkHairEquipped,
            appState.swordEquipped,
            appState.suitEquipped,
            appState.redShirtEquipped
        ]
        .filter { $0 }
        .count + (appState.equippedHomeBackground == nil ? 0 : 1)
    }
    
    
    private var categoryIsEmpty: Bool {
        switch selectedCategory {
        case .all, .accessories, .hair:
            return false // These categories include the starter items.
        case .clothing:
            return !appState.redShirtPurchased && !appState.suitPurchased
        case .backgrounds:
            return appState.purchasedHomeBackgrounds.isEmpty
        }
    }

    var body: some View {
        
        ZStack {
            
            backgroundColor
                .ignoresSafeArea()
            
            
            VStack(spacing: 0) {
                
                // MARK: - ENCABEZADO FIJO
                
                header
                
                
                // MARK: - MASCOTA FIJA
                
                petPreview

                ItemCategoryBar(selection: selectedCategory) { category in
                    selectedCategory = category
                }
                
                
                // MARK: - INVENTARIO
                
                ScrollView(
                    showsIndicators: false
                ) {
                    
                    if categoryIsEmpty {
                        VStack(spacing: 12) {
                            Image(systemName: selectedCategory.icon)
                                .font(.largeTitle)
                                .foregroundStyle(purple)
                            Text("Aún no tienes objetos de esta categoría")
                                .font(.headline)
                                .multilineTextAlignment(.center)
                            Text("Encuéntralos en la tienda.")
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(24)
                    }

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
                        
                        // MARK: Objetos iniciales
                        
                        if selectedCategory.includes(.accessories) {
                            itemCard(
                                image: "lentes",
                                name: "Lentes",
                                isEquipped:
                                    appState.glassesEquipped
                            ) {

                                withAnimation(
                                    .easeInOut(
                                        duration: 0.2
                                    )
                                ) {
                                    appState.toggleGlasses()
                                }
                            }
                        }
                        
                        
                        if selectedCategory.includes(.hair) {
                            itemCard(
                                image: "cabello",
                                name: "Cabello",
                                isEquipped:
                                    appState.hairEquipped
                            ) {

                                withAnimation(
                                    .easeInOut(
                                        duration: 0.2
                                    )
                                ) {
                                    appState.toggleHair()
                                }
                            }
                        }
                        
                        
                        if selectedCategory.includes(.accessories) {
                            itemCard(
                                image: "bufanda",
                                name: "Bufanda",
                                isEquipped:
                                    appState.scarfEquipped
                            ) {

                                withAnimation(
                                    .easeInOut(
                                        duration: 0.2
                                    )
                                ) {
                                    appState.toggleScarf()
                                }
                            }
                        }
                        
                        
                        ForEach(HomeBackground.allCases) { background in
                            if selectedCategory.includes(.backgrounds) && appState.purchasedHomeBackgrounds.contains(background) {
                                itemCard(
                                    image: background.imageName,
                                    name: background.title,
                                    isEquipped: appState.equippedHomeBackground == background,
                                    isBackground: true
                                ) {
                                    withAnimation(.easeInOut(duration: 0.2)) {
                                        appState.toggleHomeBackground(background)
                                    }
                                }
                            }
                        }

                        // MARK: Objetos comprados

                        if selectedCategory.includes(.clothing) && appState.redShirtPurchased {
                            itemCard(
                                image: "playeritaRoja",
                                name: "Playerita roja",
                                isEquipped: appState.redShirtEquipped
                            ) {
                                withAnimation(.easeInOut(duration: 0.2)) {
                                    appState.toggleRedShirt()
                                }
                            }
                        }
                        
                        if selectedCategory.includes(.accessories) && appState.sunglassesPurchased {
                            
                            itemCard(
                                image: "lentesdesol",
                                name: "Lentes de sol",
                                isEquipped:
                                    appState.sunglassesEquipped
                            ) {

                                withAnimation(
                                    .easeInOut(
                                        duration: 0.2
                                    )
                                ) {
                                    appState.toggleSunglasses()
                                }
                            }
                        }
                        
                        
                        if selectedCategory.includes(.hair) && appState.pinkHairPurchased {
                            
                            itemCard(
                                image: "cabellorosa",
                                name: "Cabello rosa",
                                isEquipped:
                                    appState.pinkHairEquipped
                            ) {

                                withAnimation(
                                    .easeInOut(
                                        duration: 0.2
                                    )
                                ) {
                                    appState.togglePinkHair()
                                }
                            }
                        }
                        
                        
                        if selectedCategory.includes(.accessories) && appState.swordPurchased {
                            
                            itemCard(
                                image: "espada",
                                name: "Espada real",
                                isEquipped:
                                    appState.swordEquipped
                            ) {

                                withAnimation(
                                    .easeInOut(
                                        duration: 0.2
                                    )
                                ) {
                                    appState.toggleSword()
                                }
                            }
                        }
                        
                        if selectedCategory.includes(.clothing) && appState.suitPurchased {
                            itemCard(
                                image: "traje1k",
                                name: "Traje elegante",
                                isEquipped:
                                    appState.suitEquipped
                            ) {
                                withAnimation(
                                    .easeInOut(
                                        duration: 0.2
                                    )
                                ) {
                                    appState.toggleSuit()
                                }
                            }
                        }
                    }
                    .padding(.horizontal, 18)
                    .padding(.top, 12)
                    .padding(.bottom, 30)
                }
            }
        }
        .onAppear {
            
            mascotScaleY = 1.0
            
            // Pequeño retraso para que SwiftUI
            // muestre primero el estado normal
            
            DispatchQueue.main.asyncAfter(
                deadline: .now() + 0.1
            ) {
                
                withAnimation(
                    .easeInOut(duration: 1.6)
                        .repeatForever(
                            autoreverses: true
                        )
                ) {
                    mascotScaleY = 1.08
                }
            }
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
            
            
            VStack(
                alignment: .leading,
                spacing: 5
            ) {
                
                Text("Mi Mochila")
                    .font(
                        .system(
                            size: 24,
                            weight: .bold
                        )
                    )
                    .foregroundStyle(.white)
                
                
                Text(
                    "\(totalItems) objetos · \(equippedItems) equipados"
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
            .frame(
                maxWidth: .infinity,
                alignment: .leading
            )
            .padding(.horizontal, 20)
            .padding(.top, 18)
        }
        .frame(height: 105)
    }
    
    
    // MARK: - VISTA PREVIA MASCOTA
    
    private var petPreview: some View {
        
        VStack(spacing: 14) {
            
            Text(appState.petName)
                .font(
                    .system(
                        size: 16,
                        weight: .bold
                    )
                )
                .offset(y: -10)
            
            
            PetView(size: 115)
                .scaleEffect(
                    x: 1.0,
                    y: mascotScaleY,
                    anchor: .bottom
                )
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 12)
        .background(.white)
        .shadow(
            color: .black.opacity(0.035),
            radius: 5,
            x: 0,
            y: 3
        )
    }
    
    
    // MARK: - TARJETA DE OBJETO
    
    private func itemCard(
        image: String,
        name: String,
        isEquipped: Bool,
        isBackground: Bool = false,
        action: @escaping () -> Void
    ) -> some View {
        
        VStack(spacing: 8) {
            
            HStack {
                
                Spacer()
                
                
                if isEquipped {
                    
                    Text("Equipado")
                        .font(
                            .system(
                                size: 9,
                                weight: .semibold
                            )
                        )
                        .foregroundStyle(
                            turquoise
                        )
                        .padding(
                            .horizontal,
                            8
                        )
                        .padding(
                            .vertical,
                            4
                        )
                        .background(
                            turquoise.opacity(0.10)
                        )
                        .clipShape(Capsule())
                }
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
            
            
            Button {

                if isEquipped {

                    SoundManager.shared.play(
                        "audioQuitarItem",
                        volume: Float(appState.soundVolume)
                    )

                } else {

                    SoundManager.shared.play(
                        "audioEquipar",
                        volume: min(
                            Float(appState.soundVolume) * 0.5,
                            1.0
                        )
                    )
                }

                action()

            } label: {
                
                Text(
                    isEquipped
                    ? (isBackground ? "Quitar fondo" : "Quitar")
                    : (isBackground ? "Equipar fondo" : "Equipar")
                )
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
                .background(
                    isEquipped
                    ? turquoise
                    : purple
                )
                .clipShape(
                    RoundedRectangle(
                        cornerRadius: 10
                    )
                )
            }
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
                isEquipped
                ? turquoise
                : Color.clear,
                lineWidth: 2
            )
        }
        .shadow(
            color: .black.opacity(0.035),
            radius: 5,
            x: 0,
            y: 2
        )
    }
}


// MARK: - PREVIEW

#Preview {
    
    BackpackView()
        .environmentObject(
            AppState()
        )
}
