package com.imt.API_joueur.config;

import com.imt.API_joueur.model.Player;
import com.imt.API_joueur.repository.PlayerRepository;
import com.imt.API_joueur.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultUsersSeederTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private PlayerRepository playerRepository;

    private DefaultUsersProperties properties;
    private DefaultUsersSeeder seeder;

    @BeforeEach
    void setUp() {
        properties = new DefaultUsersProperties();
        seeder = new DefaultUsersSeeder(playerService, playerRepository, properties);
    }

    @Test
    void run_shouldCreateBothDefaultPlayers_whenNeitherExists() {
        properties.getAdmin().setUsername("admin");
        properties.getUser().setUsername("user");

        when(playerRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(playerRepository.findByUsername("user")).thenReturn(Optional.empty());

        seeder.run();

        verify(playerService).createPlayer("admin");
        verify(playerService).createPlayer("user");
    }

    @Test
    void run_shouldSkipCreation_whenPlayersAlreadyExist() {
        properties.getAdmin().setUsername("admin");
        properties.getUser().setUsername("user");

        when(playerRepository.findByUsername("admin")).thenReturn(Optional.of(new Player("admin")));
        when(playerRepository.findByUsername("user")).thenReturn(Optional.of(new Player("user")));

        seeder.run();

        verify(playerService, never()).createPlayer("admin");
        verify(playerService, never()).createPlayer("user");
    }

    @Test
    void run_shouldSkipAdminSeed_whenAdminUsernameBlank() {
        properties.getAdmin().setUsername("");
        properties.getUser().setUsername("user");

        when(playerRepository.findByUsername("user")).thenReturn(Optional.empty());

        seeder.run();

        verify(playerService, never()).createPlayer("");
        verify(playerService).createPlayer("user");
    }

    @Test
    void run_shouldSkipUserSeed_whenUserUsernameBlank() {
        properties.getAdmin().setUsername("admin");
        properties.getUser().setUsername("");

        when(playerRepository.findByUsername("admin")).thenReturn(Optional.empty());

        seeder.run();

        verify(playerService).createPlayer("admin");
        verify(playerService, never()).createPlayer("");
    }
}
