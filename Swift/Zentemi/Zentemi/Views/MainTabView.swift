import SwiftUI

/// Pantalla principal con 3 pestañas: Tienda (vestidor), Inicio y Perfil.
/// Espejo exacto de `MainScreen.kt` en Android v1.1.
struct MainTabView: View {

    @EnvironmentObject var appState: AppState

    /// Inicio (índice 1) es la pestaña por defecto.
    @State private var selectedTab = 1
    @State private var storeBounceScale: CGFloat = 1.0

    private let tabs: [(icon: String, label: String)] = [
        ("ic_tienda", "Tienda"),
        ("ic_inicio", "Inicio"),
        ("ic_perfil", "Perfil")
    ]

    var body: some View {
        ZStack(alignment: .bottom) {
            Group {
                switch selectedTab {
                case 0:
                    StoreView()
                case 1:
                    HomeView()
                default:
                    ProfileView()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            bottomBar
        }
        .onChange(of: appState.purchaseAnimationID) { _, _ in
            guard appState.purchasedItemAnimation != nil else { return }
            withAnimation(.spring(response: 0.25, dampingFraction: 0.45)) {
                storeBounceScale = 1.30
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.18) {
                withAnimation(.spring(response: 0.28, dampingFraction: 0.65)) {
                    storeBounceScale = 1.0
                }
                appState.purchasedItemAnimation = nil
            }
        }
        .onAppear {
            if appState.musicVolume > 0 {
                MusicManager.shared.playMusic(
                    named: "audiolobby",
                    volume: Float(appState.musicVolume)
                )
            }
        }
        .navigationBarBackButtonHidden(true)
        .ignoresSafeArea(edges: .bottom)
    }

    // MARK: - Barra Inferior Redondeada (3 Pestañas)

    private var bottomBar: some View {
        HStack(spacing: 0) {
            ForEach(Array(tabs.enumerated()), id: \.offset) { index, tab in
                let isSelected = (selectedTab == index)

                Button {
                    withAnimation(.easeInOut(duration: 0.16)) {
                        selectedTab = index
                    }
                } label: {
                    VStack(spacing: 2) {
                        ZentemiIconView(
                            name: tab.icon,
                            size: isSelected ? 32 : 28,
                            muted: !isSelected
                        )
                        .scaleEffect(index == 0 ? storeBounceScale : 1.0)

                        Text(tab.label)
                            .font(.system(size: 11, weight: isSelected ? .bold : .medium))
                            .foregroundStyle(isSelected ? GameTheme.purple : Color.gray.opacity(0.6))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 4)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 8)
        .padding(.top, 8)
        .padding(.bottom, 24)
        .background(
            UnevenRoundedRectangle(
                topLeadingRadius: 24,
                topTrailingRadius: 24
            )
            .fill(Color.white)
            .shadow(color: GameTheme.purple.opacity(0.18), radius: 12, y: -2)
        )
    }
}

#Preview {
    MainTabView()
        .environmentObject(AppState())
}
