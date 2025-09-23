package com.valhala.mypokedex.cache;

import jakarta.inject.Singleton;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.valhala.mypokedex.dto.PokemonDTO;

import java.time.Duration;
import java.util.Optional;

@Singleton
public class CaffeineCacheAdapter {
    private final Cache<String, PokemonDTO> cache;

    public CaffeineCacheAdapter() {
        cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofDays(7))
                .maximumSize(10_000)
                .build();
    }

    public Optional<PokemonDTO> get(String key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    public void put(String key, PokemonDTO dto) {
        cache.put(key, dto);
    }
}
