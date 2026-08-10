package com.imt.API_joueur;

import com.imt.commonsecurity.devauth.DevAutoAuthConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

// gatcha-common-security n'a aucun component-scan cross-package (voir son README) : le filtre
// dev-only (Story 1.1 / AD-8) doit être importé explicitement. @Import est sans effet tant que le
// profil Spring "dev-local" n'est pas actif — DevAutoAuthConfig est lui-même @Profile("dev-local").
@Import(DevAutoAuthConfig.class)
@SpringBootApplication
public class ApiJoueurApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiJoueurApplication.class, args);
	}

}
