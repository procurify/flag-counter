package com.procurify.flagcounter.launchdarkly

import arrow.core.Either
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Parameters
import com.procurify.flagcounter.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class LaunchDarklyFlagReader(
        private val apiKey: String,
        private val project: String = "default",
        private val environment: String = "production"
) : FlagReader {

    override val flagListUrl: String by lazy { "https://app.launchdarkly.com/$project/$environment/features" }

    override fun getFlagDetails(): Either<FlagError, List<FlagDetail>> {
        return getFlags().map { flags ->
            getFlagStatuses().fold<List<FlagDetail>>(
                    { flagError -> return Either.left(flagError) },
                    { flagStatuses -> return Either.right(zipFlagsAndStatuses(flags, flagStatuses)) }
            )
        }
    }

    private fun getFlags(): Either<FlagError, LDFlagResponse> {
        val url = "$BASE_URL/flags/$project"
        val parameters: Parameters = listOf(
                "env" to environment,
                "summary" to true
        )
        val (_, response, result) = Fuel
                .get(url, parameters)
                .header(Headers.AUTHORIZATION, apiKey)
                .response()

        return result.fold(
                { value ->
                    val flagResponse = MAPPER.readValue(value, LDFlagResponse::class.java)
                    LOG.debug("Fetched ${flagResponse.totalCount} flags from LaunchDarkly for project $project")
                    Either.right(flagResponse)
                },
                { error ->
                    LOG.error(error.message)
                    Either.left(FlagError("Failed to fetch flags for $project, status code ${response.statusCode}"))
                }
        )
    }

    private fun getFlagStatuses(): Either<FlagError, LDFlagStatusResponse> {
        val url = "$BASE_URL/flag-statuses/$project/$environment"
        val (_, response, result) = Fuel
                .get(url)
                .header(Headers.AUTHORIZATION, apiKey)
                .response()

        return result.fold(
                { value ->
                    val statusResponse = MAPPER.readValue(value, LDFlagStatusResponse::class.java)
                    LOG.debug("Retrieved ${statusResponse.items} statuses from LaunchDarkly for project $project")
                    Either.right(statusResponse)
                },
                { error ->
                    LOG.error(error.message)
                    Either.left(FlagError("Failed to fetch statuses for $project, status code ${response.statusCode}"))
                }
        )
    }

    companion object {
        const val BASE_URL = "https://app.launchdarkly.com/api/v2"
        val LOG: Logger = LogManager.getLogger(this::class.java)
        val MAPPER: ObjectMapper = ObjectMapper()
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(KotlinModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        /**
         * Zips together the flag details and flag statuses to create a list of a [FlagDetail]
         */
        fun zipFlagsAndStatuses(flags: LDFlagResponse, flagStatuses: LDFlagStatusResponse): List<FlagDetail> {
            return flags.items.mapNotNull { flag ->
                flagStatuses.items.find { status ->
                    status.links.parent.href == flag.links.self.href
                }?.let { status ->
                    FlagDetail(
                            key = flag.key,
                            owner = Owner(flag.maintainer.firstName, flag.maintainer.email),
                            status = if (status.name in listOf(LDStatus.LAUNCHED, LDStatus.INACTIVE)) {
                                Status.REMOVABLE
                            } else {
                                Status.ACTIVE
                            }
                    )
                }
            }
        }
    }
}
