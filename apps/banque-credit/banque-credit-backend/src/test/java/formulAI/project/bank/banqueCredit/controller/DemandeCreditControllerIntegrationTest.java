package formulAI.project.bank.banqueCredit.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import formulAI.project.bank.banqueCredit.dto.AuthRequestDto;
import formulAI.project.bank.banqueCredit.dto.ClientCreateDto;
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
class DemandeCreditControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String conseillerToken;
    private String responsableToken;
    private Long clientId;

    @BeforeEach
    void setUp() throws Exception {
        conseillerToken = login("conseiller1", "Conseiller123!");
        responsableToken = login("responsable1", "Responsable123!");
        clientId = creerClient();
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new AuthRequestDto(username, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private Long creerClient() throws Exception {
        ClientCreateDto dto = new ClientCreateDto();
        dto.setNom("Client Demande Test");
        dto.setRevenuMensuel(new BigDecimal("2500.00"));
        dto.setChargesMensuelles(new BigDecimal("300.00"));
        String body = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private DemandeCreditCreateDto demande(BigDecimal montant, Integer duree) {
        DemandeCreditCreateDto dto = new DemandeCreditCreateDto();
        dto.setClientId(clientId);
        dto.setMontantDemande(montant);
        dto.setDureeMois(duree);
        dto.setTauxFictif(new BigDecimal("5.00"));
        return dto;
    }

    @Test
    void creationDemandeMontant1001Duree12EstBrouillon() throws Exception {
        mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(demande(new BigDecimal("1001"), 12))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("BROUILLON"));
    }

    @Test
    void creationDemandeBornesHautesInclusivesAcceptee() throws Exception {
        mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(demande(new BigDecimal("100000"), 84))))
                .andExpect(status().isCreated());
    }

    @Test
    void creationDemandeMontant1000BorneExclueRejetee() throws Exception {
        mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(demande(new BigDecimal("1000"), 12))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creationDemandeMontant100001Rejetee() throws Exception {
        mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(demande(new BigDecimal("100001"), 12))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creationDemandeDuree11Rejetee() throws Exception {
        mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(demande(new BigDecimal("2000"), 11))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creationDemandeDuree85Rejetee() throws Exception {
        mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(demande(new BigDecimal("2000"), 85))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creationDemandeClientInexistantRetourne404() throws Exception {
        DemandeCreditCreateDto dto = demande(new BigDecimal("2000"), 24);
        dto.setClientId(999999L);
        mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void creationDemandeSansClientIdRejetee400() throws Exception {
        // E20 : demande sans client associe -> clientId null rejete par Bean Validation.
        DemandeCreditCreateDto dto = demande(new BigDecimal("2000"), 24);
        dto.setClientId(null);
        mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CHAMP_OBLIGATOIRE"));
    }

    @Test
    void creationDemandeParResponsableCreditRejetee403() throws Exception {
        // E18 : Responsable credit ne peut pas creer de demande (role reserve CONSEILLER).
        mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + responsableToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(demande(new BigDecimal("2000"), 24))))
                .andExpect(status().isForbidden());
    }

    @Test
    void simulationRetourneMensualiteTauxEtScoreCoherents() throws Exception {
        String body = mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(demande(new BigDecimal("5000"), 24))))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        Long demandeId = node.get("id").asLong();

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/simuler")
                        .header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensualiteEstimee").isNumber())
                .andExpect(jsonPath("$.tauxEndettement").isNumber())
                .andExpect(jsonPath("$.scoreSimplifie").isNotEmpty());
    }

    @Test
    void listerEtConsulterDemande() throws Exception {
        String body = mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(demande(new BigDecimal("5000"), 24))))
                .andReturn().getResponse().getContentAsString();
        Long demandeId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(get("/api/v1/demandes/" + demandeId)
                        .header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("BROUILLON"));

        mockMvc.perform(get("/api/v1/demandes").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

