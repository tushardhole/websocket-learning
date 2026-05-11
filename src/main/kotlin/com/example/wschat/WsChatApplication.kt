package com.example.wschat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class WsChatApplication

fun main(args: Array<String>) {
    runApplication<WsChatApplication>(*args)
}
