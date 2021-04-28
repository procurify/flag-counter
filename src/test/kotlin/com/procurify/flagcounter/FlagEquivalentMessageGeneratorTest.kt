package com.procurify.flagcounter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FlagEquivalentMessageGeneratorTest {

    @Test
    fun `ensure that a number out of bounds returns a generic message`() {
        val expected = "That's 10^903 different configurations!"
        val actual = FlagEquivalentMessageGenerator.getNearestNumberMessage(3000)
        assertEquals(expected, actual)
    }

    @Test
    fun `ensure that exact matches are given preference`() {
        val expected = "That's as many configurations as the number of ways to order the cards in a 52-card deck!"
        val actual = FlagEquivalentMessageGenerator.getNearestNumberMessage(223)
        assertEquals(expected, actual)
    }

    @Test
    fun `ensure that two messages are properly joined when a match isn't found`() {
        val expected = "That's as many configurations as the possible combinations for the Megaminx multiplied by the largest known prime factor found by ECM factorization!"
        val actual = FlagEquivalentMessageGenerator.getNearestNumberMessage(466)
        assertEquals(expected, actual)
    }
}