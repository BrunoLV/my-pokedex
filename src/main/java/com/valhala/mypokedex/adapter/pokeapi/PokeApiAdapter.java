package com.valhala.mypokedex.adapter.pokeapi;

import java.util.Optional;

public interface PokeApiAdapter {
    Optional<String> fetchPokemonRaw(String identifier);
}
