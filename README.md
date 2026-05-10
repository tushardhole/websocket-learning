# WebSocket Learning — Spring Boot + Kotlin

A practical, incremental learning project for WebSockets. Each level builds on the previous, evolving a simple echo server into a production-grade, horizontally-scalable chat system.

## Tech Stack

- **Backend:** Spring Boot 3.3 + Kotlin
- **Build:** Gradle (Kotlin DSL)
- **Runtime:** JDK 17+

## Curriculum

| Level | Title | What You Learn |
|-------|-------|----------------|
| 0 | Project Scaffold | Spring Boot + Kotlin project setup |
| 1 | Echo Server | Raw WebSocket, handshake, full-duplex communication |
| 2 | STOMP Chat Room | STOMP protocol, `@MessageMapping`, pub/sub |
| 3 | Users & Private Messages | Sessions, user identity, `@SendToUser` |
| 4 | Error Handling & Resilience | Heartbeats, reconnection, exception handling |
| 5 | Security | JWT authentication, channel interceptors |
| 6 | Persistence | Message history, JPA, hybrid REST + WebSocket |
| 7 | Scaling Out | Redis pub/sub, multi-instance, load balancer |
| 8 | Pod Restarts | Graceful shutdown, session recovery |
| 9 | Performance | Rate limiting, metrics, load testing |
| 10 | Production Architecture | Presence, delivery guarantees, observability |

## Running

```bash
./gradlew bootRun
```

Then visit: http://localhost:8080/health

## Structure

Each level is developed on its own branch and merged via PR. Check the PR descriptions for detailed explanations of each concept.
