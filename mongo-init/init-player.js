db = db.getSiblingDB('gatcha-player-db');

db.createCollection('players');

db.players.insertMany([
    {
        "username": "Sacha",
        "level": 0,
        "experience": 0,
        "monsterIds": []
    },
    {
        "username": "Ondine",
        "level": 10,
        "experience": 500,
        "monsterIds": ["staross_id_01", "stari_id_02"]
    },
    {
        "username": "AdminTest",
        "level": 50,
        "experience": 0,
        "monsterIds": []
    }
    {
        "username": "Rémy",
        "level": 50,
        "experience": 0,
        "monsterIds": ["Adrien", "Léonce", "Jules"]
    }
]);