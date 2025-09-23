package com.valhala.mypokedex.integration;

import com.valhala.mypokedex.adapter.HttpUpstreamAdapter;
import com.valhala.mypokedex.cache.CaffeineCacheAdapter;
import com.valhala.mypokedex.repository.InMemoryPokemonRepository;
import com.valhala.mypokedex.repository.PokemonEntity;
import com.valhala.mypokedex.repository.PokemonRepository;
import com.valhala.mypokedex.service.PokemonService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceIntegrationWithMockServerTest {

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
    void service_parses_and_persists_upstream() {
        String body = "{" +
                "\"id\":25,\"name\":\"pikachu\"," +
                "\"types\":[{" +
                "\"slot\":1,\"type\":{\"name\":\"electric\"}}]," +
                "\"stats\":[{" +
                "\"base_stat\":35,\"stat\":{\"name\":\"hp\"}}]," +
                "\"sprites\":{\"front_default\":\"https://img\"}," +
                "\"abilities\":[{\"ability\":{\"name\":\"static\"}}]" +
                "}";

        server.enqueue(new MockResponse().setResponseCode(200).setBody(body));

        String base = server.url("/api/pokemon/").toString();
        HttpUpstreamAdapter upstream = new HttpUpstreamAdapter(base);
        PokemonRepository repo = new InMemoryPokemonRepository();
        CaffeineCacheAdapter cache = new CaffeineCacheAdapter();
        PokemonService service = new PokemonService(repo, upstream, cache);

        Optional<?> dtoOpt = service.getPokemon("pikachu");
        assertTrue(dtoOpt.isPresent());

        // verify repository persisted raw payload
        Optional<PokemonEntity> ent = repo.findByName("pikachu");
        assertTrue(ent.isPresent());
        assertNotNull(ent.get().payload);

        // second call should be cache hit (no additional request)
        int before = server.getRequestCount();
        Optional<?> dto2 = service.getPokemon("pikachu");
        assertTrue(dto2.isPresent());
        int after = server.getRequestCount();
        assertEquals(before, after);
    }
}
