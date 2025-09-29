package com.valhala.mypokedex.domain.pokemon.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PokemonDTO(
        int id,
        String identifier,
        List<String> types,
        @JsonProperty("base_stats") Map<String, Integer> baseStats,
        Map<String, String> sprites,
        List<String> abilities,
        @JsonProperty("source_url") String sourceUrl) {
    public static PokemonDTO empty() {
        return new PokemonDTO(
                0,
                null,
                new ArrayList<>(),
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>(),
                null);
    }
}
