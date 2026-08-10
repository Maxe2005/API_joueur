package com.imt.API_joueur.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.imt.commonsecurity.auth.AuthTokenResponse;
import com.imt.commonsecurity.devauth.DevAutoAuthFilter;
import com.imt.commonsecurity.devauth.DevTokenClient;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Preuve bout-en-bout de la Story 1.1 (AC #2 / AC #3) côté API_joueur : compose {@link
 * DevAutoAuthFilter} (dev-only) et {@link AuthInterceptor} (vérification réelle, inchangée) dans
 * l'ordre où le conteneur servlet les exécute réellement — le filtre en amont, l'interceptor en
 * aval — sans jamais faire dire au filtre lui-même s'il faut autoriser la requête.
 */
@ExtendWith(MockitoExtension.class)
class DevAutoAuthFlowIntegrationTest {

    @Mock
    private RestTemplate authRestTemplate;

    @Mock
    private DevTokenClient devTokenClient;

    @Test
    void requestWithoutAuthorizationHeader_devTokenAvailable_reachesProtectedEndpoint() throws Exception {
        when(devTokenClient.fetchToken()).thenReturn(Optional.of("dev-token-1"));
        when(authRestTemplate.postForEntity(any(String.class), any(), eq(AuthTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(new AuthTokenResponse("Sacha", "USER"), HttpStatus.OK));

        DevAutoAuthFilter filter = new DevAutoAuthFilter(devTokenClient);
        AuthInterceptor authInterceptor = new AuthInterceptor(authRestTemplate, "http://api-authentification", "8080");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/players/Sacha");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);
        HttpServletRequest forwardedRequest = (HttpServletRequest) chain.getRequest();

        boolean allowed = authInterceptor.preHandle(forwardedRequest, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(forwardedRequest.getHeader("Authorization")).isEqualTo("Bearer dev-token-1");
    }

    @Test
    void requestWithoutAuthorizationHeader_sidecarUnavailable_stillRejectedNormally() throws Exception {
        when(devTokenClient.fetchToken()).thenReturn(Optional.empty());

        DevAutoAuthFilter filter = new DevAutoAuthFilter(devTokenClient);
        AuthInterceptor authInterceptor = new AuthInterceptor(authRestTemplate, "http://api-authentification", "8080");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/players/Sacha");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);
        HttpServletRequest forwardedRequest = (HttpServletRequest) chain.getRequest();

        boolean allowed = authInterceptor.preHandle(forwardedRequest, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        // Le filtre n'ayant rien pu attacher, AbstractAuthInterceptor rejette avant même d'appeler
        // API_authentification — jamais de token/identité de repli fabriqués (AD-7, fail-closed).
        verifyNoInteractions(authRestTemplate);
    }

    @Test
    void requestWithAuthorizationHeaderAlready_devTokenNeverConsulted() throws Exception {
        when(authRestTemplate.postForEntity(any(String.class), any(), eq(AuthTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(new AuthTokenResponse("Sacha", "USER"), HttpStatus.OK));

        DevAutoAuthFilter filter = new DevAutoAuthFilter(devTokenClient);
        AuthInterceptor authInterceptor = new AuthInterceptor(authRestTemplate, "http://api-authentification", "8080");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/players/Sacha");
        request.addHeader("Authorization", "Bearer real-user-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);
        HttpServletRequest forwardedRequest = (HttpServletRequest) chain.getRequest();

        boolean allowed = authInterceptor.preHandle(forwardedRequest, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(forwardedRequest.getHeader("Authorization")).isEqualTo("Bearer real-user-token");
    }
}
