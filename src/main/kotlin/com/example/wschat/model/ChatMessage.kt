package com.example.wschat.model

data class ChatMessage(
    val sender: String,
    val content: String,
    val type: MessageType = MessageType.CHAT
)

enum class MessageType {
    CHAT, JOIN, LEAVE
}
