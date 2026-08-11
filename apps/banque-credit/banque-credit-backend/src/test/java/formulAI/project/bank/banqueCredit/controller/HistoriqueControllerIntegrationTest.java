package formulAI.project.bank.banqueCredit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import formulAI.project.bank.banqueCredit.dto.AuthRequestDto;
import formulAI.project.bank.banqueCredit.dto.ClientCreateDto;
import formulAI.project.bank.banqueCredit.dto.DecisionRefusDto;
import formulAI.project.bank.banqueCredit.dto.DemandeCreditCreateDto;
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
class HistoriqueControllerIntegrationTest {

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

    @Test
    void historiqueAvec3TransitionsDansLOrdreChronologique() throws Exception {
        ClientCreateDto client = new ClientCreateDto();
        client.setNom("Historique Test");
        client.setRevenuMensuel(new BigDecimal("2000"));
        client.setChargesMensuelles(BigDecimal.ZERO);
        String clientBody = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(client)))
                .andReturn().getResponse().getContentAsString();
        Long clientId = objectMapper.readTree(clientBody).get("id").asLong();

        DemandeCreditCreateDto demande = new DemandeCreditCreateDto();
        demande.setClientId(clientId);
        demande.setMontantDemande(new BigDecimal("5000"));
        demande.setDureeMois(24);
        demande.setTauxFictif(BigDecimal.ZERO);
        String demandeBody = mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(demande)))
                .andReturn().getResponse().getContentAsString();
        Long demandeId = objectMapper.readTree(demandeBody).get("id").asLong();

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/soumettre").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/analyser").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/refuser")
                        .header("Authorization", "Bearer " + responsableToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DecisionRefusDto("Dossier incomplet"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/demandes/" + demandeId + "/historique")
                        .header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].nouveauStatut").value("BROUILLON"))
                .andExpect(jsonPath("$[0].ancienStatut").doesNotExist())
                .andExpect(jsonPath("$[1].nouveauStatut").value("SOUMISE"))
                .andExpect(jsonPath("$[2].nouveauStatut").value("EN_ANALYSE"))
                .andExpect(jsonPath("$[3].nouveauStatut").value("REFUSEE"))
                .andExpect(jsonPath("$[3].commentaire").value("Dossier incomplet"))
                .andExpect(jsonPath("$[3].auteur").value("responsable1"));
    }

    @Test
    void historiqueDemandeBrouillonAAuMoinsUneEntree() throws Exception {
        ClientCreateDto client = new ClientCreateDto();
        client.setNom("Brouillon Test");
        client.setRevenuMensuel(new BigDecimal("2000"));
        client.setChargesMensuelles(BigDecimal.ZERO);
        String clientBody = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(client)))
                .andReturn().getResponse().getContentAsString();
        Long clientId = objectMapper.readTree(clientBody).get("id").asLong();

        DemandeCreditCreateDto demande = new DemandeCreditCreateDto();
        demande.setClientId(clientId);
        demande.setMontantDemande(new BigDecimal("5000"));
        demande.setDureeMois(24);
        demande.setTauxFictif(BigDecimal.ZERO);
        String demandeBody = mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(demande)))
                .andReturn().getResponse().getContentAsString();
        Long demandeId = objectMapper.readTree(demandeBody).get("id").asLong();

        mockMvc.perform(get("/api/v1/demandes/" + demandeId + "/historique")
                        .header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void historiqueDemandeInexistanteRetourne404() throws Exception {
        mockMvc.perform(get("/api/v1/demandes/999999/historique").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isNotFound());
    }
}

