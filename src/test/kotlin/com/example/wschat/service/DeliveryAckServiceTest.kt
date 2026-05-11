package com.example.wschat.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration

class DeliveryAckServiceTest {

    private val redisTemplate: StringRedisTemplate = mock()
    private val valueOps: ValueOperations<String, String> = mock()
    private val setOps: SetOperations<String, String> = mock()
    private lateinit var ackService: DeliveryAckService

    @BeforeEach
    fun setup() {
        whenever(redisTemplate.opsForValue()).thenReturn(valueOps)
        whenever(redisTemplate.opsForSet()).thenReturn(setOps)
        ackService = DeliveryAckService(redisTemplate)
    }

    @Test
    fun `markPending adds messageId to recipient's pending set`() {
        ackService.markPending(42, "bob")
        verify(setOps).add("msg:pending:bob", "42")
        verify(redisTemplate).expire("msg:pending:bob", Duration.ofHours(24))
    }

    @Test
    fun `acknowledge removes from pending and sets ack key`() {
        ackService.acknowledge(42, "bob")
        verify(setOps).remove("msg:pending:bob", "42")
        verify(valueOps).set("msg:ack:42:bob", "ACK", Duration.ofHours(24))
    }

    @Test
    fun `isAcknowledged returns true when ack key exists`() {
        whenever(redisTemplate.hasKey("msg:ack:42:bob")).thenReturn(true)
        assertTrue(ackService.isAcknowledged(42, "bob"))
    }

    @Test
    fun `getPendingMessages returns pending set`() {
        whenever(setOps.members("msg:pending:bob")).thenReturn(setOf("42", "43"))
        assertEquals(setOf("42", "43"), ackService.getPendingMessages("bob"))
    }
}
