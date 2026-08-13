package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.dto.DemandeCreditDto;
import formulAI.project.bank.banqueCredit.exception.CommentaireObligatoireException;
import formulAI.project.bank.banqueCredit.exception.EligibiliteNonRespecteeException;
import formulAI.project.bank.banqueCredit.exception.RessourceNotFoundException;
import formulAI.project.bank.banqueCredit.exception.RoleNonAutoriseException;
import formulAI.project.bank.banqueCredit.exception.TransitionInvalideException;
import formulAI.project.bank.banqueCredit.model.*;
import formulAI.project.bank.banqueCredit.repository.DemandeCreditRepository;
import formulAI.project.bank.banqueCredit.repository.HistoriqueDecisionRepository;
import formulAI.project.bank.banqueCredit.repository.UserRepository;
import formulAI.project.bank.banqueCredit.service.EligibiliteService;
import formulAI.project.bank.banqueCredit.service.SimulationService;
import formulAI.project.bank.banqueCredit.dto.SimulationResultDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * QA-TASK-003 - Unitaire pur (Mockito) de DecisionServiceImpl : les 5 transitions de statut,
 * isolees de la persistance/simulation reelles.
 * Business rules : RG-BANK-05 (transitions), RG-BANK-06 (eligibilite deleguee), RG-BANK-07 (commentaire refus).
 */
@ExtendWith(MockitoExtension.class)
class DecisionServiceImplTest {

    @Mock
    private DemandeCreditRepository demandeCreditRepository;
    @Mock
    private HistoriqueDecisionRepository historiqueDecisionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SimulationService simulationService;
    @Mock
    private EligibiliteService eligibiliteService;

    @InjectMocks
    private DecisionServiceImpl decisionService;

    private Client client;
    private User conseiller;
    private User responsable;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setRevenuMensuel(new BigDecimal("2000"));
        client.setChargesMensuelles(new BigDecimal("300"));

        conseiller = new User();
        conseiller.setUsername("conseiller1");
        conseiller.setRole(Role.CONSEILLER);

        responsable = new User();
        responsable.setUsername("responsable1");
        responsable.setRole(Role.RESPONSABLE_CREDIT);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private DemandeCredit demande(Statut statut) {
        DemandeCredit d = new DemandeCredit();
        d.setId(1L);
        d.setClient(client);
        d.setMontantDemande(new BigDecimal("5000"));
        d.setDureeMois(24);
        d.setTauxFictif(new BigDecimal("5"));
        d.setStatut(statut);
        return d;
    }

    /** Authentifie l'utilisateur donne dans le SecurityContext ET stubbe son lookup par TransitionRules/currentUser(). */
    private void authenticateAs(User user) {
        TestingAuthenticationToken token = new TestingAuthenticationToken(user.getUsername(), null);
        token.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(token);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }

    private void stubSave() {
        when(demandeCreditRepository.save(any(DemandeCredit.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---- soumettre (RG-BANK-05) ----

    @Test
    void soumettre_shouldTransitionToSoumiseAndCreateHistorique_whenBrouillon() {
        DemandeCredit d = demande(Statut.BROUILLON);
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(d));
        authenticateAs(conseiller);
        stubSave();
        stubSimulation();

        DemandeCreditDto result = decisionService.soumettre(1L);

        assertThat(result.getStatut()).isEqualTo(Statut.SOUMISE);
        ArgumentCaptor<HistoriqueDecision> captor = ArgumentCaptor.forClass(HistoriqueDecision.class);
        verify(historiqueDecisionRepository).save(captor.capture());
        assertThat(captor.getValue().getAncienStatut()).isEqualTo(Statut.BROUILLON);
        assertThat(captor.getValue().getNouveauStatut()).isEqualTo(Statut.SOUMISE);
    }

    @ParameterizedTest
    @EnumSource(value = Statut.class, names = {"SOUMISE", "EN_ANALYSE", "ACCEPTEE", "REFUSEE", "ANNULEE"})
    void soumettre_shouldThrowTransitionInvalideException_whenNotBrouillon(Statut statut) {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(statut)));

        assertThatThrownBy(() -> decisionService.soumettre(1L)).isInstanceOf(TransitionInvalideException.class);
        verifyNoInteractions(historiqueDecisionRepository);
    }

    @Test
    void soumettre_shouldThrowRoleNonAutoriseException_whenRoleIsResponsableCredit() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.BROUILLON)));
        authenticateAs(responsable);

        assertThatThrownBy(() -> decisionService.soumettre(1L)).isInstanceOf(RoleNonAutoriseException.class);
        verify(demandeCreditRepository, never()).save(any());
        verifyNoInteractions(historiqueDecisionRepository);
    }

    // ---- annuler (RG-BANK-05, E14/E15) ----

    @Test
    void annuler_shouldTransitionToAnnulee_whenBrouillon() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.BROUILLON)));
        authenticateAs(conseiller);
        stubSave();
        stubSimulation();

        DemandeCreditDto result = decisionService.annuler(1L);

        assertThat(result.getStatut()).isEqualTo(Statut.ANNULEE);
    }

    @Test
    void annuler_shouldTransitionToAnnulee_whenSoumise() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.SOUMISE)));
        authenticateAs(conseiller);
        stubSave();
        stubSimulation();

        DemandeCreditDto result = decisionService.annuler(1L);

        assertThat(result.getStatut()).isEqualTo(Statut.ANNULEE);
    }

    @ParameterizedTest
    @EnumSource(value = Statut.class, names = {"EN_ANALYSE", "ACCEPTEE", "REFUSEE"})
    void annuler_shouldThrowTransitionInvalideException_whenEnAnalyseOrDecided(Statut statut) {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(statut)));

        assertThatThrownBy(() -> decisionService.annuler(1L)).isInstanceOf(TransitionInvalideException.class);
    }

    @Test
    void annuler_shouldThrowRoleNonAutoriseException_whenRoleIsResponsableCredit() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.BROUILLON)));
        authenticateAs(responsable);

        assertThatThrownBy(() -> decisionService.annuler(1L)).isInstanceOf(RoleNonAutoriseException.class);
        verify(demandeCreditRepository, never()).save(any());
    }

    // ---- analyser (RG-BANK-05) ----

    @Test
    void analyser_shouldTransitionToEnAnalyse_whenSoumise() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.SOUMISE)));
        authenticateAs(responsable);
        stubSave();
        stubSimulation();

        DemandeCreditDto result = decisionService.analyser(1L);

        assertThat(result.getStatut()).isEqualTo(Statut.EN_ANALYSE);
    }

    @Test
    void analyser_shouldThrowTransitionInvalideException_whenNotSoumise() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.BROUILLON)));

        assertThatThrownBy(() -> decisionService.analyser(1L)).isInstanceOf(TransitionInvalideException.class);
    }

    @Test
    void analyser_shouldThrowRoleNonAutoriseException_whenRoleIsConseiller() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.SOUMISE)));
        authenticateAs(conseiller);

        assertThatThrownBy(() -> decisionService.analyser(1L)).isInstanceOf(RoleNonAutoriseException.class);
        verify(demandeCreditRepository, never()).save(any());
    }

    // ---- accepter (RG-BANK-05, RG-BANK-06) ----

    @Test
    void accepter_shouldTransitionToAccepteeAndSnapshotTaux_whenEligible() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.EN_ANALYSE)));
        authenticateAs(responsable);
        stubSave();
        stubSimulation();
        doNothing().when(eligibiliteService).verifierEligibilite(any(), any(), any());

        DemandeCreditDto result = decisionService.accepter(1L);

        assertThat(result.getStatut()).isEqualTo(Statut.ACCEPTEE);
        verify(eligibiliteService).verifierEligibilite(BigDecimal.TEN, client.getRevenuMensuel(), new BigDecimal("5000"));
        ArgumentCaptor<HistoriqueDecision> captor = ArgumentCaptor.forClass(HistoriqueDecision.class);
        verify(historiqueDecisionRepository).save(captor.capture());
        assertThat(captor.getValue().getTauxEndettementSnapshot()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void accepter_shouldThrowTransitionInvalideException_whenNotEnAnalyse() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.BROUILLON)));

        assertThatThrownBy(() -> decisionService.accepter(1L)).isInstanceOf(TransitionInvalideException.class);
        verifyNoInteractions(eligibiliteService);
    }

    @Test
    void accepter_shouldThrowRoleNonAutoriseException_whenRoleIsConseiller() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.EN_ANALYSE)));
        authenticateAs(conseiller);

        assertThatThrownBy(() -> decisionService.accepter(1L)).isInstanceOf(RoleNonAutoriseException.class);
        verifyNoInteractions(eligibiliteService);
        verify(demandeCreditRepository, never()).save(any());
    }

    @Test
    void accepter_shouldPropagateEligibiliteNonRespecteeException_whenNotEligible() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.EN_ANALYSE)));
        authenticateAs(responsable);
        stubSimulation();
        doThrow(new EligibiliteNonRespecteeException("Acceptation impossible : taux d'endettement superieur a 35%"))
                .when(eligibiliteService).verifierEligibilite(any(), any(), any());

        assertThatThrownBy(() -> decisionService.accepter(1L)).isInstanceOf(EligibiliteNonRespecteeException.class);
        verify(demandeCreditRepository, never()).save(any());
    }

    // ---- refuser (RG-BANK-05, RG-BANK-07) ----

    @Test
    void refuser_shouldTransitionToRefuseeWithCommentaire_whenValidComment() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.EN_ANALYSE)));
        authenticateAs(responsable);
        stubSave();
        stubSimulation();

        DemandeCreditDto result = decisionService.refuser(1L, "Revenu insuffisant");

        assertThat(result.getStatut()).isEqualTo(Statut.REFUSEE);
        assertThat(result.getCommentaireDecision()).isEqualTo("Revenu insuffisant");
    }

    @Test
    void refuser_shouldThrowCommentaireObligatoireException_whenCommentIsNull() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.EN_ANALYSE)));
        authenticateAs(responsable);

        assertThatThrownBy(() -> decisionService.refuser(1L, null)).isInstanceOf(CommentaireObligatoireException.class);
    }

    @Test
    void refuser_shouldThrowCommentaireObligatoireException_whenCommentIsBlank() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.EN_ANALYSE)));
        authenticateAs(responsable);

        assertThatThrownBy(() -> decisionService.refuser(1L, "   ")).isInstanceOf(CommentaireObligatoireException.class);
    }

    @Test
    void refuser_shouldThrowTransitionInvalideException_whenNotEnAnalyse() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.SOUMISE)));

        assertThatThrownBy(() -> decisionService.refuser(1L, "motif")).isInstanceOf(TransitionInvalideException.class);
    }

    @Test
    void refuser_shouldThrowRoleNonAutoriseException_whenRoleIsConseiller() {
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(demande(Statut.EN_ANALYSE)));
        authenticateAs(conseiller);

        assertThatThrownBy(() -> decisionService.refuser(1L, "motif")).isInstanceOf(RoleNonAutoriseException.class);
        verify(demandeCreditRepository, never()).save(any());
    }

    @Test
    void anyTransition_shouldThrowRessourceNotFoundException_whenDemandeDoesNotExist() {
        when(demandeCreditRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> decisionService.soumettre(404L)).isInstanceOf(RessourceNotFoundException.class);
    }

    private void stubSimulation() {
        when(simulationService.simuler(any(), any(), any(), any(), any()))
                .thenReturn(new SimulationResultDto(new BigDecimal("200"), BigDecimal.TEN, ScoreSimplifie.EXCELLENT));
    }
}

