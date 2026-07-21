package com.imt.API_joueur.config;

import com.imt.API_joueur.repository.PlayerRepository;
import com.imt.API_joueur.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Crée au démarrage les profils joueur correspondant aux comptes par défaut
 * seedés côté API_authentification (mêmes usernames), pour que ces comptes
 * disposent d'un profil joueur utilisable dès leur première connexion.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultUsersSeeder implements CommandLineRunner {

    private final PlayerService playerService;
    private final PlayerRepository playerRepository;
    private final DefaultUsersProperties properties;

    @Override
    public void run(String... args) {
        seed(properties.getAdmin().getUsername(), "admin");
        seed(properties.getUser().getUsername(), "user");
    }

    private void seed(String username, String label) {
        if (username == null || username.isBlank()) {
            log.warn("Skipping default {} player seed: username not configured", label);
            return;
        }
        if (playerRepository.findByUsername(username).isPresent()) {
            log.info("Default {} player '{}' already exists, skipping", label, username);
            return;
        }
        playerService.createPlayer(username);
        log.info("Seeded default {} player '{}'", label, username);
    }
}
