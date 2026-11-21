#!/usr/bin/env bash
# Sends 10 UDP signals per second (alternating t1 / h1) with gradually growing values,
# so that they eventually exceed the alert limits (.env: TEMPERATURE_LIMIT=35, HUMIDITY_LIMIT=50).
#
# Usage:
#   ./scripts/send-signals.sh
#   DURATION_SEC=30 TEMP_END=50 ./scripts/send-signals.sh
#
# Requires: bash, awk, nc (netcat; preinstalled on macOS)

set -euo pipefail

TARGET_HOST="${TARGET_HOST:-127.0.0.1}"
TEMP_PORT="${TEMP_PORT:-3355}"
HUMIDITY_PORT="${HUMIDITY_PORT:-3344}"
TEMP_ID="${TEMP_ID:-t1}"
HUMIDITY_ID="${HUMIDITY_ID:-h1}"
RATE_PER_SEC="${RATE_PER_SEC:-10}"
DURATION_SEC="${DURATION_SEC:-10}"
TEMP_START="${TEMP_START:-20}"
TEMP_END="${TEMP_END:-45}"            # limit is 35
HUMIDITY_START="${HUMIDITY_START:-30}"
HUMIDITY_END="${HUMIDITY_END:-65}"    # limit is 50

total=$((RATE_PER_SEC * DURATION_SEC))
interval=$(awk -v r="$RATE_PER_SEC" 'BEGIN { printf "%.4f", 1 / r }')

for ((i = 0; i < total; i++)); do
  if ((i % 2 == 0)); then
    id=$TEMP_ID; port=$TEMP_PORT; start=$TEMP_START; end=$TEMP_END
  else
    id=$HUMIDITY_ID; port=$HUMIDITY_PORT; start=$HUMIDITY_START; end=$HUMIDITY_END
  fi

  value=$(awk -v s="$start" -v e="$end" -v i="$i" -v t="$total" \
    'BEGIN { p = (t > 1) ? i / (t - 1) : 1; printf "%.1f", s + (e - s) * p }')
  payload="$id,$value"

  printf '%s\n' "$payload" | nc -u -w 1 "$TARGET_HOST" "$port"
  printf '[%4d/%d] -> :%s  %s\n' "$((i + 1))" "$total" "$port" "$payload"

  sleep "$interval"
done

echo "Done. Check central-monitoring-service logs for ALERT messages."
