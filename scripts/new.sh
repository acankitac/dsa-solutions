#!/usr/bin/env bash
# Scaffold a new problem from the templates.
#
#   scripts/new.sh <topic> <slug> [cpp|java|both] [link]
#   scripts/new.sh graphs number-of-islands cpp https://leetcode.com/problems/number-of-islands/
set -euo pipefail

if [[ $# -lt 2 ]]; then
    sed -n '2,5p' "$0" | sed 's/^# \{0,1\}//'
    exit 1
fi

topic=$1
slug=$2
lang=${3:-cpp}
link=${4:-}

root=$(cd "$(dirname "$0")/.." && pwd)
dir="$root/topics/$topic/$slug"
# "number-of-islands" -> "Number Of Islands"
title=$(echo "$slug" | tr '-' ' ' | awk '{for (i = 1; i <= NF; i++) $i = toupper(substr($i, 1, 1)) substr($i, 2)} 1')

mkdir -p "$dir"

render() {
    local src=$1 dst=$2
    if [[ -e $dst ]]; then
        echo "exists: ${dst#$root/}"
        return
    fi
    sed -e "s|{{TITLE}}|$title|" -e "s|{{LINK}}|$link|" \
        -e "s|{{DIFFICULTY}}||" -e "s|{{TAGS}}||" "$src" > "$dst"
    echo "created: ${dst#$root/}"
}

case $lang in
    cpp)  render "$root/templates/solution.cpp" "$dir/solution.cpp" ;;
    java) render "$root/templates/Solution.java" "$dir/Solution.java" ;;
    both)
        render "$root/templates/solution.cpp" "$dir/solution.cpp"
        render "$root/templates/Solution.java" "$dir/Solution.java"
        ;;
    *) echo "unknown language: $lang (use cpp, java, or both)" >&2; exit 1 ;;
esac
