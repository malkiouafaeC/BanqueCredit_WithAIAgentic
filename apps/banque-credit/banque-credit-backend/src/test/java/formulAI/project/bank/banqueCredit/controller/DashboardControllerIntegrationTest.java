package formulAI.project.bank.banqueCredit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import formulAI.project.bank.banqueCredit.dto.*;
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
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String conseillerToken;
    private String responsableToken;

    @BeforeEach
    void setUp() throws Exception {
        conseillerToken = login("conseiller1", "Conseiller123!");
        responsableToken = login("responsable1", "Responsable123!");
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AuthRequestDto(username, password))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private Long creerClient() throws Exception {
        ClientCreateDto dto = new ClientCreateDto();
        dto.setNom("Dashboard Client");
        dto.setRevenuMensuel(new BigDecimal("2000"));
        dto.setChargesMensuelles(BigDecimal.ZERO);
        String body = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long creerDemande(Long clientId, BigDecimal montant) throws Exception {
        DemandeCreditCreateDto dto = new DemandeCreditCreateDto();
        dto.setClientId(clientId);
        dto.setMontantDemande(montant);
        dto.setDureeMois(24);
        dto.setTauxFictif(BigDecimal.ZERO);
        String body = mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    void dashboardVideRetourneCompteursZeroEtValeursNeutres() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nbSoumises").value(0))
                .andExpect(jsonPath("$.nbEnAnalyse").value(0))
                .andExpect(jsonPath("$.nbAcceptees").value(0))
                .andExpect(jsonPath("$.nbRefusees").value(0))
                .andExpect(jsonPath("$.montantTotalDemande").value(0))
                .andExpect(jsonPath("$.tauxMoyenEndettement").value(0));
    }

    @Test
    void dashboardRefleteExactementLesCompteursParStatutEtExclutBrouillonEtAnnulee() throws Exception {
        Long clientId = creerClient();

        // 1 BROUILLON (exclu des agregats)
        creerDemande(clientId, new BigDecimal("2000"));

        // 1 ANNULEE (exclu des agregats)
        Long annulee = creerDemande(clientId, new BigDecimal("3000"));
        mockMvc.perform(post("/api/v1/demandes/" + annulee + "/annuler").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk());

        // 1 SOUMISE
        Long soumise = creerDemande(clientId, new BigDecimal("4000"));
        mockMvc.perform(post("/api/v1/demandes/" + soumise + "/soumettre").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk());

        // 1 EN_ANALYSE
        Long enAnalyse = creerDemande(clientId, new BigDecimal("5000"));
        mockMvc.perform(post("/api/v1/demandes/" + enAnalyse + "/soumettre").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/demandes/" + enAnalyse + "/analyser").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isOk());

        // 1 REFUSEE
        Long refusee = creerDemande(clientId, new BigDecimal("6000"));
        mockMvc.perform(post("/api/v1/demandes/" + refusee + "/soumettre").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/demandes/" + refusee + "/analyser").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/demandes/" + refusee + "/refuser")
                        .header("Authorization", "Bearer " + responsableToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DecisionRefusDto("non"))))
                .andExpect(status().isOk());

        // Perimetre agregats (hors BROUILLON/ANNULEE) : SOUMISE(4000) + EN_ANALYSE(5000) + REFUSEE(6000) = 15000
        mockMvc.perform(get("/api/v1/dashboard").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nbSoumises").value(1))
                .andExpect(jsonPath("$.nbEnAnalyse").value(1))
                .andExpect(jsonPath("$.nbAcceptees").value(0))
                .andExpect(jsonPath("$.nbRefusees").value(1))
                .andExpect(jsonPath("$.montantTotalDemande").value(15000));
    }
}

