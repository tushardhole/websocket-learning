package com.example.wschat.controller

import com.example.wschat.service.PresenceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/presence")
class PresenceController(
    private val presenceService: PresenceService
) {

    @GetMapping
    fun getAllPresence(): Map<String, String> {
        return presenceService.getAllOnlineUsers()
    }

    @GetMapping("/{username}")
    fun getUserPresence(@PathVariable username: String): Map<String, String> {
        return mapOf("username" to username, "status" to presenceService.getStatus(username))
    }
}
