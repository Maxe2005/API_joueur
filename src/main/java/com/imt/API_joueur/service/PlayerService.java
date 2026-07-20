package com.imt.API_joueur.service;

import com.imt.API_joueur.exception.DuplicateUsernameException;
import com.imt.API_joueur.exception.InventoryFullException;
import com.imt.API_joueur.exception.MonsterNotOwnedException;
import com.imt.API_joueur.exception.PlayerNotFoundException;
import com.imt.API_joueur.model.Player;
import com.imt.API_joueur.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final PlayerRepository playerRepository;

    private static final int MAX_LEVEL = 50;
    private static final double XP_MULTIPLIER = 1.1;
    private static final int BASE_XP_THRESHOLD = 50;
    private static final int BASE_MONSTER_SLOTS = 10;
    // Nombre de nouvelles tentatives en cas de conflit de version (deux mutations concurrentes
    // sur le même joueur, ex. deux appels d'API_invocations qui se chevauchent).
    private static final int MAX_SAVE_ATTEMPTS = 3;

    /**
     * Calcule l'XP nécessaire pour atteindre le niveau suivant.
     * Formule : 50 * (1.1 ^ (niveau - 1))
     */
    public double getXpForNextLevel(int currentLevel) {
        return BASE_XP_THRESHOLD * Math.pow(XP_MULTIPLIER, currentLevel - 1);
    }

    public Player getPlayer(String username) {
        return getPlayerOrThrow(username);
    }

    public Player addExperience(String username, double amount) {
        for (int attempt = 1; ; attempt++) {
            Player player = getPlayerOrThrow(username);

            if (player.getLevel() >= MAX_LEVEL) {
                log.debug("Le joueur {} est déjà au niveau max.", username);
                return player;
            }

            player.setExperience(player.getExperience() + amount);
            checkLevelUp(player);

            try {
                return playerRepository.save(player);
            } catch (OptimisticLockingFailureException e) {
                retryOrThrow(username, attempt, e);
            }
        }
    }

    private void checkLevelUp(Player player) {
        while (player.getLevel() < MAX_LEVEL) {
            double threshold = getXpForNextLevel(player.getLevel());
            if (player.getExperience() >= threshold) {
                player.setLevel(player.getLevel() + 1);
                player.setExperience(player.getExperience() - threshold);
                log.info("Le joueur {} est passé au niveau {} !", player.getUsername(), player.getLevel());
            } else {
                break;
            }
        }
    }

    public Player addMonster(String username, String monsterId) {
        for (int attempt = 1; ; attempt++) {
            Player player = getPlayerOrThrow(username);

            int maxSlots = BASE_MONSTER_SLOTS + (player.getLevel() - 1);
            if (player.getMonsterIds().size() >= maxSlots) {
                log.warn("Inventaire plein pour {}", username);
                throw new InventoryFullException(username);
            }

            player.getMonsterIds().add(monsterId);

            try {
                Player saved = playerRepository.save(player);
                log.info("Monstre {} ajouté à l'inventaire de {}", monsterId, username);
                return saved;
            } catch (OptimisticLockingFailureException e) {
                retryOrThrow(username, attempt, e);
            }
        }
    }

    public Player removeMonster(String username, String monsterId) {
        for (int attempt = 1; ; attempt++) {
            Player player = getPlayerOrThrow(username);

            if (!player.getMonsterIds().contains(monsterId)) {
                throw new MonsterNotOwnedException(username, monsterId);
            }

            player.getMonsterIds().remove(monsterId);

            try {
                Player saved = playerRepository.save(player);
                log.info("Monstre {} retiré de l'inventaire de {}", monsterId, username);
                return saved;
            } catch (OptimisticLockingFailureException e) {
                retryOrThrow(username, attempt, e);
            }
        }
    }

    /**
     * Relance la mutation depuis un état à jour après un conflit de version, jusqu'à
     * MAX_SAVE_ATTEMPTS fois, puis laisse remonter l'exception (409 côté GlobalExceptionHandler).
     */
    private void retryOrThrow(String username, int attempt, OptimisticLockingFailureException e) {
        if (attempt >= MAX_SAVE_ATTEMPTS) {
            throw e;
        }
        log.debug("Conflit de version pour {} (tentative {}/{}), nouvelle tentative", username, attempt, MAX_SAVE_ATTEMPTS);
    }

    public Player createPlayer(String username) {
        if (playerRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUsernameException(username);
        }

        try {
            log.info("Création du nouveau joueur : {}", username);
            return playerRepository.save(new Player(username));
        } catch (DuplicateKeyException e) {
            throw new DuplicateUsernameException(username);
        }
    }

    private Player getPlayerOrThrow(String username) {
        return playerRepository.findByUsername(username)
                .orElseThrow(() -> new PlayerNotFoundException(username));
    }
}