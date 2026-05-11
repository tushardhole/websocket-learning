package com.example.wschat.model

data class ChatMessage(
    val id: Long? = null,
    val sender: String,
    val content: String,
    val type: MessageType = MessageType.CHAT,
    val timestamp: Long? = null
)

enum class MessageType {
    CHAT, JOIN, LEAVE, SERVER_SHUTDOWN
}
