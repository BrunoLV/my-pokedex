package com.valhala.mypokedex.adapter.output.pokeapi;

import com.valhala.mypokedex.domain.pokemon.ports.PokeApiPort;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Singleton
@Requires(notEnv = "test")
public class PokeApiHttpAdapter implements PokeApiPort {

    private static final Logger LOG = LoggerFactory.getLogger(PokeApiHttpAdapter.class);

    private final String baseUrl;
    private final HttpClient client;

    public PokeApiHttpAdapter() {
        this("https://pokeapi.co/api/v2/pokemon/");
    }

    public PokeApiHttpAdapter(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public Optional<String> fetchPokemonRaw(String identifier) {
        String url = baseUrl + identifier;
        LOG.debug("Preparing request to fetch pokemon '{}' from {}", identifier, url);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .header("Accept", "application/json")
                .build();

        int maxAttempts = 3;
        long backoff = 250L; // ms

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            LOG.debug("Attempt {}/{} to fetch '{}'", attempt, maxAttempts, identifier);
            try {
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                int code = resp.statusCode();
                LOG.debug("Upstream response for '{}' attempt {}: status={}", identifier, attempt, code);
                if (code == 200) {
                    LOG.info("Successfully fetched '{}' from upstream (status=200)", identifier);
                    return Optional.ofNullable(resp.body());
                } else if (code == 404) {
                    LOG.info("Upstream returned 404 for '{}', treating as not found", identifier);
                    return Optional.empty();
                } else {
                    LOG.warn("Unexpected status {} fetching '{}' from upstream (attempt {})", code, identifier, attempt);
                    // retry for server errors if attempts remain
                    if (attempt == maxAttempts) {
                        LOG.error("Giving up fetching '{}' after {} attempts; last status={}", identifier, attempt, code);
                        return Optional.empty();
                    }
                    Thread.sleep(backoff);
                    backoff *= 2;
                }
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                LOG.warn("Error when fetching '{}' from upstream on attempt {}: {}", identifier, attempt, e.getMessage());
                if (attempt == maxAttempts) {
                    LOG.error("Final failure fetching '{}' from upstream after {} attempts", identifier, attempt, e);
                    return Optional.empty();
                }
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    LOG.error("Interrupted while backing off fetching '{}'", identifier, ex);
                    return Optional.empty();
                }
                backoff *= 2;
            }
        }

        LOG.debug("Exhausted attempts to fetch '{}' from upstream, returning empty", identifier);
        return Optional.empty();
    }
}
