#!/usr/bin/env bash
# Sends 10 UDP signals per second (alternating t1 / h1) with gradually growing values,
# so that they exceed the alert limits (.env: TEMPERATURE_LIMIT=35, HUMIDITY_LIMIT=50)
# within DURATION_SEC (10 s by default).
#
# Usage:
#   ./scripts/send-signals.sh
#   DURATION_SEC=30 TEMP_END=50 ./scripts/send-signals.sh
#
# Requires: bash (uses the built-in /dev/udp, no nc needed; works on macOS and Linux), awk

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

# All payloads are computed in a single awk run: "<port> <id>,<value>"
i=0
while read -r port payload; do
  i=$((i + 1))
  # Built-in UDP send: no external process and no nc timeout
  printf '%s\n' "$payload" > "/dev/udp/$TARGET_HOST/$port"
  printf '[%4d/%d] -> :%s  %s\n' "$i" "$total" "$port" "$payload"
  sleep "$interval"
done < <(awk -v n="$total" \
  -v tid="$TEMP_ID" -v tport="$TEMP_PORT" -v ts="$TEMP_START" -v te="$TEMP_END" \
  -v hid="$HUMIDITY_ID" -v hport="$HUMIDITY_PORT" -v hs="$HUMIDITY_START" -v he="$HUMIDITY_END" '
  BEGIN {
    for (i = 0; i < n; i++) {
      p = (n > 1) ? i / (n - 1) : 1
      if (i % 2 == 0) printf "%s %s,%.1f\n", tport, tid, ts + (te - ts) * p
      else            printf "%s %s,%.1f\n", hport, hid, hs + (he - hs) * p
    }
  }')

echo "Done. Check central-monitoring-service logs for ALERT messages."
