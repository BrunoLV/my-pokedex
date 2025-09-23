package com.valhala.mypokedex.test;

import com.valhala.mypokedex.adapter.HttpUpstreamAdapter;
import com.valhala.mypokedex.adapter.UpstreamAdapter;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import okhttp3.mockwebserver.MockWebServer;

import java.io.IOException;

@Factory
@Requires(env = "test")
public class TestHttpAdapterFactory {

    private final MockWebServer server;

    public TestHttpAdapterFactory() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @Singleton
    public MockWebServer mockWebServer() {
        return server;
    }

    @Singleton
    @Replaces(UpstreamAdapter.class)
    public UpstreamAdapter testHttpUpstreamAdapter(MockWebServer server) {
        return new HttpUpstreamAdapter(server.url("/api/pokemon/").toString());
    }
}
