package com.valhala.mypokedex.integration;

import com.valhala.mypokedex.service.PokemonService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(environments = "test")
public class MicronautPokemonIntegrationTest {

    @Inject
    PokemonService service;

    @Inject
    MockWebServer server;

    @Test
    void di_integration_parses_upstream_and_caches() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"id\":25,\"name\":\"pikachu\"}"));

        Optional<?> res = service.getPokemon("pikachu");
        assertTrue(res.isPresent());

        // second call should be served from cache
        int before = server.getRequestCount();
        Optional<?> res2 = service.getPokemon("pikachu");
        assertTrue(res2.isPresent());
        int after = server.getRequestCount();
        assertTrue(after == before);
    }
}
