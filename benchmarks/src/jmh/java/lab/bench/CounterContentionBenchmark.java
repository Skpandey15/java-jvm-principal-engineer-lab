package lab.bench;

import lab.wp03.LostUpdate;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

import java.util.concurrent.TimeUnit;

/**
 * WP-03: cost of each thread-safe counter under contention.
 * Rerun with different thread counts: ./gradlew :benchmarks:jmh -Pjmh.includes=CounterContention
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Threads(8)
public class CounterContentionBenchmark {

    @Param({"synchronized", "atomic", "adder"})
    String kind;

    LostUpdate.Counter counter;

    @Setup(Level.Iteration)
    public void setUp() {
        counter = switch (kind) {
            case "synchronized" -> new LostUpdate.SynchronizedCounter();
            case "atomic" -> new LostUpdate.AtomicCounter();
            case "adder" -> new LostUpdate.AdderCounter();
            default -> throw new IllegalArgumentException(kind);
        };
    }

    @Benchmark
    public void increment() {
        counter.increment();
    }
}
