package com.valhala.mypokedex.adapter;

import java.util.Optional;

public interface UpstreamAdapter {
    Optional<String> fetchPokemonRaw(String identifier);
}
