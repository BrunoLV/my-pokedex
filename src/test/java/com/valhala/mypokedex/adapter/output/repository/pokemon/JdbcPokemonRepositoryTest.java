package com.valhala.mypokedex.adapter.output.repository.pokemon;

import com.valhala.mypokedex.domain.pokemon.repository.PokemonEntity;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class JdbcPokemonRepositoryTest {

    @Inject
    JdbcPokemonRepository repository;

    @Inject
    DataSource dataSource;

    @BeforeEach
    void cleanup() throws Exception {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM pokemons")) {
            ps.executeUpdate();
        }
    }

    @Test
    void saveAndFindByIdentifier() {
        PokemonEntity e = new PokemonEntity();
        e.setIdentifier("bulbasaur");
        e.setPayload("{\"name\":\"bulbasaur\"}");

        repository.save(e);

        Optional<PokemonEntity> opt = repository.findByIdentifier("bulbasaur");
        assertTrue(opt.isPresent(), "Expected saved pokemon to be present");
        PokemonEntity found = opt.get();
        assertEquals("bulbasaur", found.getIdentifier());
        assertEquals("{\"name\":\"bulbasaur\"}", found.getPayload());
        assertNotEquals(0L, found.getId(), "Expected auto-generated id > 0");
        assertNotNull(found.getUpdatedAt(), "updatedAt should be set by repository");
        assertNotNull(found.getExpiresAt(), "expiresAt should be set by repository");
        assertTrue(found.getExpiresAt().isAfter(found.getUpdatedAt()) || found.getExpiresAt().equals(found.getUpdatedAt().plusSeconds(30L * 24L * 3600L)), "expiresAt should be after updatedAt");
    }

    @Test
    void findByIdentifierNotFound() {
        Optional<PokemonEntity> opt = repository.findByIdentifier("missing");
        assertTrue(opt.isEmpty(), "Should return empty when pokemon does not exist");
    }

    @Test
    void saveUpdatesExistingRecord() {
        PokemonEntity first = new PokemonEntity();
        first.setIdentifier("charmander");
        first.setPayload("first");
        repository.save(first);

        PokemonEntity second = new PokemonEntity();
        second.setIdentifier("charmander");
        second.setPayload("second");
        repository.save(second);

        Optional<PokemonEntity> opt = repository.findByIdentifier("charmander");
        assertTrue(opt.isPresent());
        PokemonEntity found = opt.get();
        assertEquals("second", found.getPayload(), "Payload should be updated to latest value");
    }
}

