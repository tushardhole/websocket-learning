package com.example.wschat.service

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class PresenceService(
    private val redisTemplate: StringRedisTemplate
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val PRESENCE_PREFIX = "presence:"
        private val ONLINE_TTL = Duration.ofSeconds(30)
    }

    fun setOnline(username: String) {
        redisTemplate.opsForValue().set("$PRESENCE_PREFIX$username", "ONLINE", ONLINE_TTL)
    }

    fun setAway(username: String) {
        redisTemplate.opsForValue().set("$PRESENCE_PREFIX$username", "AWAY", ONLINE_TTL)
    }

    fun setOffline(username: String) {
        redisTemplate.delete("$PRESENCE_PREFIX$username")
    }

    fun getStatus(username: String): String {
        return redisTemplate.opsForValue().get("$PRESENCE_PREFIX$username") ?: "OFFLINE"
    }

    fun getAllOnlineUsers(): Map<String, String> {
        val keys = redisTemplate.keys("$PRESENCE_PREFIX*")
        return keys.associate { key ->
            val username = key.removePrefix(PRESENCE_PREFIX)
            val status = redisTemplate.opsForValue().get(key) ?: "OFFLINE"
            username to status
        }
    }

    fun heartbeat(username: String) {
        redisTemplate.expire("$PRESENCE_PREFIX$username", ONLINE_TTL)
    }
}
