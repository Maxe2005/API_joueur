package com.imt.API_joueur.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;
import java.util.ArrayList;

@Document(collection = "players")
@Setter
@Getter
public class Player {

    @MongoId
    private Integer id;

    private String username;
    private Integer level;
    private double Experience;
    private List<String> monsterIds = new ArrayList<>();

    // Constructors
    public Player() {}

    public Player(String username) {
        this.username = username;
        this.level = 0;
        this.Experience = 0.0;
    }
}
