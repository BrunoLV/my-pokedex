package com.valhala.mypokedex.integration;

import com.valhala.mypokedex.dto.PokemonDTO;
import com.valhala.mypokedex.service.PokemonService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(environments = "test")
public class CacheUpstreamIntegrationTest {

    @Inject
    PokemonService pokemonService;

    @Inject
    MockWebServer server;

    @Test
    void cacheMissTriggersUpstreamAndPopulatesCache() throws Exception {
        String name = "bulbasaur";

        String raw = "{\"id\":1,\"name\":\"bulbasaur\",\"types\":[] }";

        server.enqueue(new MockResponse().setResponseCode(200).setBody(raw).addHeader("Content-Type", "application/json"));

        // First call should fetch from upstream (via test upstream factory) and persist/cache
        PokemonDTO dto1 = pokemonService.getPokemon(name).orElseThrow();
        assertEquals("bulbasaur", dto1.name);

        // Enqueue a different response to prove the next call is served from cache
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"id\":1,\"name\":\"bulbasaur-fake\"}").addHeader("Content-Type", "application/json"));

        PokemonDTO dto2 = pokemonService.getPokemon(name).orElseThrow();
        // Should still be the original since it should come from cache
        assertEquals("bulbasaur", dto2.name);
    }
}
