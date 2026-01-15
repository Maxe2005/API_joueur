package com.imt.API_joueur.service;

import com.imt.API_joueur.model.Player;
import com.imt.API_joueur.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        if (currentLevel == 0) return BASE_XP_THRESHOLD;

        return BASE_XP_THRESHOLD * Math.pow(XP_MULTIPLIER, currentLevel - 1);
    }

    public Player addExperience(String username, double amount) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Ce joueur n'existe pas : " + username));

        if (player.getLevel() >= MAX_LEVEL) {
            return player;
        }

        player.setExperience(player.getExperience() + amount);

        boolean hasLeveledUp = false;
        while (player.getLevel() < MAX_LEVEL) {
            double threshold = getXpForNextLevel(player.getLevel());

            if (player.getExperience() >= threshold) {
                player.setLevel(player.getLevel() + 1);
                player.setExperience(player.getExperience() - threshold);

                hasLeveledUp = true;
            } else {
                break;
            }
        }

        return playerRepository.save(player);
    }

    public boolean canAddMonster(Player player) {
        int maxSlots = BASE_MONSTER_SLOTS + player.getLevel();
        return player.getMonsterIds().size() < maxSlots;
    }
}