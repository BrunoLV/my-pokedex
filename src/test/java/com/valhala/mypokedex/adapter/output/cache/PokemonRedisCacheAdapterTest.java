package com.valhala.mypokedex.adapter.output.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valhala.mypokedex.domain.pokemon.dto.PokemonDTO;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PokemonRedisCacheAdapterTest {

    @Test
    void putAndGetRoundtripUsesRedisCommands() throws Exception {
        // mocks
        @SuppressWarnings("unchecked")
        StatefulRedisConnection<String, String> conn = (StatefulRedisConnection<String, String>) mock(
                StatefulRedisConnection.class);
        @SuppressWarnings("unchecked")
        RedisCommands<String, String> commands = (RedisCommands<String, String>) mock(RedisCommands.class);
        when(conn.sync()).thenReturn(commands);

        // create adapter instance (will try to connect but we'll overwrite)
        PokemonRedisCacheAdapter adapter = new PokemonRedisCacheAdapter();

        // inject mocked connection into adapter via reflection
        Field connField = PokemonRedisCacheAdapter.class.getDeclaredField("connection");
        connField.setAccessible(true);
        connField.set(adapter, conn);

        // prepare DTO
        PokemonDTO dto = new PokemonDTO(
                25,
                "pikachu",
                java.util.List.of(),
                java.util.Map.of(),
                java.util.Map.of(),
                java.util.List.of(),
                null);

        // capture serialization performed by adapter using real ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dto);

        // stub get to return json
        when(commands.get("p-25")).thenReturn(json);

        // call put (should call setex) and get
        adapter.put("p-25", dto);
        // verify setex was called (ttl value unknown, but at least called)
        verify(commands, atLeastOnce()).setex(eq("p-25"), anyLong(), anyString());

        Optional<PokemonDTO> fetched = adapter.get("p-25");
        assertTrue(fetched.isPresent());
        assertEquals(dto.identifier(), fetched.get().identifier());
    }

    @Test
    void getReturnsEmptyOnNullKey() throws Exception {
        PokemonRedisCacheAdapter adapter = new PokemonRedisCacheAdapter();
        Optional<PokemonDTO> result = adapter.get(null);
        assertFalse(result.isPresent());
    }
}
