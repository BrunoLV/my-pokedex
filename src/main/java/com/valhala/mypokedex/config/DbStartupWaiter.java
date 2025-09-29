package com.valhala.mypokedex.config;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;

@Singleton
@Requires(notEnv = "test")
@Requires(property = "wait.for.db.enabled", notEquals = "false")
public class DbStartupWaiter implements ApplicationEventListener<ApplicationStartupEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(DbStartupWaiter.class);
    private final DataSource dataSource;
    private final DbWaitConfiguration config;

    public DbStartupWaiter(DataSource dataSource, DbWaitConfiguration config) {
        this.dataSource = dataSource;
        this.config = config;
    }

    @Override
    public void onApplicationEvent(ApplicationStartupEvent event) {
        int maxRetries = config.getMaxRetries();
        int waitSeconds = config.getWaitSeconds();
        LOG.info("Waiting for database to become available (maxRetries={} waitSeconds={})", maxRetries, waitSeconds);
        int attempt = 0;
        while (attempt < maxRetries) {
            attempt++;
            try (Connection ignored = dataSource.getConnection()) {
                LOG.info("Database is available (attempt={})", attempt);
                return;
            } catch (Exception e) {
                LOG.warn("Database not ready yet (attempt={}): {}", attempt, e.getMessage());
                try {
                    Thread.sleep(waitSeconds * 1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for DB", ie);
                }
            }
        }
        LOG.error("Database did not become available after {} attempts, shutting down.", maxRetries);
        throw new IllegalStateException("Database not available after startup retries");
    }
}
