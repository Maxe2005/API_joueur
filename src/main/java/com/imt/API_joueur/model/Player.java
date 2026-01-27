package com.imt.API_joueur.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "players")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Représente un joueur et son inventaire")
public class Player {

    @Id
    @Schema(description = "ID unique MongoDB", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Schema(description = "Nom unique du joueur", example = "Sacha")
    @Indexed(unique = true)
    private String username;

    @Schema(description = "Niveau actuel (0 à 50)", example = "10")
    private Integer level;

    @Schema(description = "Points d'expérience accumulés vers le prochain niveau", example = "150.5")
    private double experience;

    @Schema(description = "Liste des IDs des monstres possédés", example = "[\"pikachu_001\", \"dracaufeu_99\"]")
    private List<String> monsterIds = new ArrayList<>();

    public Player(String username) {
        this.username = username;
        this.level = 1;
        this.experience = 0.0;
    }
}