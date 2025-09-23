package com.valhala.mypokedex.repository;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Requires(env = "test")
public class InMemoryPokemonRepository implements PokemonRepository {
    private final Map<String, PokemonEntity> store = new ConcurrentHashMap<>();

    @Override
    public Optional<PokemonEntity> findByName(String name) {
        PokemonEntity e = store.get(name.toLowerCase());
        if (e == null) return Optional.empty();
        if (Instant.now().isAfter(e.expiresAt)) {
            store.remove(name.toLowerCase());
            return Optional.empty();
        }
        return Optional.of(e);
    }

    @Override
    public Optional<PokemonEntity> findById(long id) {
        return store.values().stream().filter(e -> e.id == id).findFirst();
    }

    @Override
    public void save(PokemonEntity entity) {
        if (entity.updatedAt == null) entity.updatedAt = Instant.now();
        if (entity.expiresAt == null) entity.expiresAt = entity.updatedAt.plus(30, ChronoUnit.DAYS);
        store.put(entity.name.toLowerCase(), entity);
    }
}
