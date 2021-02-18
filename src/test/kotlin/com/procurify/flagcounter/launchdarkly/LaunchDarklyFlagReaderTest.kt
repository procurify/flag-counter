package com.procurify.flagcounter.launchdarkly

import com.procurify.flagcounter.FlagDetail
import com.procurify.flagcounter.Owner
import com.procurify.flagcounter.Status
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * TODO Tests with mocked responses for error handling
 */
class LaunchDarklyFlagReaderTest {

    @Test
    fun `ensure that flags and flag statuses are zipped together`() {

        val keyAlpha = "alpha"
        val keyBeta = "beta"
        val keyGamma = "gamma"

        val maintainer = LDFlagMaintainer("", "")

        val status = LDStatus.LAUNCHED

        val referenceAlpha = LDFlagReference("/path/alpha")
        val referenceBeta = LDFlagReference("/path/beta")
        val referenceGamma = LDFlagReference("/path/gamma")
        val flagResponse = LDFlagResponse(
                totalCount = 3,
                items = listOf(
                        LDFlagDetail(keyAlpha, maintainer, LDFlagLinks(referenceAlpha)),
                        LDFlagDetail(keyBeta, maintainer, LDFlagLinks(referenceBeta)),
                        LDFlagDetail(keyGamma, maintainer, LDFlagLinks(referenceGamma))
                )
        )

        val flagStatusResponse = LDFlagStatusResponse(
                listOf(
                        LDFlagStatus(status, LDStatusLinks(referenceAlpha)),
                        LDFlagStatus(status, LDStatusLinks(referenceBeta)),
                        LDFlagStatus(status, LDStatusLinks(LDFlagReference("no match")))
                )
        )

        val actualZipped = LaunchDarklyFlagReader.zipFlagsAndStatuses(flagResponse, flagStatusResponse)

        val expectedOwner = Owner(maintainer.firstName, maintainer.email)
        val expectedStatus = Status.REMOVABLE
        val expectedZipped = listOf(
                FlagDetail(keyAlpha, expectedOwner, expectedStatus),
                FlagDetail(keyBeta, expectedOwner, expectedStatus),
        )

        assertEquals(expectedZipped, actualZipped)
    }

    @Test
    fun `zipping handles maintainers with null first names`() {

        val keyAlpha = "alpha"

        val maintainer = LDFlagMaintainer(null, "")

        val status = LDStatus.LAUNCHED

        val referenceAlpha = LDFlagReference("/path/alpha")
        val flagResponse = LDFlagResponse(
                totalCount = 1,
                items = listOf(
                        LDFlagDetail(keyAlpha, maintainer, LDFlagLinks(referenceAlpha)),
                )
        )

        val flagStatusResponse = LDFlagStatusResponse(
                listOf(
                        LDFlagStatus(status, LDStatusLinks(referenceAlpha))
                )
        )

        val actualZipped = LaunchDarklyFlagReader.zipFlagsAndStatuses(flagResponse, flagStatusResponse)

        val expectedOwner = Owner(maintainer.firstName, maintainer.email)
        val expectedStatus = Status.REMOVABLE
        val expectedZipped = listOf(
                FlagDetail(keyAlpha, expectedOwner, expectedStatus),
        )

        assertEquals(expectedZipped, actualZipped)
    }
}