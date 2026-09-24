#!/usr/bin/env bash
# Runs a k6 script in Docker and writes its metrics to the lab Prometheus (remote write, native histograms),
# so client-side latency appears next to server-side latency in the "JVM Lab — Service Overview" dashboard.
#
#   load-tests/run-k6.sh                                   # order-create.js, 50 req/s for 1m
#   RATE=200 DURATION=5m load-tests/run-k6.sh              # heavier run
#   load-tests/run-k6.sh order-create.js                   # explicit script
#
# Requires the platform to be up (docker compose -f platform/docker-compose.yml up -d) and order-service running.
set -euo pipefail

script="${1:-order-create.js}"
here="$(cd "$(dirname "$0")" && pwd)"
testid="${TESTID:-$(date +%Y%m%d-%H%M%S)}"

# Git Bash on Windows rewrites /paths in arguments; disable that for docker.
export MSYS_NO_PATHCONV=1
mount_dir="$here"
if command -v cygpath > /dev/null; then mount_dir="$(cygpath -w "$here")"; fi

echo "k6 run $script (testid=$testid) -> Prometheus"
docker run --rm -i \
  --network jvm-lab_default \
  -v "$mount_dir:/scripts:ro" \
  -e BASE_URL="${BASE_URL:-http://host.docker.internal:18080}" \
  -e RATE="${RATE:-50}" \
  -e DURATION="${DURATION:-1m}" \
  -e K6_PROMETHEUS_RW_SERVER_URL=http://observability:9090/api/v1/write \
  -e K6_PROMETHEUS_RW_TREND_AS_NATIVE_HISTOGRAM=true \
  grafana/k6:1.2.3 run -o experimental-prometheus-rw --tag testid="$testid" "/scripts/$script"
