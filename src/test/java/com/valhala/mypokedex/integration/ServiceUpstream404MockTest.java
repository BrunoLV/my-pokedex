package com.valhala.mypokedex.integration;

import com.valhala.mypokedex.adapter.HttpUpstreamAdapter;
import com.valhala.mypokedex.cache.CaffeineCacheAdapter;
import com.valhala.mypokedex.repository.InMemoryPokemonRepository;
import com.valhala.mypokedex.repository.PokemonRepository;
import com.valhala.mypokedex.service.PokemonService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ServiceUpstream404MockTest {

    private MockWebServer server;

    @BeforeEach
    void start() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stop() throws IOException {
        server.shutdown();
    }

    @Test
    void service_returns_empty_on_404() {
        server.enqueue(new MockResponse().setResponseCode(404));
        String base = server.url("/api/pokemon/").toString();
        HttpUpstreamAdapter upstream = new HttpUpstreamAdapter(base);
        PokemonRepository repo = new InMemoryPokemonRepository();
        CaffeineCacheAdapter cache = new CaffeineCacheAdapter();
        PokemonService service = new PokemonService(repo, upstream, cache);

        Optional<?> res = service.getPokemon("doesnotexist");
        assertFalse(res.isPresent());
    }
}
