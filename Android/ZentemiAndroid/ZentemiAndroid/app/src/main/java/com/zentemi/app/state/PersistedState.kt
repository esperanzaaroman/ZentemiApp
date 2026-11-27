package com.zentemi.app.state

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Estado de Compose que además se guarda en SharedPreferences con la clave
 * "zentemi.<key>". Equivalente a los `didSet { save(...) }` de AppState.swift.
 */
class PersistedState<T>(
    private val prefs: SharedPreferences,
    private val key: String,
    private val default: T,
    private val read: SharedPreferences.(String, T) -> T,
    private val write: SharedPreferences.Editor.(String, T) -> Unit,
    private val onChange: (T) -> Unit = {}
) : ReadWriteProperty<Any?, T> {

    private val fullKey = "zentemi.$key"
    private val state: MutableState<T> = mutableStateOf(
        if (prefs.contains(fullKey)) prefs.read(fullKey, default) else default
    )

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = state.value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        state.value = value
        prefs.edit().apply { write(fullKey, value) }.apply()
        onChange(value)
    }

    /** Vuelve al valor inicial sin tocar SharedPreferences (se borra aparte). */
    fun reset() {
        state.value = default
    }
}

fun SharedPreferences.persistedInt(key: String, default: Int) =
    PersistedState(this, key, default, { k, d -> getInt(k, d) }, { k, v -> putInt(k, v) })

fun SharedPreferences.persistedFloat(key: String, default: Float, onChange: (Float) -> Unit = {}) =
    PersistedState(this, key, default, { k, d -> getFloat(k, d) }, { k, v -> putFloat(k, v) }, onChange)

fun SharedPreferences.persistedBoolean(key: String, default: Boolean) =
    PersistedState(this, key, default, { k, d -> getBoolean(k, d) }, { k, v -> putBoolean(k, v) })

fun SharedPreferences.persistedString(key: String, default: String) =
    PersistedState(this, key, default, { k, d -> getString(k, d) ?: d }, { k, v -> putString(k, v) })

fun SharedPreferences.persistedLong(key: String, default: Long?) =
    PersistedState(
        this, key, default,
        { k, d -> getLong(k, d ?: 0L) },
        { k, v -> if (v == null) remove(k) else putLong(k, v) }
    )
