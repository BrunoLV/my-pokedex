package com.valhala.mypokedex.domain.pokemon.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valhala.mypokedex.adapter.output.cache.PokemonCaffeineCacheAdapter;
import com.valhala.mypokedex.domain.pokemon.dto.PokemonDTO;
import com.valhala.mypokedex.domain.pokemon.ports.PokeApiPort;
import com.valhala.mypokedex.domain.pokemon.repository.PokemonEntity;
import com.valhala.mypokedex.domain.pokemon.repository.PokemonRepository;
import io.micronaut.data.connection.annotation.Connectable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Singleton
public class GetPokemonUseCase {
    private static final Logger LOG = LoggerFactory.getLogger(GetPokemonUseCase.class);
    private final PokemonRepository repository;
    private final PokeApiPort upstream;
    private final PokemonCaffeineCacheAdapter cache;

    @Inject
    public GetPokemonUseCase(PokemonRepository repository, PokeApiPort upstream, PokemonCaffeineCacheAdapter cache) {
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
        Optional<PokemonEntity> entity = repository.findByIdentifier(identifier);
        if (entity.isPresent()) {
            LOG.info("Repository hit for '{}'", identifier);
            // map to DTO minimally
            PokemonEntity e = entity.get();
            PokemonDTO dto = new PokemonDTO(
                    (int) e.getId(),
                    e.getIdentifier(),
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
            e.setIdentifier(identifier);
            e.setId(0);
            e.setPayload(body);
            repository.save(e);

            // parse JSON into DTO per contract
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(body);

                int id = root.path("id").asInt(0);
                String name = root.path("identifier").asText(identifier);

                // types
                List<String> types = new ArrayList<>();
                JsonNode typesNode = root.path("types");
                if (typesNode.isArray()) {
                    for (JsonNode t : typesNode) {
                        JsonNode typeObj = t.path("type");
                        if (typeObj != null) {
                            String typeName = typeObj.path("identifier").asText(null);
                            if (typeName != null) types.add(typeName);
                        }
                    }
                }

                // base_stats from stats array
                Map<String, Integer> baseStats = new HashMap<>();
                JsonNode statsNode = root.path("stats");
                if (statsNode.isArray()) {
                    for (JsonNode s : statsNode) {
                        String statName = s.path("stat").path("identifier").asText(null);
                        int base = s.path("base_stat").asInt(0);
                        if (statName != null) baseStats.put(statName, base);
                    }
                }

                // sprites: take first-level string fields
                Map<String, String> sprites = new HashMap<>();
                JsonNode spritesNode = root.path("sprites");
                if (spritesNode.isObject()) {
                    java.util.Iterator<String> it = spritesNode.fieldNames();
                    while (it.hasNext()) {
                        String key = it.next();
                        JsonNode val = spritesNode.path(key);
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
                JsonNode abilitiesNode = root.path("abilities");
                if (abilitiesNode.isArray()) {
                    for (JsonNode a : abilitiesNode) {
                        String abilityName = a.path("ability").path("identifier").asText(null);
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
