package com.imt.API_joueur.controller;

import com.imt.API_joueur.model.Player;
import com.imt.API_joueur.repository.PlayerRepository;
import com.imt.API_joueur.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@Tag(name = "Joueur", description = "Gestion du profil joueur, XP et inventaire")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerRepository playerRepository;

    // --- Records DTO ---
    public record XpRequest(double amount) {}
    public record MonsterRequest(String monsterId) {}
    public record CreatePlayerRequest(String username) {}

    @Operation(summary = "Récupérer un joueur par son pseudo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Joueur trouvé"),
            @ApiResponse(responseCode = "404", description = "Joueur introuvable")
    })
    @GetMapping("/{username}")
    public ResponseEntity<Player> getPlayer(
            @Parameter(description = "Pseudo du joueur", required = true)
            @PathVariable String username) {

        return playerRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Ajouter de l'expérience au joueur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "XP ajoutée"),
            @ApiResponse(responseCode = "404", description = "Joueur introuvable")
    })
    @PostMapping("/{username}/xp")
    public ResponseEntity<?> addExperience(
            @PathVariable String username,
            @RequestBody XpRequest request) {
        try {
            return ResponseEntity.ok(playerService.addExperience(username, request.amount()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Operation(summary = "Ajouter un monstre à l'inventaire")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monstre ajouté"),
            @ApiResponse(responseCode = "400", description = "Erreur (Inventaire plein, etc.)")
    })
    @PostMapping("/{username}/monsters") // Renommé pour respecter REST standard (plutôt que add_monster)
    public ResponseEntity<?> addMonster(
            @PathVariable String username,
            @RequestBody MonsterRequest request) {
        try {
            return ResponseEntity.ok(playerService.addMonster(username, request.monsterId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Supprimer un monstre de l'inventaire")
    @DeleteMapping("/{username}/monsters/{monsterId}")
    public ResponseEntity<?> removeMonster(
            @PathVariable String username,
            @PathVariable String monsterId) {
        try {
            return ResponseEntity.ok(playerService.removeMonster(username, monsterId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Créer un nouveau joueur")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Joueur créé"),
            @ApiResponse(responseCode = "409", description = "Le pseudo existe déjà")
    })
    @PostMapping
    public ResponseEntity<?> createPlayer(@RequestBody CreatePlayerRequest request) {
        try {
            return ResponseEntity.status(201).body(playerService.createPlayer(request.username()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }
}