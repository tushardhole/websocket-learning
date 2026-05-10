package com.example.wschat.handler

import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

class EchoWebSocketHandler : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info("Connection opened: id=${session.id}, remote=${session.remoteAddress}")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        log.info("Received from ${session.id}: ${message.payload}")
        session.sendMessage(TextMessage("Echo: ${message.payload}"))
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        log.info("Connection closed: id=${session.id}, status=${status.code} ${status.reason ?: ""}")
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        log.error("Transport error on ${session.id}: ${exception.message}")
    }
}
