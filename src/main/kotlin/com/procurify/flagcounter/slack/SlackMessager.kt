package com.procurify.flagcounter.slack

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.Fuel
import com.procurify.flagcounter.Messager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class SlackMessager(
        private val slackUrl: String
) : Messager {
    override fun postMessage(message: String) {
        val (_, _, result) = Fuel.post(slackUrl)
                .body(MAPPER.writeValueAsString(createPayload(message)))
                .responseString()

        // TODO Add better logging / error handling
        result.fold({ LOG.debug("Successfully Posted to Slack") }, { error -> LOG.error(error.message) })
    }

    private fun createPayload(message: String) = Payload(
            text = message,
            username = "Flag Bot",
            icon_emoji = ":flags:"
    )

    data class Payload(
            val text: String,
            val username: String,
            val icon_emoji: String
    )

    companion object {
        val LOG: Logger = LogManager.getLogger(this::class.java)
        val MAPPER = ObjectMapper()
    }
}
