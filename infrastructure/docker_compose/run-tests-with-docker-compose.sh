#!/bin/bash
set -euo pipefail

# Git Bash otherwise rewrites "/app/..." inside -v and mounts break (empty folders, "logs;C")
export MSYS_NO_PATHCONV=1

# Script lives in infrastructure/docker_compose/ (next to docker-compose.yml)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
IMAGE_NAME=nbank-tests
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR="$PROJECT_ROOT/test-output/$TIMESTAMP"
TEST_EXIT_CODE=0

# Docker Desktop needs Windows paths; Git Bash gives /c/Users/... -> broken C:\c\Users\...
to_docker_path() {
  if command -v cygpath >/dev/null 2>&1; then
    cygpath -w "$1"
  else
    printf '%s\n' "$1"
  fi
}

PROJECT_ROOT_DOCKER="$(to_docker_path "$PROJECT_ROOT")"

# From inside the test container, localhost is the container itself.
APIBASEURL="${APIBASEURL:-http://host.docker.internal:4111}"
UIBASEURL="${UIBASEURL:-http://host.docker.internal:3000}"
SELENOID_URL="${SELENOID_URL:-http://host.docker.internal:4444}"
SELENOID_UI_URL="${SELENOID_UI_URL:-http://host.docker.internal:8080}"
DB_URL="${DB_URL:-jdbc:postgresql://host.docker.internal:5432/nbank}"

cleanup() {
  echo
  echo ">>> Останавливаем тестовое окружение (docker compose down)..."
  # cd into compose dir — no -f path needed (avoids Git Bash C:\c\... bug)
  (cd "$SCRIPT_DIR" && docker compose down) || true
  echo ">>> Тестовое окружение остановлено"
}
trap cleanup EXIT

echo "=== nbank: окружение + все тесты ==="
echo "Compose dir: $SCRIPT_DIR"
echo "Project:     $PROJECT_ROOT_DOCKER"
echo

echo ">>> [1/5] Запускаем тестовое окружение (docker compose up -d)..."
(cd "$SCRIPT_DIR" && docker compose up -d)

echo ">>> [2/5] Ожидаем запуск сервисов..."
for i in $(seq 1 60); do
  if curl -sf "$APIBASEURL/actuator/health" >/dev/null 2>&1 \
     && curl -sf "$UIBASEURL" >/dev/null 2>&1 \
     && curl -sf "${SELENOID_URL}/status" >/dev/null 2>&1; then
    echo "    API, UI и Selenoid готовы"
    break
  fi
  if [ "$i" -eq 60 ]; then
    echo "ERROR: сервисы не поднялись вовремя"
    (cd "$SCRIPT_DIR" && docker compose ps)
    exit 1
  fi
  sleep 2
done

echo ">>> [3/5] Собираем образ тестов ($IMAGE_NAME)..."
docker build -t "$IMAGE_NAME" "$PROJECT_ROOT_DOCKER"

mkdir -p "$TEST_OUTPUT_DIR/logs" "$TEST_OUTPUT_DIR/results" "$TEST_OUTPUT_DIR/report"
HOST_LOGS="$(to_docker_path "$TEST_OUTPUT_DIR/logs")"
HOST_RESULTS="$(to_docker_path "$TEST_OUTPUT_DIR/results")"
HOST_REPORT="$(to_docker_path "$TEST_OUTPUT_DIR/report")"

echo ">>> [4/5] Запускаем все API + UI тесты в контейнере..."
echo "    APIBASEURL=$APIBASEURL"
echo "    UIBASEURL=$UIBASEURL"
echo "    SELENOID_URL=$SELENOID_URL"
echo "    SELENOID_UI_URL=$SELENOID_UI_URL"
echo "    DB_URL=$DB_URL"

set +e
docker run --rm \
  --add-host=host.docker.internal:host-gateway \
  -v "${HOST_LOGS}:/app/logs" \
  -v "${HOST_RESULTS}:/app/target/surefire-reports" \
  -v "${HOST_REPORT}:/app/target/site" \
  -e "APIBASEURL=$APIBASEURL" \
  -e "UIBASEURL=$UIBASEURL" \
  -e "SELENOID_URL=$SELENOID_URL" \
  -e "SELENOID_UI_URL=$SELENOID_UI_URL" \
  -e "UIREMOTE=${SELENOID_URL}/wd/hub" \
  -e "DB_URL=$DB_URL" \
  "$IMAGE_NAME" \
  bash -c 'set -o pipefail; mkdir -p /app/logs /app/target/surefire-reports /app/target/site; {
    echo ">>> Running ALL tests (API + UI, no Maven profile filter)";
    mvn test;
    echo ">>> Generating surefire-report";
    mvn -DskipTests=true surefire-report:report;
  } 2>&1 | tee /app/logs/run.log'
TEST_EXIT_CODE=$?
set -e

echo
echo ">>> [5/5] Тесты завершены с кодом: $TEST_EXIT_CODE"
echo "    Log:     $TEST_OUTPUT_DIR/logs/run.log"
echo "    Results: $TEST_OUTPUT_DIR/results"
echo "    Report:  $TEST_OUTPUT_DIR/report"
echo
echo "(Тестовое окружение будет остановлено)"

exit "$TEST_EXIT_CODE"
