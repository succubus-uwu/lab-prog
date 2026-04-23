#!/bin/bash
# Run client script

GSON_JAR="/tmp/gson-2.10.1.jar"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

HOST=${1:-localhost}
PORT=${2:-9999}

echo "Connecting to server at $HOST:$PORT"
echo ""

java -cp "$GSON_JAR:$PROJECT_DIR/client/target/classes:$PROJECT_DIR/common/target/classes" \
    axl.itmo.client.ClientApp "$HOST" "$PORT"

