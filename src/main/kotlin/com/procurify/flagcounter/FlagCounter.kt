package com.procurify.flagcounter

import arrow.core.Either
import com.procurify.flagcounter.launchdarkly.FlagDetail
import com.procurify.flagcounter.launchdarkly.FlagResponse

/**
 * FlagCounter which fetches flags using a [FlagReader] and messages updates both with a total project count with
 * [totalMessager] and team specific information using [teamMessagers]
 *
 * Uses an [errorMessager] to post error messages beyond simple logging
 * TODO Refactor to use a single slack instance rather than making N + 1 network calls (overall count + 1 per team)
 */
class FlagCounter(
        private val totalMessager: Messager,
        private val errorMessager: Messager,
        private val flagReader: FlagReader,
        private val teamMessagers: Map<TeamEmail, Messager>
) {
    fun postFlagUpdate() {
        flagReader.getFlags().fold(
                { error ->
                    errorMessager.postOrThrow("Error fetching flags. Response code ${error.responseCode}")
                },
                { value ->
                    postFlagResponse(value)
                }
        )
    }

    private fun postFlagResponse(flagResponse: FlagResponse) {
        // Post Total Count Update
        val comparisonMessage = FlagEquivalentMessageGenerator.getNearestNumberMessage(flagResponse.totalCount)
        totalMessager.postMessage(
                "There are currently ${flagResponse.totalCount} flags in the system!\n$comparisonMessage"
        ).fold(
                { errorMessager.postOrThrow("Failed to post total count message") },
                {
                    val teamFlagMap = groupFlagsByMaintainer(flagResponse, teamMessagers.keys)

                    // Message Each team and collect a list of teams that have failed to send
                    // TODO Fold over the messagers to remove the need for this map
                    val failedTeamMessages = mutableListOf<TeamEmail>()
                    teamMessagers.forEach { (email, messager) ->
                        teamFlagMap[email]?.let {
                            // TODO Message each team with more information than a simple count
                            val teamCount = it.size
                            if (teamCount > 0) {
                                messager
                                        .postMessage("Your Team Currently owns $teamCount production flags!")
                                        .mapLeft { failedTeamMessages.add(email) }
                            }
                        }
                    }
                    if (failedTeamMessages.size > 0) {
                        errorMessager.postOrThrow(
                                "Failed to send updates for ${
                                    failedTeamMessages
                                            .map { it.email }
                                            .joinToString(separator = "\n")
                                }}"
                        )
                    }
                }
        )
    }

    /**
     * Groups the [FlagDetail] in a list as values to a map keyed by the provided [teamEmails]. If there are maintainers
     * returned in the [FlagResponse] that are not in the provided [teamEmails], those flags are not added to the map
     * TODO There is likely a cleaner way to do this without a mutable map
     */
    fun groupFlagsByMaintainer(flagResponse: FlagResponse, teamEmails: Set<TeamEmail>): Map<TeamEmail, List<FlagDetail>> =
            flagResponse.items.fold(teamEmails.associateWith { listOf<FlagDetail>() }.toMutableMap()) { acc, flagDetail ->
                val email = TeamEmail(flagDetail._maintainer.email)
                if (acc.containsKey(email)) {
                    acc[email] = acc[email].orEmpty() + flagDetail
                }
                acc
            }

    /**
     * Post a message to the [Messager] and throw an exception with the same [message] if the post fails
     */
    private fun Messager.postOrThrow(message: String) =
            this.postMessage(message).mapLeft { throw Exception(message) }
}

/**
 * Identifier for a Team
 */
data class TeamEmail(
        val email: String
)

interface Messager {
    fun postMessage(message: String): Either<MessagerError, Unit>

    data class MessagerError(
            val message: String
    )
}

interface FlagReader {
    fun getFlags(): Either<FlagError, FlagResponse>

    data class FlagError(
            val responseCode: Int,
            val message: String
    )
}


