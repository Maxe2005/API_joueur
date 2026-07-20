package com.imt.API_joueur.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            @Value("${gatcha.auth-api.connect-timeout-ms}") long connectTimeoutMs,
            @Value("${gatcha.auth-api.read-timeout-ms}") long readTimeoutMs) {
        // Sans timeout, un API_authentification qui traîne bloque indéfiniment les threads
        // Tomcat de ce service (RestTemplate synchrone) et peut épuiser le pool sous charge.
        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }
}
