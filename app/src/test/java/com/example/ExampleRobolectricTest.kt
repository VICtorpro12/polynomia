package com.example

import com.example.model.Polynomial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {
    @Test
    fun biquadratic_roots_areCorrect() {
        // x^4 - 5x^2 + 4 = 0 -> roots ±1, ±2
        val poly = Polynomial(listOf(1, 0, -5, 0, 4))
        val roots = poly.findIntegerRoots()
        assertTrue(roots.contains(1))
        assertTrue(roots.contains(-1))
        assertTrue(roots.contains(2))
        assertTrue(roots.contains(-2))
    }

    @Test
    fun polynomial_formatting_degree3() {
        val poly = Polynomial(listOf(1, -4, 1, 6))
        assertEquals("x³ - 4x² + x + 6 = 0", poly.toFormattedString())
    }
}
