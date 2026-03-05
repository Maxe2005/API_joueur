# Gatcha API - Service Joueur

## Présentation
Ce projet est l'API "Joueur" d'un système de jeu de type Gatcha. Elle permet de gérer les profils des joueurs, leur expérience (XP) et leur inventaire de monstres.

## Prérequis
Pour lancer ce projet très facilement sur n'importe quel ordinateur, vous devez avoir installé :
* **Docker**
* **Docker Compose**

## Lancement du projet
L'ensemble du projet (base de données MongoDB et l'application Spring Boot) a été conteneurisé pour tourner entièrement sous Docker.

Pour démarrer le projet complet, ouvrez votre terminal à la racine du projet (là où se trouve le fichier `docker-compose.yml`) et tapez la commande suivante :

```bash
docker-compose up --build
```