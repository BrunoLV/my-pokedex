package com.valhala.mypokedex.controller;

import com.valhala.mypokedex.dto.PokemonDTO;
import com.valhala.mypokedex.service.PokemonService;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;

import java.util.Optional;

@Controller("/api/pokemon")
public class PokemonController {
    private final PokemonService service;

    public PokemonController() {
        this.service = new PokemonService();
    }

    public PokemonController(PokemonService service) {
        this.service = service;
    }

    @Get("/{identifier}")
    public HttpResponse<?> get(@PathVariable String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return HttpResponse.badRequest();
        }
        Optional<PokemonDTO> result = service.getPokemon(identifier);
        return result.map(HttpResponse::ok).orElseGet(() -> HttpResponse.notFound());
    }
}
