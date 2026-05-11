package com.example.wschat.service

import com.example.wschat.entity.MessageEntity
import com.example.wschat.model.ChatMessage
import com.example.wschat.model.MessageType
import com.example.wschat.repository.MessageRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ChatService(private val messageRepository: MessageRepository) {

    fun savePublicMessage(message: ChatMessage): MessageEntity {
        return messageRepository.save(
            MessageEntity(
                sender = message.sender,
                content = message.content,
                type = message.type.name
            )
        )
    }

    fun savePrivateMessage(sender: String, recipient: String, content: String): MessageEntity {
        return messageRepository.save(
            MessageEntity(
                sender = sender,
                content = content,
                type = MessageType.CHAT.name,
                recipient = recipient
            )
        )
    }

    fun getRecentPublicMessages(limit: Int = 50): List<ChatMessage> {
        return messageRepository.findByRecipientIsNullOrderByTimestampDesc(PageRequest.of(0, limit))
            .reversed()
            .filter { it.type == MessageType.CHAT.name }
            .map { ChatMessage(id = it.id, sender = it.sender, content = it.content, type = MessageType.CHAT, timestamp = it.timestamp.toEpochMilli()) }
    }

    fun getPrivateMessages(username: String, limit: Int = 50): List<ChatMessage> {
        return messageRepository.findPrivateMessages(username, PageRequest.of(0, limit))
            .reversed()
            .map { ChatMessage(id = it.id, sender = it.sender, content = it.content, type = MessageType.CHAT, timestamp = it.timestamp.toEpochMilli()) }
    }
}
