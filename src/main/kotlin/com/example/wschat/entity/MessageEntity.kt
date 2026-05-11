package com.example.wschat.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "messages")
class MessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val sender: String,

    @Column(nullable = false, length = 1000)
    val content: String,

    @Column(nullable = false)
    val type: String,

    @Column
    val recipient: String? = null,

    @Column(nullable = false)
    val timestamp: Instant = Instant.now()
)
