package com.valhala.mypokedex.domain.pokemon.ports;

import com.valhala.mypokedex.domain.pokemon.dto.PokemonDTO;

import java.util.Optional;

public interface PokemonCachePort {

    Optional<PokemonDTO> get(String key);
    void put(String key, PokemonDTO dto);

}
