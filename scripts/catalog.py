#!/usr/bin/env python3
"""Regenerate the solution catalog in README.md from solution file headers.

    scripts/catalog.py           # rewrite README.md
    scripts/catalog.py --check   # exit 1 if README.md is out of date
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
README = ROOT / "README.md"
TOPICS = ROOT / "topics"
START, END = "<!-- CATALOG:START -->", "<!-- CATALOG:END -->"
SOLUTION_FILES = {"solution.cpp": "C++", "Solution.java": "Java"}
DIFFICULTY_ORDER = {"Easy": 0, "Medium": 1, "Hard": 2}
HEADER_RE = re.compile(r"^//\s*(\w+):\s*(.*?)\s*$")


def parse_header(path):
    meta = {}
    for line in path.read_text().splitlines():
        m = HEADER_RE.match(line)
        if not m:
            if line.strip():
                break
            continue
        meta[m.group(1).lower()] = m.group(2)
    return meta


def collect():
    problems = []
    for problem_dir in sorted(p for p in TOPICS.glob("*/*") if p.is_dir()):
        files = [problem_dir / f for f in SOLUTION_FILES if (problem_dir / f).exists()]
        if not files:
            continue
        meta = {}
        for f in files:
            # Earlier files win; later files only fill in blanks.
            for k, v in parse_header(f).items():
                if v and not meta.get(k):
                    meta[k] = v
        problems.append({
            "topic": problem_dir.parent.name,
            "dir": problem_dir.relative_to(ROOT).as_posix(),
            "title": meta.get("problem") or problem_dir.name,
            "link": meta.get("link", ""),
            "difficulty": meta.get("difficulty", ""),
            "tags": meta.get("tags", ""),
            "langs": [(SOLUTION_FILES[f.name], f.relative_to(ROOT).as_posix()) for f in files],
        })
    return problems


def render(problems):
    if not problems:
        return "_No solutions yet. Run `scripts/new.sh` to add one._"

    counts = {d: sum(p["difficulty"] == d for p in problems) for d in DIFFICULTY_ORDER}
    out = [
        f"**{len(problems)} problem{'' if len(problems) == 1 else 's'}** — "
        + " · ".join(f"{d}: {n}" for d, n in counts.items()),
        "",
    ]
    by_topic = {}
    for p in problems:
        by_topic.setdefault(p["topic"], []).append(p)

    for topic, items in by_topic.items():
        items.sort(key=lambda p: (DIFFICULTY_ORDER.get(p["difficulty"], 3), p["title"].lower()))
        out += [
            f"### {topic} ({len(items)})",
            "",
            "| Problem | Difficulty | Solution | Tags |",
            "|---|---|---|---|",
        ]
        for p in items:
            title = f"[{p['title']}]({p['link']})" if p["link"] else p["title"]
            langs = " · ".join(f"[{lang}]({path})" for lang, path in p["langs"])
            out.append(f"| {title} | {p['difficulty']} | {langs} | {p['tags']} |")
        out.append("")
    return "\n".join(out).rstrip()


def main():
    text = README.read_text()
    if START not in text or END not in text:
        sys.exit(f"README.md is missing {START} / {END} markers")
    head, rest = text.split(START, 1)
    _, tail = rest.split(END, 1)
    new = f"{head}{START}\n{render(collect())}\n{END}{tail}"

    if "--check" in sys.argv:
        if new != text:
            sys.exit("README.md catalog is out of date; run scripts/catalog.py")
        return
    if new != text:
        README.write_text(new)
        print("README.md updated")


if __name__ == "__main__":
    main()
