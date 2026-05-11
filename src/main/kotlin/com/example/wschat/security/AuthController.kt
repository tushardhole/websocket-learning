package com.example.wschat.security

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(private val jwtUtil: JwtUtil) {

    @PostMapping("/auth/login")
    fun login(@RequestBody request: LoginRequest): LoginResponse {
        val token = jwtUtil.generateToken(request.username, request.role ?: "USER")
        return LoginResponse(token = token, username = request.username)
    }
}

data class LoginRequest(val username: String, val role: String? = null)
data class LoginResponse(val token: String, val username: String)
