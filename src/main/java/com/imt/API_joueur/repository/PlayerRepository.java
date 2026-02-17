package com.imt.API_joueur.repository;

import com.imt.API_joueur.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends MongoRepository<Player, String> {
    Optional<Player> findByUsername(String username);
}