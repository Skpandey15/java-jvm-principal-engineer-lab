package lab.wp03;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;

/**
 * WP-03 lab: count++ is read-modify-write, not atomic. The unsafe counter loses updates
 * under contention; the three fixes trade correctness mechanisms and contention cost.
 * Benchmark the fixes with JMH in the benchmarks project.
 */
public final class LostUpdate {

    public interface Counter {
        void increment();

        long value();
    }

    public static final class UnsafeCounter implements Counter {
        private long count;

        public void increment() {
            count++;
        }

        public long value() {
            return count;
        }
    }

    public static final class SynchronizedCounter implements Counter {
        private long count;

        public synchronized void increment() {
            count++;
        }

        public synchronized long value() {
            return count;
        }
    }

    public static final class AtomicCounter implements Counter {
        private final AtomicLong count = new AtomicLong();

        public void increment() {
            count.incrementAndGet();
        }

        public long value() {
            return count.get();
        }
    }

    public static final class AdderCounter implements Counter {
        private final LongAdder count = new LongAdder();

        public void increment() {
            count.increment();
        }

        public long value() {
            return count.sum();
        }
    }

    /** Increments from {@code threads} platform threads, {@code perThread} times each. */
    public static long run(Counter counter, int threads, int perThread) throws InterruptedException {
        Thread[] workers = IntStream.range(0, threads)
                .mapToObj(i -> Thread.ofPlatform().unstarted(() -> {
                    for (int n = 0; n < perThread; n++) {
                        counter.increment();
                    }
                }))
                .toArray(Thread[]::new);
        for (Thread t : workers) {
            t.start();
        }
        for (Thread t : workers) {
            t.join();
        }
        return counter.value();
    }

    public static void main(String[] args) throws InterruptedException {
        int threads = 8;
        int perThread = 1_000_000;
        long expected = (long) threads * perThread;
        for (Counter c : new Counter[]{new UnsafeCounter(), new SynchronizedCounter(), new AtomicCounter(), new AdderCounter()}) {
            long actual = run(c, threads, perThread);
            System.out.printf("%-20s expected=%,d actual=%,d lost=%,d%n",
                    c.getClass().getSimpleName(), expected, actual, expected - actual);
        }
    }

    private LostUpdate() {
    }
}
