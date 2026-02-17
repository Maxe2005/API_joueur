package com.imt.API_joueur.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "players")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Entité représentant un joueur, son niveau et son inventaire.")
public class Player {

    @Id
    @Schema(description = "Identifiant unique technique (MongoDB)", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Indexed(unique = true)
    @Schema(description = "Nom d'utilisateur unique", example = "Sacha")
    private String username;

    @Schema(description = "Niveau du joueur (Max 50)", example = "5")
    private Integer level;

    @Schema(description = "Expérience accumulée vers le prochain niveau", example = "150.0")
    private double experience;

    @Schema(description = "Liste des identifiants des monstres dans l'inventaire", example = "[\"pikachu_001\", \"dracaufeu_99\"]")
    private List<String> monsterIds = new ArrayList<>();

    public Player(String username) {
        this.username = username;
        this.level = 1;
        this.experience = 0.0;
    }
}