package com.valhala.mypokedex.controller;

import com.valhala.mypokedex.dto.PokemonDTO;
import com.valhala.mypokedex.service.PokemonService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import io.micronaut.test.annotation.MockBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class PokemonControllerTest {

    @Inject
    @Client("/api/pokemon")
    HttpClient client;

    @Inject
    PokemonService service; // will be the mocked bean provided below

    @MockBean(PokemonService.class)
    PokemonService pokemonService() {
        return Mockito.mock(PokemonService.class);
    }

    @Test
    void testGetPokemonReturnsOk() {
        PokemonDTO dto = new PokemonDTO(
                25,
                "pikachu",
                List.of("electric"),
                Map.of("speed", 90),
                Map.of("front_default", "https://.../sprite.png"),
                List.of("static"),
                "https://pokeapi.co/api/v2/pokemon/pikachu"
        );

        Mockito.when(service.getPokemon("pikachu")).thenReturn(Optional.of(dto));

        var request = HttpRequest.GET("/pikachu");
        var response = client.toBlocking().exchange(request, PokemonDTO.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(response.getBody().isPresent());
        assertEquals(dto, response.getBody().get());
    }

    @Test
    void testGetPokemonNotFound() {
        Mockito.when(service.getPokemon("missingmon")).thenReturn(Optional.empty());

        var request = HttpRequest.GET("/missingmon");
        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, PokemonDTO.class);
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void testGetPokemonBadRequest() {
        // service should not be called in this case, but we can leave it as default
        var request = HttpRequest.GET("/%20"); // encoded space -> controller identifier will be " " which is blank
        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, PokemonDTO.class);
        });
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testGetPokemonServerError() {
        Mockito.when(service.getPokemon("boom")).thenThrow(new RuntimeException("uh-oh"));

        var request = HttpRequest.GET("/boom");
        HttpClientResponseException ex = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, PokemonDTO.class);
        });
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
    }
}

