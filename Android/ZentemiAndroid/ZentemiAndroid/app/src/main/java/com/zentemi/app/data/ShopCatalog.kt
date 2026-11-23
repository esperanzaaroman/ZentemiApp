package com.zentemi.app.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.zentemi.app.R
import com.zentemi.app.ui.theme.RarityEpic
import com.zentemi.app.ui.theme.RarityLegendary
import com.zentemi.app.ui.theme.RarityNew
import com.zentemi.app.ui.theme.SplashDotActive

/**
 * Zona donde se pone un artículo. Solo cabe uno por zona: al equipar uno,
 * se quita el que estaba en la misma zona.
 * El orden define qué se dibuja encima (el último queda hasta arriba).
 */
enum class ItemSlot(val label: String) {
    BODY("Ropa"),
    HAIR("Cabello"),
    HAT("Sombrero"),
    ADORNMENT("Adorno"),
    FACE("Ojos"),
    DETAIL("Bigote y mejillas"),
    NECK("Cuello"),
    HAND("Mano"),
    BACKGROUND("Fondo")
}

/**
 * Pestañas de la tienda. Cada una agrupa zonas parecidas para que sea fácil
 * encontrar las cosas.
 */
enum class ItemCategory(val label: String, val slots: Set<ItemSlot>) {
    BACKGROUNDS("Fondos", setOf(ItemSlot.BACKGROUND)),
    CLOTHES("Ropa", setOf(ItemSlot.NECK, ItemSlot.BODY)),
    HEAD("Cabeza", setOf(ItemSlot.HAIR, ItemSlot.HAT, ItemSlot.ADORNMENT)),
    FACE("Cara", setOf(ItemSlot.FACE, ItemSlot.DETAIL)),
    OBJECTS("Objetos", setOf(ItemSlot.HAND));

    fun contains(item: ShopItem): Boolean = item.slot in slots
}

enum class Rarity(val label: String, val color: Color) {
    NEW("NUEVO", RarityNew),
    RARE("RARO", SplashDotActive),
    EPIC("ÉPICO", RarityEpic),
    LEGENDARY("LEGENDARIO", RarityLegendary)
}

/**
 * Cómo se acomoda un accesorio sobre Maicito, en la medida base de 115
 * (igual que PetView en iOS): ancho y desplazamiento desde el centro.
 */
data class AccessoryLayout(val width: Float, val x: Float, val y: Float)

/** Fondos que se dibujan detrás de Maicito (ver MaicitoBackdrop). */
enum class Backdrop {
    SUNSET, STARRY_NIGHT, PAPEL_PICADO, TALAVERA,
    MILPA, PLAYA, BOSQUE, PIRAMIDE, ARCOIRIS, DIA_DE_MUERTOS
}

data class ShopItem(
    val id: String,
    val name: String,
    val slot: ItemSlot,
    /** 0 = viene incluido desde el inicio (no se vende). */
    val price: Int,
    val rarity: Rarity,
    @param:DrawableRes val image: Int? = null,
    val layout: AccessoryLayout? = null,
    val backdrop: Backdrop? = null
) {
    val isAccessory: Boolean get() = slot != ItemSlot.BACKGROUND
}

/**
 * Todos los artículos de Maicito: 10 por pestaña. Los primeros 7 son los
 * originales (también existen en iOS); el resto son solo de Android.
 */
object ShopCatalog {

    val items: List<ShopItem> = listOf(
        // ── Incluidos desde el inicio ──
        ShopItem("glasses", "Lentes", ItemSlot.FACE, 0, Rarity.NEW, R.drawable.lentes, AccessoryLayout(70f, 1f, 0f)),
        ShopItem("hair", "Cabello", ItemSlot.HAIR, 0, Rarity.NEW, R.drawable.cabello, AccessoryLayout(100f, 0f, -30f)),
        ShopItem("scarf", "Bufanda", ItemSlot.NECK, 0, Rarity.NEW, R.drawable.bufanda, AccessoryLayout(110f, -2f, 40f)),

        // ── Tienda original ──
        ShopItem("sunglasses", "Lentes de sol", ItemSlot.FACE, 120, Rarity.NEW, R.drawable.lentesdesol, AccessoryLayout(70f, 0f, 0f)),
        ShopItem("pinkHair", "Cabello rosa", ItemSlot.HAIR, 250, Rarity.RARE, R.drawable.cabellorosa, AccessoryLayout(105f, 0f, -25f)),
        ShopItem("sword", "Espada real", ItemSlot.HAND, 500, Rarity.EPIC, R.drawable.espada, AccessoryLayout(70f, 45f, 9f)),
        ShopItem("suit", "Traje elegante", ItemSlot.BODY, 1000, Rarity.LEGENDARY, R.drawable.traje1k, AccessoryLayout(135f, 1f, 1f)),

        // ── Cabeza ──
        ShopItem("pinkBow", "Moño rosa", ItemSlot.ADORNMENT, 120, Rarity.NEW, R.drawable.acc_mono_rosa, AccessoryLayout(40f, 22f, -32f)),
        ShopItem("cap", "Gorra", ItemSlot.HAT, 150, Rarity.NEW, R.drawable.acc_gorra, AccessoryLayout(88f, 6f, -44f)),
        ShopItem("bun", "Chongo", ItemSlot.HAIR, 160, Rarity.NEW, R.drawable.acc_chongo, AccessoryLayout(115f, 0f, 0f)),
        ShopItem("braids", "Trenzas", ItemSlot.HAIR, 240, Rarity.RARE, R.drawable.acc_trenzas, AccessoryLayout(115f, 0f, 0f)),
        ShopItem("curls", "Rizos", ItemSlot.HAIR, 320, Rarity.RARE, R.drawable.acc_rizos, AccessoryLayout(115f, 0f, 0f)),
        ShopItem("partyHat", "Gorro de fiesta", ItemSlot.HAT, 180, Rarity.NEW, R.drawable.acc_gorro_fiesta, AccessoryLayout(44f, 1f, -56f)),
        ShopItem("marigold", "Flor de cempasúchil", ItemSlot.ADORNMENT, 200, Rarity.RARE, R.drawable.acc_flor_cempasuchil, AccessoryLayout(32f, 24f, -30f)),
        ShopItem("papelBow", "Moño papel picado", ItemSlot.ADORNMENT, 220, Rarity.RARE, R.drawable.acc_mono_papel_picado, AccessoryLayout(42f, 22f, -32f)),
        ShopItem("flowerCrown", "Corona de flores", ItemSlot.HAT, 380, Rarity.RARE, R.drawable.acc_corona_flores, AccessoryLayout(64f, 0f, -33f)),
        ShopItem("charro", "Sombrero de charro", ItemSlot.HAT, 600, Rarity.EPIC, R.drawable.acc_sombrero_charro, AccessoryLayout(106f, 0f, -43f)),
        ShopItem("crown", "Corona dorada", ItemSlot.HAT, 900, Rarity.LEGENDARY, R.drawable.acc_corona, AccessoryLayout(56f, 0f, -49f)),

        // ── Cara ──
        ShopItem("heartCheeks", "Mejillas de corazón", ItemSlot.DETAIL, 100, Rarity.NEW, R.drawable.acc_mejillas_corazon, AccessoryLayout(56f, 0f, 9f)),
        ShopItem("mustache", "Bigote", ItemSlot.DETAIL, 150, Rarity.NEW, R.drawable.acc_bigote, AccessoryLayout(38f, 0f, 9f)),
        ShopItem("heartGlasses", "Lentes de corazón", ItemSlot.FACE, 180, Rarity.RARE, R.drawable.acc_lentes_corazon, AccessoryLayout(72f, 1f, 0f)),
        ShopItem("starGlasses", "Lentes de estrella", ItemSlot.FACE, 220, Rarity.RARE, R.drawable.acc_lentes_estrella, AccessoryLayout(72f, 1f, 1f)),
        ShopItem("eyePatch", "Parche pirata", ItemSlot.FACE, 260, Rarity.RARE, R.drawable.acc_parche_pirata, AccessoryLayout(72f, 1f, -1f)),
        ShopItem("charroMustache", "Bigote de charro", ItemSlot.DETAIL, 300, Rarity.RARE, R.drawable.acc_bigote_charro, AccessoryLayout(44f, 0f, 14f)),
        ShopItem("mask", "Antifaz", ItemSlot.FACE, 450, Rarity.EPIC, R.drawable.acc_antifaz, AccessoryLayout(74f, 1f, 0f)),
        ShopItem("monocle", "Monóculo", ItemSlot.FACE, 700, Rarity.LEGENDARY, R.drawable.acc_monoculo, AccessoryLayout(72f, 1f, 2f)),

        // ── Ropa ──
        ShopItem("bowTie", "Moño de gala", ItemSlot.NECK, 130, Rarity.NEW, R.drawable.acc_mono_gala, AccessoryLayout(36f, 0f, 22f)),
        ShopItem("tie", "Corbata", ItemSlot.NECK, 160, Rarity.NEW, R.drawable.acc_corbata, AccessoryLayout(20f, 0f, 38f)),
        ShopItem("bandana", "Paliacate", ItemSlot.NECK, 200, Rarity.RARE, R.drawable.acc_paliacate, AccessoryLayout(84f, 0f, 32f)),
        ShopItem("overalls", "Overol", ItemSlot.BODY, 300, Rarity.RARE, R.drawable.acc_overol, AccessoryLayout(115f, 0f, 0f)),
        ShopItem("dress", "Vestido floreado", ItemSlot.BODY, 420, Rarity.EPIC, R.drawable.acc_vestido, AccessoryLayout(115f, 0f, 0f)),
        ShopItem("sarape", "Sarape", ItemSlot.NECK, 350, Rarity.RARE, R.drawable.acc_sarape, AccessoryLayout(78f, 0f, 30f)),
        ShopItem("lei", "Collar de flores", ItemSlot.NECK, 400, Rarity.EPIC, R.drawable.acc_collar_flores, AccessoryLayout(70f, 0f, 24f)),
        ShopItem("balletDress", "Vestido de bailarina", ItemSlot.BODY, 480, Rarity.EPIC, R.drawable.acc_vestido_bailarina, AccessoryLayout(115f, 0f, 0f)),
        ShopItem("medal", "Medalla de oro", ItemSlot.NECK, 750, Rarity.LEGENDARY, R.drawable.acc_medalla, AccessoryLayout(30f, 0f, 24f)),

        // ── Objetos ──
        ShopItem("lollipop", "Paleta", ItemSlot.HAND, 120, Rarity.NEW, R.drawable.acc_paleta, AccessoryLayout(32f, 42f, 3f)),
        ShopItem("balloon", "Globo", ItemSlot.HAND, 150, Rarity.NEW, R.drawable.acc_globo, AccessoryLayout(32f, 45f, -12f)),
        ShopItem("chocolate", "Chocolate caliente", ItemSlot.HAND, 180, Rarity.NEW, R.drawable.acc_taza_chocolate, AccessoryLayout(34f, 43f, 11f)),
        ShopItem("pinwheel", "Rehilete", ItemSlot.HAND, 220, Rarity.RARE, R.drawable.acc_rehilete, AccessoryLayout(42f, 42f, 1f)),
        ShopItem("maraca", "Maraca", ItemSlot.HAND, 250, Rarity.RARE, R.drawable.acc_maraca, AccessoryLayout(32f, 42f, 5f)),
        ShopItem("trompo", "Trompo", ItemSlot.HAND, 280, Rarity.RARE, R.drawable.acc_trompo, AccessoryLayout(30f, 42f, 8f)),
        ShopItem("book", "Libro", ItemSlot.HAND, 320, Rarity.RARE, R.drawable.acc_libro, AccessoryLayout(42f, 42f, 13f)),
        ShopItem("sunflower", "Girasol", ItemSlot.HAND, 400, Rarity.EPIC, R.drawable.acc_girasol, AccessoryLayout(42f, 42f, 1f)),
        ShopItem("wand", "Varita mágica", ItemSlot.HAND, 650, Rarity.EPIC, R.drawable.acc_varita, AccessoryLayout(44f, 54f, 6f)),

        // ── Fondos ──
        ShopItem("bgSunset", "Atardecer", ItemSlot.BACKGROUND, 150, Rarity.NEW, backdrop = Backdrop.SUNSET),
        ShopItem("bgMilpa", "Milpa", ItemSlot.BACKGROUND, 180, Rarity.NEW, backdrop = Backdrop.MILPA),
        ShopItem("bgRainbow", "Arcoíris", ItemSlot.BACKGROUND, 200, Rarity.NEW, backdrop = Backdrop.ARCOIRIS),
        ShopItem("bgNight", "Noche estrellada", ItemSlot.BACKGROUND, 250, Rarity.RARE, backdrop = Backdrop.STARRY_NIGHT),
        ShopItem("bgBeach", "Playa", ItemSlot.BACKGROUND, 280, Rarity.RARE, backdrop = Backdrop.PLAYA),
        ShopItem("bgPapelPicado", "Papel picado", ItemSlot.BACKGROUND, 300, Rarity.RARE, backdrop = Backdrop.PAPEL_PICADO),
        ShopItem("bgForest", "Bosque", ItemSlot.BACKGROUND, 320, Rarity.RARE, backdrop = Backdrop.BOSQUE),
        ShopItem("bgTalavera", "Talavera", ItemSlot.BACKGROUND, 450, Rarity.EPIC, backdrop = Backdrop.TALAVERA),
        ShopItem("bgPyramid", "Pirámide", ItemSlot.BACKGROUND, 550, Rarity.EPIC, backdrop = Backdrop.PIRAMIDE),
        ShopItem("bgMuertos", "Día de Muertos", ItemSlot.BACKGROUND, 800, Rarity.LEGENDARY, backdrop = Backdrop.DIA_DE_MUERTOS)
    )

    /**
     * Con algunos cabellos los moños y flores se perderían entre el pelo:
     * este desplazamiento (x, y) los acomoda encima del peinado.
     */
    val adornmentShiftForHair: Map<String, Pair<Float, Float>> = mapOf(
        "pinkHair" to (-14f to -16f),
        "curls" to (12f to -14f),
        "bun" to (-6f to -12f)
    )

    /** Artículos a la venta (los incluidos no aparecen con precio). */
    val forSale: List<ShopItem> get() = items.filter { it.price > 0 }

    fun byId(id: String): ShopItem? = items.firstOrNull { it.id == id }
}
