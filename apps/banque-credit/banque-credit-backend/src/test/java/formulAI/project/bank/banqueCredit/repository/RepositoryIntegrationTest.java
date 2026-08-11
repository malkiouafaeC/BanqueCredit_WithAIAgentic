package formulAI.project.bank.banqueCredit.repository;

import formulAI.project.bank.banqueCredit.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class RepositoryIntegrationTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DemandeCreditRepository demandeCreditRepository;

    @Autowired
    private HistoriqueDecisionRepository historiqueDecisionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void sauvegardeEtLectureClientAvecDemandeEtHistorique() {
        User conseiller = new User();
        conseiller.setUsername("conseiller.test");
        conseiller.setPassword("hash");
        conseiller.setRole(Role.CONSEILLER);
        conseiller = userRepository.save(conseiller);

        Client client = new Client();
        client.setNom("Jean Dupont");
        client.setRevenuMensuel(new BigDecimal("2000.00"));
        client.setChargesMensuelles(new BigDecimal("300.00"));
        client = clientRepository.save(client);

        DemandeCredit demande = new DemandeCredit();
        demande.setClient(client);
        demande.setMontantDemande(new BigDecimal("5000.00"));
        demande.setDureeMois(24);
        demande.setTauxFictif(new BigDecimal("5.00"));
        demande.setStatut(Statut.BROUILLON);
        demande = demandeCreditRepository.save(demande);

        HistoriqueDecision historique = new HistoriqueDecision();
        historique.setDemandeCredit(demande);
        historique.setAncienStatut(null);
        historique.setNouveauStatut(Statut.BROUILLON);
        historique.setAuteur(conseiller);
        historique = historiqueDecisionRepository.save(historique);

        assertThat(clientRepository.findById(client.getId())).isPresent();
        assertThat(demandeCreditRepository.findByClientId(client.getId())).hasSize(1);
        assertThat(historiqueDecisionRepository.findByDemandeCreditIdOrderByDateAsc(demande.getId()))
                .hasSize(1)
                .first()
                .satisfies(h -> {
                    assertThat(h.getAncienStatut()).isNull();
                    assertThat(h.getNouveauStatut()).isEqualTo(Statut.BROUILLON);
                    assertThat(h.getAuteur().getUsername()).isEqualTo("conseiller.test");
                });
        assertThat(userRepository.findByUsername("conseiller.test")).isPresent();
    }

    @Test
    void findByStatutFiltreCorrectement() {
        User responsable = new User();
        responsable.setUsername("resp.test");
        responsable.setPassword("hash");
        responsable.setRole(Role.RESPONSABLE_CREDIT);
        userRepository.save(responsable);

        Client client = new Client();
        client.setNom("Marie Curie");
        client.setRevenuMensuel(new BigDecimal("3000.00"));
        client.setChargesMensuelles(new BigDecimal("500.00"));
        client = clientRepository.save(client);

        DemandeCredit d1 = new DemandeCredit();
        d1.setClient(client);
        d1.setMontantDemande(new BigDecimal("2000.00"));
        d1.setDureeMois(12);
        d1.setTauxFictif(BigDecimal.ZERO);
        d1.setStatut(Statut.BROUILLON);
        demandeCreditRepository.save(d1);

        DemandeCredit d2 = new DemandeCredit();
        d2.setClient(client);
        d2.setMontantDemande(new BigDecimal("3000.00"));
        d2.setDureeMois(24);
        d2.setTauxFictif(BigDecimal.ZERO);
        d2.setStatut(Statut.SOUMISE);
        demandeCreditRepository.save(d2);

        assertThat(demandeCreditRepository.findByStatut(Statut.BROUILLON)).hasSize(1);
        assertThat(demandeCreditRepository.findByStatut(Statut.SOUMISE)).hasSize(1);
        assertThat(demandeCreditRepository.findByStatutNotIn(java.util.List.of(Statut.BROUILLON, Statut.ANNULEE)))
                .hasSize(1);
    }
}

