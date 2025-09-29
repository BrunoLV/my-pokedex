package com.valhala.mypokedex.domain.pokemon.usecase;

import com.valhala.mypokedex.adapter.output.cache.PokemonCaffeineCacheAdapter;
import com.valhala.mypokedex.domain.pokemon.dto.PokemonDTO;
import com.valhala.mypokedex.domain.pokemon.ports.PokeApiPort;
import com.valhala.mypokedex.domain.pokemon.repository.PokemonEntity;
import com.valhala.mypokedex.domain.pokemon.repository.PokemonRepository;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MicronautTest
class GetPokemonUseTest {

    @Inject
    GetPokemonUseCase service;

    @Inject
    PokeApiPort upstream;

    @Inject
    PokemonRepository repository;

    @Inject
    PokemonCaffeineCacheAdapter cache;

    @MockBean(PokeApiPort.class)
    PokeApiPort pokeApiAdapter() {
        return Mockito.mock(PokeApiPort.class);
    }

    @MockBean(PokemonRepository.class)
    PokemonRepository pokemonRepository() {
        return Mockito.mock(PokemonRepository.class);
    }

    @MockBean(PokemonCaffeineCacheAdapter.class)
    PokemonCaffeineCacheAdapter caffeineCacheAdapter() {
        return Mockito.mock(PokemonCaffeineCacheAdapter.class);
    }

    @Test
    void upstreamSuccess_parsesAndSavesAndCaches() {
        String id = "charmander";

        // cache miss
        when(cache.get(id)).thenReturn(Optional.empty());
        // repository miss
        when(repository.findByIdentifier(id)).thenReturn(Optional.empty());

        String payload = "{\n" +
                "  \"id\": 4,\n" +
                "  \"identifier\": \"charmander\",\n" +
                "  \"types\": [{\"type\": {\"identifier\": \"fire\"}}],\n" +
                "  \"stats\": [{\"base_stat\": 65, \"stat\": {\"identifier\": \"speed\"}}],\n" +
                "  \"sprites\": {\"front_default\": \"https://img.pk/sprite.png\"},\n" +
                "  \"abilities\": [{\"ability\": {\"identifier\": \"blaze\"}}]\n" +
                "}";

        when(upstream.fetchPokemonRaw(id)).thenReturn(Optional.of(payload));

        Optional<PokemonDTO> result = service.getPokemon(id);

        assertTrue(result.isPresent());
        PokemonDTO dto = result.get();
        assertEquals(4, dto.id());
        assertEquals("charmander", dto.identifier());
        assertEquals(List.of("fire"), dto.types());
        assertEquals(Map.of("speed", 65), dto.baseStats());
        assertEquals(Map.of("front_default", "https://img.pk/sprite.png"), dto.sprites());
        assertEquals(List.of("blaze"), dto.abilities());
        assertTrue(dto.sourceUrl().contains("pokeapi"));

        // verify repository.save called
        verify(repository, times(1)).save(any(PokemonEntity.class));
        // verify cache.put called with lowercased key
        verify(cache, times(1)).put(eq(id), any(PokemonDTO.class));
    }

    @Test
    void upstreamNoData_returnsEmpty() {
        String id = "missingmon";
        when(cache.get(id)).thenReturn(Optional.empty());
        when(repository.findByIdentifier(id)).thenReturn(Optional.empty());
        when(upstream.fetchPokemonRaw(id)).thenReturn(Optional.empty());

        Optional<PokemonDTO> result = service.getPokemon(id);
        assertTrue(result.isEmpty());

        verify(repository, never()).save(any());
        verify(cache, never()).put(anyString(), any());
    }

    @Test
    void upstreamMalformedPayload_fallsbackToMinimalDtoAndCaches() {
        String id = "weirdmon";
        when(cache.get(id)).thenReturn(Optional.empty());
        when(repository.findByIdentifier(id)).thenReturn(Optional.empty());
        when(upstream.fetchPokemonRaw(id)).thenReturn(Optional.of("not-a-json"));

        Optional<PokemonDTO> result = service.getPokemon(id);
        assertTrue(result.isPresent());
        PokemonDTO dto = result.get();
        assertEquals(0, dto.id());
        assertEquals(id, dto.identifier());
        assertNotNull(dto.baseStats());
        assertNotNull(dto.sprites());
        assertNotNull(dto.types());
        assertTrue(dto.sourceUrl().contains(id));

        verify(repository, times(1)).save(any(PokemonEntity.class));
        verify(cache, times(1)).put(eq(id), any(PokemonDTO.class));
    }

    @Test
    void cacheHit_returnsCachedValueAndSkipsRepositoryAndUpstream() {
        String id = "bulbasaur";
        PokemonDTO cached = new PokemonDTO(1, "bulbasaur", List.of(), Map.of(), Map.of(), List.of(), "local");
        when(cache.get(id)).thenReturn(Optional.of(cached));

        Optional<PokemonDTO> result = service.getPokemon(id);
        assertTrue(result.isPresent());
        assertEquals(cached, result.get());

        verify(repository, never()).findByIdentifier(anyString());
        verify(upstream, never()).fetchPokemonRaw(anyString());
    }
}
