package com.valhala.mypokedex.service;

import com.valhala.mypokedex.adapter.UpstreamAdapter;
import com.valhala.mypokedex.cache.CaffeineCacheAdapter;
import com.valhala.mypokedex.dto.PokemonDTO;
import com.valhala.mypokedex.repository.InMemoryPokemonRepository;
import com.valhala.mypokedex.repository.PokemonEntity;
import com.valhala.mypokedex.repository.PokemonRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class PokemonService {
    private final PokemonRepository repository;
    private final UpstreamAdapter upstream;
    private final CaffeineCacheAdapter cache;

    @Inject
    public PokemonService(PokemonRepository repository, UpstreamAdapter upstream, CaffeineCacheAdapter cache) {
        this.repository = repository;
        this.upstream = upstream;
        this.cache = cache;
    }

    // No-arg constructor kept for tests and quick instantiation
    public PokemonService() {
        this.repository = new InMemoryPokemonRepository();
        this.upstream = new com.valhala.mypokedex.adapter.HttpUpstreamAdapter();
        this.cache = new CaffeineCacheAdapter();
    }

    public Optional<PokemonDTO> getPokemon(String identifier) {
        // check cache
        Optional<PokemonDTO> cached = cache.get(identifier.toLowerCase());
        if (cached.isPresent()) return cached;

        // check repository
        Optional<PokemonEntity> entity = repository.findByName(identifier);
        if (entity.isPresent()) {
            // map to DTO minimally
            PokemonDTO dto = new PokemonDTO();
            dto.name = entity.get().name;
            dto.id = (int)entity.get().id;
            dto.source_url = "local";
            cache.put(identifier.toLowerCase(), dto);
            return Optional.of(dto);
        }

        // call upstream
        Optional<String> raw = upstream.fetchPokemonRaw(identifier);
        if (raw.isPresent()) {
            String body = raw.get();
            // persist raw payload
            PokemonEntity e = new PokemonEntity();
            e.name = identifier;
            e.id = 0;
            e.payload = body;
            repository.save(e);

            // parse JSON into DTO per contract
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(body);

                PokemonDTO dto = new PokemonDTO();
                dto.id = root.path("id").asInt(0);
                dto.name = root.path("name").asText(identifier);

                // types
                com.fasterxml.jackson.databind.JsonNode typesNode = root.path("types");
                if (typesNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode t : typesNode) {
                        com.fasterxml.jackson.databind.JsonNode typeObj = t.path("type");
                        if (typeObj != null) {
                            String typeName = typeObj.path("name").asText(null);
                            if (typeName != null) dto.types.add(typeName);
                        }
                    }
                }

                // base_stats from stats array
                com.fasterxml.jackson.databind.JsonNode statsNode = root.path("stats");
                if (statsNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode s : statsNode) {
                        String statName = s.path("stat").path("name").asText(null);
                        int base = s.path("base_stat").asInt(0);
                        if (statName != null) dto.base_stats.put(statName, base);
                    }
                }

                // sprites: take first-level string fields
                com.fasterxml.jackson.databind.JsonNode spritesNode = root.path("sprites");
                if (spritesNode.isObject()) {
                    java.util.Iterator<String> it = spritesNode.fieldNames();
                    while (it.hasNext()) {
                        String key = it.next();
                        com.fasterxml.jackson.databind.JsonNode val = spritesNode.path(key);
                        if (val.isTextual()) {
                            dto.sprites.put(key, val.asText());
                        } else if (val.isNull()) {
                            dto.sprites.put(key, null);
                        } else {
                            // non-textual (object) — serialize to string as fallback
                            try { dto.sprites.put(key, mapper.writeValueAsString(val)); } catch (Exception ex) { dto.sprites.put(key, null); }
                        }
                    }
                }

                // abilities
                com.fasterxml.jackson.databind.JsonNode abilitiesNode = root.path("abilities");
                if (abilitiesNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode a : abilitiesNode) {
                        String abilityName = a.path("ability").path("name").asText(null);
                        if (abilityName != null) dto.abilities.add(abilityName);
                    }
                }

                dto.source_url = "https://pokeapi.co/api/v2/pokemon/" + identifier;
                cache.put(identifier.toLowerCase(), dto);
                return Optional.of(dto);
            } catch (Exception parseEx) {
                // fallback to minimal DTO on parse errors
                PokemonDTO dto = new PokemonDTO();
                dto.name = identifier;
                dto.id = 0;
                dto.source_url = "https://pokeapi.co/api/v2/pokemon/" + identifier;
                cache.put(identifier.toLowerCase(), dto);
                return Optional.of(dto);
            }
        }

        // If upstream didn't return a payload, do not fabricate a result: let caller handle 404/not-found
        return Optional.empty();
    }
}
