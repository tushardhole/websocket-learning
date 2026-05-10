package com.example.wschat.exception

import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.web.bind.annotation.ControllerAdvice

@ControllerAdvice
class ChatExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    fun handleException(ex: Exception): ErrorMessage {
        log.error("Message handling error: ${ex.message}", ex)
        return ErrorMessage(error = ex.message ?: "Unknown error")
    }
}

data class ErrorMessage(val error: String)
