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
    FACE("Cara"),
    NECK("Cuello"),
    HAND("Mano"),
    BACKGROUND("Fondo")
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
enum class Backdrop { SUNSET, STARRY_NIGHT, PAPEL_PICADO, TALAVERA }

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
 * Todos los artículos de Maicito. Los primeros 7 son los originales
 * (también existen en iOS); el resto son nuevos.
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

        // ── Divertidos ──
        ShopItem("pinkBow", "Moño rosa", ItemSlot.ADORNMENT, 120, Rarity.NEW, R.drawable.acc_mono_rosa, AccessoryLayout(42f, 24f, -42f)),
        ShopItem("cap", "Gorra", ItemSlot.HAT, 150, Rarity.NEW, R.drawable.acc_gorra, AccessoryLayout(88f, 6f, -44f)),
        ShopItem("heartGlasses", "Lentes de corazón", ItemSlot.FACE, 180, Rarity.RARE, R.drawable.acc_lentes_corazon, AccessoryLayout(72f, 1f, 0f)),
        ShopItem("crown", "Corona dorada", ItemSlot.HAT, 900, Rarity.LEGENDARY, R.drawable.acc_corona, AccessoryLayout(56f, 0f, -49f)),

        // ── Toque mexicano ──
        ShopItem("marigold", "Flor de cempasúchil", ItemSlot.ADORNMENT, 200, Rarity.RARE, R.drawable.acc_flor_cempasuchil, AccessoryLayout(34f, 26f, -40f)),
        ShopItem("papelBow", "Moño papel picado", ItemSlot.ADORNMENT, 220, Rarity.RARE, R.drawable.acc_mono_papel_picado, AccessoryLayout(44f, 24f, -42f)),
        ShopItem("sarape", "Sarape", ItemSlot.NECK, 350, Rarity.RARE, R.drawable.acc_sarape, AccessoryLayout(100f, 0f, 42f)),
        ShopItem("charro", "Sombrero de charro", ItemSlot.HAT, 600, Rarity.EPIC, R.drawable.acc_sombrero_charro, AccessoryLayout(106f, 0f, -43f)),

        // ── Fondos ──
        ShopItem("bgSunset", "Atardecer", ItemSlot.BACKGROUND, 150, Rarity.NEW, backdrop = Backdrop.SUNSET),
        ShopItem("bgNight", "Noche estrellada", ItemSlot.BACKGROUND, 250, Rarity.RARE, backdrop = Backdrop.STARRY_NIGHT),
        ShopItem("bgPapelPicado", "Papel picado", ItemSlot.BACKGROUND, 300, Rarity.RARE, backdrop = Backdrop.PAPEL_PICADO),
        ShopItem("bgTalavera", "Talavera", ItemSlot.BACKGROUND, 450, Rarity.EPIC, backdrop = Backdrop.TALAVERA)
    )

    /** Artículos a la venta (los incluidos no aparecen en la tienda). */
    val forSale: List<ShopItem> get() = items.filter { it.price > 0 }

    fun byId(id: String): ShopItem? = items.firstOrNull { it.id == id }
}
