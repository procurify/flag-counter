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
        private val teamMessagers: Map<TeamEmail, Messager>
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
     * returned in details that are not in the provided [teamEmails], those flags are not added to the map
     * TODO There is likely a cleaner way to do this without a mutable map
     */
    fun groupFlagsByOwner(
            flagDetails: List<FlagDetail>,
            teamEmails: Set<TeamEmail>
    ): Map<Owner, List<FlagDetail>> = flagDetails
            .fold(mapOf<Owner, List<FlagDetail>>().toMutableMap()) { acc, flagDetail ->
                acc[flagDetail.owner] = acc[flagDetail.owner].orEmpty() + flagDetail
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

    /**
     * Formats a [owner] and [teamFlags] into a message for the team about the flags they maintain
     */
    private fun formatTeamMessage(owner: Owner, teamFlags: List<FlagDetail>): String {
        val removeCount = teamFlags.filter { it.status == Status.REMOVABLE }.size
        // TODO Parameterize link url based on project/environment configuration
        return """Hey ${owner.name}!
           |Launch Darkly thinks $removeCount of your ${teamFlags.size} flags could be ready for removal.
           |Take a look ${flagReader.flagListUrl}""".trimMargin()
    }
}

/**
 * Identifier for a Team
 */
data class TeamEmail(
        val email: String
)


