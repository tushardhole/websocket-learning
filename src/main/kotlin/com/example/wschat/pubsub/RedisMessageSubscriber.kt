package com.example.wschat.pubsub

import com.example.wschat.model.ChatMessage
import com.example.wschat.model.MessageType
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Component

@Component
class RedisMessageSubscriber(
    private val messagingTemplate: SimpMessageSendingOperations,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun onPublicMessage(message: String) {
        log.debug("Received from Redis chat:messages: $message")
        val chatMessage = objectMapper.readValue(message, ChatMessage::class.java)
        messagingTemplate.convertAndSend("/topic/messages", chatMessage)
    }

    fun onPrivateMessage(message: String) {
        log.debug("Received from Redis chat:private: $message")
        val envelope = objectMapper.readValue(message, PrivateEnvelope::class.java)
        messagingTemplate.convertAndSendToUser(
            envelope.recipient,
            "/queue/private",
            ChatMessage(id = envelope.messageId, sender = envelope.sender, content = envelope.content, type = MessageType.CHAT)
        )
    }
}
