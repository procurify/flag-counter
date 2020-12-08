package com.procurify.flagcounter

import com.procurify.flagcounter.launchdarkly.LaunchDarklyFlagReader
import com.procurify.flagcounter.slack.SlackMessager
import io.mockk.mockk
import org.junit.Ignore
import org.junit.Test

class TestApplication {

    /**
     * This test runs an end to end test using Slack and LaunchDarkly credentials from env vars
     *
     * Ignored since it posts to Slack
     */
    @Test
    @Ignore
    fun `Test Implementation End to End`() {
        val slackMessager = SlackMessager(System.getenv("SLACK_URL"))
        val launchDarklyFlagReader = LaunchDarklyFlagReader(System.getenv("LAUNCHDARKLY_KEY"))

        FlagCounter(
                totalMessager = slackMessager,
                errorMessager = mockk(),
                flagReader = launchDarklyFlagReader,
                teamMessagers = mapOf()
        ).fetchFlagsAndPostMessages()
    }

    /**
     * This test reads the environment variable for the teams map and asserts it is non-empty
     *
     * Ignored since it requires a real configuration
     */
    @Test
    @Ignore
    fun `Test that teams map is read correctly from environment`() {
        val teamsMap = EnvironmentTeamParser.parseJsonIntoTeamsMap(System.getenv("FLAG_COUNTER_TEAMS"))

        assert(teamsMap.isNotEmpty())
    }
}