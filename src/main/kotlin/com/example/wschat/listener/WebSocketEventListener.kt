package com.example.wschat.listener

import com.example.wschat.model.ChatMessage
import com.example.wschat.model.MessageType
import com.example.wschat.pubsub.RedisMessagePublisher
import com.example.wschat.session.RedisSessionRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketEventListener(
    private val redisPublisher: RedisMessagePublisher,
    private val sessionRegistry: RedisSessionRegistry
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun handleConnect(event: SessionConnectedEvent) {
        val username = event.user?.name ?: "unknown"
        log.info("User connected: $username")
        sessionRegistry.registerUser(username)
        redisPublisher.publishPublicMessage(
            ChatMessage(sender = username, content = "", type = MessageType.JOIN)
        )
    }

    @EventListener
    fun handleDisconnect(event: SessionDisconnectEvent) {
        val username = event.user?.name ?: "unknown"
        log.info("User disconnected: $username")
        sessionRegistry.unregisterUser(username)
        redisPublisher.publishPublicMessage(
            ChatMessage(sender = username, content = "", type = MessageType.LEAVE)
        )
    }
}
