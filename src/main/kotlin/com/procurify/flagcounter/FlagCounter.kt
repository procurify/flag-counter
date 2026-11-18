package com.procurify.flagcounter

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
        totalMessager.postMessage(formatTotalCountMessage(flagDetails.size)).fold(
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
                                }}"
                        )
                    }
                }
        )
    }

    /**
     * Groups the [FlagDetail] in a list as values to a map keyed by the provided [teamEmails].
     * Flags with no owner, or with an owner outside [teamEmails], are left out.
     */
    fun groupFlagsByOwner(
            flagDetails: List<FlagDetail>,
            teamEmails: Set<TeamIdentifier>
    ): Map<TeamIdentifier, List<FlagDetail>> = flagDetails
            .mapNotNull { detail -> detail.owner?.let { TeamIdentifier(it.email) to detail } }
            .groupBy({ it.first }, { it.second })
            .filterKeys(teamEmails::contains)

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

    /**
     * Formats [teamFlags] into a message for the team about the flags they maintain
     * All flags should have the same owner
     */
    private fun formatTeamMessage(teamFlags: List<FlagDetail>): String {
        // TODO Parameterize link url based on project/environment configuration
        return """Hey ${teamFlags.firstOrNull()?.owner?.name ?: "team"}!
           |Launch Darkly thinks ${removableFlagCount(teamFlags)} of your ${teamFlags.size} flags could be ready for removal.
           |Take a look ${flagReader.flagListUrl}""".trimMargin()
    }

    private fun removableFlagCount(flags: List<FlagDetail>): Int {
        return flags.filter { it.status == Status.REMOVABLE }.size
    }
}

/**
 * Identifier for a Team
 */
data class TeamIdentifier(
        val id: String
)


