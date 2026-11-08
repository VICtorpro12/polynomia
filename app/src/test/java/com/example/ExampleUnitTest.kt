package com.example

import com.example.model.Polynomial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun polynomial_evaluatesCorrectly() {
        // P(x) = x^3 - 4x^2 + x + 6
        // Roots are -1, 2, 3
        val poly = Polynomial(listOf(1, -4, 1, 6))
        assertEquals(0.0, poly.evaluate(2.0), 0.001)
        assertEquals(0.0, poly.evaluate(-1.0), 0.001)
        assertEquals(0.0, poly.evaluate(3.0), 0.001)
        assertEquals(6.0, poly.evaluate(0.0), 0.001)
    }

    @Test
    fun polynomial_findsIntegerRoots() {
        val poly = Polynomial(listOf(1, -4, 1, 6))
        val roots = poly.findIntegerRoots()
        assertTrue(roots.contains(2))
        assertTrue(roots.contains(-1))
        assertTrue(roots.contains(3))
    }

    @Test
    fun polynomial_divisorsCalculated() {
        val poly = Polynomial(listOf(1, 0, -5, 0, 4)) // x^4 - 5x^2 + 4
        val divisors = poly.getPossibleDivisors()
        assertTrue(divisors.contains(1))
        assertTrue(divisors.contains(-1))
        assertTrue(divisors.contains(2))
        assertTrue(divisors.contains(-2))
        assertTrue(divisors.contains(4))
        assertTrue(divisors.contains(-4))
    }
}
