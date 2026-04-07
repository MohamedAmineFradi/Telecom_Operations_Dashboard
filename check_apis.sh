#!/usr/bin/env bash
set -u

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

START_STACK=1
NO_BUILD=0
TIMEOUT_SECONDS="${CHECK_APIS_TIMEOUT_SECONDS:-420}"
INTERVAL_SECONDS=5

usage() {
  cat <<'EOF'
Usage: ./check_apis.sh [options]

Starts core API services (optional), waits for them, and checks HTTP health.

Options:
  --no-start     Do not start services; only run checks.
  --no-build     Start services without rebuilding images.
  -h, --help     Show this help message.

Environment variables:
  CHECK_APIS_TIMEOUT_SECONDS   Max wait time for startup (default: 420)
EOF
}

for arg in "$@"; do
  case "$arg" in
    --no-start) START_STACK=0 ;;
    --no-build) NO_BUILD=1 ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $arg"
      usage
      exit 1
      ;;
  esac
done

if docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD=(docker-compose)
else
  echo "ERROR: Neither 'docker compose' nor 'docker-compose' is available."
  exit 1
fi

SERVICES=(
  eureka-server
  kafka
  kafka-init
  traffic-service
  sms-service
  call-service
  internet-service
  alert-service
  cell-service
  mobility-service
  producer
  gateway-service
)

# kafka-init is a one-shot setup job and should not be treated as long-running.
WAIT_SERVICES=(
  eureka-server
  kafka
  traffic-service
  sms-service
  call-service
  internet-service
  alert-service
  cell-service
  mobility-service
  producer
  gateway-service
)

# name|url|kind
HEALTH_CHECKS=(
  "traffic-service|http://localhost:8103/actuator/health|strict-up"
  "sms-service|http://localhost:8105/actuator/health|strict-up"
  "call-service|http://localhost:8106/actuator/health|strict-up"
  "internet-service|http://localhost:8107/actuator/health|strict-up"
  "alert-service|http://localhost:8101/actuator/health|strict-up"
  "cell-service|http://localhost:8102/actuator/health|strict-up"
  "mobility-service|http://localhost:8104/actuator/health|strict-up"
  "producer|http://localhost:8108/actuator/health|strict-up"
  "gateway-service|http://localhost:8090/actuator/health|http-only"
)

# name|url|kind
BUSINESS_CHECKS=(
  "traffic-service|http://localhost:8103/api/traffic/current/stream?intervalMs=1000|sse-200"
  "traffic-service-heatmap|http://localhost:8103/api/traffic/heatmap/stream?intervalMs=1000&limit=10|sse-200"
  "traffic-service-congestion|http://localhost:8103/api/traffic/congestion/stream?intervalMs=1000&limit=10|sse-200"
  "traffic-service-heatmap-realtime|http://localhost:8103/api/traffic/heatmap/realtime/stream|sse-200"
  "traffic-service-congestion-realtime|http://localhost:8103/api/traffic/congestion/realtime/stream|sse-200"
  "traffic-service-heatmap-history|http://localhost:8103/api/traffic/heatmap/history?durationHours=1&limit=5|json-2xx"
  "traffic-service-congestion-history|http://localhost:8103/api/traffic/congestion/history?durationHours=1&limit=5|json-2xx"
  "sms-service|http://localhost:8105/api/sms/current/stream?intervalMs=1000|sse-200"
  "call-service|http://localhost:8106/api/call/current/stream?intervalMs=1000|sse-200"
  "internet-service|http://localhost:8107/api/internet/current/stream?intervalMs=1000|sse-200"
  "alert-service-list|http://localhost:8101/api/alerts|json-2xx"
  "alert-service|http://localhost:8101/api/alerts/page?page=0&size=1|json-2xx"
  "cell-service-list|http://localhost:8102/api/cells|json-2xx"
  "cell-service|http://localhost:8102/api/cells/page?page=0&size=1|json-2xx"
  "province-service|http://localhost:8102/api/provinces|json-2xx"
  "mobility-service-cell-flows|http://localhost:8104/api/mobility/mobility/cell-flows?limit=10|json-2xx"
  "mobility-service-province-flows|http://localhost:8104/api/mobility/province-flows?limit=10|json-2xx"
  "mobility-service-province-summary|http://localhost:8104/api/mobility/province-summary?limit=10|json-2xx"
  "mobility-service|http://localhost:8104/api/mobility/stats|json-2xx"
  "gateway-service|http://localhost:8090/api/cells/page?page=0&size=1|json-2xx"
)

# Frontend-facing checks through gateway only.
GATEWAY_BUSINESS_CHECKS=(
  "gateway-traffic|http://localhost:8090/api/traffic/current/stream?intervalMs=1000|sse-200"
  "gateway-traffic-heatmap|http://localhost:8090/api/traffic/heatmap/stream?intervalMs=1000&limit=10|sse-200"
  "gateway-traffic-congestion|http://localhost:8090/api/traffic/congestion/stream?intervalMs=1000&limit=10|sse-200"
  "gateway-traffic-heatmap-realtime|http://localhost:8090/api/traffic/heatmap/realtime/stream|sse-200"
  "gateway-traffic-congestion-realtime|http://localhost:8090/api/traffic/congestion/realtime/stream|sse-200"
  "gateway-traffic-heatmap-history|http://localhost:8090/api/traffic/heatmap/history?durationHours=1&limit=5|json-2xx"
  "gateway-traffic-congestion-history|http://localhost:8090/api/traffic/congestion/history?durationHours=1&limit=5|json-2xx"
  "gateway-sms|http://localhost:8090/api/sms/current/stream?intervalMs=1000|sse-200"
  "gateway-call|http://localhost:8090/api/call/current/stream?intervalMs=1000|sse-200"
  "gateway-internet|http://localhost:8090/api/internet/current/stream?intervalMs=1000|sse-200"
  "gateway-alerts-list|http://localhost:8090/api/alerts|json-2xx"
  "gateway-alerts|http://localhost:8090/api/alerts/page?page=0&size=1|json-2xx"
  "gateway-cells-list|http://localhost:8090/api/cells|json-2xx"
  "gateway-cells|http://localhost:8090/api/cells/page?page=0&size=1|json-2xx"
  "gateway-provinces|http://localhost:8090/api/provinces|json-2xx"
  "gateway-mobility-cell-flows|http://localhost:8090/api/mobility/mobility/cell-flows?limit=10|json-2xx"
  "gateway-mobility-province-flows|http://localhost:8090/api/mobility/province-flows?limit=10|json-2xx"
  "gateway-mobility-province-summary|http://localhost:8090/api/mobility/province-summary?limit=10|json-2xx"
  "gateway-mobility-stream-alias-current|http://localhost:8090/api/mobility/mobility/current/stream?intervalMs=1000|sse-200"
  "gateway-mobility-stream-alias-flows|http://localhost:8090/api/mobility/mobility/flows/stream?intervalMs=1000|sse-200"
  "gateway-mobility-stream-alias-cell|http://localhost:8090/api/mobility/mobility/cell-flows/stream?intervalMs=1000|sse-200"
  "gateway-mobility-stream-alias-province|http://localhost:8090/api/mobility/mobility/province-flows/stream?intervalMs=1000|sse-200"
  "gateway-mobility-stream-alias-summary|http://localhost:8090/api/mobility/mobility/province-summary/stream?intervalMs=1000|sse-200"
  "gateway-mobility|http://localhost:8090/api/mobility/stats|json-2xx"
)

start_services() {
  local args=(up -d)
  if [[ "$NO_BUILD" -eq 1 ]]; then
    args+=(--no-build)
  else
    args+=(--build)
  fi

  echo "Starting services: ${SERVICES[*]}"
  "${COMPOSE_CMD[@]}" "${args[@]}" "${SERVICES[@]}"
}

wait_for_compose_containers() {
  local start_ts now elapsed
  start_ts="$(date +%s)"

  while true; do
    local running_count=0
    for svc in "${WAIT_SERVICES[@]}"; do
      local state
      state="$("${COMPOSE_CMD[@]}" ps --status running --services "$svc" 2>/dev/null || true)"
      if [[ "$state" == "$svc" ]]; then
        running_count=$((running_count + 1))
      fi
    done

    if [[ "$running_count" -eq "${#WAIT_SERVICES[@]}" ]]; then
      echo "All compose services are running."
      return 0
    fi

    now="$(date +%s)"
    elapsed=$((now - start_ts))
    if [[ "$elapsed" -ge "$TIMEOUT_SECONDS" ]]; then
      echo "WARN: Timeout waiting for all containers to be running (${TIMEOUT_SECONDS}s)."
      return 1
    fi

    echo "Waiting for containers... (${running_count}/${#WAIT_SERVICES[@]} running)"
    sleep "$INTERVAL_SECONDS"
  done
}

check_one() {
  local name="$1"
  local url="$2"
  local kind="$3"
  local body headers code

  body="$(mktemp)"
  headers="$(mktemp)"
  code="$(curl -s -m 8 -D "$headers" -o "$body" -w "%{http_code}" "$url" 2>/dev/null || true)"

  if [[ "$kind" == "strict-up" ]]; then
    if [[ "$code" =~ ^2[0-9][0-9]$ ]] && grep -q '"status"[[:space:]]*:[[:space:]]*"UP"' "$body"; then
      echo "PASS  $name -> $url (HTTP $code, UP)"
      rm -f "$body" "$headers"
      return 0
    fi
  elif [[ "$kind" == "json-2xx" ]]; then
    if [[ "$code" =~ ^2[0-9][0-9]$ ]] && grep -qi '^content-type:.*application/json' "$headers"; then
      echo "PASS  $name -> $url (HTTP $code, JSON)"
      rm -f "$body" "$headers"
      return 0
    fi
  elif [[ "$kind" == "sse-200" ]]; then
    if [[ "$code" == "200" ]] && grep -qi '^content-type:.*text/event-stream' "$headers"; then
      echo "PASS  $name -> $url (HTTP $code, SSE)"
      rm -f "$body" "$headers"
      return 0
    fi
  else
    if [[ "$code" =~ ^[23][0-9][0-9]$ ]]; then
      echo "PASS  $name -> $url (HTTP $code)"
      rm -f "$body" "$headers"
      return 0
    fi
  fi

  echo "FAIL  $name -> $url (HTTP ${code:-000})"
  if [[ -s "$body" ]]; then
    echo "      Body: $(tr -d '\n' < "$body" | head -c 220)"
  fi
  if [[ -s "$headers" ]]; then
    echo "      Headers: $(tr -d '\r' < "$headers" | tr '\n' ' ' | head -c 220)"
  fi
  rm -f "$body" "$headers"
  return 1
}

run_checks_group() {
  local title="$1"
  local -n checks_ref="$2"
  local total=0 passed=0 failed=0

  echo
  echo "Running ${title} checks..."
  for item in "${checks_ref[@]}"; do
    IFS='|' read -r name url kind <<< "$item"
    total=$((total + 1))
    if check_one "$name" "$url" "$kind"; then
      passed=$((passed + 1))
    else
      failed=$((failed + 1))
    fi
  done

  echo
  echo "${title} summary: ${passed}/${total} passed, ${failed} failed"
  if [[ "$failed" -gt 0 ]]; then
    echo "One or more ${title} checks failed."
    return 1
  fi

  echo "All ${title} checks passed."
  return 0
}

if [[ "$START_STACK" -eq 1 ]]; then
  start_services
  wait_for_compose_containers || true
fi

overall_rc=0
run_checks_group "health" HEALTH_CHECKS || overall_rc=1
run_checks_group "business" BUSINESS_CHECKS || overall_rc=1
run_checks_group "gateway-business" GATEWAY_BUSINESS_CHECKS || overall_rc=1

exit "$overall_rc"
