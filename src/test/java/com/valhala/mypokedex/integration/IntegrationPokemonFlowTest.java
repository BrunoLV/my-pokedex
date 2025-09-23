package com.valhala.mypokedex.integration;

import com.valhala.mypokedex.controller.PokemonController;
import io.micronaut.http.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationPokemonFlowTest {

    @Test
    void cache_and_repository_flow() {
        PokemonController controller = new PokemonController();

        // first call should create scaffold DTO and return 200
        HttpResponse<?> resp1 = controller.get("bulbasaur");
        assertEquals(200, resp1.getStatus().getCode());
        Object body = resp1.getBody().orElse(null);
        assertNotNull(body);

        // second call should hit cache and return 200 quickly
        HttpResponse<?> resp2 = controller.get("bulbasaur");
        assertEquals(200, resp2.getStatus().getCode());
    }
}
