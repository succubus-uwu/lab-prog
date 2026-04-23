#!/bin/bash
# Run server script

GSON_JAR="/tmp/gson-2.10.1.jar"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ -z "$FILE" ]; then
    echo "Error: FILE environment variable not set"
    echo "Usage: export FILE=/path/to/persons.json && $0"
    exit 1
fi

PORT=${1:-9999}
HOST=${2:-localhost}

echo "Starting server on $HOST:$PORT"
echo "Collection file: $FILE"
echo ""

java -cp "$GSON_JAR:$PROJECT_DIR/server/target/classes:$PROJECT_DIR/common/target/classes" \
    axl.itmo.server.ServerApp "$PORT" "$HOST"

