// Problem:    In-Memory KV Store With Nested Transactions
// Link:
// Difficulty: Medium
// Tags:       design, hash-map, stack, transactions
// Time:       get O(d) for d open txns; set/delete/begin/rollback O(1); commit O(size of committed txn)
// Space:      O(keys + writes buffered in open txns)

import java.util.*;

// Write-buffer design: each open txn holds its own writes; commit merges the innermost
// txn into its parent (or the store), rollback discards it.
class KVStore {
  private final Map<String, String> globalStore = new HashMap<>();
  // Innermost txn first. Each maps key -> value written in that txn; a null value means deleted.
  private final Deque<Map<String, String>> txns = new ArrayDeque<>();

  void set(String key, String value) {
    Objects.requireNonNull(value, "null values are reserved for deletes");
    if (txns.isEmpty()) globalStore.put(key, value);
    else txns.peek().put(key, value);
  }

  // null if absent
  String get(String key) {
    for (Map<String, String> txn : txns) {        // innermost to outermost
      if (txn.containsKey(key)) return txn.get(key);
    }
    return globalStore.get(key);
  }

  void delete(String key) {
    if (txns.isEmpty()) globalStore.remove(key);
    else txns.peek().put(key, null);
  }

  // start a (possibly nested) transaction
  void begin() { txns.push(new HashMap<>()); }

  // merge innermost txn into its parent (or the store); false if none open
  boolean commit() {
    if (txns.isEmpty()) return false;
    Map<String, String> child = txns.pop();
    if (txns.isEmpty()) {
      child.forEach((k, v) -> {
        if (v == null) globalStore.remove(k);
        else globalStore.put(k, v);
      });
    } else {
      txns.peek().putAll(child);                  // deletes carry over to the parent
    }
    return true;
  }

  // discard innermost txn; false if none open
  boolean rollback() {
    if (txns.isEmpty()) return false;
    txns.pop();
    return true;
  }
}

class Solution {

    // Local driver: covers the read-through, nested-delete, and marker-collision cases.
    // Expected: 1 | null null | ##### | 2 1 | false false
    public static void main(String[] args) {
        // Reads see writes from outer txns.
        KVStore kv = new KVStore();
        kv.begin();
        kv.set("a", "1");
        kv.begin();
        System.out.print(kv.get("a") + " | ");

        // A delete committed into a parent txn stays deleted all the way to the store.
        kv = new KVStore();
        kv.set("a", "1");
        kv.begin();
        kv.begin();
        kv.delete("a");
        kv.commit();
        System.out.print(kv.get("a") + " ");
        kv.commit();
        System.out.print(kv.get("a") + " | ");

        // Any string is a valid value; there is no in-band deletion marker.
        kv = new KVStore();
        kv.begin();
        kv.set("k", "#####");
        System.out.print(kv.get("k") + " | ");

        // Rollback discards only the innermost txn.
        kv = new KVStore();
        kv.set("x", "1");
        kv.begin();
        kv.set("x", "2");
        kv.begin();
        kv.set("x", "3");
        kv.rollback();
        System.out.print(kv.get("x") + " ");
        kv.rollback();
        System.out.print(kv.get("x") + " | ");

        System.out.print(kv.commit() + " ");
        System.out.println(kv.rollback());
    }
}
