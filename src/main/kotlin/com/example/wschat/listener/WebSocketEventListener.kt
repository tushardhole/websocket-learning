package com.example.wschat.listener

import com.example.wschat.model.ChatMessage
import com.example.wschat.model.MessageType
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketEventListener(
    private val messagingTemplate: SimpMessageSendingOperations
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun handleConnect(event: SessionConnectedEvent) {
        val username = event.user?.name ?: "unknown"
        log.info("User connected: $username")
        messagingTemplate.convertAndSend(
            "/topic/messages",
            ChatMessage(sender = username, content = "", type = MessageType.JOIN)
        )
    }

    @EventListener
    fun handleDisconnect(event: SessionDisconnectEvent) {
        val username = event.user?.name ?: "unknown"
        log.info("User disconnected: $username")
        messagingTemplate.convertAndSend(
            "/topic/messages",
            ChatMessage(sender = username, content = "", type = MessageType.LEAVE)
        )
    }
}
