package lab.wp01;

/** Small class used as the class-loading subject and as a javap target (record, switch pattern). */
public class Greeter {

    sealed interface Audience permits Person, Team {
    }

    record Person(String name) implements Audience {
    }

    record Team(String name, int size) implements Audience {
    }

    public String greet(Audience audience) {
        return switch (audience) {
            case Person(var name) -> "Hello, " + name;
            case Team(var name, var size) when size > 10 -> "Hello, everyone in " + name;
            case Team(var name, var _) -> "Hello, team " + name;
        };
    }
}
