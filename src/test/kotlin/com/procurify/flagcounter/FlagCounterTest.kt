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

        val alphaMaintainer = FlagMaintainer("", alpha.email)

        val alphaDetail1 = FlagCounter.FlagDetailAndStatus("ABC", alphaMaintainer, Status.ACTIVE)
        val alphaDetail2 = FlagCounter.FlagDetailAndStatus("DEF", alphaMaintainer, Status.NEW)

        val teamsMap = mapOf(
                alpha to mockk<Messager>(),
                beta to mockk()
        )

        val flagResponse = listOf(
                alphaDetail1,
                alphaDetail2,
                FlagCounter.FlagDetailAndStatus("GHI", FlagMaintainer("", "third@test.com"), Status.LAUNCHED),
                FlagCounter.FlagDetailAndStatus("JKL", FlagMaintainer("", "third@test.com"), Status.LAUNCHED),
                FlagCounter.FlagDetailAndStatus("MNO", FlagMaintainer("", "fourth@test.com"), Status.LAUNCHED),
        )

        val flagCounter = FlagCounter(
                totalMessager = mockk(),
                errorMessager = mockk(),
                flagReader = mockk(),
                teamMessagers = teamsMap
        )

        val actualMap = flagCounter.groupFlagsByMaintainer(flagResponse, teamsMap.keys)

        val expectedMap = mapOf(
                alphaMaintainer to listOf(alphaDetail1, alphaDetail2)
        )

        assertEquals(expectedMap, actualMap)
    }

    @Test
    fun `ensure that flags and flag statuses are zipped together`() {
        val flagCounter = FlagCounter(
                totalMessager = mockk(),
                errorMessager = mockk(),
                flagReader = mockk(),
                teamMessagers = mapOf()
        )

        val keyAlpha = "alpha"
        val keyBeta = "beta"
        val keyGamma = "gamma"

        val maintainer = FlagMaintainer("", "")

        val status = Status.LAUNCHED

        val referenceAlpha = FlagReference("/path/alpha")
        val referenceBeta = FlagReference("/path/beta")
        val referenceGamma = FlagReference("/path/gamma")
        val flagResponse = FlagResponse(
                totalCount = 3,
                items = listOf(
                        FlagDetail(keyAlpha, maintainer, FlagLinks(referenceAlpha)),
                        FlagDetail(keyBeta, maintainer, FlagLinks(referenceBeta)),
                        FlagDetail(keyGamma, maintainer, FlagLinks(referenceGamma))
                )
        )

        val flagStatusResponse = FlagStatusResponse(
                listOf(
                        FlagStatus(status, FlagStatusLinks(referenceAlpha)),
                        FlagStatus(status, FlagStatusLinks(referenceBeta)),
                        FlagStatus(status, FlagStatusLinks(FlagReference("no match")))
                )
        )

        val actualZipped = flagCounter.zipFlagsAndStatuses(flagResponse, flagStatusResponse)

        val expectedZipped = listOf(
                FlagCounter.FlagDetailAndStatus(keyAlpha, maintainer, status),
                FlagCounter.FlagDetailAndStatus(keyBeta, maintainer, status),
        )

        assertEquals(expectedZipped, actualZipped)
    }
}