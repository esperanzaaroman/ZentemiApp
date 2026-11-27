package com.zentemi.app.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset
import com.zentemi.app.R
import kotlin.math.floor

/** Lado del laberinto por el que sale una salida. */
enum class MazeExitSide { TOP, LEFT, RIGHT, BOTTOM }

data class MazeExit(
    val id: Int,
    val column: Int,
    val row: Int = 0,
    val side: MazeExitSide = MazeExitSide.TOP,
    val translation: String,
    val isCorrect: Boolean
) {
    val center: Offset get() = Offset(column + 0.5f, row + 0.5f)

    /** Dónde se regresa a la mascota tras una salida equivocada. */
    val returnPosition: Offset
        get() = when (side) {
            MazeExitSide.TOP -> Offset(center.x, center.y + 1)
            MazeExitSide.BOTTOM -> Offset(center.x, center.y - 1)
            MazeExitSide.LEFT -> Offset(center.x + 1, center.y)
            MazeExitSide.RIGHT -> Offset(center.x - 1, center.y)
        }
}

data class MazeCoin(val id: Int, val position: Offset)

enum class MazeTheme { GRASS, STONE, BRICK, ICE, LAVA }

/**
 * Los cinco niveles del laberinto, copiados de MazeLevel.swift.
 * "#" es pared y "." camino; las coordenadas van en casillas.
 * Nota: los niveles 2 a 5 aún tienen textos provisionales en iOS
 * ("*yuhmu*", "salidaBuena", "palabraX"...); se dejan iguales para que
 * ambas apps cambien a la vez cuando tengan la traducción.
 */
data class MazeLevel(
    val id: Int,
    val phrase: String,
    @DrawableRes val background: Int,
    val theme: MazeTheme,
    val map: List<String>,
    val start: Offset,
    val exits: List<MazeExit>,
    val coins: List<MazeCoin>,
    val followsPlayerVertically: Boolean = false,
    val startsInside: Boolean = false
) {
    val columns: Int get() = map.firstOrNull()?.length ?: 0
    val rows: Int get() = map.size

    fun isWall(column: Int, row: Int): Boolean {
        if (column !in 0 until columns || row !in 0 until rows) return true
        return map[row][column] == '#'
    }

    /** La mascota (un círculo de radio [PET_RADIUS]) cabe en ese punto sin tocar paredes. */
    fun canOccupy(point: Offset): Boolean {
        val radius = PET_RADIUS
        if (point.x < radius || point.y < radius || point.x > columns - radius || point.y > rows - radius) return false
        for (row in floor(point.y - radius).toInt()..floor(point.y + radius).toInt()) {
            for (column in floor(point.x - radius).toInt()..floor(point.x + radius).toInt()) {
                if (!isWall(column, row)) continue
                val closestX = point.x.coerceIn(column.toFloat(), (column + 1).toFloat())
                val closestY = point.y.coerceIn(row.toFloat(), (row + 1).toFloat())
                val dx = point.x - closestX
                val dy = point.y - closestY
                if (dx * dx + dy * dy < radius * radius) return false
            }
        }
        return true
    }

    companion object {
        const val PET_RADIUS = 0.35f

        private fun coins(vararg points: Pair<Float, Float>) =
            points.mapIndexed { index, (x, y) -> MazeCoin(index, Offset(x, y)) }

        val ONE = MazeLevel(
            id = 1, phrase = "Buenos días", background = R.drawable.maze_grass_background, theme = MazeTheme.GRASS,
            map = listOf(
                "#.###.###.#",
                "#...#.....#",
                "###.#.###.#",
                "#...#...#.#",
                "#.#####.#.#",
                "#.....#...#",
                "#.###.###.#",
                "#...#.....#",
                "###.#####.#",
                "#...#.....#",
                "#.###.###.#",
                "#.....#...#",
                "#####.#####"
            ),
            start = Offset(5.5f, 12.5f),
            exits = listOf(
                MazeExit(0, 1, translation = "Nzengwa", isCorrect = false),
                MazeExit(1, 5, translation = "Té xki hats’i", isCorrect = true),
                MazeExit(2, 9, translation = "Ra hyadi", isCorrect = false)
            ),
            coins = emptyList()
        )

        val TWO = MazeLevel(
            id = 2, phrase = "Necesito ayuda", background = R.drawable.maze_stone_background, theme = MazeTheme.STONE,
            map = listOf(
                "###.#######.###",
                "#...#.....#...#",
                "#.#.#.###.#.#.#",
                "#.#...#...#.#.#",
                "#.#####.###.#.#",
                "....#...#...#.#",
                "###.#.#.#.###.#",
                "#...#.#...#...#",
                "#.###.#.###.#.#",
                "#.....#.....#.#",
                "#.#####.###.#.#",
                "#...#...#......",
                "###.#.###.###.#",
                "#...#.....#...#",
                "#.###.#####.#.#",
                "#.......#.....#",
                "#######.#######"
            ),
            start = Offset(7.5f, 16.5f),
            exits = listOf(
                MazeExit(0, 3, translation = "Nzengwa", isCorrect = false),
                MazeExit(1, 11, translation = "Té xki hats’i", isCorrect = false),
                MazeExit(2, 0, 5, MazeExitSide.LEFT, "Ra hyadi", false),
                // Texto provisional (igual que iOS), pendiente de traducción definitiva.
                MazeExit(3, 14, 11, MazeExitSide.RIGHT, "*yuhmu*", true)
            ),
            coins = coins(1.5f to 1.5f, 13.5f to 3.5f, 1.5f to 13.5f, 9.5f to 15.5f, 7.5f to 9.5f)
        )

        val THREE = MazeLevel(
            id = 3, phrase = "palabraX", background = R.drawable.maze_stone_background, theme = MazeTheme.BRICK,
            map = listOf(
                "#.#######.#",
                "#...#.....#",
                "###.#.###.#",
                "#...#...#.#",
                "#.#####.#.#",
                "#.....#...#",
                "#.###.###.#",
                "#...#.....#",
                "###.#####.#",
                "....#.....#",
                "#.###.###.#",
                "#.....#...#",
                "#.###.###.#",
                "#...#.....#",
                "###.#.###.#",
                "#...#...#.#",
                "#.#####.#.#",
                "#.....#...#",
                "#.###.###.#",
                "#...#.....#",
                "###.#####.#",
                "#...#.....#",
                "#.###.###.#",
                "#.....#....",
                "#.###.###.#",
                "#...#.....#",
                "###.#.###.#",
                "#...#...#.#",
                "#.#####.#.#",
                "#.....#...#",
                "#.###.###.#",
                "#.........#",
                "#####.#####"
            ),
            start = Offset(5.5f, 32.5f),
            exits = listOf(
                MazeExit(0, 1, translation = "salidaMal1", isCorrect = false),
                MazeExit(1, 9, translation = "salidaBuena", isCorrect = true),
                MazeExit(2, 0, 9, MazeExitSide.LEFT, "salidaMal2", false),
                MazeExit(3, 10, 23, MazeExitSide.RIGHT, "salidaMal3", false)
            ),
            coins = coins(
                1.5f to 31.5f, 9.5f to 29.5f, 3.5f to 25.5f, 7.5f to 23.5f, 1.5f to 21.5f,
                5.5f to 17.5f, 9.5f to 13.5f, 1.5f to 9.5f, 7.5f to 5.5f, 3.5f to 1.5f
            ),
            followsPlayerVertically = true
        )

        val FOUR = MazeLevel(
            id = 4, phrase = "palabraHielo", background = R.drawable.maze_ice_background, theme = MazeTheme.ICE,
            map = listOf(
                "#.#######.#",
                "#.#.......#",
                "#...#.#.###",
                "#.#.#.#...#",
                "#.#.#.###.#",
                "#.#.#...#.#",
                "#.#.###.#.#",
                "#.#...#.#.#",
                "#.###.#.#.#",
                "#...#.#.#.#",
                "###.#.#.#.#",
                "....#...#.#",
                "#.#####.#.#",
                "#.#.....#.#",
                "#.###.###.#",
                "#...#...#.#",
                "###.#.###.#",
                "#.#.#.#...#",
                "#.#.#.#.###",
                "#...#.#...#",
                "#.###.###.#",
                "#.#.....#.#",
                "#.#.#####.#",
                "#.#.#.....#",
                "#.###.###.#",
                "#...#.#...#",
                "###.#.#.###",
                "#.#...#...#",
                "#.#######.#",
                "#.....#....",
                "#####.#.###",
                "#.....#...#",
                "#.#######.#",
                "#...#...#.#",
                "#.#.#.#.#.#",
                "#.#...#...#",
                "#.#########",
                "#.#.......#",
                "#.###.###.#",
                "#.....#...#",
                "#########.#"
            ),
            start = Offset(5.5f, 21.5f),
            exits = listOf(
                MazeExit(0, 1, translation = "salidaMal1", isCorrect = false),
                MazeExit(1, 9, translation = "salidaMal2", isCorrect = false),
                MazeExit(2, 0, 11, MazeExitSide.LEFT, "salidaMal3", false),
                MazeExit(3, 10, 29, MazeExitSide.RIGHT, "salidaMal4", false),
                MazeExit(4, 9, 40, MazeExitSide.BOTTOM, "salidaBuena", true)
            ),
            coins = coins(
                9.5f to 3.5f, 9.5f to 5.5f, 9.5f to 7.5f, 3.5f to 9.5f, 1.5f to 13.5f,
                3.5f to 15.5f, 1.5f to 17.5f, 1.5f to 19.5f, 5.5f to 23.5f, 3.5f to 25.5f,
                1.5f to 27.5f, 5.5f to 31.5f, 1.5f to 33.5f, 1.5f to 35.5f, 9.5f to 37.5f
            ),
            followsPlayerVertically = true,
            startsInside = true
        )

        val FIVE = MazeLevel(
            id = 5, phrase = "palabraLava", background = R.drawable.maze_lava_background, theme = MazeTheme.LAVA,
            map = listOf(
                "#.#######.#",
                "#.......#.#",
                "#######.#.#",
                "#...#.#...#",
                "#.#.#.###.#",
                "#.#.....#.#",
                "#.###.###.#",
                "#.#...#...#",
                "#.###.#.###",
                "#...#.#.#.#",
                "#.#.#.#.#.#",
                "#.#.#.#.#.#",
                "###.#.#.#.#",
                "....#.#...#",
                "#.###.###.#",
                "#.#...#.#.#",
                "#.#.###.#.#",
                "#.#.....#.#",
                "#.#####.#.#",
                "#...#.#.#.#",
                "#.#.#.#.#.#",
                "#.#.#...#.#",
                "###.#.###.#",
                "#...#.#...#",
                "#.###.#.###",
                "#.#...#...#",
                "#.###.###.#",
                "#...#...#.#",
                "###.###.#.#",
                "#...#.#.#.#",
                "#.###.#.#.#",
                "#.#.....#.#",
                "#.#####.#.#",
                "#.....#.#.#",
                "#.###.###.#",
                "#.#.#.....#",
                "#.#.#######",
                "#.#.....#.#",
                "#.#.###.#.#",
                "#...#.#...#",
                "#####.###.#",
                "#.#.....#.#",
                "#.#.###.#.#",
                "#...#.#....",
                "#.###.#####",
                "#.#.......#",
                "#.#.###.#.#",
                "#.#.#...#.#",
                "#.#.#.###.#",
                "#.#.#...#.#",
                "#.#####.#.#",
                "#.......#.#",
                "#########.#",
                "#.......#.#",
                "#.###.#.#.#",
                "#.#...#.#.#",
                "###.#.###.#",
                "#...#.#...#",
                "#.#####.###",
                "#.........#",
                "#.#######.#"
            ),
            start = Offset(5.5f, 31.5f),
            exits = listOf(
                MazeExit(0, 1, translation = "salidaMal1", isCorrect = false),
                MazeExit(1, 9, translation = "salidaMal2", isCorrect = false),
                MazeExit(2, 0, 13, MazeExitSide.LEFT, "salidaMal3", false),
                MazeExit(3, 10, 43, MazeExitSide.RIGHT, "salidaMal4", false),
                MazeExit(4, 1, 60, MazeExitSide.BOTTOM, "salidaMal5", false),
                MazeExit(5, 9, 60, MazeExitSide.BOTTOM, "salidaBuena", true)
            ),
            coins = coins(
                7.5f to 3.5f, 9.5f to 5.5f, 7.5f to 9.5f, 7.5f to 11.5f, 9.5f to 15.5f,
                9.5f to 17.5f, 9.5f to 21.5f, 9.5f to 23.5f, 9.5f to 27.5f, 9.5f to 29.5f,
                9.5f to 33.5f, 3.5f to 35.5f, 5.5f to 39.5f, 1.5f to 41.5f, 3.5f to 45.5f,
                3.5f to 47.5f, 9.5f to 51.5f, 1.5f to 53.5f, 1.5f to 55.5f, 5.5f to 57.5f
            ),
            followsPlayerVertically = true,
            startsInside = true
        )

        val ALL = listOf(ONE, TWO, THREE, FOUR, FIVE)
    }
}
