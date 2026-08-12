package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.dto.HistoriqueDecisionDto;
import formulAI.project.bank.banqueCredit.exception.RessourceNotFoundException;
import formulAI.project.bank.banqueCredit.model.HistoriqueDecision;
import formulAI.project.bank.banqueCredit.model.Statut;
import formulAI.project.bank.banqueCredit.model.User;
import formulAI.project.bank.banqueCredit.repository.DemandeCreditRepository;
import formulAI.project.bank.banqueCredit.repository.HistoriqueDecisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * QA-TASK-006 - Unitaire pur (Mockito) de HistoriqueServiceImpl.
 * Business rule : US-016 (ordre chronologique, tous les champs mappes).
 */
@ExtendWith(MockitoExtension.class)
class HistoriqueServiceImplTest {

    @Mock
    private HistoriqueDecisionRepository historiqueDecisionRepository;
    @Mock
    private DemandeCreditRepository demandeCreditRepository;

    @InjectMocks
    private HistoriqueServiceImpl historiqueService;

    private User auteur;

    @BeforeEach
    void setUp() {
        auteur = new User();
        auteur.setUsername("conseiller1");
    }

    @Test
    void listerParDemande_shouldThrowRessourceNotFoundException_whenDemandeDoesNotExist() {
        when(demandeCreditRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> historiqueService.listerParDemande(99L))
                .isInstanceOf(RessourceNotFoundException.class);
    }

    @Test
    void listerParDemande_shouldReturnEntriesInChronologicalOrderWithAllFieldsMapped() {
        when(demandeCreditRepository.existsById(1L)).thenReturn(true);

        HistoriqueDecision h1 = historiqueEntry(null, Statut.BROUILLON, null, null, Instant.parse("2024-01-01T00:00:00Z"));
        HistoriqueDecision h2 = historiqueEntry(Statut.BROUILLON, Statut.SOUMISE, null, null, Instant.parse("2024-01-02T00:00:00Z"));
        HistoriqueDecision h3 = historiqueEntry(Statut.EN_ANALYSE, Statut.REFUSEE, "Revenu insuffisant", new BigDecimal("40"),
                Instant.parse("2024-01-03T00:00:00Z"));

        when(historiqueDecisionRepository.findByDemandeCreditIdOrderByDateAsc(1L)).thenReturn(List.of(h1, h2, h3));

        List<HistoriqueDecisionDto> result = historiqueService.listerParDemande(1L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getAncienStatut()).isNull();
        assertThat(result.get(0).getNouveauStatut()).isEqualTo(Statut.BROUILLON);
        assertThat(result.get(2).getCommentaire()).isEqualTo("Revenu insuffisant");
        assertThat(result.get(2).getTauxEndettementSnapshot()).isEqualByComparingTo("40");
        assertThat(result.get(2).getAuteur()).isEqualTo("conseiller1");
    }

    @Test
    void listerParDemande_shouldReturnEmptyList_whenNoHistoriqueFound() {
        when(demandeCreditRepository.existsById(1L)).thenReturn(true);
        when(historiqueDecisionRepository.findByDemandeCreditIdOrderByDateAsc(1L)).thenReturn(List.of());

        List<HistoriqueDecisionDto> result = historiqueService.listerParDemande(1L);

        assertThat(result).isEmpty();
    }

    private HistoriqueDecision historiqueEntry(Statut ancien, Statut nouveau, String commentaire, BigDecimal snapshot, Instant date) {
        HistoriqueDecision h = new HistoriqueDecision();
        h.setAncienStatut(ancien);
        h.setNouveauStatut(nouveau);
        h.setCommentaire(commentaire);
        h.setTauxEndettementSnapshot(snapshot);
        h.setAuteur(auteur);
        h.setDate(date);
        return h;
    }
}

