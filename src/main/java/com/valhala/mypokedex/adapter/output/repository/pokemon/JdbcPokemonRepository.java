package com.valhala.mypokedex.adapter.output.repository.pokemon;

import com.valhala.mypokedex.domain.pokemon.repository.PokemonEntity;
import com.valhala.mypokedex.domain.pokemon.repository.PokemonRepository;
import io.micronaut.data.connection.annotation.Connectable;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

@Singleton
public class JdbcPokemonRepository implements PokemonRepository {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcPokemonRepository.class);
    private static final long DEFAULT_TTL_SECONDS = 30L * 24L * 3600L; // 30 days
    private final DataSource ds;

    public JdbcPokemonRepository(Provider<DataSource> dsProvider) {
        this.ds = dsProvider.get();
    }

    @Override
    @Connectable
    public Optional<PokemonEntity> findByIdentifier(String identifier) {
        // Use the 'identifier' column consistently
        String sql = "SELECT id, identifier, payload, updated_at, expires_at FROM pokemons WHERE identifier = ? LIMIT 1";
        LOG.debug("Querying pokemon by identifier='{}'", identifier);
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, identifier);
            Optional<PokemonEntity> e = getPokemonEntity(ps);
            if (e.isPresent()) {
                LOG.info("Found pokemon in DB: {}", identifier);
                return e;
            }
        } catch (SQLException ex) {
            LOG.error("SQL error when querying pokemon by identifier={}", identifier, ex);
        }
        LOG.debug("Pokemon not found in DB: {}", identifier);
        return Optional.empty();
    }

    private Optional<PokemonEntity> getPokemonEntity(PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            PokemonEntity e = new PokemonEntity();
            e.setId(rs.getLong("id"));
            e.setIdentifier(rs.getString("identifier"));
            e.setPayload(rs.getString("payload"));
            long updated = rs.getLong("updated_at");
            if (rs.wasNull()) {
                e.setUpdatedAt(null);
            } else {
                e.setUpdatedAt(Instant.ofEpochSecond(updated));
            }
            long expires = rs.getLong("expires_at");
            if (rs.wasNull()) {
                e.setExpiresAt(null);
            } else {
                e.setExpiresAt(Instant.ofEpochSecond(expires));
            }
            return Optional.of(e);
        }
        return Optional.empty();
    }

    @Override
    @Connectable
    public void save(PokemonEntity entity) {
        // Ensure timestamps are present to avoid NPE when accessing getEpochSecond()
        Instant now = Instant.now();
        if (entity.getUpdatedAt() == null) {
            entity.setUpdatedAt(now);
        }
        if (entity.getExpiresAt() == null) {
            entity.setExpiresAt(entity.getUpdatedAt().plusSeconds(DEFAULT_TTL_SECONDS));
        }
        // Use MySQL upsert to insert or update existing record by identifier
        String insert = "INSERT INTO pokemons (identifier, payload, updated_at, expires_at) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE payload=VALUES(payload), updated_at=VALUES(updated_at), expires_at=VALUES(expires_at)";
        LOG.debug("Saving pokemon '{}', updatedAt={}, expiresAt={}", entity.getIdentifier(), entity.getUpdatedAt(), entity.getExpiresAt());
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(insert)) {
            ps.setString(1, entity.getIdentifier());
            ps.setString(2, entity.getPayload());
            ps.setLong(3, entity.getUpdatedAt().getEpochSecond());
            ps.setLong(4, entity.getExpiresAt().getEpochSecond());
            int rows = ps.executeUpdate();
            LOG.info("Saved pokemon '{}' (rows affected={})", entity.getIdentifier(), rows);
        } catch (SQLException ex) {
            LOG.error("SQL error when saving pokemon='{}'", entity.getIdentifier(), ex);
        }
    }
}
