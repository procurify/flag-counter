package com.procurify.flagcounter

import org.junit.Test
import kotlin.test.assertEquals

class EnvironmentTeamParserTest {

    @Test
    fun `ensure that JSON is correctly parsed into the teams map`() {

        val jsonString = "{\"teamsList\":[{\"email\":\"a@test.com\", \"url\": \"google.com\"},{\"email\":\"b@test.com\", \"url\":\"bing.com\"}]}"

        val expectedMap = mapOf(
                "a@test.com" to "google.com",
                "b@test.com" to "bing.com",
        )

        val actualMap = EnvironmentTeamParser.parseJsonIntoTeamsMap(jsonString)

        assertEquals(expectedMap, actualMap)
    }

    @Test
    fun `ensure that an empty string returns an empty map`() {

        val jsonString = ""

        val expectedMap = emptyMap<String, String>()

        val actualMap = EnvironmentTeamParser.parseJsonIntoTeamsMap(jsonString)

        assertEquals(expectedMap, actualMap)
    }
}