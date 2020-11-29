package com.procurify.flagcounter.launchdarkly

/**
 * Representation of the https://apidocs.launchdarkly.com/reference#list-feature-flags response
 */
data class FlagResponse(
        val totalCount: Int,
        val items: List<FlagDetail>
)

/**
 * Details of one feature flag
 */
data class FlagDetail(
        val key: String,
        val _maintainer: FlagMaintainer,
        val _links: FlagLinks
)

/**
 * Feature flag maintainer
 */
data class FlagMaintainer(
        val firstName: String,
        val email: String
)

/**
 * JSON API Links for feature flags
 */
data class FlagLinks(
        val self: FlagReference
)

/**
 * Reference to a flag in the LaunchDarkly API
 */
data class FlagReference(
        val href: String
)

/**
 * Representation of the https://apidocs.launchdarkly.com/reference#list-feature-flag-statuses response
 */
data class FlagStatusResponse(
        val items: List<FlagStatus>
)

/**
 * Status of a feature flag
 */
data class FlagStatus(
        val name: Status,
        val _links: FlagStatusLinks
)

/**
 * JSON API Link for flag statuses
 */
data class FlagStatusLinks(
        val parent: FlagReference
)

/**
 * The LaunchDarkly flag statuses, see: https://docs.launchdarkly.com/home/managing-flags/dashboard#flag-statuses
 */
enum class Status {
    NEW,
    ACTIVE,
    INACTIVE,
    LAUNCHED
}