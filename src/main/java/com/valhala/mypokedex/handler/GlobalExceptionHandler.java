package com.valhala.mypokedex.handler;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Singleton
public class GlobalExceptionHandler implements ExceptionHandler<Throwable, HttpResponse<?>> {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public HttpResponse<?> handle(HttpRequest request, Throwable exception) {
        String reqId = MDC.get("reqId");
        if (reqId != null) {
            LOG.error("[reqId={}] Unhandled exception for request {} {}", reqId, request.getMethod(), request.getUri(), exception);
        } else {
            LOG.error("Unhandled exception for request {} {}", request.getMethod(), request.getUri(), exception);
        }
        return HttpResponse.serverError();
    }
}

