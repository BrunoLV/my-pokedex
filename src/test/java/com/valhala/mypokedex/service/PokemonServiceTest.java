package com.valhala.mypokedex.service;

import com.valhala.mypokedex.adapter.pokeapi.PokeApiAdapter;
import com.valhala.mypokedex.cache.CaffeineCacheAdapter;
import com.valhala.mypokedex.dto.PokemonDTO;
import com.valhala.mypokedex.repository.PokemonEntity;
import com.valhala.mypokedex.repository.PokemonRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MicronautTest
class PokemonServiceTest {

    @Inject
    PokemonService service;

    @Inject
    PokeApiAdapter upstream;

    @Inject
    PokemonRepository repository;

    @Inject
    CaffeineCacheAdapter cache;

    @MockBean(PokeApiAdapter.class)
    PokeApiAdapter pokeApiAdapter() {
        return Mockito.mock(PokeApiAdapter.class);
    }

    @MockBean(PokemonRepository.class)
    PokemonRepository pokemonRepository() {
        return Mockito.mock(PokemonRepository.class);
    }

    @MockBean(CaffeineCacheAdapter.class)
    CaffeineCacheAdapter caffeineCacheAdapter() {
        return Mockito.mock(CaffeineCacheAdapter.class);
    }

    @Test
    void upstreamSuccess_parsesAndSavesAndCaches() {
        String id = "charmander";

        // cache miss
        when(cache.get(id)).thenReturn(Optional.empty());
        // repository miss
        when(repository.findByName(id)).thenReturn(Optional.empty());

        String payload = "{\n" +
                "  \"id\": 4,\n" +
                "  \"name\": \"charmander\",\n" +
                "  \"types\": [{\"type\": {\"name\": \"fire\"}}],\n" +
                "  \"stats\": [{\"base_stat\": 65, \"stat\": {\"name\": \"speed\"}}],\n" +
                "  \"sprites\": {\"front_default\": \"https://img.pk/sprite.png\"},\n" +
                "  \"abilities\": [{\"ability\": {\"name\": \"blaze\"}}]\n" +
                "}";

        when(upstream.fetchPokemonRaw(id)).thenReturn(Optional.of(payload));

        Optional<PokemonDTO> result = service.getPokemon(id);

        assertTrue(result.isPresent());
        PokemonDTO dto = result.get();
        assertEquals(4, dto.id());
        assertEquals("charmander", dto.name());
        assertEquals(List.of("fire"), dto.types());
        assertEquals(Map.of("speed", 65), dto.base_stats());
        assertEquals(Map.of("front_default", "https://img.pk/sprite.png"), dto.sprites());
        assertEquals(List.of("blaze"), dto.abilities());
        assertTrue(dto.source_url().contains("pokeapi"));

        // verify repository.save called
        verify(repository, times(1)).save(any(PokemonEntity.class));
        // verify cache.put called with lowercased key
        verify(cache, times(1)).put(eq(id), any(PokemonDTO.class));
    }

    @Test
    void upstreamNoData_returnsEmpty() {
        String id = "missingmon";
        when(cache.get(id)).thenReturn(Optional.empty());
        when(repository.findByName(id)).thenReturn(Optional.empty());
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
        when(repository.findByName(id)).thenReturn(Optional.empty());
        when(upstream.fetchPokemonRaw(id)).thenReturn(Optional.of("not-a-json"));

        Optional<PokemonDTO> result = service.getPokemon(id);
        assertTrue(result.isPresent());
        PokemonDTO dto = result.get();
        assertEquals(0, dto.id());
        assertEquals(id, dto.name());
        assertNotNull(dto.base_stats());
        assertNotNull(dto.sprites());
        assertNotNull(dto.types());
        assertTrue(dto.source_url().contains(id));

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

        verify(repository, never()).findByName(anyString());
        verify(upstream, never()).fetchPokemonRaw(anyString());
    }
}

