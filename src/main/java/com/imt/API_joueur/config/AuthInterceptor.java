package com.imt.API_joueur.config;

import com.imt.API_joueur.dto.auth.TokenRequest;
import com.imt.API_joueur.dto.auth.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Value("${gatcha.auth-api.url}")
    private String authApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. Récupérer le token du Header "Authorization"
        String authHeader = request.getHeader("Authorization");

        // Vérification basique
        if (authHeader == null || authHeader.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token manquant dans le header Authorization");
            return false;
        }

        // Nettoyage: Si le front envoie "Bearer mon_token", on garde juste "mon_token"
        // car l'API de ton collègue semble attendre le token brut dans le JSON.
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            // 2. Préparer le Body pour l'API de ton collègue
            // Il attend un @RequestBody TokenHttpRequestDTO
            TokenRequest body = new TokenRequest(token);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<TokenRequest> entity = new HttpEntity<>(body, headers);

            // 3. Appel POST vers son API
            ResponseEntity<TokenResponse> authResponse = restTemplate.postForEntity(
                    authApiUrl,
                    entity,
                    TokenResponse.class
            );

            // 4. Si succès (200 OK)
            if (authResponse.getStatusCode() == HttpStatus.OK && authResponse.getBody() != null) {
                // Le PDF dit: "l'API authentification retourne le username lié au token" [cite: 41]
                // On l'injecte dans la requête pour que ton Controller puisse savoir QUI est connecté s'il veut.
                request.setAttribute("username", authResponse.getBody().user());
                return true; // On laisse passer
            }

        } catch (HttpClientErrorException e) {
            // Si son API renvoie 401 ou 403 (TokenInvalidException)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalide ou expiré");
            return false;
        } catch (Exception e) {
            // Si son API est éteinte ou erreur technique
            System.err.println("Erreur Auth: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossible de joindre le service d'authentification");
            return false;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}