package formulAI.project.bank.banqueCredit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import formulAI.project.bank.banqueCredit.dto.*;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * QA-TASK-008 - Test de non-regression du parcours metier complet (baseline pour AgentReviewer/AgentDeployment) :
 * login -> creation client -> creation demande -> simulation -> soumission -> passage en analyse ->
 * decision (refus avec commentaire, RG-BANK-07) -> historique -> dashboard.
 * Ce test sert de reference de non-regression : toute rupture de ce parcours doit etre traitee en priorite.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ParcoursCompletNonRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AuthRequestDto(username, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void parcoursCompletLoginClientDemandeSimulationSoumissionAnalyseDecisionHistoriqueDashboard() throws Exception {
        String conseillerToken = login("conseiller1", "Conseiller123!");
        String responsableToken = login("responsable1", "Responsable123!");

        // 1. Creation client (RG-BANK-01)
        ClientCreateDto clientDto = new ClientCreateDto();
        clientDto.setNom("Client Parcours Complet");
        clientDto.setRevenuMensuel(new BigDecimal("2000"));
        clientDto.setChargesMensuelles(new BigDecimal("1900"));
        String clientBody = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(clientDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long clientId = objectMapper.readTree(clientBody).get("id").asLong();

        // 2. Creation demande (RG-BANK-02, RG-BANK-03) - charges elevees pour garantir un refus plus tard.
        DemandeCreditCreateDto demandeDto = new DemandeCreditCreateDto();
        demandeDto.setClientId(clientId);
        demandeDto.setMontantDemande(new BigDecimal("5000"));
        demandeDto.setDureeMois(24);
        demandeDto.setTauxFictif(new BigDecimal("5"));
        String demandeBody = mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(demandeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("BROUILLON"))
                .andReturn().getResponse().getContentAsString();
        long demandeId = objectMapper.readTree(demandeBody).get("id").asLong();

        // 3. Simulation (RG-BANK-04, RG-BANK-08)
        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/simuler")
                        .header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensualiteEstimee").isNotEmpty())
                .andExpect(jsonPath("$.tauxEndettement").isNotEmpty())
                .andExpect(jsonPath("$.scoreSimplifie").isNotEmpty());

        // 4. Soumission (RG-BANK-05)
        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/soumettre")
                        .header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("SOUMISE"));

        // 5. Passage en analyse (RG-BANK-05)
        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/analyser")
                        .header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_ANALYSE"));

        // 6. Decision : refus avec commentaire obligatoire (RG-BANK-06 non eligible du fait des charges elevees, RG-BANK-07)
        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/refuser")
                        .header("Authorization", "Bearer " + responsableToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DecisionRefusDto("Taux d'endettement trop eleve"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("REFUSEE"))
                .andExpect(jsonPath("$.commentaireDecision").value("Taux d'endettement trop eleve"));

        // 7. Historique (US-016) - 4 entrees attendues : creation, soumission, analyse, refus.
        mockMvc.perform(get("/api/v1/demandes/" + demandeId + "/historique")
                        .header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].nouveauStatut").value("BROUILLON"))
                .andExpect(jsonPath("$[0].ancienStatut").doesNotExist())
                .andExpect(jsonPath("$[3].nouveauStatut").value("REFUSEE"))
                .andExpect(jsonPath("$[3].commentaire").value("Taux d'endettement trop eleve"));

        // 8. Dashboard (US-017) - compteur refusees >= 1.
        mockMvc.perform(get("/api/v1/dashboard")
                        .header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nbRefusees").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }
}

