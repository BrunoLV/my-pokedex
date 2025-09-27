package com.valhala.mypokedex.db;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.data.connection.annotation.Connectable;
import jakarta.inject.Singleton;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

@Singleton
@Requires(beans = DataSource.class)
public class FlywayMigrationRunner implements ApplicationEventListener<StartupEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(FlywayMigrationRunner.class);

    private final DataSource dataSource;

    public FlywayMigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    @Connectable
    public void onApplicationEvent(StartupEvent event) {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .load();

            LOG.info("Running Flyway migrations on StartupEvent...");
            flyway.migrate();
            LOG.info("Flyway migrations completed.");
        } catch (Exception e) {
            LOG.warn("Flyway failed: {}. Falling back to manual SQL execution.", e.getMessage());
            try {
                runManualMigrations();
            } catch (Exception ex) {
                LOG.error("Failed to run manual migrations fallback", ex);
                throw new RuntimeException("Migration failed", ex);
            }
        }
    }

    private void runManualMigrations() throws Exception {
        // Simple fallback: execute the V1 SQL migration file if present
        String resourcePath = "/db/migration/V1__create_pokemon_table.sql";
        try (var in = FlywayMigrationRunner.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                LOG.warn("Fallback migration file {} not found on classpath", resourcePath);
                return;
            }

            String sql = new String(in.readAllBytes());
            try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
                LOG.info("Executing fallback migration SQL from {}", resourcePath);
                stmt.execute(sql);
            }
        }
    }
}
