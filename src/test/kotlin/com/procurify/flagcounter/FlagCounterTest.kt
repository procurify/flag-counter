package com.procurify.flagcounter

import com.procurify.flagcounter.launchdarkly.FlagDetail
import com.procurify.flagcounter.launchdarkly.FlagMaintainer
import com.procurify.flagcounter.launchdarkly.FlagResponse
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

// TODO Add better testing for the FlagCounter
class FlagCounterTest {

    @Test
    fun `ensure that grouping by email groups flags for all provided email addresses`() {
        val alpha = TeamEmail("alpha@test.com")
        val beta = TeamEmail("beta@test.com")

        val alphaDetail1 = FlagDetail("ABC", FlagMaintainer(alpha.email))
        val alphaDetail2 = FlagDetail("DEF", FlagMaintainer(alpha.email))

        val teamsMap = mapOf(
                alpha to mockk<Messager>(),
                beta to mockk()
        )

        val flagResponse = FlagResponse(0, listOf(
                alphaDetail1,
                alphaDetail2,
                FlagDetail("GHI", FlagMaintainer("third@test.com")),
                FlagDetail("JKL", FlagMaintainer("third@test.com")),
                FlagDetail("MNO", FlagMaintainer("fourth@test.com")),
        ))

        val flagCounter = FlagCounter(
                totalMessager = mockk(),
                errorMessager = mockk(),
                flagReader = mockk(),
                teamMessagers = teamsMap
        )

        val actualMap = flagCounter.groupFlagsByMaintainer(flagResponse, teamsMap.keys)

        val expectedMap = mapOf(
                alpha to listOf(alphaDetail1, alphaDetail2),
                beta to listOf()
        )

        assertEquals(expectedMap, actualMap)
    }
}