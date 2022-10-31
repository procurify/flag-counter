package com.procurify.flagcounter

import com.procurify.flagcounter.FlagCounter.Companion.getMedianAgeInDays

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
        private val teamMessagers: Map<TeamIdentifier, Messager>
) {
    fun fetchFlagsAndPostMessages() {
        flagReader.getFlagDetails().fold(
                { error -> errorMessager.postOrThrow(error.message) },
                { flagDetails -> postFlagDetails(flagDetails) }
        )
    }

    /**
     * TODO Cleanup this method
     */
    private fun postFlagDetails(flagDetails: List<FlagDetail>) {

        // Post Total Count Update
        totalMessager.postMessage(formatTotalCountMessage(flagDetails)).fold(
                { errorMessager.postOrThrow("Failed to post total count message") },
                {
                    val teamFlagMap = groupFlagsByOwner(flagDetails, teamMessagers.keys)

                    // Message Each team and collect a list of teams that have failed to send
                    // TODO Fold over the messagers to remove the need for this map
                    val failedTeamMessages = mutableListOf<TeamIdentifier>()
                    teamMessagers.forEach { (id, messager) ->
                        teamFlagMap[id]?.let { teamFlags ->
                            if (teamFlags.isNotEmpty() && removableFlagCount(teamFlags) > 0) {
                                messager
                                        .postMessage(formatTeamMessage(teamFlags))
                                        .mapLeft { failedTeamMessages.add(id) }
                            }
                        }
                    }
                    if (failedTeamMessages.size > 0) {
                        errorMessager.postOrThrow(
                                "Failed to send updates for ${
                                    failedTeamMessages.joinToString(separator = "\n") { it.id }
                                }"
                        )
                    }
                }
        )
    }

    /**
     * Groups the [FlagDetail] in a list as values to a map keyed by the provided [teamEmails]. If there are maintainers
     * returned in details that are not in the provided [teamEmails], those flags are not added to the map
     * TODO There is likely a cleaner way to do this without a mutable map
     */
    fun groupFlagsByOwner(
            flagDetails: List<FlagDetail>,
            teamEmails: Set<TeamIdentifier>
    ): Map<TeamIdentifier, List<FlagDetail>> = flagDetails
            .fold(mapOf<TeamIdentifier, List<FlagDetail>>().toMutableMap()) { acc, flagDetail ->
                val emailIdentifier = TeamIdentifier(flagDetail.owner.email)
                acc[emailIdentifier] = acc[emailIdentifier].orEmpty() + flagDetail
                acc
            }
            .filter { teamEmails.contains(it.key) }

    /**
     * Post a message to the [Messager] and throw an exception with the same [message] if the post fails
     */
    private fun Messager.postOrThrow(message: String) =
            this.postMessage(message).mapLeft { throw Exception(message) }

    /**
     * Formats a message about the total flag count in the system based on the [teamFlags]
     */
    private fun formatTotalCountMessage(teamFlags: List<FlagDetail>): String {
        val totalFlagCount = teamFlags.size
        val comparisonMessage = FlagEquivalentMessageGenerator.getNearestNumberMessage(totalFlagCount)
        return """
            There are currently $totalFlagCount flags in the system!
            $comparisonMessage
            The median age of the flags in the system is ${teamFlags.getMedianAgeInDays()} days
        """.trimIndent()
    }

    /**
     * Formats [teamFlags] into a message for the team about the flags they maintain
     * All flags should have the same owner
     */
    private fun formatTeamMessage(teamFlags: List<FlagDetail>): String {
        // TODO Parameterize link url based on project/environment configuration
        return """Hey ${teamFlags.firstOrNull()?.owner?.name ?: "team"}!
           |LaunchDarkly thinks ${removableFlagCount(teamFlags)} of your ${teamFlags.size} flags could be ready for removal.
           |The median age of the flags you maintain is ${teamFlags.getMedianAgeInDays()} days
           |Take a look ${flagReader.flagListUrl}""".trimMargin()
    }

    private fun removableFlagCount(flags: List<FlagDetail>): Int {
        return flags.filter { it.status == Status.REMOVABLE }.size
    }

    companion object {

        private const val MILLISECONDS_IN_A_DAY = 1000 * 60 * 60 * 24
        fun List<FlagDetail>.getMedianAgeInDays(): Long = this.sortedBy { it.creationDate }.let { flagDetails ->
            if (flagDetails.size % 2 == 0) {
                (flagDetails[flagDetails.size / 2].creationDate + flagDetails[flagDetails.size / 2 - 1].creationDate) / 2
            } else {
                flagDetails[flagDetails.size / 2].creationDate
            }.let {
                (System.currentTimeMillis() - it) / MILLISECONDS_IN_A_DAY
            }
        }
    }
}

/**
 * Identifier for a Team
 */
data class TeamIdentifier(
        val id: String
)


