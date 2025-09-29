package com.valhala.mypokedex.domain.pokemon.ports;

import java.util.Optional;

public interface PokeApiPort {
    Optional<String> fetchPokemonRaw(String identifier);
}
