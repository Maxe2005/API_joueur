# TODO

## Aligner spring-boot-starter-parent avec les autres services Java

`API_joueur` est actuellement sur `spring-boot-starter-parent` **3.2.2**, alors qu'`API_monstres`, `API_invocations` et `API_authentification` sont sur la ligne **3.5.x**.

**Pourquoi c'est souhaitable :**
- Réduit le risque de compatibilité binaire à mesure que des libs partagées apparaissent entre ces services (voir `gatcha-common-security`, adoptée par ce service pour la vérification de token — compile et fonctionne aujourd'hui contre le jar compilé en 3.5.x, mais ce n'est pas garanti indéfiniment à mesure que la lib ou Spring évoluent).
- Bénéficier des mêmes correctifs de sécurité/bugs que les trois autres services, qui reçoivent déjà les mises à jour 3.5.x.

**Pas fait dans le cadre de cette roadmap** (risque de régression hors sujet par rapport à l'objectif initial). Filet de sécurité immédiat : après toute évolution de `gatcha-common-security` ou de ses dépendances transitives, relancer `./mvnw clean package` (ou son équivalent Docker) ici pour vérifier que la compatibilité tient toujours.
