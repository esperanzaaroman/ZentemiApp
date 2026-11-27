import SwiftUI

/// Maicito con todos sus accesorios equipados en orden de zona (y soporte de vista previa en Tienda).
/// Espejo exacto de `PetView.kt` en Android, compatible con todas las llamadas `PetView(size:)`.
struct PetView: View {

    @EnvironmentObject var appState: AppState

    var size: CGFloat = 115
    var preview: ShopItem? = nil

    private var activeAccessories: [ShopItem] {
        guard let preview, preview.isAccessory else {
            return appState.equippedAccessories
        }
        return (appState.equippedAccessories.filter { $0.slot != preview.slot } + [preview])
            .sorted { $0.slot.rawValue < $1.slot.rawValue }
    }

    var body: some View {
        let scale = size / 115.0
        let hairId = activeAccessories.first { $0.slot == .hair }?.id ?? ""
        let shift = ShopCatalog.adornmentShiftForHair[hairId] ?? (0, 0)

        ZStack {
            Image("mascota")
                .resizable()
                .scaledToFit()
                .frame(width: size, height: size)
                .hueRotation(.degrees(appState.mascotHue * 360))

            ForEach(activeAccessories) { item in
                if let imageName = item.image, let layout = item.layout {
                    let isAdornment = (item.slot == .adornment)
                    let dx = (layout.x + (isAdornment ? shift.0 : 0)) * scale
                    let dy = (layout.y + (isAdornment ? shift.1 : 0)) * scale
                    let targetW = layout.width * scale

                    Image(imageName)
                        .resizable()
                        .scaledToFit()
                        .frame(width: targetW)
                        .fixedSize()
                        .offset(x: dx, y: dy)
                }
            }
        }
        .frame(width: size, height: size)
    }
}

#Preview {
    PetView(size: 190)
        .environmentObject(AppState())
}
