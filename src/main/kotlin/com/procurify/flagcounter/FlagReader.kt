package com.procurify.flagcounter

import arrow.core.Either

/**
 * Interface for a class which
 */
interface FlagReader {

    val flagListUrl: String

    /**
     * Fetch a list of [FlagDetail]
     */
    fun getFlagDetails(): Either<FlagError, List<FlagDetail>>

}

/**
 * Details about a flag
 */
data class FlagDetail(
        val key: String,
        val owner: Owner,
        val status: Status
)

/**
 * Owner/Maintainer of a flag
 */
data class Owner(
        val name: String,
        val email: String
)

/**
 * Status of a flag indicating likely ability to remove the flag
 */
enum class Status {
    REMOVABLE,
    ACTIVE
}

/**
 * Error class for attempts to retrieve flag data
 */
data class FlagError(
        val message: String
)

