import SwiftUI

/// Zona donde se coloca un artículo sobre Maicito. Solo cabe uno por zona:
/// al equipar uno se quita el que estaba en esa misma zona.
/// El orden de los casos define qué capa se dibuja encima (igual que `ItemSlot` en Android).
enum ItemSlot: Int, CaseIterable {
    case body
    case hair
    case hat
    case adornment
    case face
    case detail
    case neck
    case hand
    case background

    var label: String {
        switch self {
        case .body: return "Ropa"
        case .hair: return "Cabello"
        case .hat: return "Sombrero"
        case .adornment: return "Adorno"
        case .face: return "Ojos"
        case .detail: return "Bigote y mejillas"
        case .neck: return "Cuello"
        case .hand: return "Mano"
        case .background: return "Fondo"
        }
    }
}

/// Categorías compartidas por la tienda y la mochila.
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
    var label: String { title }

    var slots: Set<ItemSlot> {
        switch self {
        case .all: return Set(ItemSlot.allCases)
        case .clothing: return [.body]
        case .hair: return [.hair]
        case .accessories: return [.hat, .adornment, .face, .detail, .neck, .hand]
        case .backgrounds: return [.background]
        }
    }

    func contains(_ item: ShopItem) -> Bool { slots.contains(item.slot) }
}

enum ItemRarity {
    case new
    case rare
    case epic
    case legendary

    var label: String {
        switch self {
        case .new: return "NUEVO"
        case .rare: return "RARO"
        case .epic: return "ÉPICO"
        case .legendary: return "LEGENDARIO"
        }
    }

    var color: Color {
        switch self {
        case .new: return GameTheme.turquoise
        case .rare: return Color(red: 0.965, green: 0.737, blue: 0.235)      // #F6BC3C
        case .epic: return Color(red: 0.612, green: 0.153, blue: 0.690)      // #9C27B0
        case .legendary: return Color(red: 1.0, green: 0.596, blue: 0.0)     // #FF9800
        }
    }
}

/// Cómo se acomoda un accesorio sobre Maicito en medida base 115pt.
struct AccessoryLayout: Equatable {
    let width: CGFloat
    let x: CGFloat
    let y: CGFloat
}

/// Fondos decorativos detrás de Maicito (espejo exacto de `Backdrop` en Android).
enum Backdrop: String, CaseIterable, Equatable {
    case sunset
    case starryNight
    case papelPicado
    case talavera
    case milpa
    case playa
    case bosque
    case piramide
    case arcoiris
    case diaDeMuertos
}

struct ShopItem: Identifiable, Equatable {
    let id: String
    let name: String
    let slot: ItemSlot
    /// 0 = incluido desde el inicio (no se vende).
    let price: Int
    let rarity: ItemRarity
    let image: String?
    let layout: AccessoryLayout?
    let backdrop: Backdrop?

    init(
        id: String,
        name: String,
        slot: ItemSlot,
        price: Int,
        rarity: ItemRarity,
        image: String? = nil,
        layout: AccessoryLayout? = nil,
        backdrop: Backdrop? = nil
    ) {
        self.id = id
        self.name = name
        self.slot = slot
        self.price = price
        self.rarity = rarity
        self.image = image
        self.layout = layout
        self.backdrop = backdrop
    }

    var isAccessory: Bool {
        slot != .background
    }
}

/// Catálogo v1.1 más la playerita roja y los fondos propios de iOS.
enum ShopCatalog {

    static let items: [ShopItem] = [
        // ── Incluidos desde el inicio ──
        ShopItem(id: "glasses", name: "Lentes", slot: .face, price: 0, rarity: .new, image: "lentes", layout: AccessoryLayout(width: 70, x: 1, y: 0)),
        ShopItem(id: "hair", name: "Cabello", slot: .hair, price: 0, rarity: .new, image: "cabello", layout: AccessoryLayout(width: 100, x: 0, y: -30)),
        ShopItem(id: "scarf", name: "Bufanda", slot: .neck, price: 0, rarity: .new, image: "bufanda", layout: AccessoryLayout(width: 110, x: -2, y: 40)),

        ShopItem(id: "redShirt", name: "Playerita roja", slot: .body, price: 100, rarity: .new, image: "playeritaRoja", layout: AccessoryLayout(width: 135, x: 1, y: 1)),
        ShopItem(id: "backgroundBeach", name: "Fondo de playa", slot: .background, price: 300, rarity: .new, image: "homeBeachBackground"),
        ShopItem(id: "backgroundValley", name: "Fondo de valle", slot: .background, price: 500, rarity: .rare, image: "homeValleyBackground"),

        // ── Tienda original ──
        ShopItem(id: "sunglasses", name: "Lentes de sol", slot: .face, price: 120, rarity: .new, image: "lentesdesol", layout: AccessoryLayout(width: 70, x: 0, y: 0)),
        ShopItem(id: "pinkHair", name: "Cabello rosa", slot: .hair, price: 250, rarity: .rare, image: "cabellorosa", layout: AccessoryLayout(width: 105, x: 0, y: -25)),
        ShopItem(id: "sword", name: "Espada real", slot: .hand, price: 500, rarity: .epic, image: "espada", layout: AccessoryLayout(width: 70, x: 45, y: 9)),
        ShopItem(id: "suit", name: "Traje elegante", slot: .body, price: 1000, rarity: .legendary, image: "traje1k", layout: AccessoryLayout(width: 135, x: 1, y: 1)),

        // ── Cabeza ──
        ShopItem(id: "pinkBow", name: "Moño rosa", slot: .adornment, price: 120, rarity: .new, image: "acc_mono_rosa", layout: AccessoryLayout(width: 40, x: 22, y: -32)),
        ShopItem(id: "cap", name: "Gorra", slot: .hat, price: 150, rarity: .new, image: "acc_gorra", layout: AccessoryLayout(width: 88, x: 6, y: -44)),
        ShopItem(id: "bun", name: "Chongo", slot: .hair, price: 160, rarity: .new, image: "acc_chongo", layout: AccessoryLayout(width: 115, x: 0, y: 0)),
        ShopItem(id: "braids", name: "Trenzas", slot: .hair, price: 240, rarity: .rare, image: "acc_trenzas", layout: AccessoryLayout(width: 115, x: 0, y: 0)),
        ShopItem(id: "curls", name: "Rizos", slot: .hair, price: 320, rarity: .rare, image: "acc_rizos", layout: AccessoryLayout(width: 115, x: 0, y: 0)),
        ShopItem(id: "partyHat", name: "Gorro de fiesta", slot: .hat, price: 180, rarity: .new, image: "acc_gorro_fiesta", layout: AccessoryLayout(width: 44, x: 1, y: -56)),
        ShopItem(id: "marigold", name: "Flor de cempasúchil", slot: .adornment, price: 200, rarity: .rare, image: "acc_flor_cempasuchil", layout: AccessoryLayout(width: 32, x: 24, y: -30)),
        ShopItem(id: "papelBow", name: "Moño papel picado", slot: .adornment, price: 220, rarity: .rare, image: "acc_mono_papel_picado", layout: AccessoryLayout(width: 42, x: 22, y: -32)),
        ShopItem(id: "flowerCrown", name: "Corona de flores", slot: .hat, price: 380, rarity: .rare, image: "acc_corona_flores", layout: AccessoryLayout(width: 64, x: 0, y: -33)),
        ShopItem(id: "charro", name: "Sombrero de charro", slot: .hat, price: 600, rarity: .epic, image: "acc_sombrero_charro", layout: AccessoryLayout(width: 106, x: 0, y: -43)),
        ShopItem(id: "crown", name: "Corona dorada", slot: .hat, price: 900, rarity: .legendary, image: "acc_corona", layout: AccessoryLayout(width: 56, x: 0, y: -49)),

        // ── Cara ──
        ShopItem(id: "heartCheeks", name: "Mejillas de corazón", slot: .detail, price: 100, rarity: .new, image: "acc_mejillas_corazon", layout: AccessoryLayout(width: 56, x: 0, y: 9)),
        ShopItem(id: "mustache", name: "Bigote", slot: .detail, price: 150, rarity: .new, image: "acc_bigote", layout: AccessoryLayout(width: 38, x: 0, y: 9)),
        ShopItem(id: "heartGlasses", name: "Lentes de corazón", slot: .face, price: 180, rarity: .rare, image: "acc_lentes_corazon", layout: AccessoryLayout(width: 72, x: 1, y: 0)),
        ShopItem(id: "starGlasses", name: "Lentes de estrella", slot: .face, price: 220, rarity: .rare, image: "acc_lentes_estrella", layout: AccessoryLayout(width: 72, x: 1, y: 1)),
        ShopItem(id: "eyePatch", name: "Parche pirata", slot: .face, price: 260, rarity: .rare, image: "acc_parche_pirata", layout: AccessoryLayout(width: 72, x: 1, y: -1)),
        ShopItem(id: "charroMustache", name: "Bigote de charro", slot: .detail, price: 300, rarity: .rare, image: "acc_bigote_charro", layout: AccessoryLayout(width: 44, x: 0, y: 14)),
        ShopItem(id: "mask", name: "Antifaz", slot: .face, price: 450, rarity: .epic, image: "acc_antifaz", layout: AccessoryLayout(width: 74, x: 1, y: 0)),
        ShopItem(id: "monocle", name: "Monóculo", slot: .face, price: 700, rarity: .legendary, image: "acc_monoculo", layout: AccessoryLayout(width: 72, x: 1, y: 2)),

        // ── Ropa ──
        ShopItem(id: "bowTie", name: "Moño de gala", slot: .neck, price: 130, rarity: .new, image: "acc_mono_gala", layout: AccessoryLayout(width: 36, x: 0, y: 22)),
        ShopItem(id: "tie", name: "Corbata", slot: .neck, price: 160, rarity: .new, image: "acc_corbata", layout: AccessoryLayout(width: 20, x: 0, y: 38)),
        ShopItem(id: "bandana", name: "Paliacate", slot: .neck, price: 200, rarity: .rare, image: "acc_paliacate", layout: AccessoryLayout(width: 84, x: 0, y: 32)),
        ShopItem(id: "overalls", name: "Overol", slot: .body, price: 300, rarity: .rare, image: "acc_overol", layout: AccessoryLayout(width: 115, x: 0, y: 0)),
        ShopItem(id: "dress", name: "Vestido floreado", slot: .body, price: 420, rarity: .epic, image: "acc_vestido", layout: AccessoryLayout(width: 115, x: 0, y: 0)),
        ShopItem(id: "sarape", name: "Sarape", slot: .neck, price: 350, rarity: .rare, image: "acc_sarape", layout: AccessoryLayout(width: 78, x: 0, y: 30)),
        ShopItem(id: "lei", name: "Collar de flores", slot: .neck, price: 400, rarity: .epic, image: "acc_collar_flores", layout: AccessoryLayout(width: 70, x: 0, y: 24)),
        ShopItem(id: "balletDress", name: "Vestido de bailarina", slot: .body, price: 480, rarity: .epic, image: "acc_vestido_bailarina", layout: AccessoryLayout(width: 115, x: 0, y: 0)),
        ShopItem(id: "medal", name: "Medalla de oro", slot: .neck, price: 750, rarity: .legendary, image: "acc_medalla", layout: AccessoryLayout(width: 30, x: 0, y: 24)),

        // ── Objetos ──
        ShopItem(id: "lollipop", name: "Paleta", slot: .hand, price: 120, rarity: .new, image: "acc_paleta", layout: AccessoryLayout(width: 32, x: 42, y: 3)),
        ShopItem(id: "balloon", name: "Globo", slot: .hand, price: 150, rarity: .new, image: "acc_globo", layout: AccessoryLayout(width: 32, x: 45, y: -12)),
        ShopItem(id: "chocolate", name: "Chocolate caliente", slot: .hand, price: 180, rarity: .new, image: "acc_taza_chocolate", layout: AccessoryLayout(width: 34, x: 43, y: 11)),
        ShopItem(id: "pinwheel", name: "Rehilete", slot: .hand, price: 220, rarity: .rare, image: "acc_rehilete", layout: AccessoryLayout(width: 42, x: 42, y: 1)),
        ShopItem(id: "maraca", name: "Maraca", slot: .hand, price: 250, rarity: .rare, image: "acc_maraca", layout: AccessoryLayout(width: 32, x: 42, y: 5)),
        ShopItem(id: "trompo", name: "Trompo", slot: .hand, price: 280, rarity: .rare, image: "acc_trompo", layout: AccessoryLayout(width: 30, x: 42, y: 8)),
        ShopItem(id: "book", name: "Libro", slot: .hand, price: 320, rarity: .rare, image: "acc_libro", layout: AccessoryLayout(width: 42, x: 42, y: 13)),
        ShopItem(id: "sunflower", name: "Girasol", slot: .hand, price: 400, rarity: .epic, image: "acc_girasol", layout: AccessoryLayout(width: 42, x: 42, y: 1)),
        ShopItem(id: "wand", name: "Varita mágica", slot: .hand, price: 650, rarity: .epic, image: "acc_varita", layout: AccessoryLayout(width: 44, x: 54, y: 6)),

        // ── Fondos ──
        ShopItem(id: "bgSunset", name: "Atardecer", slot: .background, price: 150, rarity: .new, backdrop: .sunset),
        ShopItem(id: "bgMilpa", name: "Milpa", slot: .background, price: 180, rarity: .new, backdrop: .milpa),
        ShopItem(id: "bgRainbow", name: "Arcoíris", slot: .background, price: 200, rarity: .new, backdrop: .arcoiris),
        ShopItem(id: "bgNight", name: "Noche estrellada", slot: .background, price: 250, rarity: .rare, backdrop: .starryNight),
        ShopItem(id: "bgBeach", name: "Playa", slot: .background, price: 280, rarity: .rare, backdrop: .playa),
        ShopItem(id: "bgPapelPicado", name: "Papel picado", slot: .background, price: 300, rarity: .rare, backdrop: .papelPicado),
        ShopItem(id: "bgForest", name: "Bosque", slot: .background, price: 320, rarity: .rare, backdrop: .bosque),
        ShopItem(id: "bgTalavera", name: "Talavera", slot: .background, price: 450, rarity: .epic, backdrop: .talavera),
        ShopItem(id: "bgPyramid", name: "Pirámide", slot: .background, price: 550, rarity: .epic, backdrop: .piramide),
        ShopItem(id: "bgMuertos", name: "Día de Muertos", slot: .background, price: 800, rarity: .legendary, backdrop: .diaDeMuertos)
    ]

    /// Desplazamiento (x, y) para que moños y flores queden encima de ciertos peinados.
    static let adornmentShiftForHair: [String: (CGFloat, CGFloat)] = [
        "pinkHair": (-14, -16),
        "curls": (12, -14),
        "bun": (-6, -12)
    ]

    static var forSale: [ShopItem] {
        items.filter { $0.price > 0 }
    }

    static func byId(_ id: String) -> ShopItem? {
        items.first { $0.id == id }
    }
}
