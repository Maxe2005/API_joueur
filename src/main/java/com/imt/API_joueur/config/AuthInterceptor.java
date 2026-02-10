package com.imt.API_joueur.config;

import com.imt.API_joueur.dto.auth.TokenRequest;
import com.imt.API_joueur.dto.auth.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Value("${gatcha.auth-api.url}")
    private String authApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isEmpty()) {
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token manquant");
            return false;
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            TokenRequest body = new TokenRequest(token);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<TokenRequest> entity = new HttpEntity<>(body, headers);

            ResponseEntity<TokenResponse> authResponse = restTemplate.postForEntity(
                    authApiUrl,
                    entity,
                    TokenResponse.class
            );

            if (authResponse.getStatusCode() == HttpStatus.OK && authResponse.getBody() != null) {
                request.setAttribute("username", authResponse.getBody().user());
                return true;
            }

        } catch (HttpClientErrorException e) {
            // 401 venant de l'Auth
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token invalide (Refus Auth)");
            return false;

        } catch (HttpServerErrorException e) {
            // 500 venant de l'Auth (Crash à cause du mauvais format)
            // On masque le crash et on dit que le token est invalide
            System.err.println("Auth API Crash (500) -> Traité comme Token Invalide");
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token invalide ou corrompu");
            return false;

        } catch (Exception e) {
            // Auth éteinte
            System.err.println("Auth API Injoignable : " + e.getMessage());
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Service Auth injoignable");
            return false;
        }

        sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Accès refusé");
        return false;
    }

    // Petite méthode utilitaire pour écrire du JSON proprement sans passer par le dispatcher d'erreur Spring
    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
        response.getWriter().flush();
    }
}