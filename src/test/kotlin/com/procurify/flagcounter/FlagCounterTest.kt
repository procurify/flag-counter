package com.procurify.flagcounter

import com.procurify.flagcounter.launchdarkly.*
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

// TODO Add better testing for the FlagCounter
class FlagCounterTest {

    @Test
    fun `ensure that grouping by email groups flags for all provided email addresses`() {
        val alpha = TeamEmail("alpha@test.com")
        val beta = TeamEmail("beta@test.com")

        val alphaMaintainer = Owner("", alpha.email)

        val alphaDetail1 = FlagDetail("ABC", alphaMaintainer, Status.ACTIVE)
        val alphaDetail2 = FlagDetail("DEF", alphaMaintainer, Status.ACTIVE)

        val teamsMap = mapOf(
                alpha to mockk<Messager>(),
                beta to mockk()
        )

        val flagResponse = listOf(
                alphaDetail1,
                alphaDetail2,
                FlagDetail("GHI", Owner("", "third@test.com"), Status.ACTIVE),
                FlagDetail("JKL", Owner("", "third@test.com"), Status.ACTIVE),
                FlagDetail("MNO", Owner("", "fourth@test.com"), Status.ACTIVE),
        )

        val flagCounter = FlagCounter(
                totalMessager = mockk(),
                errorMessager = mockk(),
                flagReader = mockk(),
                teamMessagers = teamsMap
        )

        val actualMap = flagCounter.groupFlagsByOwner(flagResponse, teamsMap.keys)

        val expectedMap = mapOf(
                alphaMaintainer to listOf(alphaDetail1, alphaDetail2)
        )

        assertEquals(expectedMap, actualMap)
    }
}