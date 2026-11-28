package com.zentemi.app.data

import androidx.annotation.DrawableRes
import com.zentemi.app.R
import java.text.BreakIterator
import java.util.Locale

/** Objetivo de puntos de los juegos (igual que iOS). */
const val GAME_POINTS_GOAL = 10_000

/** Palabra de Memorama / Sopa de letras. */
data class MemoryWord(
    val id: String,
    val yuhmu: String,
    val spanish: String,
    @DrawableRes val image: Int
)

/**
 * Niveles de Memorama. Grafías del Manual de Yonca Gaspar y Alarcón Montero
 * (2024), copiadas de MemoryLevel.swift: los signos combinantes son parte de
 * la palabra.
 */
enum class MemoryLevel(val number: Int, val coins: Int, val points: Int, val theme: String) {
    ONE(1, 300, 500, "Agua, casa, flor, luna y árbol"),
    TWO(2, 450, 1_400, "Conoce a los animales"),
    THREE(3, 700, 2_500, "Objetos, alimentos y naturaleza");

    val words: List<MemoryWord>
        get() = when (this) {
            ONE -> levelOneWords
            TWO -> animalWords
            THREE -> everydayWords
        }

    /** Prefijo de las claves guardadas, igual que iOS (memoryLevelOneCompleted...). */
    val key: String get() = "memoryLevel" + LEVEL_NAMES[number - 1]

    companion object {
        val levelOneWords = listOf(
            MemoryWord("water", "Déhe", "Agua", R.drawable.memory_water),
            MemoryWord("house", "Ngǚ", "Casa", R.drawable.memory_house),
            MemoryWord("flower", "Dò̱ni", "Flor", R.drawable.memory_flower),
            MemoryWord("moon", "Zä̀nä", "Luna", R.drawable.memory_moon),
            MemoryWord("tree", "Zä̌", "Árbol", R.drawable.memory_tree)
        )
        val animalWords = listOf(
            MemoryWord("bee", "Gáne", "Abeja", R.drawable.memory_bee),
            MemoryWord("spider", "Méxe", "Araña", R.drawable.memory_spider),
            MemoryWord("eagle", "Nxù̱ni", "Águila", R.drawable.memory_eagle),
            MemoryWord("hare", "Dé", "Liebre", R.drawable.memory_hare),
            MemoryWord("horse", "Tó̱ge", "Caballo", R.drawable.memory_horse),
            MemoryWord("donkey", "’Ǎxnu", "Asno", R.drawable.memory_donkey),
            MemoryWord("sheep", "Dé̱ti", "Borrego", R.drawable.memory_sheep)
        )
        val everydayWords = listOf(
            MemoryWord("pillow", "Nhǚni", "Almohada", R.drawable.memory_pillow),
            MemoryWord("ring", "Mphòxo", "Anillo", R.drawable.memory_ring),
            MemoryWord("earring", "Ts’ú̱ngü", "Arete", R.drawable.memory_earring),
            MemoryWord("pumpkin", "Mǚ", "Calabaza", R.drawable.memory_pumpkin),
            MemoryWord("sweetPotato", "Bó̱’kw’ä", "Camote", R.drawable.memory_sweet_potato),
            MemoryWord("atole", "T’èi", "Atole", R.drawable.memory_atole),
            MemoryWord("star", "Rà tsó̱", "La estrella", R.drawable.memory_star),
            MemoryWord("broom", "Rà ’màxi", "La escoba", R.drawable.memory_broom),
            MemoryWord("key", "Ntshóki", "Llave", R.drawable.memory_key),
            MemoryWord("rainbow", "’Mè̱khu̱ni", "Arcoíris", R.drawable.memory_rainbow)
        )
    }
}

internal val LEVEL_NAMES = listOf("One", "Two", "Three", "Four", "Five")

/** Palabra de la sopa de letras. */
data class WordSearchWord(val id: String, val yuhmu: String, val spanish: String) {
    /** Letras en mayúscula; cada vocal con sus marcas ocupa una sola casilla. */
    val letters: List<String> get() = graphemes(yuhmu.uppercase(Locale.ROOT))
}

/** Separa un texto en "letras visibles" (letra + sus acentos y marcas combinantes). */
fun graphemes(text: String): List<String> {
    val iterator = BreakIterator.getCharacterInstance(Locale.ROOT)
    iterator.setText(text)
    val result = mutableListOf<String>()
    var start = iterator.first()
    var end = iterator.next()
    while (end != BreakIterator.DONE) {
        result += text.substring(start, end)
        start = end
        end = iterator.next()
    }
    return result
}

/** Niveles de la sopa de letras (WordSearchLevel.swift). */
enum class WordSearchLevel(val number: Int, val coins: Int, val points: Int) {
    ONE(1, 300, 500),
    TWO(2, 450, 1_400),
    THREE(3, 700, 2_500);

    /** Casillas por lado: 6, 8 y 10. */
    val side: Int get() = number * 2 + 4

    val key: String get() = "wordSearchLevel" + LEVEL_NAMES[number - 1]

    val instructions: String
        get() = if (this == THREE)
            "Busca en horizontal, vertical y diagonal. Algunas palabras están al revés."
        else "Busca en horizontal y vertical."

    val words: List<WordSearchWord>
        get() {
            if (this == ONE) {
                return listOf(
                    WordSearchWord("water", "Déhe", "Agua"),
                    WordSearchWord("flower", "Dò̱ni", "Flor"),
                    WordSearchWord("moon", "Zä̀nä", "Luna")
                )
            }
            val ids = if (this == TWO) listOf("bee", "spider", "eagle", "horse", "sheep")
            else listOf("pillow", "ring", "key", "atole", "pumpkin", "donkey", "rainbow")
            val vocabulary = MemoryLevel.TWO.words + MemoryLevel.THREE.words
            return ids.mapNotNull { id ->
                vocabulary.firstOrNull { it.id == id }?.let { WordSearchWord(it.id, it.yuhmu, it.spanish) }
            }
        }

    /** Recorridos sin choques (índices de casilla), iguales que iOS. */
    val paths: List<List<Int>>
        get() = when (this) {
            ONE -> listOf(listOf(7, 8, 9, 10), listOf(5, 11, 17, 23), listOf(25, 26, 27, 28))
            TWO -> listOf(
                listOf(1, 2, 3, 4), listOf(15, 23, 31, 39), listOf(25, 26, 27, 28, 29),
                listOf(24, 32, 40, 48), listOf(58, 59, 60, 61)
            )
            THREE -> listOf(
                listOf(0, 11, 22, 33, 44), listOf(9, 8, 7, 6, 5, 4),
                listOf(19, 29, 39, 49, 59, 69, 79), listOf(90, 81, 72, 63),
                listOf(96, 97), listOf(66, 56, 46, 36, 26), listOf(89, 88, 87, 86, 85, 84, 83, 82)
            )
        }
}

/** Lo que entregó un nivel del laberinto al ganarlo. */
data class MazeReward(val baseCoins: Int, val collectedCoins: Int, val points: Int) {
    val totalCoins: Int get() = baseCoins + collectedCoins

    companion object {
        val NONE = MazeReward(0, 0, 0)
    }
}

/** Logro del perfil (ProfileAchievement.swift). */
data class ProfileAchievement(
    val id: String,
    val title: String,
    val detail: String,
    @DrawableRes val image: Int,
    val current: Int,
    val target: Int
) {
    val isCompleted: Boolean get() = current >= target
    val displayedProgress: Int get() = current.coerceIn(0, target)
}

/** Tiempo como 00:00 (redondeado hacia arriba, igual que iOS). */
fun gameTimeText(seconds: Double): String {
    val value = maxOf(0, kotlin.math.ceil(seconds).toInt())
    return "%02d:%02d".format(value / 60, value % 60)
}
