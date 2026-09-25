#!/usr/bin/env bash
# Runs the ShopFlow JMeter plan headless (CLI mode) and builds the HTML dashboard report.
#   load-tests/jmeter/run-jmeter.sh
#   load-tests/jmeter/run-jmeter.sh -JorderRate=20 -JdurationMin=10
# Properties: host, port, browseRate, historyRate, orderRate, reportRate, durationMin, rampSec
set -euo pipefail

here="$(cd "$(dirname "$0")" && pwd)"
out="$here/../../build/jmeter/$(date +%Y%m%d-%H%M%S)"
mkdir -p "$out"
jmeter="${JMETER_HOME:-$HOME/tools/jmeter}/bin/jmeter"
[ -x "$jmeter" ] || jmeter="$jmeter.bat"

"$jmeter" -n -t "$here/shopflow.jmx" -l "$out/results.jtl" -j "$out/jmeter.log" -e -o "$out/report" "$@"
echo
echo "HTML report: $out/report/index.html"
