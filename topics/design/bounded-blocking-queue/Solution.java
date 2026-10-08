// Problem:    Design Bounded Blocking Queue
// Link:       https://leetcode.com/problems/design-bounded-blocking-queue/
// Difficulty: Medium
// Tags:       design, concurrency, locks, linked-list
// Time:       O(1) per put/take/size (excluding time spent blocked)
// Space:      O(capacity)

// Two-lock linked queue (same design as java.util.concurrent.LinkedBlockingQueue): producers
// and consumers use separate locks, and the AtomicInteger size publishes linked nodes across them.

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class BoundedBlockingQueue<T> {

  static final class Node<T> {
    T item;
    Node<T> next;
    Node(T item) { this.item = item; }
  }

  private final int capacity;
  private Node<T> head = new Node<>(null);
  private Node<T> tail = head;
  private final ReentrantLock putLock = new ReentrantLock();
  private final ReentrantLock takeLock = new ReentrantLock();
  private final Condition notFull = putLock.newCondition();
  private final Condition notEmpty = takeLock.newCondition();
  private final AtomicInteger size = new AtomicInteger();

  BoundedBlockingQueue(int capacity) {
    if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive: " + capacity);
    this.capacity = capacity;
  }

  // blocks while full
  void put(T item) throws InterruptedException {
    Objects.requireNonNull(item);
    int oldSize;
    putLock.lockInterruptibly();
    try {
      while (size.get() == capacity) notFull.await();
      tail.next = new Node<>(item);
      tail = tail.next;
      oldSize = size.getAndIncrement();
      if (oldSize + 1 < capacity) notFull.signal();
    } finally { putLock.unlock(); }
    if (oldSize == 0) {
      takeLock.lock();
      try {
        notEmpty.signal();
      } finally { takeLock.unlock(); }
    }
  }

  // blocks while empty
  T take() throws InterruptedException {
    int oldSize;
    T item;
    takeLock.lockInterruptibly();
    try {
      while (size.get() == 0) notEmpty.await();
      Node<T> first = head.next;
      head.next = null;
      item = first.item;
      first.item = null;
      head = first; // old head will be cleaned by GC
      oldSize = size.getAndDecrement();
      if (oldSize > 1) notEmpty.signal();
    } finally { takeLock.unlock(); }
    if (oldSize == capacity) {
      putLock.lock();
      try {
        notFull.signal();
      } finally { putLock.unlock(); }
    }
    return item;
  }

  int size() { return size.get(); }
}

class Solution {

    // Local driver: rejects a non-positive capacity, then runs 4 producers and 4 consumers
    // through a capacity-3 queue and checks nothing is lost or duplicated and the bound holds.
    // Expected: rejected 0 | sum ok=true final size=0 bound held=true
    public static void main(String[] args) throws InterruptedException {
        try {
            new BoundedBlockingQueue<Integer>(0);
            System.out.print("accepted 0 | ");
        } catch (IllegalArgumentException e) {
            System.out.print("rejected 0 | ");
        }

        int capacity = 3, producers = 4, consumers = 4, perThread = 50_000;
        BoundedBlockingQueue<Integer> q = new BoundedBlockingQueue<>(capacity);
        AtomicLong sum = new AtomicLong();
        AtomicInteger maxSize = new AtomicInteger();
        List<Thread> threads = new ArrayList<>();
        for (int p = 0; p < producers; p++) {
            threads.add(new Thread(() -> {
                try {
                    for (int i = 1; i <= perThread; i++) {
                        q.put(i);
                        maxSize.accumulateAndGet(q.size(), Math::max);
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }));
        }
        for (int c = 0; c < consumers; c++) {
            threads.add(new Thread(() -> {
                try {
                    for (int i = 0; i < perThread; i++) sum.addAndGet(q.take());
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }));
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        long expected = (long) producers * perThread * (perThread + 1) / 2;
        System.out.println("sum ok=" + (sum.get() == expected)
            + " final size=" + q.size()
            + " bound held=" + (maxSize.get() <= capacity));
    }
}
