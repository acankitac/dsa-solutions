# Thread-safe LRU cache: exclusive lock vs read-write lock

Two variants in `Solution.java`:

- **`LRUExclusiveLock`** (recommended): one `ReentrantLock` around `get` and `put`.
- **`LRUReadWriteLock`**: looks up under the shared read lock, then takes the write lock to
  reorder the list on a hit.

Both are correct. The read-write version is much slower for an LRU cache.

## Why the read-write lock loses here

1. **`get` is a write.** A hit moves the node to the front of the list, so it needs the
   write lock anyway. The read-lock step adds a second lock acquisition and a second lookup.
   Only misses can run in parallel.
2. **The read-only part is tiny.** A miss is one `HashMap.get` (tens of nanoseconds). Taking
   a read lock does an atomic update on a counter shared by all threads, and that memory
   bounces between cores on every acquisition. With a section this short, readers end up
   queuing on the counter instead of running in parallel.
3. **Writes aren't rare.** Every `put` and every hit takes the write lock, and
   `ReentrantReadWriteLock` makes new readers wait while a writer is queued.

## When each one wins

| Use an exclusive lock (`ReentrantLock` / `synchronized`) when | Use a read-write lock when |
|---|---|
| "Reads" modify shared state (LRU `get`, access counters, lazy init) | Reads are truly read-only |
| The critical section is short (well under a microsecond: a lookup, a few pointer updates) | Read sections are long (around a microsecond or more: scans, iteration, building results) |
| Writes are a meaningful share of operations | Writes are rare (around 1 in 100 or fewer) |
| You want the simplest correct code | Many threads read at the same time |

For short read-only sections, also consider:

- **`StampedLock` optimistic reads:** a reader takes a version stamp, reads, then checks
  the stamp. No write to shared memory, so no contention on the lock itself.
- **Concurrent data structures** such as `ConcurrentHashMap`, which don't lock on reads.

## Benchmarks

Apple M3 Pro (12 cores), OpenJDK 17, 8 threads. Best of 5 runs after a warm-up run.

**This LRU cache** (capacity 1000):

| Workload | ReadWriteLock | ReentrantLock |
|---|---|---|
| 90% get, ~100% hits | 823 ms | 310 ms |
| 90% get, ~10% hits | 616 ms | 300 ms |
| 100% get, ~100% misses (best case for the read lock) | 1232 ms | 44 ms |
| 99% get, ~100% misses | 371 ms | 104 ms |

The exclusive lock wins every case, by up to 28x.

**A generic read-only scan of an `int[]`** under the lock, with occasional single-slot writes:

| Read section | Writes | ReadWriteLock | ReentrantLock | Winner |
|---|---|---|---|---|
| 10 ints | 0.1% | 13941 ms | 1370 ms | Exclusive, 10.2x |
| 100 ints | 0.1% | 1841 ms | 823 ms | Exclusive, 2.2x |
| 1,000 ints | 0.1% | 150 ms | 640 ms | **Read-write, 4.3x** |
| 1,000 ints | 10% | 488 ms | 588 ms | Read-write, 1.2x |
| 10,000 ints | 0.1% | 67 ms | 547 ms | **Read-write, 8.2x** |
| 10,000 ints | 10% | 164 ms | 495 ms | Read-write, 3.0x |

On this machine the crossover sits between scanning 100 and 1,000 ints, roughly a
microsecond of read work. Rare writes push it further in the read-write lock's favor.
These numbers come from one machine; the shape of the result is what matters.

## Interview answer

> "A read-write lock doesn't help, because in an LRU cache `get` isn't really a read: a hit
> moves the node to the front, which modifies shared state. Only misses could run in
> parallel, and a miss is a single hash lookup, too short to be worth a read lock's
> overhead. I'd use one `ReentrantLock`. It's simpler and faster."

Follow-ups:

- **A read lock can't be upgraded.** You must release it, take the write lock, and look the
  key up again, because another thread may have evicted it in between.
- **`ConcurrentHashMap` alone isn't enough.** The linked list is still shared and unprotected.
- **Scaling past one lock:**
  1. **Shard the cache:** N independent caches, each with its own lock, chosen by key hash.
     Cuts contention by about N; eviction becomes least-recently-used per shard, not global.
  2. **Record reads now, reorder later:** lock-free lookups in a concurrent map, with hits
     logged to a buffer and applied to the list in batches under `tryLock`. This is how
     Caffeine works; in production, use Caffeine.
- **Fairness:** `new ReentrantReadWriteLock(true)` stops writers from being starved but
  lowers throughput.
