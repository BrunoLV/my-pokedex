package com.valhala.mypokedex.application.controller;

import com.valhala.mypokedex.domain.pokemon.dto.PokemonDTO;
import com.valhala.mypokedex.domain.pokemon.usecase.GetPokemonUseCase;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.PathVariable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;

@Controller("/api/pokemon")
public class PokemonController {
    private static final Logger LOG = LoggerFactory.getLogger(PokemonController.class);
    private final GetPokemonUseCase service;

    public PokemonController(GetPokemonUseCase service) {
        this.service = service;
    }

    @Get("/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = "Pokemon")
    @Operation(summary = "Get Pokemon by identifier", description = "Retrieve a Pokemon by its identifier (id or name)")
    @ApiResponse(responseCode = "200", description = "Pokemon found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PokemonDTO.class)))
    @ApiResponse(responseCode = "404", description = "Pokemon not found")
    @ApiResponse(responseCode = "400", description = "Bad request - invalid identifier")
    public HttpResponse<?> get(@PathVariable String identifier) {
        String reqId = UUID.randomUUID().toString();
        MDC.put("reqId", reqId);
        LOG.info("[reqId={}] Received request for pokemon identifier='{}'", reqId, identifier);
        try {
            if (identifier == null || identifier.isBlank()) {
                LOG.warn("[reqId={}] Bad request: empty identifier", reqId);
                return HttpResponse.badRequest();
            }
            try {
                Optional<PokemonDTO> result = service.getPokemon(identifier);
                if (result.isPresent()) {
                    LOG.info("[reqId={}] Returning pokemon '{}' to client", reqId, identifier);
                    return HttpResponse.ok(result.get());
                } else {
                    LOG.info("[reqId={}] Pokemon '{}' not found", reqId, identifier);
                    return HttpResponse.notFound();
                }
            } catch (Exception ex) {
                LOG.error("[reqId={}] Unhandled error while fetching pokemon '{}'", reqId, identifier, ex);
                return HttpResponse.serverError();
            }
        } finally {
            MDC.remove("reqId");
        }
    }
}
