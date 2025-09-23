package com.valhala.mypokedex.adapter;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Singleton
@Requires(notEnv = "test")
public class HttpUpstreamAdapter implements UpstreamAdapter {

    private final String baseUrl;
    private final HttpClient client;

    public HttpUpstreamAdapter() {
        this("https://pokeapi.co/api/v2/pokemon/");
    }

    public HttpUpstreamAdapter(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public Optional<String> fetchPokemonRaw(String identifier) {
    String url = baseUrl + identifier;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .header("Accept", "application/json")
                .build();

        int maxAttempts = 3;
        long backoff = 250L; // ms

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                int code = resp.statusCode();
                if (code == 200) {
                    return Optional.ofNullable(resp.body());
                } else if (code == 404) {
                    return Optional.empty();
                } else {
                    // retry for server errors
                    if (attempt == maxAttempts) {
                        return Optional.empty();
                    }
                    Thread.sleep(backoff);
                    backoff *= 2;
                }
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                if (attempt == maxAttempts) {
                    return Optional.empty();
                }
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return Optional.empty();
                }
                backoff *= 2;
            }
        }

        return Optional.empty();
    }
}

