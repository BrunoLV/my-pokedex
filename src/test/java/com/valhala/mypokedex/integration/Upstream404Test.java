package com.valhala.mypokedex.integration;

import com.valhala.mypokedex.controller.PokemonController;
import io.micronaut.http.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Upstream404Test {

    @Test
    void unknown_identifier_returns_404() {
        PokemonController controller = new PokemonController();
        // using a likely non-existent identifier to provoke no upstream result
        HttpResponse<?> resp = controller.get("__nonexistent_pokemon__");
    // When upstream doesn't have the pokemon, service should return empty and controller respond 404
    assertEquals(404, resp.getStatus().getCode());
    }
}
