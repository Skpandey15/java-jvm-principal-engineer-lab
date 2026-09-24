#!/usr/bin/env bash
# WP-01 diagnostics primer: capture a standard evidence bundle from a running JVM.
#   scripts/diagnose.sh <pid> [jfr-seconds]
# Output goes to evidence/<timestamp>-<pid>/ (git-ignored; copy what matters into the WP notes/ folder).
set -euo pipefail

pid="${1:?usage: diagnose.sh <pid> [jfr-seconds]}"
seconds="${2:-60}"
out="evidence/$(date +%Y%m%d-%H%M%S)-${pid}"
mkdir -p "$out"

echo "Collecting evidence for PID $pid into $out"
jcmd "$pid" VM.version            > "$out/vm-version.txt"
jcmd "$pid" VM.flags              > "$out/vm-flags.txt"
jcmd "$pid" VM.command_line       > "$out/vm-command-line.txt"
jcmd "$pid" GC.heap_info          > "$out/gc-heap-info.txt"
jcmd "$pid" GC.class_histogram    > "$out/class-histogram.txt"
jcmd "$pid" Thread.print -l       > "$out/thread-dump-1.txt"
jcmd "$pid" VM.native_memory summary > "$out/nmt-summary.txt" 2>&1 || echo "NMT not enabled (-XX:NativeMemoryTracking=summary)" > "$out/nmt-summary.txt"

jcmd "$pid" JFR.start name=diag settings=profile duration="${seconds}s" filename="$PWD/$out/recording.jfr" > /dev/null
echo "JFR recording for ${seconds}s..."
sleep "$((seconds / 2))"
jcmd "$pid" Thread.print -l       > "$out/thread-dump-2.txt"
sleep "$((seconds - seconds / 2 + 2))"

echo "Done. Open $out/recording.jfr in JDK Mission Control."
