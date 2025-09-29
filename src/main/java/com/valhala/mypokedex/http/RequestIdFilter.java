package com.valhala.mypokedex.http;

import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;

@Singleton
public class RequestIdFilter implements HttpServerFilter {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "reqId";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        String reqId = Optional.ofNullable(request.getHeaders().get(HEADER)).orElse(UUID.randomUUID().toString());
        MDC.put(MDC_KEY, reqId);
        Publisher<MutableHttpResponse<?>> upstream = chain.proceed(request);

        return new Publisher<>() {
            @Override
            public void subscribe(Subscriber<? super MutableHttpResponse<?>> s) {
                upstream.subscribe(new Subscriber<>() {
                    @Override
                    public void onSubscribe(Subscription subscription) {
                        s.onSubscribe(subscription);
                    }

                    @Override
                    public void onNext(MutableHttpResponse<?> mutableHttpResponse) {
                        s.onNext(mutableHttpResponse);
                    }

                    @Override
                    public void onError(Throwable t) {
                        try {
                            s.onError(t);
                        } finally {
                            MDC.remove(MDC_KEY);
                        }
                    }

                    @Override
                    public void onComplete() {
                        try {
                            s.onComplete();
                        } finally {
                            MDC.remove(MDC_KEY);
                        }
                    }
                });
            }
        };
    }
}
