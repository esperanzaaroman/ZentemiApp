import SwiftUI

/// Tienda y vestidor unificados en una sola pantalla:
/// arriba Maicito con lo que trae puesto (o lo que se está probando),
/// en medio las 5 pestañas y abajo la cuadrícula de 50 artículos.
/// Espejo exacto de `StoreScreen.kt` en Android v1.1.
struct StoreView: View {

    @EnvironmentObject var appState: AppState

    @State private var category: ItemCategory = .clothes
    @State private var previewId: String? = nil
    @State private var justBoughtId: String? = nil

    private let storeBgBottom = Color(red: 0.945, green: 0.918, blue: 0.984) // #F1EAFB
    private let cardBottom = Color(red: 0.945, green: 0.914, blue: 0.988)    // #F1E9FC
    private let cardBorder = Color(red: 0.843, green: 0.784, blue: 0.949)    // #D7C8F2
    private let turquoiseDark = Color(red: 0.118, green: 0.604, blue: 0.541) // #1E9A8A
    private let goldHighlight = Color(red: 0.965, green: 0.737, blue: 0.235) // #F6BC3C

    private var previewItem: ShopItem? {
        previewId.flatMap { ShopCatalog.byId($0) }
    }

    private var categoryItems: [ShopItem] {
        ShopCatalog.items.filter { category.contains($0) }
    }

    private var anyEquippedHere: Bool {
        categoryItems.contains { appState.isEquipped($0) }
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            LinearGradient(
                colors: [GameTheme.backgroundLight, storeBgBottom],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 0) {
                storeHero

                storeTabs

                ScrollView(showsIndicators: false) {
                    LazyVGrid(
                        columns: [
                            GridItem(.flexible(), spacing: 10),
                            GridItem(.flexible(), spacing: 10),
                            GridItem(.flexible(), spacing: 10)
                        ],
                        spacing: 10
                    ) {
                        ForEach(categoryItems) { item in
                            StoreItemCardView(
                                item: item,
                                owned: appState.isOwned(item),
                                equipped: appState.isEquipped(item),
                                previewing: previewId == item.id,
                                justBought: justBoughtId == item.id,
                                tall: category == .backgrounds,
                                cardBottom: cardBottom,
                                cardBorder: cardBorder,
                                goldHighlight: goldHighlight
                            ) {
                                onItemTap(item)
                            }
                        }
                    }
                    .padding(.horizontal, 12)
                    .padding(.top, 14)
                    .padding(.bottom, previewItem != nil ? 205 : 115)
                }
            }

            // Barra flotante para comprar lo que Maicito se está probando
            if let item = previewItem {
                buyBanner(for: item)
                    .padding(.horizontal, 12)
                    .padding(.bottom, 92)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.22), value: previewId)
    }

    // MARK: - Parte Superior (Probador de Maicito)

    private var storeHero: some View {
        let activeBackdrop = previewItem?.backdrop ?? appState.equippedBackdrop?.backdrop
        let justBoughtItem = justBoughtId.flatMap { ShopCatalog.byId($0) }
        let showUnequip = anyEquippedHere && previewItem == nil

        return ZStack {
            LinearGradient(
                colors: [
                    Color(red: 0.933, green: 0.902, blue: 0.988),
                    Color(red: 0.851, green: 0.788, blue: 0.965)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea(edges: .top)

            if let activeBackdrop {
                MaicitoBackdropView(backdrop: activeBackdrop)
                    .ignoresSafeArea(edges: .top)
            } else {
                GeometryReader { geo in
                    Circle()
                        .fill(Color.white.opacity(0.16))
                        .frame(width: 180, height: 180)
                        .offset(x: -50, y: 20)

                    Circle()
                        .fill(Color.white.opacity(0.14))
                        .frame(width: 200, height: 200)
                        .offset(x: geo.size.width - 140, y: geo.size.height - 130)
                }
                .allowsHitTesting(false)
            }

            VStack(spacing: 6) {
                // Encabezado: Tienda a la izquierda, Monedas a la derecha
                HStack {
                    HStack(spacing: 8) {
                        ZentemiIconView(name: "ic_tienda", size: 28)

                        VStack(alignment: .leading, spacing: 1) {
                            Text("Tienda")
                                .font(.system(size: 18, weight: .heavy))
                                .foregroundStyle(GameTheme.purpleDark)

                            Text("Viste a \(appState.petName)")
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundStyle(GameTheme.subText.opacity(0.85))
                        }
                    }
                    .padding(.leading, 10)
                    .padding(.trailing, 16)
                    .padding(.vertical, 6)
                    .background(Color.white.opacity(0.92))
                    .clipShape(Capsule())
                    .overlay(
                        Capsule()
                            .stroke(GameTheme.purple.opacity(0.25), lineWidth: 1)
                    )

                    Spacer()

                    HStack(spacing: 7) {
                        ZentemiIconView(name: "ic_moneda", size: 26)
                        Text("\(appState.coins)")
                            .font(.system(size: 19, weight: .heavy))
                            .foregroundStyle(GameTheme.darkText)
                    }
                    .padding(.leading, 10)
                    .padding(.trailing, 15)
                    .padding(.vertical, 7)
                    .background(Color.white.opacity(0.92))
                    .clipShape(Capsule())
                    .overlay(
                        Capsule()
                            .stroke(goldHighlight.opacity(0.6), lineWidth: 1.5)
                    )
                }

                // Maicito al centro + botón quitar a la izquierda + aviso de compra arriba
                ZStack {
                    PetView(size: 146, preview: previewItem)

                    if showUnequip {
                        HStack {
                            Button {
                                SoundManager.shared.play(
                                    "audioQuitarItem",
                                    volume: Float(appState.soundVolume)
                                )
                                appState.unequipSlots(category.slots)
                            } label: {
                                Image(systemName: "nosign")
                                    .font(.system(size: 24, weight: .bold))
                                    .foregroundStyle(GameTheme.purple)
                                    .frame(width: 50, height: 50)
                                    .background(Color.white.opacity(0.92))
                                    .clipShape(RoundedRectangle(cornerRadius: 16))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 16)
                                            .stroke(GameTheme.purple.opacity(0.25), lineWidth: 1)
                                    )
                            }
                            Spacer()
                        }
                        .transition(.scale.combined(with: .opacity))
                    }

                    if let justBoughtItem {
                        VStack {
                            Text("¡\(justBoughtItem.name) es tuyo!")
                                .font(.system(size: 14, weight: .heavy))
                                .foregroundStyle(.white)
                                .padding(.horizontal, 16)
                                .padding(.vertical, 6)
                                .background(GameTheme.turquoise)
                                .clipShape(Capsule())
                                .overlay(
                                    Capsule()
                                        .stroke(Color.white, lineWidth: 2)
                                )
                            Spacer()
                        }
                        .transition(.scale(scale: 0.6).combined(with: .opacity))
                    }
                }
                .frame(height: 168)
            }
            .padding(.horizontal, 16)
            .padding(.top, 6)
            .padding(.bottom, 12)
        }
        .fixedSize(horizontal: false, vertical: true)
        .clipShape(
            UnevenRoundedRectangle(
                bottomLeadingRadius: 28,
                bottomTrailingRadius: 28
            )
        )
    }

    // MARK: - Pestañas de Categoría

    private var storeTabs: some View {
        HStack(spacing: 0) {
            ForEach(ItemCategory.allCases) { tab in
                let isSelected = (tab == category)

                Button {
                    category = tab
                    previewId = nil
                } label: {
                    VStack(spacing: 3) {
                        ZStack {
                            if tab == .backgrounds {
                                MaicitoBackdropView(backdrop: .sunset)
                                    .frame(width: 22, height: 30)
                                    .clipShape(RoundedRectangle(cornerRadius: 6))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 6)
                                            .stroke(isSelected ? GameTheme.purple : Color.gray, lineWidth: 2)
                                    )
                            } else {
                                Image(tabIcon(for: tab))
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 30, height: 30)
                                    .saturation(isSelected ? 1 : 0)
                            }
                        }
                        .frame(width: 34, height: 34)
                        .scaleEffect(isSelected ? 1.10 : 1.0)
                        .opacity(isSelected ? 1.0 : 0.5)

                        Text(tab.label.uppercased())
                            .font(.system(size: 10.5, weight: .heavy))
                            .foregroundStyle(isSelected ? GameTheme.purple : Color.gray.opacity(0.75))
                            .lineLimit(1)

                        Circle()
                            .fill(isSelected ? GameTheme.purple : Color.clear)
                            .frame(width: 6, height: 6)
                    }
                    .frame(maxWidth: .infinity)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.vertical, 8)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .shadow(color: GameTheme.purple.opacity(0.14), radius: 6, y: 2)
        .padding(.horizontal, 12)
        .padding(.top, 10)
    }

    private func tabIcon(for category: ItemCategory) -> String {
        switch category {
        case .clothes: return "bufanda"
        case .head: return "acc_corona"
        case .face: return "lentesdesol"
        case .objects: return "espada"
        case .backgrounds: return "ic_tienda"
        }
    }

    // MARK: - Barra de Compra Flotante

    private func buyBanner(for item: ShopItem) -> some View {
        let missing = item.price - appState.coins
        let canAfford = missing <= 0

        return HStack(spacing: 10) {
            VStack(alignment: .leading, spacing: 3) {
                GameTagChip(text: item.rarity.label, color: item.rarity.color)

                Text(item.name)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(GameTheme.darkText)
                    .lineLimit(1)

                Text(
                    canAfford
                        ? "Te quedarán \(appState.coins - item.price) monedas"
                        : "Te faltan \(missing) monedas"
                )
                .font(.system(size: 12, weight: .medium))
                .foregroundStyle(canAfford ? GameTheme.subText : GameTheme.errorRed)
            }

            Spacer(minLength: 4)

            Button {
                buy(item)
            } label: {
                HStack(spacing: 5) {
                    ZentemiIconView(name: "ic_moneda", size: 20)
                    Text("\(item.price)")
                }
            }
            .buttonStyle(
                GameChunkyButtonStyle(
                    color: GameTheme.turquoise,
                    lipColor: turquoiseDark,
                    textColor: .white,
                    enabled: canAfford,
                    height: 44
                )
            )
            .disabled(!canAfford)
            .frame(width: 112)

            Button {
                previewId = nil
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundStyle(GameTheme.subText)
                    .frame(width: 34, height: 34)
                    .clipShape(Circle())
            }
        }
        .padding(.leading, 14)
        .padding(.trailing, 8)
        .padding(.vertical, 10)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(cardBorder, lineWidth: 3)
        )
        .shadow(color: .black.opacity(0.10), radius: 8, y: 3)
    }

    // MARK: - Acciones

    private func onItemTap(_ item: ShopItem) {
        if appState.isOwned(item) {
            previewId = nil
            if appState.isEquipped(item) {
                SoundManager.shared.play("audioQuitarItem", volume: Float(appState.soundVolume))
            } else {
                SoundManager.shared.play("audioEquipar", volume: min(Float(appState.soundVolume) * 0.5, 1.0))
            }
            appState.toggleEquip(item)
        } else {
            previewId = (previewId == item.id) ? nil : item.id
        }
    }

    private func buy(_ item: ShopItem) {
        guard appState.purchase(item) else { return }
        SoundManager.shared.play("audioComprado", volume: Float(appState.soundVolume))
        if !appState.isEquipped(item) {
            appState.toggleEquip(item)
        }
        appState.purchasedItemAnimation = item.id
        appState.purchaseAnimationID = UUID()
        previewId = nil
        justBoughtId = item.id

        let boughtId = item.id
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.8) {
            if justBoughtId == boughtId {
                withAnimation {
                    justBoughtId = nil
                }
            }
        }
    }
}

// MARK: - Tarjeta Individual de Artículo

private struct StoreItemCardView: View {
    let item: ShopItem
    let owned: Bool
    let equipped: Bool
    let previewing: Bool
    let justBought: Bool
    let tall: Bool
    let cardBottom: Color
    let cardBorder: Color
    let goldHighlight: Color
    let onTap: () -> Void

    @State private var bounceScale: CGFloat = 1.0

    private var borderColor: Color {
        if previewing { return goldHighlight }
        if equipped { return GameTheme.turquoise }
        return cardBorder
    }

    var body: some View {
        Button(action: onTap) {
            GeometryReader { geo in
                ZStack {
                    LinearGradient(
                        colors: [Color.white, cardBottom],
                        startPoint: .top,
                        endPoint: .bottom
                    )

                    if let backdrop = item.backdrop {
                        MaicitoBackdropView(backdrop: backdrop)
                            .clipShape(RoundedRectangle(cornerRadius: 15))
                            .padding(3)
                    } else if let imageName = item.image {
                        Image(imageName)
                            .resizable()
                            .scaledToFit()
                            .padding(.horizontal, 14)
                            .padding(.top, 12)
                            .padding(.bottom, owned ? 12 : 26)
                            .scaleEffect(zoomScale)
                            .offset(y: zoomOffsetY(cardHeight: geo.size.height))
                            .frame(width: geo.size.width, height: geo.size.height)
                    }

                    // Precio abajo si aún no es tuyo
                    if !owned {
                        VStack {
                            Spacer()
                            HStack(spacing: 4) {
                                Text("\(item.price)")
                                    .font(.system(size: 16, weight: .heavy))
                                    .foregroundStyle(GameTheme.purpleDark)
                                ZentemiIconView(name: "ic_moneda", size: 17)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.top, 8)
                            .padding(.bottom, 6)
                            .background(
                                LinearGradient(
                                    colors: [Color.clear, GameTheme.purple.opacity(0.14)],
                                    startPoint: .top,
                                    endPoint: .bottom
                                )
                            )
                        }
                    }

                    // Palomita arriba a la derecha si lo trae puesto
                    if equipped {
                        VStack {
                            HStack {
                                Spacer()
                                ZStack {
                                    Circle()
                                        .fill(GameTheme.turquoise)
                                        .frame(width: 22, height: 22)
                                        .overlay(
                                            Circle()
                                                .stroke(Color.white, lineWidth: 2)
                                        )
                                    Image(systemName: "checkmark")
                                        .font(.system(size: 11, weight: .bold))
                                        .foregroundStyle(.white)
                                }
                                .padding(6)
                            }
                            Spacer()
                        }
                    }
                }
            }
            .aspectRatio(tall ? 0.66 : 1.0, contentMode: .fit)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .overlay(
                RoundedRectangle(cornerRadius: 18)
                    .stroke(borderColor, lineWidth: (previewing || equipped) ? 3.5 : 2.0)
            )
            .scaleEffect(bounceScale)
        }
        .buttonStyle(.plain)
        .onChange(of: justBought) { _, isBought in
            guard isBought else { return }
            withAnimation(.spring(response: 0.25, dampingFraction: 0.45)) {
                bounceScale = 1.12
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.18) {
                withAnimation(.spring(response: 0.28, dampingFraction: 0.65)) {
                    bounceScale = 1.0
                }
            }
        }
    }

    private var zoomScale: CGFloat {
        guard let layout = item.layout, layout.width >= 115 else { return 1.0 }
        switch item.slot {
        case .body: return 1.6
        case .hair: return 1.3
        default: return 1.0
        }
    }

    private func zoomOffsetY(cardHeight: CGFloat) -> CGFloat {
        guard let layout = item.layout, layout.width >= 115 else { return 0 }
        switch item.slot {
        case .body: return -cardHeight * 0.20
        case .hair: return cardHeight * 0.12
        default: return 0
        }
    }
}

#Preview {
    StoreView()
        .environmentObject(AppState())
}
