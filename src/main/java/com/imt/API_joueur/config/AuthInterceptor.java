package com.imt.API_joueur.config;

import com.imt.API_joueur.dto.auth.TokenRequest;
import com.imt.API_joueur.dto.auth.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Intercepteur chargé de vérifier la validité du token JWT auprès de l'API d'Authentification.
 */
@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Value("${gatcha.auth-api.url}")
    private String authApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader == null || authHeader.isEmpty()) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token manquant");
            return false;
        }

        String token = authHeader.startsWith(BEARER_PREFIX) ? authHeader.substring(7) : authHeader;

        try {
            return validateToken(request, token);
        } catch (HttpClientErrorException e) {
            log.warn("Refus Auth (401/403) : {}", e.getMessage());
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token invalide ou expiré");
            return false;
        } catch (HttpServerErrorException e) {
            log.error("Crash API Auth (500) lors de la vérification du token : {}", e.getMessage());
            // Fail-safe : on considère le token invalide plutôt que de bloquer en 500
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token invalide (Erreur serveur distant)");
            return false;
        } catch (Exception e) {
            log.error("Service Auth injoignable : {}", e.getMessage());
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Service d'authentification indisponible");
            return false;
        }
    }

    private boolean validateToken(HttpServletRequest request, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TokenRequest> entity = new HttpEntity<>(new TokenRequest(token), headers);

        ResponseEntity<TokenResponse> authResponse = restTemplate.postForEntity(
                authApiUrl,
                entity,
                TokenResponse.class
        );

        if (authResponse.getStatusCode() == HttpStatus.OK && authResponse.getBody() != null) {
            request.setAttribute("username", authResponse.getBody().user());
            return true;
        }
        return false;
    }

    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
        response.getWriter().flush();
    }
}