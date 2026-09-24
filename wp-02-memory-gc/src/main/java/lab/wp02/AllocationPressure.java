package lab.wp02;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * WP-02 lab: steady allocation with a bounded set of survivors, so GCs happen at a
 * controlled rate. Compare collectors and JDKs with identical settings, e.g.
 *
 *   ./gradlew :wp-02-memory-gc:runOnBaselineJdk   -PmainClass=lab.wp02.AllocationPressure -PjvmArgs="-Xmx512m -XX:+UseG1GC -Xlog:gc"
 *   ./gradlew :wp-02-memory-gc:runOnBaselineJdk   -PmainClass=lab.wp02.AllocationPressure -PjvmArgs="-Xmx512m -XX:+UseZGC -Xlog:gc"
 *   ./gradlew :wp-02-memory-gc:runOnBaselineJdk   -PmainClass=lab.wp02.AllocationPressure -PjvmArgs="-Xmx512m -XX:+UseCompactObjectHeaders -Xlog:gc"
 *   ./gradlew :wp-02-memory-gc:runOnComparisonJdk -PmainClass=lab.wp02.AllocationPressure -PjvmArgs="-Xmx512m -Xlog:gc"
 *
 * Add -XX:NativeMemoryTracking=summary and run "jcmd <pid> VM.native_memory summary" while it runs.
 */
public final class AllocationPressure {

    private static final int SURVIVORS = 200_000;
    private static final long DURATION_MS = 20_000;

    record Payload(long id, byte[] data) {
    }

    public static void main(String[] args) {
        Deque<Payload> survivors = new ArrayDeque<>(SURVIVORS);
        long allocated = 0;
        long start = System.nanoTime();
        long deadline = start + DURATION_MS * 1_000_000;
        for (long i = 0; System.nanoTime() < deadline; i++) {
            survivors.addLast(new Payload(i, new byte[64 + (int) (i % 512)]));
            if (survivors.size() > SURVIVORS) {
                survivors.removeFirst();
            }
            allocated++;
        }
        double seconds = (System.nanoTime() - start) / 1e9;
        System.out.printf("JDK %s, allocated %,d objects in %.1fs (%,.0f/s)%n",
                Runtime.version(), allocated, seconds, allocated / seconds);
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.printf("  %-30s collections=%-6d time=%dms%n", gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
        }
    }

    private AllocationPressure() {
    }
}
