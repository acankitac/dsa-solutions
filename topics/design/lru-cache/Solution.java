// Problem:    LRU Cache
// Link:       https://leetcode.com/problems/lru-cache/
// Difficulty: Medium
// Tags:       design, hash-map, doubly-linked-list
// Time:       O(1) per get/put
// Space:      O(capacity)

import java.util.*;

class LRU {

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

    LRU(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity should be positive integer!");
        }
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        Node node = ref.get(key);
        if (node == null) {
            return -1;
        }
        remove(node);
        addToFront(node);
        return node.val;
    }

    public void put(int key, int val) {
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

    // Local driver: LeetCode example 1, plus updating an existing key.
    // Expected: 1 -1 -1 3 4 | 30 -1
    public static void main(String[] args) {
        LRU cache = new LRU(2);
        cache.put(1, 1);
        cache.put(2, 2);
        System.out.print(cache.get(1) + " ");
        cache.put(3, 3);
        System.out.print(cache.get(2) + " ");
        cache.put(4, 4);
        System.out.print(cache.get(1) + " ");
        System.out.print(cache.get(3) + " ");
        System.out.print(cache.get(4) + " | ");

        // Updating 3 makes it most recent, so adding 5 evicts 4.
        cache.put(3, 30);
        cache.put(5, 5);
        System.out.print(cache.get(3) + " ");
        System.out.println(cache.get(4));
    }
}
