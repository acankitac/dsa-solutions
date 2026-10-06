#!/usr/bin/env bash
# Compile and run a solution locally.
#
#   scripts/run.sh <solution-file-or-problem-dir> [input-file]
#   scripts/run.sh topics/graphs/number-of-islands            # runs solution.cpp
#   scripts/run.sh topics/graphs/number-of-islands/Solution.java input.txt
set -euo pipefail

if [[ $# -lt 1 ]]; then
    sed -n '2,6p' "$0" | sed 's/^# \{0,1\}//'
    exit 1
fi

root=$(cd "$(dirname "$0")/.." && pwd)
target=$1
input=${2:-/dev/stdin}

if [[ -d $target ]]; then
    if [[ -f $target/solution.cpp ]]; then
        target=$target/solution.cpp
    elif [[ -f $target/Solution.java ]]; then
        target=$target/Solution.java
    else
        echo "no solution.cpp or Solution.java in $target" >&2
        exit 1
    fi
fi

build=$(mktemp -d)
trap 'rm -rf "$build"' EXIT

case $target in
    *.cpp)
        g++ -std=c++20 -O2 -Wall -Wextra -DLOCAL -I"$root/include" "$target" -o "$build/a.out"
        "$build/a.out" < "$input"
        ;;
    *.java)
        javac -d "$build" "$target"
        java -cp "$build" Solution < "$input"
        ;;
    *) echo "unsupported file: $target" >&2; exit 1 ;;
esac
