package formulAI.project.bank.banqueCredit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import formulAI.project.bank.banqueCredit.dto.*;
import formulAI.project.bank.banqueCredit.repository.HistoriqueDecisionRepository;
import formulAI.project.bank.banqueCredit.service.impl.SimulationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class DecisionControllerIntegrationTest {

    private static final SimulationServiceImpl SIMULATION = new SimulationServiceImpl();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HistoriqueDecisionRepository historiqueDecisionRepository;

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

    private Long creerClient(BigDecimal revenu, BigDecimal charges) throws Exception {
        ClientCreateDto dto = new ClientCreateDto();
        dto.setNom("Client Decision");
        dto.setRevenuMensuel(revenu);
        dto.setChargesMensuelles(charges);
        String body = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long creerDemande(Long clientId, BigDecimal montant, Integer duree, BigDecimal taux) throws Exception {
        DemandeCreditCreateDto dto = new DemandeCreditCreateDto();
        dto.setClientId(clientId);
        dto.setMontantDemande(montant);
        dto.setDureeMois(duree);
        dto.setTauxFictif(taux);
        String body = mockMvc.perform(post("/api/v1/demandes")
                        .header("Authorization", "Bearer " + conseillerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private void soumettre(Long id) throws Exception {
        mockMvc.perform(post("/api/v1/demandes/" + id + "/soumettre").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk());
    }

    private void analyser(Long id) throws Exception {
        mockMvc.perform(post("/api/v1/demandes/" + id + "/analyser").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isOk());
    }

    // ---- Soumission (US-010) ----

    @Test
    void soumettreDemandeBrouillonPasseASoumiseEtHistorise() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), new BigDecimal("300"));
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, new BigDecimal("5"));

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/soumettre").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("SOUMISE"))
                .andExpect(jsonPath("$.dateSoumission").isNotEmpty());

        assertThat(historiqueDecisionRepository.findByDemandeCreditIdOrderByDateAsc(demandeId)).hasSize(2);
    }

    @Test
    void doubleSoumissionRejetee409() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), new BigDecimal("300"));
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, new BigDecimal("5"));
        soumettre(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/soumettre").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSITION_INVALIDE"));
    }

    // ---- Annulation (US-011) ----

    @Test
    void annulerDepuisBrouillonOk() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), new BigDecimal("300"));
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, new BigDecimal("5"));

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/annuler").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULEE"));
    }

    @Test
    void annulerDepuisSoumiseOk() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), new BigDecimal("300"));
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, new BigDecimal("5"));
        soumettre(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/annuler").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULEE"));
    }

    @Test
    void annulerDepuisEnAnalyseRejetee409() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), new BigDecimal("300"));
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, new BigDecimal("5"));
        soumettre(demandeId);
        analyser(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/annuler").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isConflict());
    }

    @Test
    void annulerParResponsableCreditRejetee403() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), new BigDecimal("300"));
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, new BigDecimal("5"));

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/annuler").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isForbidden());
    }

    // ---- Passage en analyse (US-012) ----

    @Test
    void analyserDepuisSoumiseOk() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), new BigDecimal("300"));
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, new BigDecimal("5"));
        soumettre(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/analyser").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_ANALYSE"));
    }

    @Test
    void analyserDepuisBrouillonRejetee409() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), new BigDecimal("300"));
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, new BigDecimal("5"));

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/analyser").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isConflict());
    }

    @Test
    void analyserParConseillerRejetee403() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), new BigDecimal("300"));
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, new BigDecimal("5"));
        soumettre(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/analyser").header("Authorization", "Bearer " + conseillerToken))
                .andExpect(status().isForbidden());
    }

    // ---- Acceptation (US-013, RG-BANK-06) ----

    @Test
    void accepterAvecTauxEndettementExactement35PourcentEstAccepte() throws Exception {
        // Borne haute inclusive RG-BANK-06 (1) : tauxEndettement = 35% exactement -> eligible.
        BigDecimal revenu = new BigDecimal("2000");
        BigDecimal montant = new BigDecimal("5000");
        int duree = 24;
        BigDecimal taux = BigDecimal.ZERO;
        BigDecimal mensualite = SIMULATION.calculerMensualite(montant, duree, taux);
        BigDecimal charges = revenu.multiply(new BigDecimal("0.35")).subtract(mensualite);

        Long clientId = creerClient(revenu, charges);
        Long demandeId = creerDemande(clientId, montant, duree, taux);
        soumettre(demandeId);
        analyser(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/accepter").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ACCEPTEE"))
                .andExpect(jsonPath("$.dateDecision").isNotEmpty());
    }

    @Test
    void accepterAvecRevenuExactement1500EstAccepte() throws Exception {
        // Borne basse inclusive RG-BANK-06 (2) : revenuMensuel = 1500 exactement -> eligible.
        Long clientId = creerClient(new BigDecimal("1500"), BigDecimal.ZERO);
        Long demandeId = creerDemande(clientId, new BigDecimal("2000"), 24, BigDecimal.ZERO);
        soumettre(demandeId);
        analyser(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/accepter").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ACCEPTEE"));
    }

    @Test
    void accepterAvecMontantExactement50000EstAccepte() throws Exception {
        // Borne haute inclusive RG-BANK-06 (3) : montantDemande = 50000 exactement -> eligible.
        // Note (deviation documentee) : combiner simultanement les 3 bornes exactes de l'AC-013-1
        // (taux=35% ET revenu=1500 ET montant=50000) est mathematiquement infaisable sous les
        // contraintes RG-BANK-02/03 (mensualite minimale a montant=50000/duree=84/taux=0 = ~595.24,
        // ce qui donne deja ~39.7% de taux d'endettement avec revenu=1500 et charges=0, superieur a 35%).
        // Chaque borne est donc verifiee independamment, en gardant les 2 autres conditions tres larges.
        Long clientId = creerClient(new BigDecimal("100000"), BigDecimal.ZERO);
        Long demandeId = creerDemande(clientId, new BigDecimal("50000"), 84, BigDecimal.ZERO);
        soumettre(demandeId);
        analyser(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/accepter").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ACCEPTEE"));
    }

    @Test
    void accepterAvecTauxEndettementSuperieur35Rejete422() throws Exception {
        // charges tres elevees -> taux d'endettement > 35%
        Long clientId = creerClient(new BigDecimal("2000"), new BigDecimal("1900"));
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, new BigDecimal("5"));
        soumettre(demandeId);
        analyser(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/accepter").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ELIGIBILITE_NON_RESPECTEE"));
    }

    @Test
    void accepterAvecRevenu1499Rejete422() throws Exception {
        Long clientId = creerClient(new BigDecimal("1499"), BigDecimal.ZERO);
        Long demandeId = creerDemande(clientId, new BigDecimal("2000"), 24, BigDecimal.ZERO);
        soumettre(demandeId);
        analyser(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/accepter").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ELIGIBILITE_NON_RESPECTEE"));
    }

    @Test
    void accepterAvecMontant50001Rejete422() throws Exception {
        Long clientId = creerClient(new BigDecimal("10000"), BigDecimal.ZERO);
        Long demandeId = creerDemande(clientId, new BigDecimal("50001"), 84, BigDecimal.ZERO);
        soumettre(demandeId);
        analyser(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/accepter").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ELIGIBILITE_NON_RESPECTEE"));
    }

    @Test
    void accepterSurStatutAutreQueEnAnalyseRejete409() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), BigDecimal.ZERO);
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, BigDecimal.ZERO);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/accepter").header("Authorization", "Bearer " + responsableToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSITION_INVALIDE"));
    }

    // ---- Refus (US-014/015, RG-BANK-07) ----

    @Test
    void refuserAvecCommentaireValideEstRefusee() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), BigDecimal.ZERO);
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, BigDecimal.ZERO);
        soumettre(demandeId);
        analyser(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/refuser")
                        .header("Authorization", "Bearer " + responsableToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DecisionRefusDto("Revenu insuffisant"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("REFUSEE"))
                .andExpect(jsonPath("$.commentaireDecision").value("Revenu insuffisant"));
    }

    @Test
    void refuserSansCommentaireRejete400() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), BigDecimal.ZERO);
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, BigDecimal.ZERO);
        soumettre(demandeId);
        analyser(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/refuser")
                        .header("Authorization", "Bearer " + responsableToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DecisionRefusDto(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMENTAIRE_OBLIGATOIRE"));
    }

    @Test
    void refuserAvecCommentaireEspacesRejete400() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), BigDecimal.ZERO);
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, BigDecimal.ZERO);
        soumettre(demandeId);
        analyser(demandeId);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/refuser")
                        .header("Authorization", "Bearer " + responsableToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DecisionRefusDto("   "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMENTAIRE_OBLIGATOIRE"));
    }

    @Test
    void refuserSurStatutAutreQueEnAnalyseRejete409() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), BigDecimal.ZERO);
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, BigDecimal.ZERO);

        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/refuser")
                        .header("Authorization", "Bearer " + responsableToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DecisionRefusDto("commentaire"))))
                .andExpect(status().isConflict());
    }

    // ---- Tracabilite historique (auteur + date) ----

    @Test
    void chaqueTransitionReussieCreeUneHistoriqueAvecAuteurEtDate() throws Exception {
        Long clientId = creerClient(new BigDecimal("2000"), BigDecimal.ZERO);
        Long demandeId = creerDemande(clientId, new BigDecimal("5000"), 24, BigDecimal.ZERO);
        soumettre(demandeId);
        analyser(demandeId);
        mockMvc.perform(post("/api/v1/demandes/" + demandeId + "/refuser")
                        .header("Authorization", "Bearer " + responsableToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DecisionRefusDto("non eligible"))))
                .andExpect(status().isOk());

        var historique = historiqueDecisionRepository.findByDemandeCreditIdOrderByDateAsc(demandeId);
        assertThat(historique).hasSize(4); // creation, soumission, analyse, refus
        historique.forEach(h -> {
            assertThat(h.getAuteur()).isNotNull();
            assertThat(h.getDate()).isNotNull();
        });
        assertThat(historique.get(3).getCommentaire()).isEqualTo("non eligible");
        assertThat(historique.get(3).getAuteur().getUsername()).isEqualTo("responsable1");
    }
}

