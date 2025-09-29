package com.valhala.mypokedex.application;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

@MicronautTest(startApplication = false)
class OpenApiGeneratedTest {

    @Test
    void buildGeneratesOpenApi(ResourceLoader resourceLoader) {
        assertTrue(resourceLoader.getResource("META-INF/swagger/my-pokedex-0.0.yml").isPresent());
    }

}
