package com.procurify.flagcounter.launchdarkly

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Representation of the https://apidocs.launchdarkly.com/reference#list-feature-flags response
 */
data class LDFlagResponse(
        val totalCount: Int,
        val items: List<LDFlagDetail>
)

/**
 * Details of one feature flag
 */
data class LDFlagDetail(
        val key: String,
        @JsonProperty("_maintainer")
        val maintainer: LDFlagMaintainer,
        @JsonProperty("_links")
        val links: LDFlagLinks
)

/**
 * Feature flag maintainer
 */
data class LDFlagMaintainer(
        val firstName: String,
        val email: String
)

/**
 * JSON API Links for feature flags
 */
data class LDFlagLinks(
        val self: LDFlagReference
)

/**
 * Reference to a flag in the LaunchDarkly API
 */
data class LDFlagReference(
        val href: String
)

/**
 * Representation of the https://apidocs.launchdarkly.com/reference#list-feature-flag-statuses response
 */
data class LDFlagStatusResponse(
        val items: List<LDFlagStatus>
)

/**
 * Status of a feature flag
 */
data class LDFlagStatus(
        val name: LDStatus,
        @JsonProperty("_links")
        val links: LDStatusLinks
)

/**
 * JSON API Link for flag statuses
 */
data class LDStatusLinks(
        val parent: LDFlagReference
)

/**
 * The LaunchDarkly flag statuses, see: https://docs.launchdarkly.com/home/managing-flags/dashboard#flag-statuses
 */
@Suppress("unused")
enum class LDStatus {
    NEW,
    ACTIVE,
    INACTIVE,
    LAUNCHED
}