package com.valhala.mypokedex.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.valhala.mypokedex.dto.PokemonDTO;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;

@Singleton
public class CaffeineCacheAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(CaffeineCacheAdapter.class);
    private final Cache<String, PokemonDTO> cache;

    public CaffeineCacheAdapter() {
        cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofDays(7))
                .maximumSize(10_000)
                .build();
    }

    public Optional<PokemonDTO> get(String key) {
        PokemonDTO dto = cache.getIfPresent(key);
        if (dto != null) {
            LOG.debug("Cache hit for key='{}'", key);
            return Optional.of(dto);
        }
        LOG.debug("Cache miss for key='{}'", key);
        return Optional.empty();
    }

    public void put(String key, PokemonDTO dto) {
        if (key == null || dto == null) {
            LOG.warn("Attempt to put null key or dto into cache: key={}, dtoNull={}", key, dto == null);
            return;
        }
        cache.put(key, dto);
        LOG.info("Cached pokemon for key='{}'", key);
    }
}
