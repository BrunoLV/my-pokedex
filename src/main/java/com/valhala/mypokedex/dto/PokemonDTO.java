package com.valhala.mypokedex.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PokemonDTO(
        int id,
        String name,
        List<String> types,
        Map<String, Integer> base_stats,
        Map<String, String> sprites,
        List<String> abilities,
        String source_url
) {
    public static PokemonDTO empty() {
        return new PokemonDTO(
                0,
                null,
                new ArrayList<>(),
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>(),
                null
        );
    }
}
