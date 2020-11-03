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

        val slackMessager = SlackMessager(System.getenv("SLACK_URL"))
        val launchDarklyFlagReader = LaunchDarklyFlagReader(System.getenv("LAUNCHDARKLY_KEY"))

        FlagCounter(slackMessager, launchDarklyFlagReader).postFlagUpdate()

        return LambdaResponse()
    }

    companion object {
        private val LOG = LogManager.getLogger(Handler::class.java)
    }
}
