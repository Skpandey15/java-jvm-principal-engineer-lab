package lab.wp01;

import java.util.List;
import java.util.function.Supplier;

/**
 * WP-01 Lab 1: source constructs to disassemble with javap and compare against your predictions.
 * See wp-01-jvm-foundations/labs/lab-01-javap.md. Do not read the bytecode before writing predictions.
 */
public class BytecodeTour {

    /** String concatenation: what does javac emit for a + b + c? */
    public String concat(String name, int count) {
        return "Hello " + name + ", you have " + count + " messages";
    }

    /** Lambda: how is the body compiled, and is there a separate class file? */
    public Supplier<String> lambda(String name) {
        return () -> "lambda " + name;
    }

    /** Anonymous class doing the same job: compare with the lambda. */
    public Supplier<String> anonymous(String name) {
        return new Supplier<>() {
            @Override
            public String get() {
                return "anonymous " + name;
            }
        };
    }

    /** Autoboxing: which method call appears, and why does == on the results surprise people? */
    public boolean sameBoxes(int a, int b) {
        Integer x = a;
        Integer y = b;
        return x == y;
    }

    /** Enhanced for over a List: what does the loop become? */
    public int sum(List<Integer> values) {
        int total = 0;
        for (int v : values) {
            total += v;
        }
        return total;
    }
}
