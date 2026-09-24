# WP-01 Lab 1 — What javac really emits (javap)

**Goal:** map modern Java source constructs to the bytecode and class-file structures the JVM actually runs,
and explain *why* the JDK compiles them that way. That "why" is what a Principal-level interviewer probes.

**Time:** 2–3 hours. **Mode:** guided. You predict, run, compare, and write findings; then ask for review.

**Rules**
1. Write every prediction in `notes/lab-01-javap-findings.md` **before** running the command for that part.
2. Wrong predictions are the most valuable part. Keep them and explain the gap.
3. Paste only the relevant bytecode lines into the findings, not whole dumps.

## Setup

Open a **new** terminal (so JDK 25's `javap` is on PATH), then:

```bash
cd java-jvm-principal-engineer-lab
./gradlew :wp-01-jvm-foundations:compileJava
cd wp-01-jvm-foundations
CP=build/classes/java/main
javap -version                      # expect 25.0.4
ls $CP/lab/wp01/                    # which .class files exist? (question A0 before you look)
```

Useful flags: `-c` bytecode, `-p` include private members, `-v` everything (constant pool, attributes, flags),
`-s` descriptors. Sources: `src/main/java/lab/wp01/Greeter.java` and `BytecodeTour.java`.

## Part A — The class file itself

**A0 (predict):** How many `.class` files will the two source files produce, and what are their names?

```bash
javap -v -cp $CP lab.wp01.BytecodeTour | head -20
```

- **A1 (predict):** What `major version` number does a Java 25 class file have? How is it derived?
- **A2 (observe):** Find the constant pool. What kinds of entries are there, and roughly how many?
- **A3 (so what):** What happens if you run this class on a JDK 21 runtime, which exception is thrown, and at which phase
  (loading, linking or initialization)? How does this relate to `--release` / toolchains in the build?

## Part B — Records

```bash
javap -v -p -cp $CP 'lab.wp01.Greeter$Person'
```

- **B1 (predict):** Which methods exist in the class file for `record Person(String name)`?
  Are `toString`, `equals` and `hashCode` compiled as normal loops or field comparisons?
- **B2 (observe):** What instruction implements `toString`/`equals`/`hashCode`, and what is the bootstrap method?
- **B3 (observe):** Find the `Record` attribute. What does it contain, and who uses it at runtime
  (hint: reflection and serialization)?
- **B4 (so what):** Why would the JDK choose this design over generating plain bytecode in every record?

## Part C — Sealed types and pattern-matching switch

```bash
javap -v -p -cp $CP 'lab.wp01.Greeter$Audience'
javap -c -p -cp $CP lab.wp01.Greeter
```

- **C1 (predict):** Where is "sealed ... permits Person, Team" recorded, and what does the JVM enforce with it?
- **C2 (predict, then observe):** How is `switch (audience) { case Person(var name) -> ... case Team(...) when ... }`
  compiled? Is it a chain of `instanceof` checks, a `tableswitch`, or something else?
- **C3 (observe):** How is the `when size > 10` guard handled, and what happens when the guard fails?
- **C4 (observe):** The switch has no `default`. Find what the compiler emits for the "impossible" case.
  Which exception type would it throw, and when could it actually happen in production?
  (Hint: separate compilation. What if a new `permits` subtype is added later and only one class is recompiled?)

## Part D — String concatenation

```bash
javap -c -p -cp $CP lab.wp01.BytecodeTour | sed -n '/concat(/,/areturn/p'
```

- **D1 (predict):** Does `"Hello " + name + ...` compile to `StringBuilder.append` calls?
- **D2 (observe):** What instruction and bootstrap method are used? Where is the constant template stored?
- **D3 (so what):** Which JEP introduced this, and what does it let the JDK change **without recompiling your code**?

## Part E — Lambda vs anonymous class

```bash
javap -c -p -cp $CP lab.wp01.BytecodeTour | sed -n '/lambda(/,/areturn/p'
javap -c -p -cp $CP 'lab.wp01.BytecodeTour$1'
javap -p -cp $CP lab.wp01.BytecodeTour | grep lambda
```

- **E1 (predict):** Does the lambda get its own `.class` file on disk? Does the anonymous class?
- **E2 (observe):** Where does the lambda's body live, and with what name and modifiers?
  What bootstrap method creates the `Supplier` at runtime?
- **E3 (observe):** How does each variant capture `name`?
- **E4 (so what):** Compare the two for class count / metaspace, startup cost (first call vs later calls), and what
  happens on the first invocation of an `invokedynamic` call site. Keep the answer; you will measure startup in **Lab 3**.

## Part F — Autoboxing and the enhanced for loop

```bash
javap -c -p -cp $CP lab.wp01.BytecodeTour | sed -n '/sameBoxes(/,/ireturn/p'
javap -c -p -cp $CP lab.wp01.BytecodeTour | sed -n '/sum(/,/ireturn/p'
```

- **F1 (predict, then observe):** Which method converts `int` to `Integer`? Why does `sameBoxes(127, 127)` return
  `true` but `sameBoxes(128, 128)` return `false` (see `BytecodeTourTest`)? Which JVM flag changes the upper bound?
- **F2 (observe):** What does `for (int v : values)` become? Where are the hidden method calls and the unboxing?
- **F3 (so what):** In a hot path handling 50k requests/s, what does F1/F2 cost, and how would you *prove* it
  (tool, metric)? You will measure it in WP-05.

## Part G — Tie it together (interview drill)

Write a 2-minute spoken answer (bullet points are fine) for:

> "Walk me from a `.java` file to native machine code."

Your answer must mention: javac and the class-file format, class loading (load, link, verify, prepare, resolve,
initialize), where bytecode and metadata live in memory, interpretation, tiered JIT (C1, C2, profiling), and where
`invokedynamic` fits. Use at least **two concrete facts you observed in this lab** as evidence.

## Definition of done

- [ ] `notes/lab-01-javap-findings.md` completed: every prediction, observation and gap filled in
- [ ] Part G answer written
- [ ] Ask Claude for review. Expect follow-up "why" questions; revise until they hold
- [ ] Tick Lab 1 in `wp-01-jvm-foundations/README.md` and commit
