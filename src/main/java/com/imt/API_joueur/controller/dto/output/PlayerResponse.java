package com.imt.API_joueur.controller.dto.output;

import com.imt.API_joueur.model.Player;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Représentation publique d'un joueur (sans l'identifiant technique Mongo).")
public record PlayerResponse(
        @Schema(example = "Sacha") String username,
        @Schema(example = "5") int level,
        @Schema(example = "150.0") double experience,
        List<String> monsterIds) {

    public static PlayerResponse from(Player player) {
        return new PlayerResponse(player.getUsername(), player.getLevel(), player.getExperience(), player.getMonsterIds());
    }
}
