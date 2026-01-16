package com.imt.API_joueur.controller;

import com.imt.API_joueur.model.Player;
import com.imt.API_joueur.repository.PlayerRepository;
import com.imt.API_joueur.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@Tag(name = "Joueur", description = "Endpoints pour la gestion du profil, de l'XP et de l'inventaire")
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerRepository playerRepository;

    public PlayerController(PlayerService playerService, PlayerRepository playerRepository) {
        this.playerService = playerService;
        this.playerRepository = playerRepository;
    }

    public record XpRequest(double amount) {}
    public record MonsterRequest(String monsterId) {}

    @Operation(summary = "Récupérer un joueur", description = "Retourne le niveau, l'expérience et la liste des monstres d'un joueur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Joueur trouvé"),
            @ApiResponse(responseCode = "404", description = "Joueur introuvable")
    })
    @GetMapping("/{username}")
    public ResponseEntity<Player> getPlayer(
            @Parameter(description = "Le nom d'utilisateur (ex: Sacha)", required = true)
            @PathVariable String username) {

        return playerRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Ajouter de l'expérience", description = "Ajoute des points d'XP. Si le seuil est atteint, le joueur monte de niveau.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "XP ajoutée avec succès (retourne le joueur mis à jour)"),
            @ApiResponse(responseCode = "404", description = "Joueur introuvable")
    })
    @PostMapping("/{username}/xp")
    public ResponseEntity<?> addExperience(
            @PathVariable String username,
            @RequestBody XpRequest request) {
        try {
            Player updatedPlayer = playerService.addExperience(username, request.amount());
            return ResponseEntity.ok(updatedPlayer);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Ajouter un monstre", description = "Appelé par l'API Invocation. Ajoute l'ID du monstre à l'inventaire.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monstre ajouté"),
            @ApiResponse(responseCode = "400", description = "Inventaire plein ou erreur")
    })
    @PostMapping("/{username}/monsters")
    public ResponseEntity<?> addMonster(
            @PathVariable String username,
            @RequestBody MonsterRequest request) {
        try {
            Player updated = playerService.addMonster(username, request.monsterId());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Relâcher un monstre", description = "Supprime un monstre de l'inventaire du joueur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monstre supprimé"),
            @ApiResponse(responseCode = "400", description = "Le joueur ne possède pas ce monstre")
    })
    @DeleteMapping("/{username}/monsters/{monsterId}")
    public ResponseEntity<?> removeMonster(
            @PathVariable String username,
            @PathVariable String monsterId) {
        try {
            Player updated = playerService.removeMonster(username, monsterId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}