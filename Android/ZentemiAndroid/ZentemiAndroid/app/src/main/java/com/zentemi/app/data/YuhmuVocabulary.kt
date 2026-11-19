package com.zentemi.app.data

import androidx.annotation.RawRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
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
    val icon: ImageVector,
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
            icon = Icons.Filled.WbSunny,
            lesson = 1
        ),
        YuhmuWord(
            id = "como_te_llamas",
            spanish = "¿Cómo te llamas?",
            yuhmu = null,
            audio = R.raw.comotellamas,
            icon = Icons.Filled.Badge,
            lesson = 1
        ),
        YuhmuWord(
            id = "cuantos_anios",
            spanish = "¿Cuántos años tienes?",
            yuhmu = null,
            audio = R.raw.cuantosaniostienes,
            icon = Icons.Filled.Cake,
            lesson = 1
        ),
        YuhmuWord(
            id = "ayudame",
            spanish = "Ayúdame por favor",
            yuhmu = null,
            audio = R.raw.ayudameporfavor,
            icon = Icons.Filled.PanTool,
            lesson = 1
        )
    )

    fun byId(id: String): YuhmuWord = all.first { it.id == id }

    /** Palabras de las lecciones que ya se completaron. */
    fun unlocked(lessonsCompleted: Int): List<YuhmuWord> =
        all.filter { it.lesson <= lessonsCompleted }
}
