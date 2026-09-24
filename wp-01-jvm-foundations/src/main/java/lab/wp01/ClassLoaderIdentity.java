package lab.wp01;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * WP-01 lab: a class's runtime identity is (binary name + defining class loader).
 * Loading the same .class file through two isolated loaders yields two distinct classes,
 * so instances of one cannot be cast to the other.
 *
 * Run: ./gradlew :wp-01-jvm-foundations:runOnBaselineJdk -PmainClass=lab.wp01.ClassLoaderIdentity
 * Then inspect the bytecode: javap -c -p -v build/classes/java/main/lab/wp01/Greeter.class
 */
public final class ClassLoaderIdentity {

    public static void main(String[] args) throws Exception {
        Result r = loadTwice(Greeter.class);
        System.out.printf("same name        : %s%n", r.sameName());
        System.out.printf("same Class object: %s%n", r.sameClass());
        System.out.printf("loader A         : %s%n", r.loaderA());
        System.out.printf("loader B         : %s%n", r.loaderB());
        System.out.printf("app loader chain : %s -> %s -> bootstrap(null)%n",
                ClassLoader.getSystemClassLoader(), ClassLoader.getPlatformClassLoader());
    }

    static Result loadTwice(Class<?> type) throws Exception {
        URL classesRoot = codeSourceRoot(type);
        // parent = platform loader, so the application loader cannot supply the class: each isolated loader defines its own copy
        try (URLClassLoader a = new URLClassLoader("isolated-A", new URL[]{classesRoot}, ClassLoader.getPlatformClassLoader());
             URLClassLoader b = new URLClassLoader("isolated-B", new URL[]{classesRoot}, ClassLoader.getPlatformClassLoader())) {
            Class<?> ca = a.loadClass(type.getName());
            Class<?> cb = b.loadClass(type.getName());
            return new Result(ca.getName().equals(cb.getName()), ca == cb,
                    ca.getClassLoader().getName(), cb.getClassLoader().getName());
        }
    }

    private static URL codeSourceRoot(Class<?> type) throws Exception {
        return Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI()).toUri().toURL();
    }

    record Result(boolean sameName, boolean sameClass, String loaderA, String loaderB) {
    }

    private ClassLoaderIdentity() {
    }
}
