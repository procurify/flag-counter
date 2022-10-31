package com.procurify.flagcounter

import com.procurify.flagcounter.launchdarkly.LaunchDarklyFlagReader
import com.procurify.flagcounter.slack.SlackMessager
import io.mockk.mockk
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class TestApplication {

    /**
     * This test runs an end to end test using Slack and LaunchDarkly credentials from env vars
     *
     * Ignored since it posts to Slack
     */
    @Test
    fun `Test Implementation End to End`() {
        val slackMessager = SlackMessager(System.getenv("SLACK_URL"))
        val launchDarklyFlagReader = LaunchDarklyFlagReader(System.getenv("LAUNCHDARKLY_KEY"))
        val teamMessagers = EnvironmentTeamParser.parseJsonIntoTeamsMap(System.getenv("TEAMS_MAP"))
            .mapKeys { TeamIdentifier(it.key) }
            .mapValues { SlackMessager(it.value) }

        FlagCounter(
                totalMessager = slackMessager,
                errorMessager = mockk(),
                flagReader = launchDarklyFlagReader,
                teamMessagers = teamMessagers
        ).fetchFlagsAndPostMessages()
    }

    /**
     * This test reads the environment variable for the teams map and asserts it is non-empty
     *
     * Ignored since it requires a real configuration
     */
    @Test
    @Disabled
    fun `Test that teams map is read correctly from environment`() {
        val teamsMap = EnvironmentTeamParser.parseJsonIntoTeamsMap(System.getenv("FLAG_COUNTER_TEAMS"))

        assert(teamsMap.isNotEmpty())
    }
}