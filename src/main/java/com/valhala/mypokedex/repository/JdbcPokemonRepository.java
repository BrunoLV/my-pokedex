package com.valhala.mypokedex.repository;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import javax.sql.DataSource;
import jakarta.inject.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

@Singleton
@Requires(notEnv = "test")
public class JdbcPokemonRepository implements PokemonRepository {

    private final DataSource ds;

    public JdbcPokemonRepository(Provider<DataSource> dsProvider) {
        this.ds = dsProvider.get();
    }

    @Override
    public Optional<PokemonEntity> findByName(String name) {
        String sql = "SELECT id, name, payload, updated_at, expires_at FROM pokemons WHERE name = ? LIMIT 1";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PokemonEntity e = new PokemonEntity();
                e.id = rs.getLong("id");
                e.name = rs.getString("name");
                e.payload = rs.getString("payload");
                long updated = rs.getLong("updated_at");
                long expires = rs.getLong("expires_at");
                e.updatedAt = Instant.ofEpochSecond(updated);
                e.expiresAt = Instant.ofEpochSecond(expires);
                return Optional.of(e);
            }
        } catch (SQLException ex) {
            // log and return empty
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<PokemonEntity> findById(long id) {
        String sql = "SELECT id, name, payload, updated_at, expires_at FROM pokemons WHERE id = ? LIMIT 1";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PokemonEntity e = new PokemonEntity();
                e.id = rs.getLong("id");
                e.name = rs.getString("name");
                e.payload = rs.getString("payload");
                long updated = rs.getLong("updated_at");
                long expires = rs.getLong("expires_at");
                e.updatedAt = Instant.ofEpochSecond(updated);
                e.expiresAt = Instant.ofEpochSecond(expires);
                return Optional.of(e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void save(PokemonEntity entity) {
        // Use MySQL upsert to insert or update existing record by name
        String insert = "INSERT INTO pokemons (name, payload, updated_at, expires_at) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE payload=VALUES(payload), updated_at=VALUES(updated_at), expires_at=VALUES(expires_at)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(insert)) {
            ps.setString(1, entity.name);
            ps.setString(2, entity.payload);
            ps.setLong(3, entity.updatedAt.getEpochSecond());
            ps.setLong(4, entity.expiresAt.getEpochSecond());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
