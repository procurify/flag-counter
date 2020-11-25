package com.procurify.flagcounter.launchdarkly

import arrow.core.Either
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Parameters
import com.procurify.flagcounter.FlagReader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class LaunchDarklyFlagReader(
        val apiKey: String,
        private val project: String = "default"
) : FlagReader {

    override fun getFlags(): Either<FlagReader.FlagError, FlagResponse> {
        val url = "https://app.launchdarkly.com/api/v2/flags/$project"
        val parameters: Parameters = listOf(
                "env" to "production",
                "summary" to true
        )
        val (_, response, result) = Fuel
                .get(url, parameters)
                .header(Headers.AUTHORIZATION, apiKey)
                .response()

        // TODO Add better logging / error handling
        return result.fold(
                { value ->
                    val flagSummary = MAPPER.readValue(value, FlagResponse::class.java)
                    LOG.debug("Retrieved $flagSummary from LaunchDarkly for project $project")
                    Either.right(flagSummary)
                },
                { error ->
                    LOG.error(error.message)
                    Either.left(FlagReader.FlagError(response.statusCode, "Failed to fetch flags for $project"))
                }
        )
    }

    companion object {
        val LOG: Logger = LogManager.getLogger(this::class.java)
        val MAPPER: ObjectMapper = ObjectMapper()
                .registerModule(KotlinModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}

