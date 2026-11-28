package com.zentemi.app.data

/**
 * Lecciones 2 a 6 del camino (la 1, "Primeras frases", tiene su propio flujo).
 * Monedas, XP y claves guardadas iguales que AppState.swift
 * (completeFamilyLesson, completeNumbersLesson, ...).
 */
enum class PathLesson(
    /** Número en el camino de aprendizaje. */
    val number: Int,
    /** Prefijo de las claves guardadas (familyRewardClaimed, ...). */
    val key: String,
    val title: String,
    val introTitle: String,
    val description: String,
    val coins: Int,
    val xp: Int,
    val estimatedMinutes: Int
) {
    FAMILY(
        2, "family", "Familia", "Mi familia",
        "Conoce las palabras para referirte a los miembros de tu familia en Yuhmu.",
        900, 180, 5
    ),
    NUMBERS(
        3, "numbers", "Números", "Números",
        "Cuenta del uno al cinco en Yuhmu con hojas, flores y canastas.",
        1000, 200, 5
    ),
    COLORS(
        4, "colors", "Colores", "Colores",
        "Descubre cuatro colores en Yuhmu y ayuda a Maicito en su taller de pintura.",
        1100, 220, 5
    ),
    ANIMALS(
        5, "animals", "Animales", "Animales",
        "Explora con Maicito y aprende cómo se llaman cuatro animales en Yuhmu.",
        1200, 240, 5
    ),
    FOOD(
        6, "food", "Alimentos", "Alimentos",
        "Ve al mercado con Maicito: alimentos, una bebida y canastas para preparar.",
        1300, 260, 5
    );

    /** Todas tienen 5 ejercicios. */
    val exerciseCount: Int get() = 5

    /** Lección anterior en el camino (null = la anterior es "Primeras frases"). */
    val previous: PathLesson? get() = entries.getOrNull(ordinal - 1)

    val previousTitle: String get() = previous?.title ?: "Primeras frases"

    companion object {
        fun fromNumber(number: Int): PathLesson? = entries.firstOrNull { it.number == number }
    }
}

/** Recibo de una lección terminada: lo que se ganó y el XP con el que se empezó (para animar la barra). */
data class LessonReward(
    val coins: Int,
    val xp: Int,
    val startingXP: Int
)
