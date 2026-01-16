package com.imt.API_joueur.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "players")
@Getter
@Setter
@NoArgsConstructor
public class Player {

    @Id
    private String id;

    private String username;

    private Integer level;

    private double experience;

    private List<String> monsterIds = new ArrayList<>();

    public Player(String username) {
        this.username = username;
        this.level = 1;
        this.experience = 0.0;
    }
}