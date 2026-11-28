package com.zentemi.app.state

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** La felicidad baja 1 punto cada 2 horas, hasta 10 por día (igual que iOS). */
@RunWith(RobolectricTestRunner::class)
class HappinessDecayTest {

    private class TestClock(var now: Long) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(now)
    }

    private val hour = 60L * 60 * 1000
    private lateinit var clock: TestClock
    private lateinit var state: AppState

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        app.getSharedPreferences("zentemi", 0).edit().clear().commit()
        clock = TestClock(1_800_000_000_000)
        state = AppState(app, clock) // empieza a contar al abrir
        state.happiness = 80
    }

    private fun after(hours: Long) {
        clock.now += hours * hour
        state.updateHappinessDecay()
    }

    @Test
    fun losesOnePointEveryTwoHours() {
        after(1)
        assertEquals(80, state.happiness)
        after(1)
        assertEquals(79, state.happiness)
        after(5) // 7 h en total: 3 puntos
        assertEquals(77, state.happiness)
    }

    @Test
    fun losesAtMostTenPerDay() {
        after(23)
        assertEquals(70, state.happiness)
        after(1) // empieza el día 2
        assertEquals(70, state.happiness)
        after(72) // 3 días más
        assertEquals(40, state.happiness)
    }

    @Test
    fun neverGoesBelowZeroAndDoesNotOweLaterPoints() {
        state.happiness = 3
        after(48)
        assertEquals(0, state.happiness)
        state.happiness = 50 // comió
        after(1)
        assertEquals(50, state.happiness)
    }

    @Test
    fun clockGoingBackDoesNothing() {
        after(4)
        assertEquals(78, state.happiness)
        clock.now -= 10 * hour
        state.updateHappinessDecay()
        assertEquals(78, state.happiness)
    }
}
