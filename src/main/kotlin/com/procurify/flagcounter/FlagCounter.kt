package com.procurify.flagcounter

import com.procurify.flagcounter.launchdarkly.FlagSummary

class FlagCounter(
        private val messager: Messager,
        private val flagReader: FlagReader
) {
    fun postFlagUpdate() {
        val flagCount = flagReader.readFlagCount().totalCount
        val comparisonMessage = FlagEquivalentMessageGenerator.getNearestNumberMessage(flagCount)
        messager.postMessage("There are currently $flagCount flags in the system!\n$comparisonMessage")
    }
}

interface Messager {
    fun postMessage(message: String)
}

interface FlagReader {
    fun readFlagCount(): FlagSummary
}


