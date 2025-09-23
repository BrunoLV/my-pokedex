package com.valhala.mypokedex.integration;

import com.valhala.mypokedex.repository.JdbcPokemonRepository;
import com.valhala.mypokedex.repository.PokemonEntity;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(environments = "test")
public class JdbcPokemonRepositoryIntegrationTest {

    @Inject
    DataSource dataSource;

    @Test
    void upsertAndTimestampsArePersisted() {
        // rely on Flyway migrations to create schema in test DB

        JdbcPokemonRepository repo = new JdbcPokemonRepository(() -> dataSource);

        PokemonEntity e = new PokemonEntity();
        e.name = "testmon";
        e.payload = "{\"id\":999,\"name\":\"testmon\"}";
        e.updatedAt = Instant.now();
        e.expiresAt = e.updatedAt.plusSeconds(60 * 60 * 24 * 30); // 30 days

        // insert
        repo.save(e);

        Optional<PokemonEntity> found = repo.findByName("testmon");
        assertTrue(found.isPresent(), "Inserted row should be found");
        PokemonEntity f = found.get();
        assertEquals("testmon", f.name);
        assertEquals(e.payload, f.payload);
        assertNotNull(f.updatedAt);
        assertNotNull(f.expiresAt);

        // update payload and timestamps
        e.payload = "{\"id\":999,\"name\":\"testmon-updated\"}";
        e.updatedAt = Instant.now();
        e.expiresAt = e.updatedAt.plusSeconds(60 * 60 * 24 * 30);
        repo.save(e);

        Optional<PokemonEntity> found2 = repo.findByName("testmon");
        assertTrue(found2.isPresent());
        PokemonEntity f2 = found2.get();
        assertEquals(e.payload, f2.payload, "Payload should be updated after upsert");

    }
}
