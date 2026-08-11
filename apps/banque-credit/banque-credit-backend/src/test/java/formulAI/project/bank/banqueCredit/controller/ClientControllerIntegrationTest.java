package formulAI.project.bank.banqueCredit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import formulAI.project.bank.banqueCredit.dto.AuthRequestDto;
import formulAI.project.bank.banqueCredit.dto.ClientCreateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String conseillerToken;
    private String responsableToken;

    @BeforeEach
    void authentifier() throws Exception {
        conseillerToken = login("conseiller1", "Conseiller123!");
        responsableToken = login("responsable1", "Responsable123!");
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AuthRequestDto(username, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private ClientCreateDto validClient(String nom) {
        ClientCreateDto dto = new ClientCreateDto();
        dto.setNom(nom);
        dto.setRevenuMensuel(new BigDecimal("2500.00"));
        dto.setChargesMensuelles(new BigDecimal("400.00"));
        return dto;
    }

    @Test
    void creationClientParConseillerRetourne201() throws Exception {
        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validClient("Jean Dupont"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void creationClientSansNomRetourne400AvecMessageParChamp() throws Exception {
        ClientCreateDto dto = validClient(null);
        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CHAMP_OBLIGATOIRE"))
                .andExpect(jsonPath("$.erreurs[?(@.champ=='nom')]").exists());
    }

    @Test
    void creationClientAvecPlusieursChampsManquantsRetourneTousLesMessages() throws Exception {
        ClientCreateDto dto = new ClientCreateDto();
        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));
    }

    @Test
    void creationClientParResponsableCreditRetourne403() throws Exception {
        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + responsableToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validClient("Interdit"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void listerClientsVideRetourneTableauVide() throws Exception {
        mockMvc.perform(get("/api/v1/clients").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void detailClientInexistantRetourne404() throws Exception {
        mockMvc.perform(get("/api/v1/clients/999999").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"));
    }

    @Test
    void detailClientSansDemandeAfficheListeVide() throws Exception {
        String body = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validClient("Sans Demande"))))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(get("/api/v1/clients/" + id).header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demandes").isArray())
                .andExpect(jsonPath("$.demandes.length()").value(0));
    }
}

