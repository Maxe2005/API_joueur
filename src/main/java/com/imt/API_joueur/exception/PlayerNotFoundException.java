package com.imt.API_joueur.exception;

public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(String username) {
        super("Joueur introuvable : " + username);
    }
}
