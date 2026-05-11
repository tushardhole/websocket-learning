package com.example.wschat.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import java.util.concurrent.TimeUnit

class PresenceServiceTest {

    private val redisTemplate: StringRedisTemplate = mock()
    private val valueOps: ValueOperations<String, String> = mock()
    private lateinit var presenceService: PresenceService

    @BeforeEach
    fun setup() {
        whenever(redisTemplate.opsForValue()).thenReturn(valueOps)
        presenceService = PresenceService(redisTemplate)
    }

    @Test
    fun `setOnline stores ONLINE with TTL`() {
        presenceService.setOnline("alice")
        verify(valueOps).set("presence:alice", "ONLINE", Duration.ofSeconds(30))
    }

    @Test
    fun `setAway stores AWAY with TTL`() {
        presenceService.setAway("alice")
        verify(valueOps).set("presence:alice", "AWAY", Duration.ofSeconds(30))
    }

    @Test
    fun `setOffline deletes key`() {
        presenceService.setOffline("alice")
        verify(redisTemplate).delete("presence:alice")
    }

    @Test
    fun `getStatus returns ONLINE when key exists`() {
        whenever(valueOps.get("presence:alice")).thenReturn("ONLINE")
        assertEquals("ONLINE", presenceService.getStatus("alice"))
    }

    @Test
    fun `getStatus returns OFFLINE when key is missing`() {
        whenever(valueOps.get("presence:alice")).thenReturn(null)
        assertEquals("OFFLINE", presenceService.getStatus("alice"))
    }

    @Test
    fun `heartbeat refreshes TTL`() {
        presenceService.heartbeat("alice")
        verify(redisTemplate).expire("presence:alice", Duration.ofSeconds(30))
    }
}
