package com.imt.API_joueur.controller;

import com.imt.API_joueur.controller.dto.output.PlayerResponse;
import com.imt.API_joueur.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@Tag(name = "Joueur", description = "Gestion du profil joueur, XP et inventaire")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    // --- Records DTO ---
    public record XpRequest(@Positive(message = "doit être strictement positif") double amount) {}
    public record MonsterRequest(@NotBlank(message = "est obligatoire") String monsterId) {}
    public record CreatePlayerRequest(@NotBlank(message = "est obligatoire") String username) {}

    @Operation(summary = "Récupérer un joueur par son pseudo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Joueur trouvé"),
            @ApiResponse(responseCode = "404", description = "Joueur introuvable")
    })
    @GetMapping("/{username}")
    public ResponseEntity<PlayerResponse> getPlayer(
            @Parameter(description = "Pseudo du joueur", required = true)
            @PathVariable String username) {

        return ResponseEntity.ok(PlayerResponse.from(playerService.getPlayer(username)));
    }

    @Operation(summary = "Ajouter de l'expérience au joueur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "XP ajoutée"),
            @ApiResponse(responseCode = "404", description = "Joueur introuvable")
    })
    @PostMapping("/{username}/xp")
    public ResponseEntity<PlayerResponse> addExperience(
            @PathVariable String username,
            @Valid @RequestBody XpRequest request) {
        return ResponseEntity.ok(PlayerResponse.from(playerService.addExperience(username, request.amount())));
    }

    @Operation(summary = "Ajouter un monstre à l'inventaire")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monstre ajouté"),
            @ApiResponse(responseCode = "400", description = "Erreur (Inventaire plein, etc.)")
    })
    @PostMapping("/{username}/monsters") // Renommé pour respecter REST standard (plutôt que add_monster)
    public ResponseEntity<PlayerResponse> addMonster(
            @PathVariable String username,
            @Valid @RequestBody MonsterRequest request) {
        return ResponseEntity.ok(PlayerResponse.from(playerService.addMonster(username, request.monsterId())));
    }

    @Operation(summary = "Supprimer un monstre de l'inventaire")
    @DeleteMapping("/{username}/monsters/{monsterId}")
    public ResponseEntity<PlayerResponse> removeMonster(
            @PathVariable String username,
            @PathVariable String monsterId) {
        return ResponseEntity.ok(PlayerResponse.from(playerService.removeMonster(username, monsterId)));
    }

    @Operation(summary = "Créer un nouveau joueur")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Joueur créé"),
            @ApiResponse(responseCode = "409", description = "Le pseudo existe déjà")
    })
    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@Valid @RequestBody CreatePlayerRequest request) {
        return ResponseEntity.status(201).body(PlayerResponse.from(playerService.createPlayer(request.username())));
    }
}