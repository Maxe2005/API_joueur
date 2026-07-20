package com.imt.API_joueur.exception;

public class MonsterNotOwnedException extends RuntimeException {

    public MonsterNotOwnedException(String username, String monsterId) {
        super("Le joueur " + username + " ne possède pas le monstre " + monsterId);
    }
}
