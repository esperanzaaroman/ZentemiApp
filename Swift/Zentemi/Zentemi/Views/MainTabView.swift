import SwiftUI

struct MainTabView: View {

    @EnvironmentObject var appState: AppState

    @State private var selectedTab = 2

    // MARK: - Animación compra → mochila

    @State private var flyingItem: String? = nil

    @State private var flyingX: CGFloat = 0
    @State private var flyingY: CGFloat = 0

    @State private var flyingScale: CGFloat = 1.0
    @State private var flyingOpacity: Double = 0

    @State private var backpackScale: CGFloat = 1.0


    var body: some View {

        GeometryReader { geometry in

            ZStack {

                // MARK: - CONTENIDO PRINCIPAL

                VStack(spacing: 0) {

                    TabView(
                        selection: $selectedTab
                    ) {

                        // 0 - Tienda

                        StoreView()
                            .tag(0)


                        // 1 - Mochila

                        BackpackView()
                            .tag(1)


                        // 2 - Jugar

                        HomeView()
                            .tag(2)


                        // 3 - Perfil

                        ProfileView()
                            .tag(3)
                    }
                    .tabViewStyle(
                        .page(
                            indexDisplayMode: .never
                        )
                    )


                    bottomBar
                }


                // MARK: - OBJETO VOLANDO

                if let flyingItem {

                    Image(flyingItem)
                        .resizable()
                        .scaledToFit()
                        .frame(
                            width: 85,
                            height: 85
                        )
                        .scaleEffect(flyingScale)
                        .opacity(flyingOpacity)
                        .position(
                            x: flyingX,
                            y: flyingY
                        )
                        .allowsHitTesting(false)
                        .zIndex(100)
                }
            }
            .onChange(
                of: appState.purchaseAnimationID
            ) {

                guard
                    let image =
                        appState.purchasedItemAnimation
                else {
                    return
                }

                startPurchaseAnimation(
                    image: image,
                    geometry: geometry
                )
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
        .ignoresSafeArea(
            edges: .bottom
        )
    }


    // MARK: - ANIMACIÓN DE COMPRA

    private func startPurchaseAnimation(
        image: String,
        geometry: GeometryProxy
    ) {

        // Imagen que acaba de comprarse

        flyingItem = image


        // Punto inicial:
        // zona central de las tarjetas

        flyingX =
            geometry.size.width / 2

        flyingY =
            geometry.size.height * 0.42

        flyingScale = 1.0
        flyingOpacity = 1.0


        // Primera parte:
        // pequeño salto hacia arriba

        withAnimation(
            .easeOut(duration: 0.30)
        ) {

            flyingY -= 35
            flyingScale = 1.12
        }


        // Segunda parte:
        // viajar hacia Mochila

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 0.30
        ) {

            withAnimation(
                .easeInOut(duration: 0.99)
            ) {

                // Mochila es el segundo botón
                // de cuatro botones

                flyingX =
                    geometry.size.width * 0.37

                flyingY =
                    geometry.size.height - 45

                flyingScale = 0.28
            }
        }


        // Llegada a Mochila

        DispatchQueue.main.asyncAfter(
            deadline: .now() + 1.23
        ) {

            withAnimation(
                .easeOut(duration: 0.10)
            ) {
                flyingOpacity = 0
            }


            // Pop de Mochila

            withAnimation(
                .spring(
                    response: 0.22,
                    dampingFraction: 0.45
                )
            ) {
                backpackScale = 1.28
            }


            DispatchQueue.main.asyncAfter(
                deadline: .now() + 0.15
            ) {

                withAnimation(
                    .spring(
                        response: 0.25,
                        dampingFraction: 0.65
                    )
                ) {
                    backpackScale = 1.0
                }
            }


            DispatchQueue.main.asyncAfter(
                deadline: .now() + 0.20
            ) {

                flyingItem = nil

                appState.purchasedItemAnimation = nil
            }
        }
    }


    // MARK: - BARRA INFERIOR

    private var bottomBar: some View {

        HStack {

            tabButton(
                icon: "storefront.fill",
                title: "Tienda",
                index: 0
            )


            Spacer()


            tabButton(
                icon: "backpack.fill",
                title: "Mochila",
                index: 1
            )
            .scaleEffect(backpackScale)


            Spacer()


            tabButton(
                icon: "play.circle.fill",
                title: "Jugar",
                index: 2
            )


            Spacer()


            tabButton(
                icon: "person.fill",
                title: "Perfil",
                index: 3
            )
        }
        .padding(
            .horizontal,
            28
        )
        .padding(
            .top,
            14
        )
        .padding(
            .bottom,
            28
        )
        .background(.white)
    }


    // MARK: - BOTÓN

    private func tabButton(
        icon: String,
        title: String,
        index: Int
    ) -> some View {

        Button {

            withAnimation {
                selectedTab = index
            }

        } label: {

            VStack(spacing: 5) {

                Image(
                    systemName: icon
                )
                .font(
                    .system(size: 20)
                )


                Text(title)
                    .font(
                        .system(size: 11)
                    )
            }
            .foregroundStyle(
                selectedTab == index
                ? Color.purple
                : Color.gray
            )
        }
    }
}


#Preview {

    MainTabView()
        .environmentObject(
            AppState()
        )
}
