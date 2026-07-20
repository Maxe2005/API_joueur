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
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.Map;

/**
 * Intercepteur chargé de vérifier la validité du token JWT auprès de l'API d'Authentification,
 * et d'appliquer la règle d'autorisation "un joueur ne modifie que son propre profil" (sauf ADMIN).
 */
@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Value("${gatcha.auth-api.host}")
    private String authApiHost;

    @Value("${gatcha.auth-api.port}")
    private String authApiPort;

    private final RestTemplate restTemplate;

    public AuthInterceptor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_ENDPOINT = "/user/verify-token";
    private static final String USERNAME_ATTRIBUTE = "username";
    private static final String ROLE_ATTRIBUTE = "role";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String PATH_USERNAME_VARIABLE = "username";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader == null || authHeader.isEmpty()) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token manquant");
            return false;
        }

        String token = authHeader.startsWith(BEARER_PREFIX) ? authHeader.substring(BEARER_PREFIX.length()) : authHeader;

        TokenResponse authenticatedUser;
        try {
            authenticatedUser = validateToken(token);
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

        if (authenticatedUser == null) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token invalide ou expiré");
            return false;
        }

        request.setAttribute(USERNAME_ATTRIBUTE, authenticatedUser.user());
        request.setAttribute(ROLE_ATTRIBUTE, authenticatedUser.role());

        // API_invocations rejoue toujours le token du joueur d'origine (jamais un credential de service),
        // donc l'identité vérifiée ici EST celle du joueur concerné par l'appel : on peut sans risque
        // interdire à un joueur de modifier le profil d'un autre (sauf ADMIN).
        if (!HttpMethod.GET.matches(request.getMethod()) && !isSelfOrAdmin(request, authenticatedUser)) {
            log.warn("Refus d'autorisation : {} a tenté une opération sur le profil d'un autre joueur", authenticatedUser.user());
            sendJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Vous ne pouvez pas modifier le profil d'un autre joueur");
            return false;
        }

        return true;
    }

    private boolean isSelfOrAdmin(HttpServletRequest request, TokenResponse authenticatedUser) {
        if (ADMIN_ROLE.equalsIgnoreCase(authenticatedUser.role())) {
            return true;
        }
        String pathUsername = extractPathUsername(request);
        return pathUsername == null || pathUsername.equals(authenticatedUser.user());
    }

    @SuppressWarnings("unchecked")
    private String extractPathUsername(HttpServletRequest request) {
        Object pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(pathVariables instanceof Map)) {
            return null;
        }
        return ((Map<String, String>) pathVariables).get(PATH_USERNAME_VARIABLE);
    }

    private TokenResponse validateToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TokenRequest> entity = new HttpEntity<>(new TokenRequest(token), headers);

        String authApiUrl = (authApiHost + ":" +  authApiPort + AUTH_ENDPOINT);

        ResponseEntity<TokenResponse> authResponse = restTemplate.postForEntity(
                authApiUrl,
                entity,
                TokenResponse.class
        );

        return authResponse.getStatusCode() == HttpStatus.OK ? authResponse.getBody() : null;
    }

    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
        response.getWriter().flush();
    }
}