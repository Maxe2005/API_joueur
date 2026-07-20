package com.imt.API_joueur.exception;

public class InventoryFullException extends RuntimeException {

    public InventoryFullException(String username) {
        super("Inventaire plein pour le joueur : " + username);
    }
}
