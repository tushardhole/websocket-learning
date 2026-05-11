#!/bin/bash
# Simulates a pod restart by stopping and restarting an app instance
# Usage: ./scripts/simulate-restart.sh [app1|app2]

CONTAINER=${1:-app1}

echo "=== Simulating restart of $CONTAINER ==="
echo "1. Stopping $CONTAINER (graceful shutdown will notify clients)..."
docker-compose stop "$CONTAINER"

echo ""
echo "2. Waiting 5 seconds (clients should be reconnecting)..."
sleep 5

echo ""
echo "3. Starting $CONTAINER back up..."
docker-compose start "$CONTAINER"

echo ""
echo "=== Restart complete ==="
echo "Clients should have reconnected automatically."
