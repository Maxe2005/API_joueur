package com.imt.API_joueur.service;

import com.imt.API_joueur.model.Player;
import com.imt.API_joueur.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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

    /**
     * Calcule l'XP nécessaire pour atteindre le niveau suivant.
     * Formule : 50 * (1.1 ^ (niveau - 1))
     */
    public double getXpForNextLevel(int currentLevel) {
        return BASE_XP_THRESHOLD * Math.pow(XP_MULTIPLIER, currentLevel - 1);
    }

    public Player addExperience(String username, double amount) {
        Player player = getPlayerOrThrow(username);

        if (player.getLevel() >= MAX_LEVEL) {
            log.debug("Le joueur {} est déjà au niveau max.", username);
            return player;
        }

        player.setExperience(player.getExperience() + amount);
        checkLevelUp(player);

        return playerRepository.save(player);
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
        Player player = getPlayerOrThrow(username);

        int maxSlots = BASE_MONSTER_SLOTS + player.getLevel();
        if (player.getMonsterIds().size() >= maxSlots) {
            log.warn("Inventaire plein pour {}", username);
            throw new RuntimeException("Inventaire plein !");
        }

        player.getMonsterIds().add(monsterId);
        log.info("Monstre {} ajouté à l'inventaire de {}", monsterId, username);
        return playerRepository.save(player);
    }

    public Player removeMonster(String username, String monsterId) {
        Player player = getPlayerOrThrow(username);

        if (!player.getMonsterIds().contains(monsterId)) {
            throw new RuntimeException("Le joueur ne possède pas ce monstre");
        }

        player.getMonsterIds().remove(monsterId);
        log.info("Monstre {} retiré de l'inventaire de {}", monsterId, username);
        return playerRepository.save(player);
    }

    public Player createPlayer(String username) {
        if (playerRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Ce pseudo est déjà pris !");
        }

        try {
            log.info("Création du nouveau joueur : {}", username);
            return playerRepository.save(new Player(username));
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("Ce pseudo est déjà pris ! (Doublon détecté par la DB)");
        }
    }

    private Player getPlayerOrThrow(String username) {
        return playerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Joueur introuvable : " + username));
    }
}