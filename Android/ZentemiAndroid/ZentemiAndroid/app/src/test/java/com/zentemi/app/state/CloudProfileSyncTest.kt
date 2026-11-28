package com.zentemi.app.state

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.zentemi.app.data.ChildProfile
import com.zentemi.app.data.MemoryLevel
import com.zentemi.app.data.PathLesson
import com.zentemi.app.data.WordSearchLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Lecciones 2 a 6, juegos por niveles y logros viajan en el perfil de Firestore. */
@RunWith(RobolectricTestRunner::class)
class CloudProfileSyncTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        clearPhone()
    }

    private fun clearPhone() {
        app.getSharedPreferences("zentemi", 0).edit().clear().commit()
    }

    /** Juega en un "teléfono" y devuelve el perfil que se subiría a la nube. */
    private fun profileWithProgress(): ChildProfile {
        val phone = AppState(app)
        phone.completeGreetingsLesson()
        phone.completeLesson(PathLesson.FAMILY, 4, 80.0)
        phone.completeMemory(MemoryLevel.ONE, 42.0)
        phone.completeWordSearch(WordSearchLevel.ONE, 55.0)
        phone.completeMaze(1, 30.0, emptySet(), 0)
        phone.completeMaze(2, 61.0, setOf(0, 2), 3)
        phone.registerMeal()
        return phone.buildChildProfile(id = "kid")
    }

    @Test
    fun progressTravelsToAnotherPhone() {
        val profile = profileWithProgress()
        clearPhone()
        val other = AppState(app)
        other.applyChildProfile(profile)

        assertTrue(other.isLessonCompleted(PathLesson.FAMILY))
        assertTrue(other.isLessonUnlocked(PathLesson.NUMBERS))
        assertEquals(4, other.lessonBestCorrectAnswers(PathLesson.FAMILY))
        assertEquals(80.0, other.lessonBestSecondsOf(PathLesson.FAMILY), 0.01)

        assertTrue(other.memoryCompleted(MemoryLevel.ONE))
        assertTrue(other.memoryUnlocked(MemoryLevel.TWO))
        assertEquals(42.0, other.memoryBestSeconds(MemoryLevel.ONE), 0.01)
        assertTrue(other.wordSearchCompleted(WordSearchLevel.ONE))
        assertTrue(other.mazeCompleted(2))
        assertTrue(other.mazeUnlocked(3))
        assertEquals(0b101, other.mazeClaimedCoinMask(2))

        assertEquals(profile.gamePoints, other.gamePoints)
        assertEquals(1, other.totalFeedings)
        assertEquals(profile.lifetimeEarnedCoins, other.lifetimeEarnedCoins)
        // Lo que se recibió es igual a lo que se volvería a subir.
        assertEquals(profile, other.buildChildProfile(id = "kid"))
    }

    @Test
    fun rewardsAreNotPaidAgainOnTheOtherPhone() {
        val profile = profileWithProgress()
        clearPhone()
        val other = AppState(app)
        other.applyChildProfile(profile)
        val coins = other.coins

        assertFalse(other.completeMemory(MemoryLevel.ONE, 10.0))
        val maze = other.completeMaze(2, 20.0, setOf(0, 2), 3)
        assertEquals(0, maze.baseCoins + maze.collectedCoins)
        assertEquals(coins, other.coins)
    }

    @Test
    fun oldProfilesWithoutTheNewFieldsKeepPhoneProgress() {
        val phone = AppState(app)
        phone.completeGreetingsLesson()
        phone.completeMemory(MemoryLevel.ONE, 42.0)
        val points = phone.gamePoints

        // Perfil creado por iOS o por una versión anterior de Android.
        phone.applyChildProfile(ChildProfile(id = "kid", pathLessonsCompleted = 1))

        assertTrue(phone.memoryCompleted(MemoryLevel.ONE))
        assertEquals(points, phone.gamePoints)
    }
}
