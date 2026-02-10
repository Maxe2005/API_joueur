package com.imt.API_joueur.service;

import com.imt.API_joueur.model.Player;
import com.imt.API_joueur.repository.PlayerRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional; // ON NE L'UTILISE PAS

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    private static final int MAX_LEVEL = 50;
    private static final double XP_MULTIPLIER = 1.1;
    private static final int BASE_XP_THRESHOLD = 50;
    private static final int BASE_MONSTER_SLOTS = 10;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public double getXpForNextLevel(int currentLevel) {
        // Niveau 1 -> 50 xp. Niveau 2 -> 50 * 1.1 ...
        return BASE_XP_THRESHOLD * Math.pow(XP_MULTIPLIER, currentLevel - 1);
    }

    // Pas de @Transactional ici (cause crash Mongo Standalone)
    public Player addExperience(String username, double amount) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Joueur introuvable : " + username));

        if (player.getLevel() >= MAX_LEVEL) return player;

        player.setExperience(player.getExperience() + amount);

        while (player.getLevel() < MAX_LEVEL) {
            double threshold = getXpForNextLevel(player.getLevel());
            if (player.getExperience() >= threshold) {
                player.setLevel(player.getLevel() + 1);
                player.setExperience(player.getExperience() - threshold);
            } else {
                break;
            }
        }
        return playerRepository.save(player);
    }

    public Player addMonster(String username, String monsterId) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Joueur introuvable : " + username));

        int maxSlots = BASE_MONSTER_SLOTS + player.getLevel();
        if (player.getMonsterIds().size() >= maxSlots) {
            throw new RuntimeException("Inventaire plein !");
        }

        player.getMonsterIds().add(monsterId);
        return playerRepository.save(player);
    }

    public Player removeMonster(String username, String monsterId) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Joueur introuvable : " + username));

        if (!player.getMonsterIds().contains(monsterId)) {
            throw new RuntimeException("Le joueur ne possède pas ce monstre");
        }

        player.getMonsterIds().remove(monsterId);
        return playerRepository.save(player);
    }

    public Player createPlayer(String username) {
        // Double sécurité : check manuel + check DB
        if (playerRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Ce pseudo est déjà pris !");
        }

        try {
            Player newPlayer = new Player(username);
            return playerRepository.save(newPlayer);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("Ce pseudo est déjà pris ! (Doublon détecté par la DB)");
        }
    }
}