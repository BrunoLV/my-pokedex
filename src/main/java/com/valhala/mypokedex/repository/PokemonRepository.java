package com.valhala.mypokedex.repository;

import java.util.Optional;

public interface    PokemonRepository {
    Optional<PokemonEntity> findByName(String name);

    Optional<PokemonEntity> findById(long id);

    void save(PokemonEntity entity);
}
