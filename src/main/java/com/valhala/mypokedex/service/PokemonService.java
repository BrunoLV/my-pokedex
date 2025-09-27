package com.valhala.mypokedex.service;

import com.valhala.mypokedex.adapter.pokeapi.PokeApiAdapter;
import com.valhala.mypokedex.cache.CaffeineCacheAdapter;
import com.valhala.mypokedex.dto.PokemonDTO;
import com.valhala.mypokedex.repository.PokemonEntity;
import com.valhala.mypokedex.repository.PokemonRepository;
import io.micronaut.data.connection.annotation.Connectable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Singleton
public class PokemonService {
    private static final Logger LOG = LoggerFactory.getLogger(PokemonService.class);
    private final PokemonRepository repository;
    private final PokeApiAdapter upstream;
    private final CaffeineCacheAdapter cache;

    @Inject
    public PokemonService(PokemonRepository repository, PokeApiAdapter upstream, CaffeineCacheAdapter cache) {
        this.repository = repository;
        this.upstream = upstream;
        this.cache = cache;
    }

    // No no-arg constructor: prefer dependency injection for all collaborators

    @Connectable
    public Optional<PokemonDTO> getPokemon(String identifier) {
        LOG.debug("getPokemon called with identifier='{}'", identifier);
        // check cache
        Optional<PokemonDTO> cached = cache.get(identifier.toLowerCase());
        if (cached.isPresent()) {
            LOG.info("Cache hit for '{}'", identifier);
            return cached;
        }
        LOG.debug("Cache miss for '{}'", identifier);

        // check repository
        Optional<PokemonEntity> entity = repository.findByName(identifier);
        if (entity.isPresent()) {
            LOG.info("Repository hit for '{}'", identifier);
            // map to DTO minimally
            PokemonEntity e = entity.get();
            PokemonDTO dto = new PokemonDTO(
                    (int) e.getId(),
                    e.getName(),
                    new ArrayList<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new ArrayList<>(),
                    "local"
            );
            cache.put(identifier.toLowerCase(), dto);
            return Optional.of(dto);
        }
        LOG.debug("Repository miss for '{}'", identifier);

        // call upstream
        LOG.info("Fetching pokemon '{}' from upstream", identifier);
        Optional<String> raw = upstream.fetchPokemonRaw(identifier);
        if (raw.isPresent()) {
            String body = raw.get();
            // persist raw payload
            PokemonEntity e = new PokemonEntity();
            e.setName(identifier);
            e.setId(0);
            e.setPayload(body);
            repository.save(e);

            // parse JSON into DTO per contract
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(body);

                int id = root.path("id").asInt(0);
                String name = root.path("name").asText(identifier);

                // types
                List<String> types = new ArrayList<>();
                com.fasterxml.jackson.databind.JsonNode typesNode = root.path("types");
                if (typesNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode t : typesNode) {
                        com.fasterxml.jackson.databind.JsonNode typeObj = t.path("type");
                        if (typeObj != null) {
                            String typeName = typeObj.path("name").asText(null);
                            if (typeName != null) types.add(typeName);
                        }
                    }
                }

                // base_stats from stats array
                Map<String, Integer> baseStats = new HashMap<>();
                com.fasterxml.jackson.databind.JsonNode statsNode = root.path("stats");
                if (statsNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode s : statsNode) {
                        String statName = s.path("stat").path("name").asText(null);
                        int base = s.path("base_stat").asInt(0);
                        if (statName != null) baseStats.put(statName, base);
                    }
                }

                // sprites: take first-level string fields
                Map<String, String> sprites = new HashMap<>();
                com.fasterxml.jackson.databind.JsonNode spritesNode = root.path("sprites");
                if (spritesNode.isObject()) {
                    java.util.Iterator<String> it = spritesNode.fieldNames();
                    while (it.hasNext()) {
                        String key = it.next();
                        com.fasterxml.jackson.databind.JsonNode val = spritesNode.path(key);
                        if (val.isTextual()) {
                            sprites.put(key, val.asText());
                        } else if (val.isNull()) {
                            sprites.put(key, null);
                        } else {
                            // non-textual (object) — serialize to string as fallback
                            try {
                                sprites.put(key, mapper.writeValueAsString(val));
                            } catch (Exception ex) {
                                sprites.put(key, null);
                            }
                        }
                    }
                }

                // abilities
                List<String> abilities = new ArrayList<>();
                com.fasterxml.jackson.databind.JsonNode abilitiesNode = root.path("abilities");
                if (abilitiesNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode a : abilitiesNode) {
                        String abilityName = a.path("ability").path("name").asText(null);
                        if (abilityName != null) abilities.add(abilityName);
                    }
                }

                String sourceUrl = "https://pokeapi.co/api/v2/pokemon/" + identifier;
                PokemonDTO dto = new PokemonDTO(id, name, types, baseStats, sprites, abilities, sourceUrl);
                cache.put(identifier.toLowerCase(), dto);
                LOG.info("Successfully fetched and parsed pokemon '{}' from upstream", identifier);
                return Optional.of(dto);
            } catch (Exception parseEx) {
                LOG.error("Failed to parse upstream payload for '{}'", identifier, parseEx);
                // fallback to minimal DTO on parse errors
                PokemonDTO dto = new PokemonDTO(
                        0,
                        identifier,
                        new ArrayList<>(),
                        new HashMap<>(),
                        new HashMap<>(),
                        new ArrayList<>(),
                        "https://pokeapi.co/api/v2/pokemon/" + identifier
                );
                cache.put(identifier.toLowerCase(), dto);
                return Optional.of(dto);
            }
        }

        LOG.info("Upstream did not return data for '{}'", identifier);
        // If upstream didn't return a payload, do not fabricate a result: let caller handle 404/not-found
        return Optional.empty();
    }
}
