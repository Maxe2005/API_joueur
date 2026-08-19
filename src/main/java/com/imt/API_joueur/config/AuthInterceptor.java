package com.imt.API_joueur.config;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.imt.commonsecurity.auth.AbstractAuthInterceptor;
import com.imt.commonsecurity.auth.TokenVerificationClient;
import com.imt.commonsecurity.auth.TokenVerificationResult;

/**
 * Vérifie la validité du token auprès de l'API d'Authentification (via gatcha-common-security),
 * et applique la règle d'autorisation "un joueur ne modifie que son propre profil" (sauf ADMIN).
 * Extrait et propage l'identifiant de correlation X-Request-Id.
 */
@Component
public class AuthInterceptor extends AbstractAuthInterceptor {

    private static final String X_REQUEST_ID = "X-Request-Id";
    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String PATH_USERNAME_VARIABLE = "username";

    public AuthInterceptor(
            RestTemplate restTemplate,
            @Value("${gatcha.auth-api.host}") String authApiHost,
            @Value("${gatcha.auth-api.port}") String authApiPort) {
        super(new TokenVerificationClient(restTemplate, authApiHost, authApiPort));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String requestId = request.getHeader(X_REQUEST_ID);
        if (requestId != null) {
            MDC.put("X-Request-Id", requestId);
            logger.debug("X-Request-Id extrait de la requête entrante : {}", requestId);
        }

        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {
        MDC.remove("X-Request-Id");
        super.afterCompletion(request, response, handler, ex);
    }

    @Override
    protected boolean postAuthorize(HttpServletRequest request, HttpServletResponse response,
            TokenVerificationResult.Valid authenticatedUser) {
        // API_invocations rejoue toujours le token du joueur d'origine (jamais un credential de service),
        // donc l'identité vérifiée ici EST celle du joueur concerné par l'appel : on peut sans risque
        // interdire à un joueur de modifier le profil d'un autre (sauf ADMIN).
        if (!HttpMethod.GET.matches(request.getMethod()) && !isSelfOrAdmin(request, authenticatedUser)) {
            logger.warn("Refus d'autorisation : {} a tenté une opération sur le profil d'un autre joueur",
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
            logger.error("Impossible d'écrire la réponse 403", e);
        }
    }
}
