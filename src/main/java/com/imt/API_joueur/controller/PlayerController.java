package com.imt.API_joueur.controller;

import com.imt.API_joueur.model.Player;
import com.imt.API_joueur.repository.PlayerRepository;
import com.imt.API_joueur.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/player")
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerRepository playerRepository;

    public PlayerController(PlayerService playerService, PlayerRepository playerRepository) {
        this.playerService = playerService;
        this.playerRepository = playerRepository;
    }

    @GetMapping("/{username}")
    public ResponseEntity<Player> getPlayer(@PathVariable String username) {
        Optional<Player> playerOpt = playerRepository.findByUsername(username);

        return playerOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{username}/xp")
    public ResponseEntity<Player> addExperience(@PathVariable String username, @RequestParam double amount) {
        try {
            Player updatedPlayer = playerService.addExperience(username, amount);
            return ResponseEntity.ok(updatedPlayer);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{username}/monsters")
    public ResponseEntity<?> addMonster(@PathVariable String username, @RequestBody String monsterId) {
        Optional<Player> playerOpt = playerRepository.findByUsername(username);
        if (playerOpt.isEmpty()) return ResponseEntity.notFound().build();

        Player player = playerOpt.get();

        if (!playerService.canAddMonster(player)) {
            return ResponseEntity.badRequest().body("Inventaire plein ! Impossible d'ajouter le monstre.");
        }

        player.getMonsterIds().add(monsterId);
        playerRepository.save(player);

        return ResponseEntity.ok(player);
    }
}