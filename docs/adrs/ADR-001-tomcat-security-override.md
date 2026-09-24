# ADR-001: Override the Spring Boot-managed Tomcat version for critical CVEs

- **Status:** Accepted (temporary)
- **Date:** 2026-09-24
- **Work package:** WP-08 / WP-09 (security & supply chain)

## Context

The CI image scan (Trivy, fixable CRITICAL = fail) blocked the first pipeline run on `main`.
Spring Boot 4.1.1, the latest release, manages `tomcat-embed-core` 11.0.24, which has three critical CVEs:

| CVE | Summary | Fixed in |
|---|---|---|
| CVE-2026-65182 | Security constraint bypass | 11.0.25 |
| CVE-2026-65905 | Authentication bypass | 11.0.25 |
| CVE-2026-68525 | Unauthorized access | 11.0.25 |

No Spring Boot patch release includes the fix yet.

## Options considered

| Option | Pros | Cons |
|---|---|---|
| Wait for Spring Boot 4.1.2 | No divergence from the Boot BOM | Ships known critical CVEs; pipeline stays red |
| Suppress in Trivy (`.trivyignore`) | Pipeline green immediately | Hides a real, fixable risk |
| **Override Tomcat to the latest 11.0.x patch** | Fixes the CVEs now; patch release, same API | Small divergence from the tested BOM; must be removed later |
| Switch to Jetty | Avoids Tomcat | Large change for a patch-level problem |

## Decision

Override all `org.apache.tomcat.embed` modules to **11.0.26** with Gradle dependency constraints in
`lab.spring-boot-conventions` (version in `gradle/libs.versions.toml`, key `tomcat`). A patch-level upgrade
inside the same minor line is low risk, and the full test suite plus the kind smoke test run against it.

## Consequences

- Every Spring Boot service gets the fixed Tomcat without per-service changes.
- `./gradlew :reference-service:order-service:dependencyInsight --dependency tomcat-embed-core` shows the override and its reason.
- **Exit criterion:** when a Spring Boot release manages Tomcat >= 11.0.25 (Dependabot will propose the Boot upgrade),
  delete the `tomcat` version and the constraints, and mark this ADR *Superseded*.

## Related override: build classpath (2026-09-24)

Dependabot's security job flagged `commons-lang3` below 3.18.0. The application already resolved 3.20.0; the vulnerable
3.16.0 was on the **build plugin classpath** (`spring-boot-gradle-plugin 4.1.1 -> commons-compress 1.27.1 -> commons-lang3`),
which runs on every developer and CI machine. A constraint in `build-logic/build.gradle` (version key `commons-lang3`)
raises it to 3.20.0. Same exit criterion: remove once the Spring Boot Gradle plugin no longer brings a vulnerable version.
