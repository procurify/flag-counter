package com.procurify.flagcounter

import arrow.core.Either
import com.procurify.flagcounter.launchdarkly.*

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
                { flags ->
                    flagReader.getFlagStatuses().fold(
                            { error ->
                                errorMessager.postOrThrow("Error fetching statuses. Response code ${error.responseCode}")
                            },
                            { flagStatuses -> postFlagMessages(zipFlagsAndStatuses(flags, flagStatuses)) }
                    )
                }
        )
    }

    /**
     * Zips together the flag details and flag statuses to create a list of a [FlagDetailAndStatus]
     */
    fun zipFlagsAndStatuses(flags: FlagResponse, flagStatuses: FlagStatusResponse): List<FlagDetailAndStatus> {
        return flags.items.mapNotNull { flag ->
            flagStatuses.items.find { status ->
                status._links.parent.href == flag._links.self.href
            }?.let {
                FlagDetailAndStatus(
                        flag.key,
                        flag._maintainer,
                        it.name)
            }
        }
    }

    private fun postFlagMessages(flagDetails: List<FlagDetailAndStatus>) {
        // Post Total Count Update
        totalMessager.postMessage(formatTotalCountMessage(flagDetails.size)).fold(
                { errorMessager.postOrThrow("Failed to post total count message") },
                {
                    val teamFlagMap = groupFlagsByMaintainer(flagDetails, teamMessagers.keys)

                    // Message Each team and collect a list of teams that have failed to send
                    // TODO Fold over the messagers to remove the need for this map
                    val failedTeamMessages = mutableListOf<TeamEmail>()
                    teamMessagers.forEach { (email, messager) ->
                        // TODO This filter is not a great solution to matching TeamEmail to FlagMaintainer by email
                        // This will always return a single entry from the map but requires an extra iteration
                        teamFlagMap.filter { TeamEmail(it.key.email) == email }.map { (maintainer, teamFlags) ->
                            if (teamFlags.isNotEmpty()) {
                                messager
                                        .postMessage(formatTeamMessage(maintainer, teamFlags))
                                        .mapLeft { failedTeamMessages.add(email) }
                            }
                        }
                    }
                    if (failedTeamMessages.size > 0) {
                        errorMessager.postOrThrow(
                                "Failed to send updates for ${
                                    failedTeamMessages.joinToString(separator = "\n") { it.email }
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
    fun groupFlagsByMaintainer(
            flagDetails: List<FlagDetailAndStatus>,
            teamEmails: Set<TeamEmail>
    ): Map<FlagMaintainer, List<FlagDetailAndStatus>> = flagDetails
            .fold(mapOf<FlagMaintainer, List<FlagDetailAndStatus>>().toMutableMap()) { acc, flagDetail ->
                acc[flagDetail._maintainer] = acc[flagDetail._maintainer].orEmpty() + flagDetail
                acc
            }
            .filter { teamEmails.contains(TeamEmail(it.key.email)) }

    /**
     * Post a message to the [Messager] and throw an exception with the same [message] if the post fails
     */
    private fun Messager.postOrThrow(message: String) =
            this.postMessage(message).mapLeft { throw Exception(message) }

    /**
     * Formats a [totalFlagCount] into a message to be sent by the messager
     */
    private fun formatTotalCountMessage(totalFlagCount: Int): String {
        val comparisonMessage = FlagEquivalentMessageGenerator.getNearestNumberMessage(totalFlagCount)
        return "There are currently $totalFlagCount flags in the system!\n$comparisonMessage"
    }

    private fun formatTeamMessage(team: FlagMaintainer, teamFlags: List<FlagDetailAndStatus>): String {
        val removeCount = teamFlags.filter { it.status in setOf(Status.INACTIVE, Status.LAUNCHED) }.size
        // TODO Parameterize link url based on project/environment configuration
        return """Hey ${team.firstName}!
           |Launch Darkly thinks $removeCount of your ${teamFlags.size} flags could be ready for removal.
           |Take a look https://app.launchdarkly.com/default/production/features""".trimMargin()
    }

    data class FlagDetailAndStatus(
            val key: String,
            val _maintainer: FlagMaintainer,
            val status: Status
    )
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

    fun getFlagStatuses(): Either<FlagError, FlagStatusResponse>

    data class FlagError(
            val responseCode: Int,
            val message: String
    )
}


