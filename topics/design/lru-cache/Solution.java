// Problem:    LRU Cache
// Link:       https://leetcode.com/problems/lru-cache/
// Difficulty: Medium
// Tags:       design, hash-map, doubly-linked-list
// Time:       O(1) per get/put
// Space:      O(capacity)

import java.util.*;

class LRU {
    private int capacity, size;
    private Map<Integer, Node> ref;
    Node head, tail;

    LRU(int cap) {
      if (cap<=0) {
        throw new IllegalArgumentException("Capacity should be positive integer!");
      }
      capacity = cap;
      size=0;
      ref = new HashMap<>();
      head = new Node(0,0);
      tail = new Node(0,0); // dummy head, tail so that we don't have to deal with null checks
      head.next = tail;
      tail.prev = head;
    }

    public void put(int key, int val) {
      if (!ref.containsKey(key)) {
        if (size>=capacity) {
          ref.remove(tail.prev.k);
          remove(tail.prev);
          size--;
        }

        Node newNode = add(key, val);
        size++;
        ref.put(key, newNode);
      }
      else {
        Node oldNode = ref.get(key);
        remove(oldNode);
        Node newNode = add(key, val);
        ref.put(key, newNode);
      }
    }

    public int get(int key) {
      if (!ref.containsKey(key)) return -1;
      Node node = ref.get(key);
      remove(node);
      add(node);
      return node.v;
    }

    private void remove(Node node) {
      // de-link the node
      node.prev.next=node.next;
      node.next.prev=node.prev;
    }

    private Node add(int key, int val) {

      Node node = new Node(key, val);
      add(node);
      return node;
    }

    private void add(Node node) {
      // always add node to the front
      head.next.prev=node;
      node.next = head.next;
      node.prev=head;
      head.next=node;
    }
  }

  class Node {
    int k,v;
    Node prev, next;

    Node(int key, int val) {
      k = key;
      v = val;
    }
  }

class Solution {

    // Local driver: LeetCode example 1. Expected: 1 -1 -1 3 4
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
        System.out.println(cache.get(4));
    }
}
