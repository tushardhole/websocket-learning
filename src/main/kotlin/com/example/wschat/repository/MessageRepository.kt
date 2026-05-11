package com.example.wschat.repository

import com.example.wschat.entity.MessageEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MessageRepository : JpaRepository<MessageEntity, Long> {

    fun findByRecipientIsNullOrderByTimestampDesc(pageable: Pageable): List<MessageEntity>

    @Query("SELECT m FROM MessageEntity m WHERE m.recipient = :username OR m.sender = :username ORDER BY m.timestamp DESC")
    fun findPrivateMessages(username: String, pageable: Pageable): List<MessageEntity>
}
