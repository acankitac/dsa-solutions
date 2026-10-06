# DSA Solutions

My solutions to data structures and algorithms problems, kept as interview prep.
I write the heavy algorithmic problems in **C++** and the rest in **Java**.

## Layout

```
topics/<topic>/<problem-slug>/
    solution.cpp      # C++ solution
    Solution.java     # Java solution
    notes.md          # optional: approach, edge cases, mistakes I made
```

Topics I use: `arrays`, `strings`, `hashing`, `two-pointers`, `sliding-window`,
`binary-search`, `linked-list`, `stack-queue`, `heap`, `trees`, `bst`, `trie`,
`graphs`, `union-find`, `dp`, `greedy`, `backtracking`, `bit-manipulation`,
`math`, `segment-tree`, `design`.

## Workflow

```bash
# 1. Scaffold a problem from a template (cpp | java | both)
scripts/new.sh graphs number-of-islands cpp https://leetcode.com/problems/number-of-islands/

# 2. Fill in the header (Difficulty, Tags, Time, Space) and write the solution

# 3. Run it locally (the LOCAL main in the C++ template only compiles here)
scripts/run.sh topics/graphs/number-of-islands [input.txt]

# 4. Commit and push. CI compiles every solution and refreshes the catalog below.
#    To refresh the catalog yourself, run: scripts/catalog.py
```

The catalog is built from the header comment at the top of each solution:

```cpp
// Problem:    Number Of Islands
// Link:       https://leetcode.com/problems/number-of-islands/
// Difficulty: Medium
// Tags:       bfs, dfs, grid
// Time:       O(m*n)
// Space:      O(m*n)
```

## Catalog

<!-- CATALOG:START -->
_No solutions yet. Run `scripts/new.sh` to add one._
<!-- CATALOG:END -->
