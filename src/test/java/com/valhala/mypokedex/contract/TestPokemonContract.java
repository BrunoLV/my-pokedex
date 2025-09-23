package com.valhala.mypokedex.contract;

import com.valhala.mypokedex.controller.PokemonController;
import io.micronaut.http.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPokemonContract {

    @Test
    void contract_should_return_200_for_existing_identifier() {
        PokemonController controller = new PokemonController();
        HttpResponse<?> resp = controller.get("pikachu");
        // implementation populates minimal DTO; current behavior will return 200 if service created a DTO
        assertEquals(200, resp.getStatus().getCode());
    }
}
