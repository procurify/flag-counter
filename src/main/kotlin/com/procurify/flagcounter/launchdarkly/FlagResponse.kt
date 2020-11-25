package com.procurify.flagcounter.launchdarkly

/**
 * Representation of the https://apidocs.launchdarkly.com/reference#list-feature-flags response
 */
data class FlagResponse(
        val totalCount: Int,
        val items: List<FlagDetail>
)

data class FlagDetail(
        val key: String,
        val _maintainer: FlagMaintainer
)

data class FlagMaintainer(
        val email: String
)