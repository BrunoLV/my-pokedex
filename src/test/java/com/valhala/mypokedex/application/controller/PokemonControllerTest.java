package com.valhala.mypokedex.application.controller;

import com.valhala.mypokedex.domain.pokemon.ports.PokemonCachePort;
import com.valhala.mypokedex.domain.pokemon.dto.PokemonDTO;
import com.valhala.mypokedex.domain.pokemon.ports.PokeApiPort;
import com.valhala.mypokedex.domain.pokemon.usecase.GetPokemonUseCase;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class PokemonControllerTest {

    @Inject
    @Client("/api/pokemon")
    HttpClient client;

    @Inject
    GetPokemonUseCase service; // real bean provided by DI

    @Inject
    PokeApiPort pokeApiPort; // will be the mocked bean provided below

    @Inject
    DataSource dataSource;

    @Inject
    PokemonCachePort cacheAdapter;

    @MockBean(PokeApiPort.class)
    PokeApiPort pokeApiPortMock() {
        return Mockito.mock(PokeApiPort.class);
    }

    @BeforeEach
    void setup() throws Exception {
        // reset mock interactions
        Mockito.reset(pokeApiPort);

        // clear pokemons table to avoid cross-test contamination
        try (Connection c = dataSource.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM pokemons")) {
            ps.executeUpdate();
        }

        // best-effort: if underlying adapter has a field named 'cache' and it's a
        // Caffeine cache, invalidate it
        try {
            Field f = cacheAdapter.getClass().getDeclaredField("cache");
            f.setAccessible(true);
            Object cache = f.get(cacheAdapter);
            if (cache instanceof com.github.benmanes.caffeine.cache.Cache) {
                ((com.github.benmanes.caffeine.cache.Cache<?, ?>) cache).invalidateAll();
            }
        } catch (NoSuchFieldException ignored) {
            // if field not present, ignore (best-effort cleanup)
        }
    }

    @Test
    void testGetPokemonReturnsOk() {
        String rawJson = "{\n" +
                "  \"id\": 25,\n" +
                "  \"identifier\": \"pikachu\",\n" +
                "  \"types\": [ { \"type\": { \"identifier\": \"electric\" } } ],\n" +
                "  \"stats\": [ { \"stat\": { \"identifier\": \"speed\" }, \"base_stat\": 90 } ],\n" +
                "  \"sprites\": { \"front_default\": \"https://.../sprite.png\" },\n" +
                "  \"abilities\": [ { \"ability\": { \"identifier\": \"static\" } } ]\n" +
                "}";

        PokemonDTO expected = new PokemonDTO(
                25,
                "pikachu",
                List.of("electric"),
                Map.of("speed", 90),
                Map.of("front_default", "https://.../sprite.png"),
                List.of("static"),
                "https://pokeapi.co/api/v2/pokemon/pikachu");

        Mockito.when(pokeApiPort.fetchPokemonRaw("pikachu")).thenReturn(Optional.of(rawJson));

        var request = HttpRequest.GET("/pikachu");
        var response = client.toBlocking().exchange(request, PokemonDTO.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(response.getBody().isPresent());
        assertEquals(expected, response.getBody().get());
    }

    @Test
    void testGetPokemonNotFound() {
        Mockito.when(pokeApiPort.fetchPokemonRaw("missingmon")).thenReturn(Optional.empty());

        var request = HttpRequest.GET("/missingmon");
        try {
            client.toBlocking().exchange(request, PokemonDTO.class);
            fail("Expected 404");
        } catch (HttpClientResponseException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        }
    }

    @Test
    void testGetPokemonBadRequest() {
        var request = HttpRequest.GET("/%20");
        try {
            client.toBlocking().exchange(request, PokemonDTO.class);
            fail("Expected 400");
        } catch (HttpClientResponseException ex) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        }
    }

    @Test
    void testGetPokemonServerError() {
        Mockito.when(pokeApiPort.fetchPokemonRaw("boom")).thenThrow(new RuntimeException("uh-oh"));

        var request = HttpRequest.GET("/boom");
        try {
            client.toBlocking().exchange(request, PokemonDTO.class);
            fail("Expected 500");
        } catch (HttpClientResponseException ex) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
        }
    }
}
