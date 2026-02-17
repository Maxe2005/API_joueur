package com.imt.API_joueur.service;

import com.imt.API_joueur.model.Player;
import com.imt.API_joueur.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player sacha;

    @BeforeEach
    void setUp() {
        sacha = new Player("Sacha");
        sacha.setLevel(1);
        sacha.setExperience(0.0);
    }

    @Test
    void testLevelUp() {
        when(playerRepository.findByUsername("Sacha")).thenReturn(Optional.of(sacha));
        when(playerRepository.save(any(Player.class))).thenAnswer(i -> i.getArgument(0));

        playerService.addExperience("Sacha", 50.0);
        assertEquals(2, sacha.getLevel(), "Le joueur doit passer niveau 2");
        assertEquals(0.0, sacha.getExperience(), 0.01);
    }

    @Test
    void testRemoveMonster() {
        sacha.getMonsterIds().add("pikachu_001");

        when(playerRepository.findByUsername("Sacha")).thenReturn(Optional.of(sacha));
        when(playerRepository.save(any(Player.class))).thenAnswer(i -> i.getArgument(0));

        Player result = playerService.removeMonster("Sacha", "pikachu_001");

        assertTrue(result.getMonsterIds().isEmpty(), "La liste doit être vide après suppression");
    }
}