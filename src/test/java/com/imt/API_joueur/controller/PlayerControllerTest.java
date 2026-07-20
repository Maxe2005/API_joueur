package com.imt.API_joueur.controller;

import com.imt.API_joueur.config.AuthInterceptor;
import com.imt.API_joueur.exception.DuplicateUsernameException;
import com.imt.API_joueur.exception.InventoryFullException;
import com.imt.API_joueur.exception.PlayerNotFoundException;
import com.imt.API_joueur.model.Player;
import com.imt.API_joueur.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PlayerController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthInterceptor.class))
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void shouldReturnPlayerInfo() throws Exception {
        Player p = new Player("Sacha");
        p.setLevel(5);

        when(playerService.getPlayer("Sacha")).thenReturn(p);

        mockMvc.perform(get("/api/players/Sacha"))
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
        // Simulation du retour (même si le controller renvoie void ou ok, le service doit retourner l'objet)
        when(playerService.removeMonster(eq("Sacha"), eq("monstre_pikachu_123"))).thenReturn(p);

        mockMvc.perform(delete("/api/players/Sacha/monsters/monstre_pikachu_123"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenPlayerNotFound() throws Exception {
        when(playerService.getPlayer("Inconnu")).thenThrow(new PlayerNotFoundException("Inconnu"));

        mockMvc.perform(get("/api/players/Inconnu"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn409WhenUsernameAlreadyTaken() throws Exception {
        when(playerService.createPlayer("Sacha")).thenThrow(new DuplicateUsernameException("Sacha"));

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"Sacha\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn400WhenInventoryFull() throws Exception {
        when(playerService.addMonster(eq("Sacha"), anyString())).thenThrow(new InventoryFullException("Sacha"));

        mockMvc.perform(post("/api/players/Sacha/monsters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monsterId\": \"monstre_x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn400WhenXpAmountIsNotPositive() throws Exception {
        mockMvc.perform(post("/api/players/Sacha/xp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": -10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenUsernameIsBlankOnCreate() throws Exception {
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"\"}"))
                .andExpect(status().isBadRequest());
    }
}