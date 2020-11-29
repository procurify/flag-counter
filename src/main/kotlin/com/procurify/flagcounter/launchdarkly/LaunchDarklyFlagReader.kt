package com.procurify.flagcounter.launchdarkly

import arrow.core.Either
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Parameters
import com.procurify.flagcounter.FlagReader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class LaunchDarklyFlagReader(
        private val apiKey: String,
        // TODO Add this to method params
        private val project: String = "default",
        // TODO Add this to method params
        private val environment: String = "production"
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

        return result.fold(
                { value ->
                    val flagResponse = MAPPER.readValue(value, FlagResponse::class.java)
                    LOG.debug("Fetched ${flagResponse.totalCount} flags from LaunchDarkly for project $project")
                    Either.right(flagResponse)
                },
                { error ->
                    LOG.error(error.message)
                    Either.left(FlagReader.FlagError(response.statusCode, "Failed to fetch flags for $project"))
                }
        )
    }

    override fun getFlagStatuses(): Either<FlagReader.FlagError, FlagStatusResponse> {
        val url = "https://app.launchdarkly.com/api/v2/flag-statuses/$project/$environment"
        val (_, response, result) = Fuel
                .get(url)
                .header(Headers.AUTHORIZATION, apiKey)
                .response()

        return result.fold(
                { value ->
                    val statusResponse = MAPPER.readValue(value, FlagStatusResponse::class.java)
                    LOG.debug("Retrieved ${statusResponse.items} statuses from LaunchDarkly for project $project")
                    Either.right(statusResponse)
                },
                { error ->
                    LOG.error(error.message)
                    Either.left(FlagReader.FlagError(response.statusCode, "Failed to fetch statuses for $project"))
                }
        )
    }


    companion object {
        val LOG: Logger = LogManager.getLogger(this::class.java)
        val MAPPER: ObjectMapper = ObjectMapper()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(KotlinModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}

