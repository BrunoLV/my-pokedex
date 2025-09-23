package com.valhala.mypokedex.test;

import com.valhala.mypokedex.cache.CaffeineCacheAdapter;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

@Factory
@Requires(env = "test")
public class TestCacheFactory {

    @Singleton
    @Replaces(CaffeineCacheAdapter.class)
    public CaffeineCacheAdapter testCaffeineCacheAdapter() {
        // reuse the same implementation for tests
        return new CaffeineCacheAdapter();
    }
}
