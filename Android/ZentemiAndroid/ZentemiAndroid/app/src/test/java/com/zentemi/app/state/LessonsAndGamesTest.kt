package com.zentemi.app.state

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.test.core.app.ApplicationProvider
import com.zentemi.app.data.MazeLevel
import com.zentemi.app.data.MemoryLevel
import com.zentemi.app.data.PathLesson
import com.zentemi.app.data.WordSearchLevel
import com.zentemi.app.data.graphemes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Reglas de las lecciones 2 a 6, los juegos por niveles y los logros (iguales a iOS). */
@RunWith(RobolectricTestRunner::class)
class LessonsAndGamesTest {

    private lateinit var app: Application
    private lateinit var state: AppState

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        app.getSharedPreferences("zentemi", 0).edit().clear().commit()
        state = AppState(app)
    }

    @Test
    fun lessonsUnlockInOrder() {
        assertFalse(state.isLessonUnlocked(PathLesson.FAMILY))
        assertNull(state.completeLesson(PathLesson.FAMILY, 5, 60.0))

        state.completeGreetingsLesson()
        assertTrue(state.isLessonUnlocked(PathLesson.FAMILY))
        assertFalse(state.isLessonUnlocked(PathLesson.NUMBERS))

        state.completeLesson(PathLesson.FAMILY, 4, 90.0)
        assertTrue(state.isLessonUnlocked(PathLesson.NUMBERS))
        assertEquals(2, state.pathLessonsCompleted)
        assertEquals(3, state.unlockedPathLesson)
    }

    @Test
    fun lessonRewardIsPaidOnlyOnce() {
        state.completeGreetingsLesson()
        val coinsBefore = state.coins
        val xpBefore = state.totalXP

        val first = state.completeLesson(PathLesson.FAMILY, 3, 100.0)
        assertNotNull(first)
        assertEquals(900, first!!.coins)
        assertEquals(180, first.xp)
        assertEquals(xpBefore, first.startingXP)
        assertEquals(coinsBefore + 900, state.coins)

        val again = state.completeLesson(PathLesson.FAMILY, 5, 50.0)
        assertEquals(0, again!!.coins)
        assertEquals(0, again.xp)
        assertEquals(coinsBefore + 900, state.coins)
        assertEquals(5, state.lessonBestCorrectAnswers(PathLesson.FAMILY))
        assertEquals(50.0, state.lessonBestSecondsOf(PathLesson.FAMILY), 0.01)
    }

    @Test
    fun lessonCompletedOnAnotherPhoneIsNotPaidAgain() {
        // El camino viaja por Firestore; el premio local no.
        state.completeGreetingsLesson()
        state.pathLessonsCompleted = 2
        assertTrue(state.isLessonCompleted(PathLesson.FAMILY))
        val reward = state.completeLesson(PathLesson.FAMILY, 5, 30.0)
        assertEquals(0, reward!!.coins)
    }

    @Test
    fun invalidResultsAreRejected() {
        state.completeGreetingsLesson()
        assertNull(state.completeLesson(PathLesson.FAMILY, 6, 10.0))
        assertNull(state.completeLesson(PathLesson.FAMILY, -1, 10.0))
        assertNull(state.completeLesson(PathLesson.FAMILY, 2, Double.NaN))
    }

    @Test
    fun memoryLevelsUnlockAndPayOnce() {
        assertTrue(state.memoryUnlocked(MemoryLevel.ONE))
        assertFalse(state.memoryUnlocked(MemoryLevel.TWO))
        assertFalse(state.completeMemory(MemoryLevel.TWO, 20.0))

        val coins = state.coins
        assertTrue(state.completeMemory(MemoryLevel.ONE, 40.0))
        assertEquals(coins + 300, state.coins)
        assertEquals(500, state.gamePoints)
        assertTrue(state.memoryUnlocked(MemoryLevel.TWO))

        assertFalse(state.completeMemory(MemoryLevel.ONE, 30.0))
        assertEquals(500, state.gamePoints)
        assertEquals(30.0, state.memoryBestSeconds(MemoryLevel.ONE), 0.01)
    }

    @Test
    fun wordSearchLevelsUnlockInOrder() {
        assertTrue(state.completeWordSearch(WordSearchLevel.ONE, 12.0))
        assertTrue(state.wordSearchUnlocked(WordSearchLevel.TWO))
        assertFalse(state.wordSearchUnlocked(WordSearchLevel.THREE))
    }

    @Test
    fun mazeCoinsAreClaimedOnlyOnce() {
        state.completeMaze(1, 30.0, emptySet(), 0)
        val coins = state.coins
        val first = state.completeMaze(2, 60.0, setOf(0, 2), 5)
        assertEquals(450, first.baseCoins)
        assertEquals(20, first.collectedCoins)
        assertEquals(coins + 470, state.coins)

        val second = state.completeMaze(2, 50.0, setOf(0, 1, 2), 5)
        assertEquals(0, second.baseCoins)
        assertEquals(10, second.collectedCoins)
        assertEquals(0b111, state.mazeClaimedCoinMask(2))
    }

    @Test
    fun mazeLevelsStartOnAPath() {
        MazeLevel.ALL.forEach { level ->
            assertTrue("Nivel ${level.id}: inicio", level.canOccupy(level.start))
            level.coins.forEach { coin ->
                assertTrue("Nivel ${level.id}: moneda ${coin.id}", level.canOccupy(coin.position))
            }
            assertEquals("Nivel ${level.id}: una salida correcta", 1, level.exits.count { it.isCorrect })
        }
        assertFalse(MazeLevel.ONE.canOccupy(Offset(0.5f, 0.5f)))
    }

    @Test
    fun wordSearchWordsFitTheirPaths() {
        WordSearchLevel.entries.forEach { level ->
            val cells = level.paths.flatten()
            assertEquals("Nivel ${level.number}: sin choques", cells.size, cells.toSet().size)
            level.words.zip(level.paths).forEach { (word, path) ->
                assertEquals("Nivel ${level.number}: ${word.yuhmu}", word.letters.size, path.size)
                path.forEach { assertTrue(it in 0 until level.side * level.side) }
            }
            assertEquals(level.paths.size, level.words.size)
        }
    }

    @Test
    fun accentedLettersStayInOneCell() {
        assertEquals(listOf("D", "Ò̱", "N", "I"), graphemes("DÒ̱NI"))
        assertEquals(4, graphemes("Zä̀nä".uppercase()).size)
    }

    @Test
    fun achievementsTrackEarnedCoinsFeedingsAndStreak() {
        val earned = state.lifetimeEarnedCoins
        state.completeGreetingsLesson()
        assertEquals(earned + 700, state.lifetimeEarnedCoins)
        // Gastar no resta avance
        state.coins -= 500
        assertEquals(earned + 700, state.lifetimeEarnedCoins)

        assertTrue(state.registerMeal())
        assertEquals(1, state.totalFeedings)
        assertTrue(state.bestAchievementStreak >= 1)

        val lessons = state.profileAchievements.first { it.id == "lessons" }
        assertEquals(1, lessons.current)
        assertEquals(6, lessons.target)
    }
}
