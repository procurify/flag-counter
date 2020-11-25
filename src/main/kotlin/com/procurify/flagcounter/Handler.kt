package com.procurify.flagcounter

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.procurify.flagcounter.launchdarkly.LaunchDarklyFlagReader
import com.procurify.flagcounter.slack.SlackMessager
import org.apache.logging.log4j.LogManager

@Suppress("unused")
class Handler : RequestHandler<Map<String, Any>, LambdaResponse> {
    override fun handleRequest(input: Map<String, Any>, context: Context): LambdaResponse {
        LOG.info("received: " + input.keys.toString())

        // TODO Refactor to use one slack instance rather than per channel messagers
        val slackMessager = SlackMessager(System.getenv("SLACK_URL"))
        val errorMessager = SlackMessager(System.getenv("SLACK_ERROR_URL"))
        val launchDarklyFlagReader = LaunchDarklyFlagReader(System.getenv("LAUNCHDARKLY_KEY"))
        val teamMessagers = EnvironmentTeamParser.parseJsonIntoTeamsMap(System.getenv("TEAMS_MAP"))
                .mapKeys { TeamEmail(it.key) }
                .mapValues { SlackMessager(it.value) }

        FlagCounter(
                totalMessager = slackMessager,
                errorMessager = errorMessager,
                flagReader = launchDarklyFlagReader,
                // TODO Create Map for per channel messaging
                teamMessagers = teamMessagers
        )
                .postFlagUpdate()

        return LambdaResponse()
    }

    companion object {
        private val LOG = LogManager.getLogger(Handler::class.java)
    }
}
