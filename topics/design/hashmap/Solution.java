// Problem:    Design HashMap
// Link:       https://leetcode.com/problems/design-hashmap/
// Difficulty: Easy
// Tags:       design, hash-map, linked-list
// Time:       O(1) average per get/put/remove/containsKey, O(n) worst case; resize O(n), amortized O(1)
// Space:      O(n + capacity)

import java.util.*;

// Generic separate-chaining hash map, modeled on java.util.HashMap (without tree bins).
class MyHashMap<K, V> {
  private static final int DEFAULT_CAPACITY = 16;
  private static final int MAX_CAPACITY = 1 << 30;
  private static final float LOAD_FACTOR = 0.75f;

  static class Node<K, V> {
    final int hash;
    final K key;
    V val;
    Node<K, V> next;

    Node(int h, K key, V val, Node<K, V> next) {
      this.hash = h;
      this.key = key;
      this.val = val;
      this.next = next;
    }
  }

  private Node<K,V>[] table;
  private int size;
  private int threshold;

  @SuppressWarnings("unchecked")
  MyHashMap() {
    this.table = (Node<K,V>[]) new Node[DEFAULT_CAPACITY];
    this.threshold = (int) (DEFAULT_CAPACITY * LOAD_FACTOR);
  }

  // Mix the high bits into the low bits, since indexFor only looks at the low bits.
  private static int hash(Object key) {
    int h;
    return key == null ? 0 : (h = key.hashCode()) ^ (h >>> 16);
  }

  // capacity is always a power of two
  private static int indexFor(int hash, int capacity) {
    return hash & (capacity - 1);
  }

  private Node<K,V> findNode(Object key) {
    int h = hash(key);
    for (Node<K,V> node = table[indexFor(h, table.length)]; node != null; node = node.next) {
      if (node.hash == h && Objects.equals(node.key, key)) return node;
    }
    return null;
  }

  public V get(K key) {
    Node<K,V> node = findNode(key);
    return node == null ? null : node.val;
  }

  public boolean containsKey(K key) {
    return findNode(key) != null;
  }

  // Returns the previous value for key, or null if there was none.
  public V put(K key, V value) {
    int h = hash(key);
    int idx = indexFor(h, table.length);
    for (Node<K,V> node = table[idx]; node != null; node = node.next) {
      if (node.hash == h && Objects.equals(node.key, key)) {
        V old = node.val;
        node.val = value;
        return old;
      }
    }
    table[idx] = new Node<>(h, key, value, table[idx]);
    if (++size > threshold) resize();
    return null;
  }

  public V remove(K key) {
    int h = hash(key);
    int idx = indexFor(h, table.length);
    Node<K,V> prev = null;
    for (Node<K,V> node = table[idx]; node != null; node = node.next) {
      if (node.hash == h && Objects.equals(node.key, key)) {
        if (prev == null) table[idx] = node.next;
        else prev.next = node.next;
        size--;
        return node.val;
      }
      prev = node;
    }
    return null;
  }

  public int size() { return size; }

  // Doubling adds one bit to the index mask, so each node either stays at i
  // or moves to i + oldCap depending on (hash & oldCap). Split each chain into
  // lo/hi lists, appending at the tail to keep the original order.
  @SuppressWarnings("unchecked")
  private void resize() {
    Node<K,V>[] oldTable = table;
    int oldCap = oldTable.length;
    if (oldCap >= MAX_CAPACITY) {
      threshold = Integer.MAX_VALUE;
      return;
    }
    int newCap = oldCap * 2;
    Node<K,V>[] newTable = (Node<K,V>[]) new Node[newCap];
    for (int i = 0; i < oldCap; i++) {
      Node<K,V> loHead = null, loTail = null, hiHead = null, hiTail = null;
      for (Node<K,V> node = oldTable[i]; node != null; node = node.next) {
        if ((node.hash & oldCap) == 0) {
          if (loTail == null) loHead = node; else loTail.next = node;
          loTail = node;
        } else {
          if (hiTail == null) hiHead = node; else hiTail.next = node;
          hiTail = node;
        }
      }
      if (loTail != null) { loTail.next = null; newTable[i] = loHead; }
      if (hiTail != null) { hiTail.next = null; newTable[i + oldCap] = hiHead; }
    }
    table = newTable;
    threshold = newCap == MAX_CAPACITY ? Integer.MAX_VALUE : (int) (newCap * LOAD_FACTOR);
  }
}

class Solution {

    // Local driver: basic ops, null key, and enough inserts to trigger several resizes.
    // Expected: 1 null 10 true | null 7 | 1000 true 999 500
    public static void main(String[] args) {
        MyHashMap<Integer, Integer> map = new MyHashMap<>();
        map.put(1, 1);
        map.put(2, 2);
        System.out.print(map.get(1) + " ");
        System.out.print(map.get(3) + " ");
        System.out.print(map.put(2, 10) == 2 ? map.get(2) + " " : "WRONG ");
        System.out.print(map.containsKey(2) + " | ");

        map.remove(1);
        map.remove(2);
        map.put(null, 7);
        System.out.print(map.get(1) + " ");
        System.out.print(map.get(null) + " | ");
        map.remove(null);

        // Keys differing only in high bits would all collide without hash spreading.
        for (int i = 0; i < 1000; i++) map.put(i << 16, i);
        System.out.print(map.size() + " ");
        boolean ok = true;
        for (int i = 0; i < 1000; i++) ok &= map.get(i << 16) == i;
        System.out.print(ok + " ");
        map.remove(0);
        System.out.print(map.size() + " ");
        System.out.println(map.get(500 << 16));
    }
}
