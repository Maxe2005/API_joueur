package com.imt.API_joueur.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.imt.API_joueur.repository.PlayerRepository;
import com.imt.commonsecurity.devauth.DevAutoAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Preuve bout-en-bout (Story 1.1, finding de revue) que le câblage {@code
 * @Import(DevAutoAuthConfig.class)} + {@code @Profile("dev-local")} enregistre réellement {@link
 * DevAutoAuthFilter} dans un vrai {@link ApplicationContext} Spring quand le profil est actif —
 * exactement le "piège de câblage" que la story identifiait elle-même et que seule une
 * vérification manuelle (curl) avait validé jusqu'ici (voir Dev Agent Record / Debug Log
 * References : deux régressions de câblage Spring ont été introduites puis corrigées en cours de
 * développement sans qu'aucun test automatisé ne les détecte).
 */
@SpringBootTest(properties = "spring.data.mongodb.auto-index-creation=false")
@ActiveProfiles("dev-local")
class DevAutoAuthWiringActiveProfileTest {

    @MockBean
    private PlayerRepository playerRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void devAutoAuthFilterIsRegistered_whenDevLocalProfileActive() {
        FilterRegistrationBean<?> registration =
                applicationContext.getBean("devAutoAuthFilter", FilterRegistrationBean.class);

        assertThat(registration.getFilter()).isInstanceOf(DevAutoAuthFilter.class);
    }
}
