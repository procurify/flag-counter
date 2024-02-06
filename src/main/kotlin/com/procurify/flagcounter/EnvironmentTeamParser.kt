package com.procurify.flagcounter

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * Object for parsing JSON in Env var into a map of teams to webhook URLs
 * TODO Everything is currently configured in env vars but this could be removed on moving to something like SSM
 */
object EnvironmentTeamParser {

    /**
     * Parse an environment variable stored as a JSON object such as
     * {
     *   "teamsList": [
     *     {
     *       "email":"a@test.com",
     *       "url": "google.com"
     *     },
     *     {
     *       "email":"b@test.com",
     *       "url":"bing.com"
     *     }
     *   ]
     * }
     * into a Map<TeamEmail, SlackWebhookUrl>
     */
    fun parseJsonIntoTeamsMap(jsonString: String): Map<String, String> =
            if (jsonString.isBlank()) {
                mapOf()
            } else {
                MAPPER.readValue(jsonString, TeamConfigList::class.java).teamsList.associate {
                    it.email to it.url
                }
            }

    private val MAPPER: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private data class TeamConfigList(
            val teamsList: List<TeamConfig>
    )

    private data class TeamConfig(
            val email: String,
            val url: String
    )
}