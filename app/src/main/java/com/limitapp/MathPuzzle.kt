package com.limitapp

import kotlin.random.Random

enum class Difficulty(val label: String) {
    FACIL("Fácil"),
    MEDIO("Medio"),
    DIFICIL("Difícil"),
}

data class MathPuzzle(val question: String, val answer: Int)

/** Genera acertijos aleatorios cuya solución es siempre un número entero no negativo. */
object PuzzleGenerator {

    fun generate(difficulty: Difficulty, random: Random = Random.Default): MathPuzzle {
        val builders = when (difficulty) {
            Difficulty.FACIL -> easy
            Difficulty.MEDIO -> medium
            Difficulty.DIFICIL -> hard
        }
        return builders.random(random)(random)
    }

    private val easy: List<(Random) -> MathPuzzle> = listOf(
        { r ->
            val a = r.nextInt(2, 21); val b = r.nextInt(2, 21)
            MathPuzzle("$a + $b", a + b)
        },
        { r ->
            val a = r.nextInt(10, 31); val b = r.nextInt(1, a)
            MathPuzzle("$a − $b", a - b)
        },
        { r ->
            val a = r.nextInt(2, 10); val b = r.nextInt(2, 10)
            MathPuzzle("$a × $b", a * b)
        },
    )

    private val medium: List<(Random) -> MathPuzzle> = listOf(
        { r ->
            val a = r.nextInt(10, 100); val b = r.nextInt(10, 100)
            MathPuzzle("$a + $b", a + b)
        },
        { r ->
            val a = r.nextInt(30, 100); val b = r.nextInt(10, a)
            MathPuzzle("$a − $b", a - b)
        },
        { r ->
            val a = r.nextInt(3, 13); val b = r.nextInt(3, 13)
            MathPuzzle("$a × $b", a * b)
        },
        { r ->
            val b = r.nextInt(2, 13); val q = r.nextInt(2, 13)
            MathPuzzle("${b * q} ÷ $b", q)
        },
        { r ->
            val a = r.nextInt(1, 21); val b = r.nextInt(2, 10); val c = r.nextInt(2, 10)
            MathPuzzle("$a + $b × $c", a + b * c)
        },
        { r ->
            val a = r.nextInt(2, 6); val x = r.nextInt(1, 11); val b = r.nextInt(1, 21)
            MathPuzzle("Si ${a}x + $b = ${a * x + b}\n¿cuánto vale x?", x)
        },
        { r ->
            val start = r.nextInt(2, 21); val step = r.nextInt(3, 16)
            val terms = (0 until 4).map { start + it * step }
            MathPuzzle("¿Qué número sigue?\n${terms.joinToString(", ")}, ?", start + 4 * step)
        },
    )

    private val hard: List<(Random) -> MathPuzzle> = listOf(
        { r ->
            val a = r.nextInt(11, 26); val b = r.nextInt(3, 10); val c = r.nextInt(1, 31)
            MathPuzzle("$a × $b − $c", a * b - c)
        },
        { r ->
            val a = r.nextInt(5, 31); val b = r.nextInt(5, 31); val c = r.nextInt(3, 10)
            MathPuzzle("($a + $b) × $c", (a + b) * c)
        },
        { r ->
            val a = r.nextInt(11, 21); val b = r.nextInt(1, 51)
            MathPuzzle("$a² + $b", a * a + b)
        },
        { r ->
            val b = r.nextInt(6, 20); val q = r.nextInt(11, 31)
            MathPuzzle("${b * q} ÷ $b", q)
        },
        { r ->
            val a = r.nextInt(3, 10); val x = r.nextInt(5, 21); val b = r.nextInt(1, 15)
            MathPuzzle("Si ${a}x − $b = ${a * x - b}\n¿cuánto vale x?", x)
        },
        { r ->
            val k = r.nextInt(1, 8); val c = r.nextInt(0, 10)
            val terms = (0 until 4).map { (k + it) * (k + it) + c }
            MathPuzzle("¿Qué número sigue?\n${terms.joinToString(", ")}, ?", (k + 4) * (k + 4) + c)
        },
    )
}
