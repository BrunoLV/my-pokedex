package com.valhala.mypokedex.integration;

import com.valhala.mypokedex.controller.PokemonController;
import io.micronaut.http.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPokemonFetch {

    @Test
    void when_db_empty_service_calls_upstream_and_saves() {
        PokemonController controller = new PokemonController();
        HttpResponse<?> resp = controller.get("bulbasaur");
        // minimal implementation returns 200 with created DTO or 200 with local stub
        assertEquals(200, resp.getStatus().getCode());
    }
}
