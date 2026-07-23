package com.imt.API_joueur.config;

import com.imt.commonsecurity.auth.AbstractAuthInterceptor;
import com.imt.commonsecurity.auth.TokenVerificationClient;
import com.imt.commonsecurity.auth.TokenVerificationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Vérifie la validité du token auprès de l'API d'Authentification (via gatcha-common-security),
 * et applique la règle d'autorisation "un joueur ne modifie que son propre profil" (sauf ADMIN).
 */
@Component
@Slf4j
public class AuthInterceptor extends AbstractAuthInterceptor {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String PATH_USERNAME_VARIABLE = "username";

    public AuthInterceptor(
            RestTemplate restTemplate,
            @Value("${gatcha.auth-api.host}") String authApiHost,
            @Value("${gatcha.auth-api.port}") String authApiPort) {
        super(new TokenVerificationClient(restTemplate, authApiHost, authApiPort));
    }

    @Override
    protected boolean postAuthorize(HttpServletRequest request, HttpServletResponse response,
            TokenVerificationResult.Valid authenticatedUser) {
        // API_invocations rejoue toujours le token du joueur d'origine (jamais un credential de service),
        // donc l'identité vérifiée ici EST celle du joueur concerné par l'appel : on peut sans risque
        // interdire à un joueur de modifier le profil d'un autre (sauf ADMIN).
        if (!HttpMethod.GET.matches(request.getMethod()) && !isSelfOrAdmin(request, authenticatedUser)) {
            log.warn("Refus d'autorisation : {} a tenté une opération sur le profil d'un autre joueur",
                    authenticatedUser.user());
            sendForbidden(response, "Vous ne pouvez pas modifier le profil d'un autre joueur");
            return false;
        }
        return true;
    }

    private boolean isSelfOrAdmin(HttpServletRequest request, TokenVerificationResult.Valid authenticatedUser) {
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

    private void sendForbidden(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"" + message + "\"}");
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("Impossible d'écrire la réponse 403", e);
        }
    }
}
