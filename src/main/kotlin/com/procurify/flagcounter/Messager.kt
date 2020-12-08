package com.procurify.flagcounter

import arrow.core.Either

/**
 * Interface for a class which sends formatted messages to a destination
 */
interface Messager {
    fun postMessage(message: String): Either<MessagerError, Unit>

    data class MessagerError(
            val message: String
    )
}