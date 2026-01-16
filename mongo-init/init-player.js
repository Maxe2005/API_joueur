db = db.getSiblingDB('gatcha-player-db');
db.createCollection('players');

db.players.insertMany([
    {
        "_class": "com.imt.API_joueur.model.Player", // Utile pour Spring Data
        "username": "Sacha",
        "level": 1,
        "experience": 0,
        "monsterIds": []
    },
    {
        "_class": "com.imt.API_joueur.model.Player",
        "username": "Ondine",
        "level": 10,
        "experience": 500,
        "monsterIds": ["staross_id_01", "stari_id_02"]
    }
]);