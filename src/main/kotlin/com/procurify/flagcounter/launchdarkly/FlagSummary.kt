package com.procurify.flagcounter.launchdarkly

/**
 * Representation of the https://apidocs.launchdarkly.com/reference#list-feature-flags response
 */
data class FlagSummary(
        val totalCount: Int
)