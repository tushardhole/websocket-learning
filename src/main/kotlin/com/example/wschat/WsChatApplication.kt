package com.example.wschat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WsChatApplication

fun main(args: Array<String>) {
    runApplication<WsChatApplication>(*args)
}
