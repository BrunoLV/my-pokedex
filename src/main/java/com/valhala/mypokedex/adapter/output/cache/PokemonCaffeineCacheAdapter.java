package com.valhala.mypokedex.adapter.output.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.valhala.mypokedex.domain.pokemon.dto.PokemonDTO;
import com.valhala.mypokedex.domain.pokemon.ports.PokemonCachePort;
import jakarta.inject.Singleton;
import io.micronaut.context.annotation.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;

@Singleton
@Requires(property = "cache.strategy", notEquals = "redis", defaultValue = "caffeine")
public class PokemonCaffeineCacheAdapter implements PokemonCachePort {
    private static final Logger LOG = LoggerFactory.getLogger(PokemonCaffeineCacheAdapter.class);
    private final Cache<String, PokemonDTO> cache;

    public PokemonCaffeineCacheAdapter() {
        cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofDays(7))
                .maximumSize(10_000)
                .build();
    }

    @Override
    public Optional<PokemonDTO> get(String key) {
        PokemonDTO dto = cache.getIfPresent(key);
        if (dto != null) {
            LOG.debug("Cache hit for key='{}'", key);
            return Optional.of(dto);
        }
        LOG.debug("Cache miss for key='{}'", key);
        return Optional.empty();
    }

    @Override
    public void put(String key, PokemonDTO dto) {
        if (key == null || dto == null) {
            LOG.warn("Attempt to put null key or dto into cache: key={}, dtoNull={}", key, dto == null);
            return;
        }
        cache.put(key, dto);
        LOG.info("Cached pokemon for key='{}'", key);
    }
}
