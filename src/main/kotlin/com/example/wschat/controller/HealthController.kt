package com.example.wschat.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController(
    @Value("\${app.instance-id:local}") private val instanceId: String
) {

    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf("status" to "UP", "instance" to instanceId)
}
