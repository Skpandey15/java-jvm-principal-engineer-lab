package lab.order.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Loads production-like data volume (db/seed/bulk-seed.sql) once, so query plans, indexes and locks behave
 * as they would with real data. Runs before the application reports ready. Disabled in tests and in CI.
 */
@Component
@ConditionalOnBooleanProperty("lab.seed.enabled")
class SeedDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLoader.class);
    private static final long ALREADY_SEEDED_CUSTOMERS = 1_000;

    private final DataSource dataSource;
    private final JdbcClient jdbc;

    SeedDataLoader(DataSource dataSource, JdbcClient jdbc) {
        this.dataSource = dataSource;
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        long customers = jdbc.sql("SELECT count(*) FROM customer").query(Long.class).single();
        if (customers >= ALREADY_SEEDED_CUSTOMERS) {
            log.info("Bulk seed skipped: {} customers already present", customers);
            return;
        }
        log.info("Loading bulk seed data (about 200k orders), this takes a minute...");
        long start = System.nanoTime();
        new ResourceDatabasePopulator(new ClassPathResource("db/seed/bulk-seed.sql")).execute(dataSource);
        log.info("Bulk seed loaded in {} s", (System.nanoTime() - start) / 1_000_000_000);
    }
}
