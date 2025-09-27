package com.valhala.mypokedex.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.Map;

@Controller
public class HealthController {

    @Get("/health")
    public HttpResponse<Map<String, String>> health() {
        return HttpResponse.ok(Map.of("status", "UP"));
    }
}
