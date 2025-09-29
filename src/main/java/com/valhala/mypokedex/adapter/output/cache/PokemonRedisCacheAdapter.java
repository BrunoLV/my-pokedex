package com.valhala.mypokedex.adapter.output.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valhala.mypokedex.domain.pokemon.dto.PokemonDTO;
import com.valhala.mypokedex.domain.pokemon.ports.PokemonCachePort;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import io.micronaut.context.annotation.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.RedisClient;

import java.util.Optional;

@Singleton
@Requires(property = "cache.strategy", value = "redis")
public class PokemonRedisCacheAdapter implements PokemonCachePort {
    private static final Logger LOG = LoggerFactory.getLogger(PokemonRedisCacheAdapter.class);
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final ObjectMapper mapper = new ObjectMapper();
    // TTL and timeout configurable via env/props
    private final int ttlSeconds;
    private final int timeoutMs;

    public PokemonRedisCacheAdapter() {
        String host = System.getProperty("redis.host", System.getenv().getOrDefault("REDIS_HOST", "localhost"));
        String port = System.getProperty("redis.port", System.getenv().getOrDefault("REDIS_PORT", "6379"));
        String redisUri = "redis://" + host + ":" + port;
        this.redisClient = RedisClient.create(redisUri);
        this.connection = redisClient.connect();
        this.ttlSeconds = Integer.parseInt(
                System.getProperty("redis.ttl-seconds", System.getenv().getOrDefault("REDIS_TTL_SECONDS", "604800")));
        this.timeoutMs = Integer.parseInt(
                System.getProperty("redis.timeout-ms", System.getenv().getOrDefault("REDIS_TIMEOUT_MS", "2000")));
        LOG.info("Initialized Lettuce Redis cache adapter (uri={} ttl={}s timeout={}ms)", redisUri, ttlSeconds,
                timeoutMs);
    }

    @Override
    public Optional<PokemonDTO> get(String key) {
        if (key == null)
            return Optional.empty();
        try {
            RedisCommands<String, String> commands = connection.sync();
            String v = commands.get(key);
            if (v == null) {
                LOG.debug("Redis cache miss for key='{}'", key);
                return Optional.empty();
            }
            try {
                PokemonDTO dto = mapper.readValue(v, PokemonDTO.class);
                LOG.debug("Redis cache hit for key='{}'", key);
                return Optional.of(dto);
            } catch (Exception ex) {
                LOG.warn("Failed to deserialize cached value for key='{}'", key, ex);
                return Optional.empty();
            }
        } catch (Exception ex) {
            LOG.error("Redis error on get for key='{}'", key, ex);
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, PokemonDTO dto) {
        if (key == null || dto == null) {
            LOG.warn("Attempt to put null key or dto into Redis cache: key={}, dtoNull={}", key, dto == null);
            return;
        }
        try {
            RedisCommands<String, String> commands = connection.sync();
            try {
                String payload = mapper.writeValueAsString(dto);
                commands.setex(key, ttlSeconds, payload);
                LOG.info("Cached pokemon in Redis for key='{}' ttl={}s", key, ttlSeconds);
            } catch (JsonProcessingException e) {
                LOG.error("Failed to serialize PokemonDTO for key='{}'", key, e);
            }
        } catch (Exception ex) {
            LOG.error("Redis error on put for key='{}'", key, ex);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (connection != null)
                connection.close();
        } catch (Exception ignored) {
        }
        try {
            if (redisClient != null)
                redisClient.shutdown();
        } catch (Exception ignored) {
        }
    }
}
