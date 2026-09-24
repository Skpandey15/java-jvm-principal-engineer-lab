package lab.wp01;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassLoaderIdentityTest {

    @Test
    void sameBytecodeInTwoLoadersIsTwoDifferentClasses() throws Exception {
        ClassLoaderIdentity.Result r = ClassLoaderIdentity.loadTwice(Greeter.class);

        assertThat(r.sameName()).isTrue();
        assertThat(r.sameClass()).isFalse();
        assertThat(r.loaderA()).isEqualTo("isolated-A");
        assertThat(r.loaderB()).isEqualTo("isolated-B");
    }

    @Test
    void greeterUsesPatternMatching() {
        Greeter g = new Greeter();

        assertThat(g.greet(new Greeter.Person("Ada"))).isEqualTo("Hello, Ada");
        assertThat(g.greet(new Greeter.Team("Platform", 12))).isEqualTo("Hello, everyone in Platform");
        assertThat(g.greet(new Greeter.Team("Core", 4))).isEqualTo("Hello, team Core");
    }
}
