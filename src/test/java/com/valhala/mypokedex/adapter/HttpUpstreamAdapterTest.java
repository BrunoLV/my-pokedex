package com.valhala.mypokedex.adapter;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HttpUpstreamAdapterTest {

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
    void fetch_returns_body_on_200() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"id\":1,\"name\":\"testmon\"}"));
        String base = server.url("/api/").toString();
        HttpUpstreamAdapter adapter = new HttpUpstreamAdapter(base + "pokemon/");
        Optional<String> res = adapter.fetchPokemonRaw("testmon");
        assertTrue(res.isPresent());
        assertTrue(res.get().contains("testmon"));
    }

    @Test
    void fetch_returns_empty_on_404() {
        server.enqueue(new MockResponse().setResponseCode(404));
        String base = server.url("/api/").toString();
        HttpUpstreamAdapter adapter = new HttpUpstreamAdapter(base + "pokemon/");
        Optional<String> res = adapter.fetchPokemonRaw("notfound");
        assertFalse(res.isPresent());
    }
}
