package com.valhala.mypokedex.domain.pokemon.repository;

import java.util.Optional;

public interface PokemonRepository {

    Optional<PokemonEntity> findByIdentifier(String identifier);
    void save(PokemonEntity entity);
}
