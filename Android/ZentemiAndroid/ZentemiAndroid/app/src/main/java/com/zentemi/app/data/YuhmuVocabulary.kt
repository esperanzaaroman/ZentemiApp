package com.zentemi.app.data

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.zentemi.app.R

/**
 * Una palabra o frase en Yuhmu con su grabación.
 *
 * @param yuhmu Texto escrito en Yuhmu. Es null cuando todavía no tenemos la
 *   escritura confirmada: en ese caso los juegos solo usan el audio.
 * @param lesson Lección donde se aprende. Los juegos solo usan palabras de
 *   lecciones terminadas, para repasar y no enseñar cosas sin contexto.
 */
data class YuhmuWord(
    val id: String,
    val spanish: String,
    val yuhmu: String?,
    @param:RawRes val audio: Int,
    @param:DrawableRes val icon: Int,
    val lesson: Int
)

/**
 * Vocabulario disponible para los juegos de "Jugar con Maicito".
 *
 * Todas las grabaciones vienen de la comunidad de San Juan Ixtenco.
 * Para agregar palabras: añade el audio en res/raw y una entrada aquí
 * (o, cuando exista, cárgalas desde el backend con la misma forma).
 * No agregues textos en Yuhmu sin confirmarlos con la comunidad.
 */
object YuhmuVocabulary {

    val all: List<YuhmuWord> = listOf(
        YuhmuWord(
            id = "buenos_dias",
            spanish = "Buenos días",
            yuhmu = "Té xki hats’i",
            audio = R.raw.buenosdias,
            icon = R.drawable.ic_buenos_dias,
            lesson = 1
        ),
        YuhmuWord(
            id = "como_te_llamas",
            spanish = "¿Cómo te llamas?",
            yuhmu = null,
            audio = R.raw.comotellamas,
            icon = R.drawable.ic_como_te_llamas,
            lesson = 1
        ),
        YuhmuWord(
            id = "cuantos_anios",
            spanish = "¿Cuántos años tienes?",
            yuhmu = null,
            audio = R.raw.cuantosaniostienes,
            icon = R.drawable.ic_cuantos_anios,
            lesson = 1
        ),
        YuhmuWord(
            id = "ayudame",
            spanish = "Ayúdame por favor",
            yuhmu = null,
            audio = R.raw.ayudameporfavor,
            icon = R.drawable.ic_ayudame,
            lesson = 1
        )
    )

    fun byId(id: String): YuhmuWord = all.first { it.id == id }

    /** Palabras de las lecciones que ya se completaron. */
    fun unlocked(lessonsCompleted: Int): List<YuhmuWord> =
        all.filter { it.lesson <= lessonsCompleted }

    /**
     * Temas de vocabulario para "Maicito pide", sacados del material del socio
     * formador: la escritura viene de "Vocabulario.xlsx" y el audio de la
     * carpeta "audios" (mismos nombres de tema). Solo se incluyen palabras
     * que tienen las dos cosas.
     *
     * No pertenecen a ninguna lección (lesson = 0), así que [unlocked] no las
     * incluye y el mercado no las usa. En Maicito pide cada partida es de un
     * tema y las opciones siempre se ven en español.
     */
    val themes: List<YuhmuTheme> = listOf(
        theme("animales", "Animales", R.drawable.ic_animales,
            "Abeja" to ("Gáne" to R.raw.voc_abeja),
            "Borrego" to ("Déti" to R.raw.voc_borrego),
            "Burro" to ("’Áxnu" to R.raw.voc_burro),
            "Caballo" to ("Tóge" to R.raw.voc_caballo),
            "Gallina" to ("Tsü’ni" to R.raw.voc_gallina),
            "Gallo" to ("Tā’ni" to R.raw.voc_gallo),
            "Gato" to ("Mixtu" to R.raw.voc_gato),
            "Perro" to ("’Yó" to R.raw.voc_perro),
            "Pollo" to ("’Óni" to R.raw.voc_pollo),
            "Víbora" to ("K’èyä" to R.raw.voc_vibora)
        ),
        theme("comida", "Comida", R.drawable.ic_alimentos,
            "Agua" to ("Déhe" to R.raw.voc_agua),
            "Atole" to ("T’ěi" to R.raw.voc_atole),
            "Huevo" to ("Dò’ni" to R.raw.voc_huevo),
            "Masa" to ("Khüni" to R.raw.voc_masa),
            "Nixtamal" to ("Tshüni" to R.raw.voc_nixtamal),
            "Pan" to ("Hmè̱mpho̱" to R.raw.voc_pan),
            "Pulque" to ("Tshěi" to R.raw.voc_pulque),
            "Tamal" to ("Thědi" to R.raw.voc_tamal),
            "Tortilla" to ("Hmé" to R.raw.voc_tortilla)
        ),
        theme("cuerpo", "Partes del cuerpo", R.drawable.ic_perfil,
            "Boca" to ("Né" to R.raw.voc_boca),
            "Cabello" to ("Xtä̌" to R.raw.voc_cabello),
            "Ceja" to ("’Yǒdo" to R.raw.voc_ceja),
            "Dedo" to ("Xǒ" to R.raw.voc_dedo),
            "Diente" to ("Ts’ii" to R.raw.voc_diente),
            "Espalda" to ("Xǔ̱tha" to R.raw.voc_espalda),
            "Frente" to ("Dë́" to R.raw.voc_frente),
            "Lengua" to ("Khàne" to R.raw.voc_lengua),
            "Mano" to ("’Yé̱" to R.raw.voc_mano),
            "Nariz" to ("Xìyü" to R.raw.voc_nariz),
            "Ojo" to ("Dó" to R.raw.voc_ojo),
            "Oreja" to ("Gǘ" to R.raw.voc_oreja),
            "Pecho" to ("Tíyä" to R.raw.voc_pecho),
            "Pie" to ("Gwá" to R.raw.voc_pie),
            "Rodilla" to ("Yǎhmü" to R.raw.voc_rodilla),
            "Uña" to ("Xíxo" to R.raw.voc_una)
        ),
        theme("naturaleza", "Cielo y naturaleza", R.drawable.ic_estrella,
            "Aire" to ("Ndáhi" to R.raw.voc_aire),
            "Cielo" to ("Mahěts’i" to R.raw.voc_cielo),
            "Estrella" to ("Tsó̱" to R.raw.voc_estrella),
            "Lluvia" to ("’Yè" to R.raw.voc_lluvia),
            "Lumbre" to ("Tsìbi" to R.raw.voc_lumbre),
            "Luna" to ("Zànä" to R.raw.voc_luna),
            "Sol" to ("Hyádi" to R.raw.voc_sol),
            "Tierra" to ("Hòi" to R.raw.voc_tierra)
        ),
        theme("numeros", "Números", R.drawable.ic_numeros,
            "Uno" to ("N’á" to R.raw.voc_num_1),
            "Dos" to ("Yóho" to R.raw.voc_num_2),
            "Tres" to ("Hyǘ" to R.raw.voc_num_3),
            "Cuatro" to ("Gǒho" to R.raw.voc_num_4),
            "Cinco" to ("Kú̱t’a" to R.raw.voc_num_5),
            "Seis" to ("’Ráto" to R.raw.voc_num_6),
            "Siete" to ("Yòto" to R.raw.voc_num_7),
            "Ocho" to ("Hyä́to" to R.raw.voc_num_8),
            "Nueve" to ("Gǔ̱to" to R.raw.voc_num_9),
            "Diez" to ("’Ré̱t’a" to R.raw.voc_num_10)
        ),
        theme("objetos", "Objetos de la casa", R.drawable.plato,
            "Casa" to ("Ngǘ" to R.raw.voc_casa),
            "Comal" to ("Dò’yo" to R.raw.voc_comal),
            "Llave" to ("Ntshóki" to R.raw.voc_llave),
            "Metate" to ("Nikhüni" to R.raw.voc_metate),
            "Molcajete" to ("Màndo" to R.raw.voc_molcajete),
            "Olla" to ("Ts’èe" to R.raw.voc_olla),
            "Petate" to ("Xíphi" to R.raw.voc_petate),
            "Plato" to ("Mánza" to R.raw.voc_plato)
        ),
        theme("plantas", "Frutas y plantas", R.drawable.manzana,
            "Calabaza" to ("Mǚ" to R.raw.voc_calabaza),
            "Capulín" to ("Dè̱se̱" to R.raw.voc_capulin),
            "Caña" to ("K’á’yo" to R.raw.voc_cana),
            "Durazno" to ("Nǎxnu" to R.raw.voc_durazno),
            "Flor" to ("Dòni" to R.raw.voc_flor),
            "Frijol" to ("Ts’ünkhu̱" to R.raw.voc_frijol),
            "Maíz" to ("Dèthä" to R.raw.voc_maiz),
            "Mazorca" to ("Thá" to R.raw.voc_mazorca),
            "Milpa" to ("Thúhü" to R.raw.voc_milpa),
            "Quelite" to ("Gink’ani" to R.raw.voc_quelite)
        ),
        theme("familia", "Familia", R.drawable.ic_familia,
            "Abuela" to ("Tsǘ" to R.raw.voc_abuela),
            "Abuelo" to ("Dä̀ta" to R.raw.voc_abuelo),
            "Mamá" to ("Nana" to R.raw.voc_mama),
            "Papá" to ("Dada" to R.raw.voc_papa),
            "Mi hijo" to ("Ma bä̀tsiga" to R.raw.voc_mi_hijo),
            "Mi hija" to ("Ma T’ìxüga" to R.raw.voc_mi_hija),
            "Mi tía" to ("Ma Zìtsiga" to R.raw.voc_mi_tia),
            "Mi tío" to ("Ma ’Wěga" to R.raw.voc_mi_tio),
            "Padrino" to ("Tàikhä" to R.raw.voc_padrino),
            "Madrina" to ("Měkhä" to R.raw.voc_madrina)
        ),
        theme("colores", "Colores y tamaños", R.drawable.ic_colores,
            "Blanco" to ("T’àxi" to R.raw.voc_blanco),
            "Negro" to ("Mbòi" to R.raw.voc_negro),
            "Grande" to ("Ndòte̱" to R.raw.voc_grande),
            "Pequeño" to ("Chitépi" to R.raw.voc_pequeno),
            "Alto" to ("Hěts’i" to R.raw.voc_alto)
        )
    )

    private fun theme(
        id: String,
        title: String,
        @DrawableRes icon: Int,
        vararg words: Pair<String, Pair<String, Int>>
    ) = YuhmuTheme(
        id = id,
        title = title,
        icon = icon,
        words = words.map { (spanish, entry) ->
            YuhmuWord(
                id = "${id}_${spanish.lowercase()}",
                spanish = spanish,
                yuhmu = entry.first,
                audio = entry.second,
                icon = icon,
                lesson = 0
            )
        }
    )
}

/** Un grupo de palabras del mismo tema (animales, comida, números…). */
data class YuhmuTheme(
    val id: String,
    val title: String,
    @param:DrawableRes val icon: Int,
    val words: List<YuhmuWord>
)
