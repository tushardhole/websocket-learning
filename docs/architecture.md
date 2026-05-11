# Production Architecture

## System Overview

```
                    ┌─────────┐
                    │  Client  │ (Browser / Mobile)
                    └────┬─────┘
                         │ WebSocket (SockJS + STOMP)
                    ┌────▼─────┐
                    │  Nginx   │ (Load Balancer, ip_hash)
                    └────┬─────┘
              ┌──────────┼──────────┐
         ┌────▼────┐          ┌────▼────┐
         │  App 1  │          │  App 2  │ (Spring Boot)
         └────┬────┘          └────┬────┘
              │                    │
         ┌────▼────────────────────▼────┐
         │           Redis              │
         │  • Pub/Sub (message relay)   │
         │  • Sessions (ws:sessions)    │
         │  • Presence (TTL keys)       │
         │  • Delivery ACKs             │
         └──────────────┬───────────────┘
                        │
                   ┌────▼────┐
                   │  H2 DB  │ (Message persistence)
                   └─────────┘

         ┌──────────┐     ┌──────────┐
         │Prometheus │────▶│ Grafana  │ (Monitoring)
         └──────────┘     └──────────┘
```

## Message Flow

### Public Message
1. Client sends STOMP SEND to `/app/chat`
2. Spring routes to `ChatController.sendMessage()`
3. Message saved to H2 (gets an ID + timestamp)
4. Enriched message published to Redis channel `chat:messages`
5. All instances receive via `RedisMessageSubscriber.onPublicMessage()`
6. Each instance broadcasts to local subscribers on `/topic/messages`
7. Client receives message with ID — sends ACK to `/app/ack`

### Private Message
1. Client sends STOMP SEND to `/app/private`
2. Spring routes to `ChatController.sendPrivateMessage()`
3. Message saved to H2
4. `PrivateEnvelope` published to Redis channel `chat:private`
5. All instances receive and check if recipient is locally connected
6. `convertAndSendToUser()` delivers to the right user session

### Presence
1. On WebSocket connect → `PresenceService.setOnline(username)` sets Redis key `presence:{username}=ONLINE` with 30s TTL
2. Client periodically sends heartbeat to `/app/chat.heartbeat` → refreshes TTL
3. On disconnect → `PresenceService.setOffline(username)` deletes key
4. If client crashes (no disconnect event) → key expires after 30s → user appears OFFLINE
5. REST endpoint `GET /presence` returns all online users with status

## Scaling Strategy

### Horizontal Scaling
- **Stateless app servers**: All shared state lives in Redis
- **Nginx ip_hash**: Ensures WebSocket connections stick to one pod
- **Redis pub/sub**: Cross-instance message relay (broadcast approach)

### Trade-offs
- **Broadcast inefficiency**: Every instance receives every private message, only one delivers it. Acceptable at moderate scale (<10 instances). At higher scale, consider instance-level Redis channels or Redis Streams with consumer groups.
- **H2 in-memory DB**: Each instance has its own DB. For production, replace with PostgreSQL.
- **Simple broker**: Spring's SimpleBroker is in-memory. For very high throughput, consider an external STOMP broker (RabbitMQ, ActiveMQ).

## Delivery Guarantees

### At-Least-Once Delivery
1. Server persists message to DB before broadcasting
2. Message includes a unique ID
3. Client sends ACK after receiving and displaying message
4. Server tracks ACKs in Redis (`msg:ack:{id}:{user}`)
5. On reconnect, client fetches history — messages with known IDs are deduplicated client-side

### Deduplication
- Each message has a unique database ID
- Client maintains a Set of seen message IDs
- Duplicate messages (from reconnect + history load) are filtered

## Security Layers

| Layer | Mechanism |
|-------|-----------|
| Handshake | JWT validation in `UserHandshakeInterceptor` |
| STOMP Connect | Principal created in `UserPrincipalHandshakeHandler` |
| Subscribe | Role check in `StompChannelInterceptor` (/topic/admin*) |
| Send | Rate limiting in `RateLimitInterceptor` (5 msg/sec) |
| Message | Content size validation (500 char limit) |

## Monitoring

### Metrics (Micrometer → Prometheus → Grafana)
- `ws.connections.active` — gauge, synced every 10s
- `ws.messages.received` — counter, per incoming message
- `ws.messages.sent` — counter, per broadcast
- `ws.ratelimit.hits` — counter, rate limit rejections
- `ws.message.processing.time` — timer, end-to-end latency

### Health
- `GET /actuator/health` — Spring Boot health check
- `GET /actuator/prometheus` — Prometheus scrape endpoint

## Graceful Shutdown

1. SIGTERM received by pod
2. `@PreDestroy` in `GracefulShutdownHandler` fires
3. `SERVER_SHUTDOWN` message sent to `/topic/system`
4. 1-second grace period for clients to receive notification
5. Redis session registry cleaned up for this instance
6. Spring's graceful shutdown drains in-flight requests (30s timeout)
7. Clients detect disconnect and begin reconnection with exponential backoff

## Key Design Decisions

1. **Redis for presence over DB polling**: TTL-based keys give automatic expiry — no cleanup cron needed
2. **Message IDs for dedup over sequence numbers**: Simpler, works across reconnects without per-user state
3. **SockJS over raw WebSocket**: Automatic fallback for environments that block WebSocket upgrades
4. **JWT over session cookies**: Stateless auth, works with load balancers without sticky sessions for the auth layer
5. **Broadcast pub/sub over routed delivery**: Simpler implementation, acceptable at 2-10 instances
