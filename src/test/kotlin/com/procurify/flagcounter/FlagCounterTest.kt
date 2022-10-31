package com.procurify.flagcounter

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// TODO Add better testing for the FlagCounter
class FlagCounterTest {

    @Test
    fun `ensure that grouping by email groups flags for all provided email addresses`() {
        val creationDate = System.currentTimeMillis()

        val alpha = TeamIdentifier("alpha@test.com")
        val beta = TeamIdentifier("beta@test.com")

        val alphaMaintainer = Owner("", alpha.id)

        val alphaDetail1 = FlagDetail("ABC", alphaMaintainer, Status.ACTIVE, creationDate)
        val alphaDetail2 = FlagDetail("DEF", alphaMaintainer, Status.ACTIVE, creationDate)

        val teamsMap = mapOf(
                alpha to mockk<Messager>(),
                beta to mockk()
        )

        val flagResponse = listOf(
                alphaDetail1,
                alphaDetail2,
                FlagDetail("GHI", Owner("", "third@test.com"), Status.ACTIVE, creationDate),
                FlagDetail("JKL", Owner("", "third@test.com"), Status.ACTIVE, creationDate),
                FlagDetail("MNO", Owner("", "fourth@test.com"), Status.ACTIVE, creationDate),
        )

        val flagCounter = FlagCounter(
                totalMessager = mockk(),
                errorMessager = mockk(),
                flagReader = mockk(),
                teamMessagers = teamsMap
        )

        val actualMap = flagCounter.groupFlagsByOwner(flagResponse, teamsMap.keys)

        val expectedMap = mapOf(
                alpha to listOf(alphaDetail1, alphaDetail2)
        )

        assertEquals(expectedMap, actualMap)
    }
}