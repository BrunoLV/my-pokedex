package com.valhala.mypokedex.unit;

import com.valhala.mypokedex.repository.InMemoryPokemonRepository;
import com.valhala.mypokedex.repository.PokemonEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class TestPokemonRepository {

    @Test
    void save_sets_expires_at_and_read_respects_ttl() {
        InMemoryPokemonRepository repo = new InMemoryPokemonRepository();
        PokemonEntity e = new PokemonEntity();
        e.id = 1;
        e.name = "testmon";
        e.updatedAt = Instant.now();
        repo.save(e);
        assertTrue(repo.findByName("testmon").isPresent());
        // simulate expiry by setting expiresAt in the past
        PokemonEntity stored = repo.findByName("testmon").get();
        stored.expiresAt = Instant.now().minusSeconds(1);
        repo.save(stored);
        assertFalse(repo.findByName("testmon").isPresent());
    }
}
