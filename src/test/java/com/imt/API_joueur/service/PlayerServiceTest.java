package com.imt.API_joueur.service;

import com.imt.API_joueur.exception.DuplicateUsernameException;
import com.imt.API_joueur.exception.InventoryFullException;
import com.imt.API_joueur.exception.MonsterNotOwnedException;
import com.imt.API_joueur.exception.PlayerNotFoundException;
import com.imt.API_joueur.model.Player;
import com.imt.API_joueur.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    void testAddExperience_atMaxLevel_doesNotChangeOrSave() {
        sacha.setLevel(50);
        sacha.setExperience(0.0);
        when(playerRepository.findByUsername("Sacha")).thenReturn(Optional.of(sacha));

        Player result = playerService.addExperience("Sacha", 1000.0);

        assertEquals(50, result.getLevel());
        assertEquals(0.0, result.getExperience(), 0.01);
        verify(playerRepository, never()).save(any());
    }

    @Test
    void testGetPlayer_notFound_throws() {
        when(playerRepository.findByUsername("Inconnu")).thenReturn(Optional.empty());

        assertThrows(PlayerNotFoundException.class, () -> playerService.getPlayer("Inconnu"));
    }

    @Test
    void testAddMonster_inventoryFull_throws() {
        for (int i = 0; i < 10; i++) {
            sacha.getMonsterIds().add("monstre_" + i);
        }
        when(playerRepository.findByUsername("Sacha")).thenReturn(Optional.of(sacha));

        assertThrows(InventoryFullException.class, () -> playerService.addMonster("Sacha", "monstre_10"));
        verify(playerRepository, never()).save(any());
    }

    @Test
    void testRemoveMonster_notOwned_throws() {
        when(playerRepository.findByUsername("Sacha")).thenReturn(Optional.of(sacha));

        assertThrows(MonsterNotOwnedException.class, () -> playerService.removeMonster("Sacha", "inexistant"));
    }

    @Test
    void testCreatePlayer_usernameAlreadyPresent_throws() {
        when(playerRepository.findByUsername("Sacha")).thenReturn(Optional.of(sacha));

        assertThrows(DuplicateUsernameException.class, () -> playerService.createPlayer("Sacha"));
        verify(playerRepository, never()).save(any());
    }

    @Test
    void testCreatePlayer_duplicateKeyRaceDetectedByDb_throws() {
        when(playerRepository.findByUsername("Sacha")).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenThrow(new DuplicateKeyException("index conflict"));

        assertThrows(DuplicateUsernameException.class, () -> playerService.createPlayer("Sacha"));
    }

    @Test
    void testAddExperience_retriesOnceOnVersionConflictThenSucceeds() {
        when(playerRepository.findByUsername("Sacha")).thenReturn(Optional.of(sacha));
        when(playerRepository.save(any(Player.class)))
                .thenThrow(new OptimisticLockingFailureException("conflit"))
                .thenAnswer(i -> i.getArgument(0));

        Player result = playerService.addExperience("Sacha", 10.0);

        assertNotNull(result);
        verify(playerRepository, times(2)).findByUsername("Sacha");
        verify(playerRepository, times(2)).save(any());
    }

    @Test
    void testAddExperience_versionConflictExhaustsRetries_throws() {
        when(playerRepository.findByUsername("Sacha")).thenReturn(Optional.of(sacha));
        when(playerRepository.save(any(Player.class))).thenThrow(new OptimisticLockingFailureException("conflit"));

        assertThrows(OptimisticLockingFailureException.class, () -> playerService.addExperience("Sacha", 10.0));
        verify(playerRepository, times(3)).save(any());
    }
}
