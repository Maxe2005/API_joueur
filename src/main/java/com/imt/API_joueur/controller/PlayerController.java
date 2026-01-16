package com.imt.API_joueur.controller;

import com.imt.API_joueur.model.Player;
import com.imt.API_joueur.repository.PlayerRepository;
import com.imt.API_joueur.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerRepository playerRepository;

    public PlayerController(PlayerService playerService, PlayerRepository playerRepository) {
        this.playerService = playerService;
        this.playerRepository = playerRepository;
    }

    @GetMapping("/{username}")
    public ResponseEntity<Player> getPlayer(@PathVariable String username) {
        return playerRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{username}/xp")
    public ResponseEntity<?> addExperience(@PathVariable String username, @RequestBody Map<String, Double> payload) {
        try {
            Double amount = payload.get("amount");
            if (amount == null) return ResponseEntity.badRequest().body("Montant 'amount' requis");

            Player updatedPlayer = playerService.addExperience(username, amount);
            return ResponseEntity.ok(updatedPlayer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{username}/monsters")
    public ResponseEntity<?> addMonster(@PathVariable String username, @RequestBody Map<String, String> payload) {
        try {
            String monsterId = payload.get("monsterId");
            if (monsterId == null) return ResponseEntity.badRequest().body("ID monstre 'monsterId' requis");

            Player updated = playerService.addMonster(username, monsterId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{username}/monsters/{monsterId}")
    public ResponseEntity<?> removeMonster(@PathVariable String username, @PathVariable String monsterId) {
        try {
            Player updated = playerService.removeMonster(username, monsterId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}