import SwiftUI

struct PetView: View {

    @EnvironmentObject var appState: AppState

    var size: CGFloat = 115


    // MARK: - AJUSTE DE ACCESORIOS
    //
    // ESTA ES LA ÚNICA ZONA QUE TENDRÁS QUE
    // MODIFICAR PARA ACOMODAR LOS ACCESORIOS.


    // MARK: Lentes normales

    private let glassesWidth: CGFloat = 70
    private let glassesX: CGFloat = 1
    private let glassesY: CGFloat = 0


    // MARK: Cabello normal

    private let hairWidth: CGFloat = 100
    private let hairX: CGFloat = 0
    private let hairY: CGFloat = -30


    // MARK: Bufanda

    private let scarfWidth: CGFloat = 110
    private let scarfX: CGFloat = -2
    private let scarfY: CGFloat = 40


    // MARK: Lentes de sol

    private let sunglassesWidth: CGFloat = 70
    private let sunglassesX: CGFloat = 0
    private let sunglassesY: CGFloat = 0


    // MARK: Cabello rosa

    private let pinkHairWidth: CGFloat = 105
    private let pinkHairX: CGFloat = 0
    private let pinkHairY: CGFloat = -25


    // MARK: Espada real

    private let swordWidth: CGFloat = 70
    private let swordX: CGFloat = 45
    private let swordY: CGFloat = 9
    
    // MARK: Traje elegante

    private let suitWidth: CGFloat = 135
    private let suitX: CGFloat = 1
    private let suitY: CGFloat = 1


    var body: some View {

        ZStack {

            // MARK: Mascota base

            Image("mascota")
                .resizable()
                .scaledToFit()
                .frame(
                    width: size,
                    height: size
                )
                .hueRotation(
                    .degrees(
                        appState.mascotHue * 360
                    )
                )
            
            // MARK: Traje elegante

            if appState.suitEquipped {

                Image("traje1k")
                    .resizable()
                    .scaledToFit()
                    .frame(
                        width: suitWidth * size / 115
                    )
                    .fixedSize()
                    .offset(
                        x: suitX * size / 115,
                        y: suitY * size / 115
                    )
            }


            // MARK: Cabello normal

            if appState.hairEquipped {

                Image("cabello")
                    .resizable()
                    .scaledToFit()
                    .frame(
                        width:
                            hairWidth
                            * size / 115
                    )
                    .offset(
                        x:
                            hairX
                            * size / 115,
                        y:
                            hairY
                            * size / 115
                    )
            }


            // MARK: Cabello rosa

            if appState.pinkHairEquipped {

                Image("cabellorosa")
                    .resizable()
                    .scaledToFit()
                    .frame(
                        width:
                            pinkHairWidth
                            * size / 115
                    )
                    .offset(
                        x:
                            pinkHairX
                            * size / 115,
                        y:
                            pinkHairY
                            * size / 115
                    )
            }


            // MARK: Lentes normales

            if appState.glassesEquipped {

                Image("lentes")
                    .resizable()
                    .scaledToFit()
                    .frame(
                        width:
                            glassesWidth
                            * size / 115
                    )
                    .offset(
                        x:
                            glassesX
                            * size / 115,
                        y:
                            glassesY
                            * size / 115
                    )
            }


            // MARK: Lentes de sol

            if appState.sunglassesEquipped {

                Image("lentesdesol")
                    .resizable()
                    .scaledToFit()
                    .frame(
                        width:
                            sunglassesWidth
                            * size / 115
                    )
                    .offset(
                        x:
                            sunglassesX
                            * size / 115,
                        y:
                            sunglassesY
                            * size / 115
                    )
            }


            // MARK: Bufanda

            if appState.scarfEquipped {

                Image("bufanda")
                    .resizable()
                    .scaledToFit()
                    .frame(
                        width:
                            scarfWidth
                            * size / 115
                    )
                    .offset(
                        x:
                            scarfX
                            * size / 115,
                        y:
                            scarfY
                            * size / 115
                    )
            }


            // MARK: Espada real

            if appState.swordEquipped {

                Image("espada")
                    .resizable()
                    .scaledToFit()
                    .frame(
                        width:
                            swordWidth
                            * size / 115
                    )
                    .offset(
                        x:
                            swordX
                            * size / 115,
                        y:
                            swordY
                            * size / 115
                    )
            }
        }
        .frame(
            width: size,
            height: size
        )
    }
}


// MARK: - PREVIEW PARA AJUSTAR ACCESORIOS

#Preview {

    PetPreviewTest()
}


private struct PetPreviewTest: View {

    @StateObject private var appState = AppState()

    var body: some View {

        ScrollView {

            VStack(spacing: 20) {

                PetView(size: 220)
                    .environmentObject(appState)


                Text("Vista previa de accesorios")
                    .font(.headline)


                // MARK: - CARA

                Text("Cara")
                    .font(.subheadline.bold())

                Toggle(
                    "Lentes",
                    isOn:
                        $appState.glassesEquipped
                )

                Toggle(
                    "Lentes de sol",
                    isOn:
                        $appState.sunglassesEquipped
                )


                // MARK: - CABELLO

                Text("Cabello")
                    .font(.subheadline.bold())

                Toggle(
                    "Cabello",
                    isOn:
                        $appState.hairEquipped
                )

                Toggle(
                    "Cabello rosa",
                    isOn:
                        $appState.pinkHairEquipped
                )


                // MARK: - ACCESORIOS

                Text("Accesorios")
                    .font(.subheadline.bold())

                Toggle(
                    "Bufanda",
                    isOn:
                        $appState.scarfEquipped
                )

                Toggle(
                    "Espada real",
                    isOn:
                        $appState.swordEquipped
                )
                // MARK: - ROPA

                Text("Ropa")
                    .font(.subheadline.bold())

                Toggle(
                    "Traje elegante",
                    isOn:
                        $appState.suitEquipped
                )
            }
            .padding(30)
        }
    }
}
