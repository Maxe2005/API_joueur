package com.imt.API_joueur.exception;

public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException(String username) {
        super("Ce pseudo est déjà pris : " + username);
    }
}
