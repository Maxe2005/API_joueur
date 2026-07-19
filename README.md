# Gatcha API - Service Joueur

## Présentation
Ce projet est l'API "Joueur" d'un système de jeu de type Gatcha. Elle permet de gérer les profils des joueurs, leur expérience (XP) et leur inventaire de monstres.

## Prérequis
* **Docker**
* **Docker Compose**

## Lancement du projet
Ce service se lance **exclusivement** via le dépôt orchestrateur [GatchaApi](https://github.com/Maxe2005/GatchaApi), qui l'intègre à la stack complète avec son `docker-compose.yaml` racine (il n'y a plus de `docker-compose.yml` local dans ce dépôt).

```bash
git clone --recurse-submodules https://github.com/Maxe2005/GatchaApi.git
cd GatchaApi
make up   # ou : docker compose up -d --build
```

Toute la configuration (MongoDB, URL de l'API d'authentification) est fournie par le `docker-compose.yaml` de l'orchestrateur. L'API est exposée sur `http://localhost:8082` (Swagger : `http://localhost:8082/swagger-ui/index.html`).
