CREATE TABLE IF NOT EXISTS pokemons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL,
    payload LONGTEXT,
    updated_at BIGINT NOT NULL,
    expires_at BIGINT NOT NULL,
    UNIQUE KEY uq_pokemon_name(identifier)
);
