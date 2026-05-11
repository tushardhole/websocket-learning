// k6 WebSocket load test
// Install: brew install k6
// Run: k6 run loadtest/websocket-loadtest.js

import ws from 'k6/ws';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import http from 'k6/http';

const messagesReceived = new Counter('ws_messages_received');
const messageLatency = new Trend('ws_message_latency_ms');

export const options = {
    stages: [
        { duration: '10s', target: 50 },   // Ramp up to 50 users
        { duration: '30s', target: 50 },   // Hold at 50
        { duration: '10s', target: 100 },  // Ramp to 100
        { duration: '30s', target: 100 },  // Hold at 100
        { duration: '10s', target: 0 },    // Ramp down
    ],
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
    const username = `user-${__VU}-${__ITER}`;

    // Login to get JWT
    const loginRes = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
        username: username,
        role: 'USER'
    }), { headers: { 'Content-Type': 'application/json' } });

    check(loginRes, { 'login success': (r) => r.status === 200 });

    const token = JSON.parse(loginRes.body).token;
    const wsUrl = BASE_URL.replace('http', 'ws') + `/ws/chat?token=${token}`;

    const res = ws.connect(wsUrl, {}, function (socket) {
        socket.on('open', function () {
            // STOMP CONNECT frame
            socket.send('CONNECT\naccept-version:1.2\n\n\0');
        });

        socket.on('message', function (msg) {
            messagesReceived.add(1);

            if (msg.includes('CONNECTED')) {
                // Subscribe
                socket.send('SUBSCRIBE\nid:sub-0\ndestination:/topic/messages\n\n\0');

                // Send messages periodically
                for (let i = 0; i < 5; i++) {
                    const sendTime = Date.now();
                    const payload = JSON.stringify({
                        sender: username,
                        content: `Load test message ${i} from ${username}`,
                        type: 'CHAT'
                    });
                    socket.send(`SEND\ndestination:/app/chat\ncontent-type:application/json\n\n${payload}\0`);
                    sleep(1);
                }
            }
        });

        socket.on('error', function (e) {
            console.error('WebSocket error:', e.error());
        });

        // Keep connection open for the test duration
        sleep(10);

        // STOMP DISCONNECT
        socket.send('DISCONNECT\n\n\0');
        socket.close();
    });

    check(res, { 'ws connected': (r) => r && r.status === 101 });
}
