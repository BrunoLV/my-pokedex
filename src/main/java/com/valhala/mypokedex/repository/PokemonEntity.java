package com.valhala.mypokedex.repository;

import java.time.Instant;

public class PokemonEntity {
    public long id;
    public String name;
    public String payload; // raw JSON
    public Instant updatedAt;
    public Instant expiresAt;

    public PokemonEntity() {}
}
