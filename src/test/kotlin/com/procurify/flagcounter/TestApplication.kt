package com.procurify.flagcounter

import com.procurify.flagcounter.launchdarkly.LaunchDarklyFlagReader
import com.procurify.flagcounter.slack.SlackMessager
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

        FlagCounter(slackMessager, launchDarklyFlagReader).postFlagUpdate()
    }
}