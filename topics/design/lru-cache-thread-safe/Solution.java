// Problem:    LRU Cache (Thread-Safe)
// Link:       https://leetcode.com/problems/lru-cache/
// Difficulty: Medium
// Tags:       design, concurrency, locks, hash-map, doubly-linked-list
// Time:       O(1) per get/put
// Space:      O(capacity)
// Notes:      Two variants. See notes.md for when each one wins (with benchmarks).

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

// Variant 1 (recommended): one exclusive lock around every operation.
// A hit reorders the list, so get is really a write; an exclusive lock is the honest model.
class LRUExclusiveLock {

    private static class Node {
        final int key;
        int val;
        Node prev, next;

        Node(int key, int val) {
            this.key = key;
            this.val = val;
        }
    }

    private final int capacity;
    private final Map<Integer, Node> ref = new HashMap<>();
    // Dummy head and tail so we never have to deal with null checks.
    private final Node head = new Node(0, 0);
    private final Node tail = new Node(0, 0);
    private final ReentrantLock lock = new ReentrantLock();

    LRUExclusiveLock(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity should be positive integer!");
        }
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        lock.lock();
        try {
            Node node = ref.get(key);
            if (node == null) {
                return -1;
            }
            remove(node);
            addToFront(node);
            return node.val;
        } finally {
            lock.unlock();
        }
    }

    public void put(int key, int val) {
        lock.lock();
        try {
            Node node = ref.get(key);
            if (node != null) {
                // Existing key: update in place and mark as most recently used.
                node.val = val;
                remove(node);
                addToFront(node);
                return;
            }

            if (ref.size() >= capacity) {
                ref.remove(removeLast().key);
            }
            node = new Node(key, val);
            addToFront(node);
            ref.put(key, node);
        } finally {
            lock.unlock();
        }
    }

    // De-link the node from the list.
    private void remove(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    // Evict the least recently used node, which sits just before tail.
    private Node removeLast() {
        Node last = tail.prev;
        remove(last);
        return last;
    }

    // The most recently used node always sits just after head.
    private void addToFront(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }
}

// Variant 2: read lock for the lookup, write lock to reorder.
// Correct, but slower for an LRU cache: every hit still needs the write lock.
class LRUReadWriteLock {

    private static class Node {
        final int key;
        int val;
        Node prev, next;

        Node(int key, int val) {
            this.key = key;
            this.val = val;
        }
    }

    private final int capacity;
    private final Map<Integer, Node> ref = new HashMap<>();
    // Dummy head and tail so we never have to deal with null checks.
    private final Node head = new Node(0, 0);
    private final Node tail = new Node(0, 0);
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    LRUReadWriteLock(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity should be positive integer!");
        }
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        // Misses are answered under the shared read lock.
        rwLock.readLock().lock();
        try {
            if (ref.get(key) == null) {
                return -1;
            }
        } finally {
            rwLock.readLock().unlock();
        }

        // A read lock can't be upgraded to a write lock (that deadlocks), so release
        // it first, then look the key up again: another thread may have evicted it.
        rwLock.writeLock().lock();
        try {
            Node node = ref.get(key);
            if (node == null) {
                return -1;
            }
            remove(node);
            addToFront(node);
            return node.val;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void put(int key, int val) {
        rwLock.writeLock().lock();
        try {
            Node node = ref.get(key);
            if (node != null) {
                // Existing key: update in place and mark as most recently used.
                node.val = val;
                remove(node);
                addToFront(node);
                return;
            }

            if (ref.size() >= capacity) {
                ref.remove(removeLast().key);
            }
            node = new Node(key, val);
            addToFront(node);
            ref.put(key, node);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    // De-link the node from the list.
    private void remove(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    // Evict the least recently used node, which sits just before tail.
    private Node removeLast() {
        Node last = tail.prev;
        remove(last);
        return last;
    }

    // The most recently used node always sits just after head.
    private void addToFront(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }
}

class Solution {

    interface Cache {
        int get(int key);
        void put(int key, int val);
    }

    // Local driver: LeetCode example 1 on each variant, then a 16-thread stress test
    // where every get must return either -1 or the value stored for that key.
    // Expected per variant: "1 -1 -1 3 4" then "stress OK".
    public static void main(String[] args) throws Exception {
        Map<String, java.util.function.IntFunction<Cache>> variants = new LinkedHashMap<>();
        variants.put("ExclusiveLock", cap -> {
            LRUExclusiveLock c = new LRUExclusiveLock(cap);
            return new Cache() {
                public int get(int k) { return c.get(k); }
                public void put(int k, int v) { c.put(k, v); }
            };
        });
        variants.put("ReadWriteLock", cap -> {
            LRUReadWriteLock c = new LRUReadWriteLock(cap);
            return new Cache() {
                public int get(int k) { return c.get(k); }
                public void put(int k, int v) { c.put(k, v); }
            };
        });

        for (var e : variants.entrySet()) {
            Cache cache = e.getValue().apply(2);
            cache.put(1, 1);
            cache.put(2, 2);
            int a = cache.get(1);
            cache.put(3, 3);
            int b = cache.get(2);
            cache.put(4, 4);
            System.out.printf("%s: %d %d %d %d %d | ", e.getKey(), a, b, cache.get(1), cache.get(3), cache.get(4));
            System.out.println(stress(e.getValue().apply(64)));
        }
    }

    static String stress(Cache cache) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(16);
        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < 16; t++) {
            futures.add(pool.submit(() -> {
                ThreadLocalRandom r = ThreadLocalRandom.current();
                for (int i = 0; i < 200_000; i++) {
                    int k = r.nextInt(200);
                    if (r.nextBoolean()) {
                        cache.put(k, k);
                    } else {
                        int v = cache.get(k);
                        if (v != -1 && v != k) {
                            throw new IllegalStateException("wrong value for key " + k);
                        }
                    }
                }
            }));
        }
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();
        return "stress OK";
    }
}
