package com.imt.API_joueur.config;

import com.imt.API_joueur.dto.auth.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private RestTemplate restTemplate;

    private AuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new AuthInterceptor(restTemplate);
        ReflectionTestUtils.setField(interceptor, "authApiHost", "http://auth-api");
        ReflectionTestUtils.setField(interceptor, "authApiPort", "8080");
    }

    private void stubAuthResponse(String user, String role) {
        when(restTemplate.postForEntity(any(String.class), any(), eq(TokenResponse.class)))
                .thenReturn(new ResponseEntity<>(new TokenResponse(user, role), HttpStatus.OK));
    }

    private MockHttpServletRequest requestFor(String method, String pathUsername) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/api/players/" + pathUsername);
        request.addHeader("Authorization", "Bearer valid-token");
        if (pathUsername != null) {
            request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("username", pathUsername));
        }
        return request;
    }

    @Test
    void missingToken_returns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/players/Sacha");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void validTokenOnRead_ownerMismatchAllowed() throws Exception {
        stubAuthResponse("Sacha", "USER");
        MockHttpServletRequest request = requestFor("GET", "AutreJoueur");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result, "La lecture doit rester ouverte à tout joueur authentifié");
        assertEquals("Sacha", request.getAttribute("username"));
        assertEquals("USER", request.getAttribute("role"));
    }

    @Test
    void mutationOnOwnProfile_isAllowed() throws Exception {
        stubAuthResponse("Sacha", "USER");
        MockHttpServletRequest request = requestFor("POST", "Sacha");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void mutationOnOtherProfile_isForbidden() throws Exception {
        stubAuthResponse("Sacha", "USER");
        MockHttpServletRequest request = requestFor("POST", "AutreJoueur");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    void mutationOnOtherProfile_allowedForAdmin() throws Exception {
        stubAuthResponse("AdminUser", "ADMIN");
        MockHttpServletRequest request = requestFor("DELETE", "AutreJoueur");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void authServiceRejectsToken_returns401() throws Exception {
        when(restTemplate.postForEntity(any(String.class), any(), eq(TokenResponse.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));
        MockHttpServletRequest request = requestFor("GET", "Sacha");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
    }

    @Test
    void authServiceCrashes_returns401NotServerError() throws Exception {
        when(restTemplate.postForEntity(any(String.class), any(), eq(TokenResponse.class)))
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Boom", null, null, null));
        MockHttpServletRequest request = requestFor("GET", "Sacha");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
    }

    @Test
    void authServiceUnreachable_returns500() throws Exception {
        when(restTemplate.postForEntity(any(String.class), any(), eq(TokenResponse.class)))
                .thenThrow(new ResourceAccessException("connection refused"));
        MockHttpServletRequest request = requestFor("GET", "Sacha");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(500, response.getStatus());
    }
}
