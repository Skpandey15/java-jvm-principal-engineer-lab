package lab.bench;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * WP-05: cost of HashMap resizing. The returned map is consumed by JMH, which prevents
 * dead-code elimination (a benchmark trap covered in WP-07).
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class HashMapPresizingBenchmark {

    @Param({"1000", "100000"})
    int size;

    @Benchmark
    public Map<Integer, Integer> defaultCapacity() {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(i, i);
        }
        return map;
    }

    @Benchmark
    public Map<Integer, Integer> presized() {
        Map<Integer, Integer> map = HashMap.newHashMap(size);
        for (int i = 0; i < size; i++) {
            map.put(i, i);
        }
        return map;
    }
}
