package com.imt.API_joueur.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/players/**") // Protège ton API
                .excludePathPatterns(
                        "/swagger-ui/**",      // Laisse passer Swagger
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/api/players"         // Laisse passer la création de compte (POST /api/players) si tu veux permettre l'inscription sans token
                );
    }
}