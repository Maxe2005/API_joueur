package com.imt.API_joueur;

import com.imt.API_joueur.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

// spring.data.mongodb.auto-index-creation=false : sans ça, la création d'index Mongo au
// démarrage du contexte tente une vraie connexion réseau même avec PlayerRepository mocké
// ci-dessous, et le test échoue si aucun MongoDB n'est lancé (ce que ce test prétend éviter).
@SpringBootTest(properties = "spring.data.mongodb.auto-index-creation=false")
class ApiJoueurApplicationTests {

	// On "mock" (simule) le repository.
	// Cela permet au test de réussir même si MongoDB n'est pas lancé.
	@MockBean
	private PlayerRepository playerRepository;

	@Test
	void contextLoads() {
		// Si on arrive ici, c'est que toute l'architecture Spring (Controlleurs, Services, Sécurité)
		// s'est chargée correctement.
	}

}