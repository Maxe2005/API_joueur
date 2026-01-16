package com.imt.API_joueur.controller;

import com.imt.API_joueur.model.Player;
import com.imt.API_joueur.repository.PlayerRepository;
import com.imt.API_joueur.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private PlayerRepository playerRepository;

    @Test
    void shouldReturnPlayerInfo() throws Exception {
        Player p = new Player("Sacha");
        p.setLevel(5);

        when(playerRepository.findByUsername("Sacha")).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/players/Sacha")) // Attention: j'ai mis /api/players dans le Controller propre
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Sacha"))
                .andExpect(jsonPath("$.level").value(5));
    }

    @Test
    void shouldAddExperience() throws Exception {
        Player updated = new Player("Sacha");
        updated.setLevel(2);

        when(playerService.addExperience(eq("Sacha"), anyDouble())).thenReturn(updated);

        String jsonContent = "{\"amount\": 100}";

        mockMvc.perform(post("/api/players/Sacha/xp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(2));
    }

    @Test
    void shouldAddMonster() throws Exception {
        Player p = new Player("Sacha");
        p.getMonsterIds().add("monstre_pikachu_123");

        when(playerService.addMonster(eq("Sacha"), eq("monstre_pikachu_123"))).thenReturn(p);

        String jsonContent = "{\"monsterId\": \"monstre_pikachu_123\"}";

        mockMvc.perform(post("/api/players/Sacha/monsters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monsterIds[0]").value("monstre_pikachu_123"));
    }

    @Test
    void shouldRemoveMonster() throws Exception {
        Player p = new Player("Sacha");

        when(playerService.removeMonster(eq("Sacha"), eq("monstre_pikachu_123"))).thenReturn(p);

        mockMvc.perform(delete("/api/players/Sacha/monsters/monstre_pikachu_123"))
                .andExpect(status().isOk());
    }
}