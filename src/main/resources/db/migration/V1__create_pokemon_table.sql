CREATE TABLE IF NOT EXISTS pokemons (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  payload TEXT,
  updated_at BIGINT NOT NULL,
  expires_at BIGINT NOT NULL,
  UNIQUE KEY uq_pokemon_name (name)
);
