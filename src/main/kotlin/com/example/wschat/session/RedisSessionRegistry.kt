package com.example.wschat.session

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisSessionRegistry(
    private val redisTemplate: StringRedisTemplate,
    @Value("\${app.instance-id:local}") private val instanceId: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun registerUser(username: String) {
        redisTemplate.opsForHash<String, String>()
            .put("ws:sessions", username, instanceId)
        redisTemplate.opsForValue()
            .set("ws:user:$username:lastSeen", System.currentTimeMillis().toString(), Duration.ofHours(24))
        log.info("Registered $username on $instanceId")
    }

    fun unregisterUser(username: String) {
        val currentInstance = redisTemplate.opsForHash<String, String>()
            .get("ws:sessions", username)
        if (currentInstance == instanceId) {
            redisTemplate.opsForHash<String, String>()
                .delete("ws:sessions", username)
        }
        redisTemplate.opsForValue()
            .set("ws:user:$username:lastSeen", System.currentTimeMillis().toString(), Duration.ofHours(24))
        log.info("Unregistered $username from $instanceId")
    }

    fun getUserInstance(username: String): String? {
        return redisTemplate.opsForHash<String, String>()
            .get("ws:sessions", username)
    }

    fun getLastSeen(username: String): Long? {
        return redisTemplate.opsForValue()
            .get("ws:user:$username:lastSeen")?.toLongOrNull()
    }

    fun cleanupInstance() {
        val sessions = redisTemplate.opsForHash<String, String>().entries("ws:sessions")
        val removed = sessions.filter { it.value == instanceId }.keys
        removed.forEach { username ->
            redisTemplate.opsForHash<String, String>().delete("ws:sessions", username)
        }
        log.info("Cleaned up ${removed.size} sessions for $instanceId")
    }
}
