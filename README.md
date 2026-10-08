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
**8 problems** — Easy: 1 · Medium: 5 · Hard: 2

### design (6)

| Problem | Difficulty | Solution | Tags |
|---|---|---|---|
| [Design HashMap](https://leetcode.com/problems/design-hashmap/) | Easy | [Java](topics/design/hashmap/Solution.java) | design, hash-map, linked-list |
| [Design Bounded Blocking Queue](https://leetcode.com/problems/design-bounded-blocking-queue/) | Medium | [Java](topics/design/bounded-blocking-queue/Solution.java) | design, concurrency, locks, linked-list |
| In-Memory KV Store With Nested Transactions | Medium | [Java](topics/design/kv-store-transactions/Solution.java) | design, hash-map, stack, transactions |
| [LRU Cache](https://leetcode.com/problems/lru-cache/) | Medium | [Java](topics/design/lru-cache/Solution.java) | design, hash-map, doubly-linked-list |
| [LRU Cache (Thread-Safe)](https://leetcode.com/problems/lru-cache/) | Medium | [Java](topics/design/lru-cache-thread-safe/Solution.java) | design, concurrency, locks, hash-map, doubly-linked-list |
| [Design In-Memory File System](https://leetcode.com/problems/design-in-memory-file-system/) | Hard | [Java](topics/design/in-memory-file-system/Solution.java) | design, trie, tree-map, string |

### dp (1)

| Problem | Difficulty | Solution | Tags |
|---|---|---|---|
| [Longest Increasing Subsequence](https://leetcode.com/problems/longest-increasing-subsequence/) | Medium | [Java](topics/dp/longest-increasing-subsequence/Solution.java) | dp, binary-search, patience-sorting, tree-map |

### heap (1)

| Problem | Difficulty | Solution | Tags |
|---|---|---|---|
| [Meeting Rooms III](https://leetcode.com/problems/meeting-rooms-iii/) | Hard | [Java](topics/heap/meeting-rooms-iii/Solution.java) | heap, sorting, simulation |
<!-- CATALOG:END -->
