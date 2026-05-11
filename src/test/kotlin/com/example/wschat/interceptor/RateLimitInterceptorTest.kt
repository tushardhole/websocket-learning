package com.example.wschat.interceptor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RateLimitInterceptorTest {

    @Test
    fun `token bucket allows burst up to capacity`() {
        val bucket = TokenBucket(5, 5)
        repeat(5) {
            assertTrue(bucket.tryConsume(), "Should allow consume #$it")
        }
        assertFalse(bucket.tryConsume(), "Should reject after capacity exceeded")
    }

    @Test
    fun `token bucket refills over time`() {
        val bucket = TokenBucket(2, 100)
        assertTrue(bucket.tryConsume())
        assertTrue(bucket.tryConsume())
        assertFalse(bucket.tryConsume())

        Thread.sleep(50)
        assertTrue(bucket.tryConsume(), "Should have refilled after delay")
    }
}
