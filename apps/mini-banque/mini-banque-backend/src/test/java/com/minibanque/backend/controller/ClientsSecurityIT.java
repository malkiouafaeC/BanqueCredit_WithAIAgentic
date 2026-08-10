package com.minibanque.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.minibanque.backend.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
class ClientsSecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));
    }

        @Test
        void shouldReturnUnauthorizedForCreateWithoutToken() throws Exception {
        String body = """
            {
              "nom": "Client C",
              "email": "c@bank.test"
            }
            """;

        mockMvc.perform(post("/api/clients/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("AUTH_UNAUTHORIZED"));
        }

    @Test
    void shouldAllowAccessWithValidToken() throws Exception {
        String token = jwtService.generateToken("conseiller1");

        mockMvc.perform(get("/api/clients")
                        .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].nom").value("Client A"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].nom").value("Client B"));
        }

        @Test
        void shouldCreateClientWithValidPayload() throws Exception {
        String token = jwtService.generateToken("conseiller1");
        String body = """
            {
              "nom": "Client C",
              "email": "c@bank.test"
            }
            """;

        mockMvc.perform(post("/api/clients/new")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.nom").value("Client C"))
            .andExpect(jsonPath("$.email").value("c@bank.test"));
        }

        @Test
        void shouldReturnBadRequestForInvalidClientPayload() throws Exception {
        String token = jwtService.generateToken("conseiller1");
        String body = """
            {
              "nom": "",
              "email": "invalid-email"
            }
            """;

        mockMvc.perform(post("/api/clients/new")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("CLIENT_VALIDATION_ERROR"));
    }
}
