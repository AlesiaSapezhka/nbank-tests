#!/bin/bash

# Git Bash otherwise rewrites "/app/..." inside -v and mounts break (empty folders, "logs;C")
export MSYS_NO_PATHCONV=1

IMAGE_NAME=nbank-tests
TEST_PROFILE=${1:-api}
TIMESTAMP=$(date +"%Y%m%d_%H%M")
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TEST_OUTPUT_DIR="$SCRIPT_DIR/test-output/$TIMESTAMP"

echo ">>> Сборка тестов запущена"
docker build -t "$IMAGE_NAME" .

mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

# Docker Desktop on Windows needs a Windows host path for bind mounts
if command -v cygpath >/dev/null 2>&1; then
  HOST_LOGS="$(cygpath -w "$TEST_OUTPUT_DIR/logs")"
  HOST_RESULTS="$(cygpath -w "$TEST_OUTPUT_DIR/results")"
  HOST_REPORT="$(cygpath -w "$TEST_OUTPUT_DIR/report")"
else
  HOST_LOGS="$TEST_OUTPUT_DIR/logs"
  HOST_RESULTS="$TEST_OUTPUT_DIR/results"
  HOST_REPORT="$TEST_OUTPUT_DIR/report"
fi

echo ">>> Тесты запущены"
docker run --rm \
  -p 8080:8080 \
  -v "${HOST_LOGS}:/app/logs" \
  -v "${HOST_RESULTS}:/app/target/surefire-reports" \
  -v "${HOST_REPORT}:/app/target/site" \
  -e "TEST_PROFILE=$TEST_PROFILE" \
  -e "APIBASEURL=http://host.docker.internal:4111" \
  -e "UIBASEURL=http://host.docker.internal:3000" \
  "$IMAGE_NAME"

echo ">>> Тесты завершены"
echo "Лог файл: $TEST_OUTPUT_DIR/logs/run.log"
echo "Результаты тестов: $TEST_OUTPUT_DIR/results"
echo "Репорт: $TEST_OUTPUT_DIR/report"
