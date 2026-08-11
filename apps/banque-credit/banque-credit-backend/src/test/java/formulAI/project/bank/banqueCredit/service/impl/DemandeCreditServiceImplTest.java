package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.dto.DemandeCreditCreateDto;
import formulAI.project.bank.banqueCredit.dto.DemandeCreditDto;
import formulAI.project.bank.banqueCredit.dto.DemandeCreditSummaryDto;
import formulAI.project.bank.banqueCredit.exception.RessourceNotFoundException;
import formulAI.project.bank.banqueCredit.model.*;
import formulAI.project.bank.banqueCredit.repository.ClientRepository;
import formulAI.project.bank.banqueCredit.repository.DemandeCreditRepository;
import formulAI.project.bank.banqueCredit.repository.HistoriqueDecisionRepository;
import formulAI.project.bank.banqueCredit.repository.UserRepository;
import formulAI.project.bank.banqueCredit.service.SimulationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * QA-TASK-002 - Unitaire pur (Mockito) de DemandeCreditServiceImpl.
 * Business rules : RG-BANK-02/03 (delegation, bornes deja verifiees par Bean Validation en amont),
 * Q2/DEC-ENT-005 (historique initial cree a la creation).
 */
@ExtendWith(MockitoExtension.class)
class DemandeCreditServiceImplTest {

    @Mock
    private DemandeCreditRepository demandeCreditRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private HistoriqueDecisionRepository historiqueDecisionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SimulationService simulationService;

    @InjectMocks
    private DemandeCreditServiceImpl demandeCreditService;

    private Client client;
    private User auteur;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setNom("Dupont");
        client.setRevenuMensuel(new BigDecimal("2000"));
        client.setChargesMensuelles(new BigDecimal("300"));

        auteur = new User();
        auteur.setId(1L);
        auteur.setUsername("conseiller1");
        auteur.setRole(Role.CONSEILLER);

        TestingAuthenticationToken token = new TestingAuthenticationToken("conseiller1", null);
        token.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void creer_shouldThrowRessourceNotFoundException_whenClientDoesNotExist() {
        DemandeCreditCreateDto request = new DemandeCreditCreateDto();
        request.setClientId(99L);
        request.setMontantDemande(new BigDecimal("5000"));
        request.setDureeMois(24);
        request.setTauxFictif(new BigDecimal("5"));

        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> demandeCreditService.creer(request))
                .isInstanceOf(RessourceNotFoundException.class);
        verifyNoInteractions(demandeCreditRepository, historiqueDecisionRepository);
    }

    @Test
    void creer_shouldPersistBrouillonAndInitialHistoriqueWithNullAncienStatut_whenClientExists() {
        DemandeCreditCreateDto request = new DemandeCreditCreateDto();
        request.setClientId(1L);
        request.setMontantDemande(new BigDecimal("5000"));
        request.setDureeMois(24);
        request.setTauxFictif(new BigDecimal("5"));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(demandeCreditRepository.save(any(DemandeCredit.class))).thenAnswer(invocation -> {
            DemandeCredit d = invocation.getArgument(0);
            d.setId(10L);
            return d;
        });
        when(userRepository.findByUsername("conseiller1")).thenReturn(Optional.of(auteur));
        when(simulationService.simuler(any(), any(), any(), any(), any()))
                .thenReturn(new SimulationResultDto(BigDecimal.TEN, BigDecimal.ONE, ScoreSimplifie.EXCELLENT));

        DemandeCreditDto result = demandeCreditService.creer(request);

        assertThat(result.getStatut()).isEqualTo(Statut.BROUILLON);

        ArgumentCaptor<HistoriqueDecision> historiqueCaptor = ArgumentCaptor.forClass(HistoriqueDecision.class);
        verify(historiqueDecisionRepository).save(historiqueCaptor.capture());
        assertThat(historiqueCaptor.getValue().getAncienStatut()).isNull();
        assertThat(historiqueCaptor.getValue().getNouveauStatut()).isEqualTo(Statut.BROUILLON);
        assertThat(historiqueCaptor.getValue().getAuteur()).isEqualTo(auteur);
    }

    @Test
    void lister_shouldFilterByStatutAndClientId_whenBothProvided() {
        DemandeCredit d1 = demande(1L, Statut.SOUMISE);
        DemandeCredit d2 = demande(2L, Statut.BROUILLON);
        when(demandeCreditRepository.findByClientId(1L)).thenReturn(List.of(d1, d2));

        List<DemandeCreditSummaryDto> result = demandeCreditService.lister(Statut.SOUMISE, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(demandeCreditRepository, never()).findByStatut(any());
        verify(demandeCreditRepository, never()).findAll();
    }

    @Test
    void lister_shouldFilterByStatutOnly_whenOnlyStatutProvided() {
        when(demandeCreditRepository.findByStatut(Statut.EN_ANALYSE)).thenReturn(List.of(demande(3L, Statut.EN_ANALYSE)));

        List<DemandeCreditSummaryDto> result = demandeCreditService.lister(Statut.EN_ANALYSE, null);

        assertThat(result).hasSize(1);
        verify(demandeCreditRepository).findByStatut(Statut.EN_ANALYSE);
    }

    @Test
    void lister_shouldFilterByClientIdOnly_whenOnlyClientIdProvided() {
        when(demandeCreditRepository.findByClientId(1L)).thenReturn(List.of(demande(4L, Statut.BROUILLON)));

        List<DemandeCreditSummaryDto> result = demandeCreditService.lister(null, 1L);

        assertThat(result).hasSize(1);
        verify(demandeCreditRepository).findByClientId(1L);
    }

    @Test
    void lister_shouldReturnAllDemandes_whenNoFilterProvided() {
        when(demandeCreditRepository.findAll()).thenReturn(List.of(demande(5L, Statut.ACCEPTEE)));

        List<DemandeCreditSummaryDto> result = demandeCreditService.lister(null, null);

        assertThat(result).hasSize(1);
        verify(demandeCreditRepository).findAll();
    }

    @Test
    void consulter_shouldThrowRessourceNotFoundException_whenDemandeDoesNotExist() {
        when(demandeCreditRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> demandeCreditService.consulter(42L))
                .isInstanceOf(RessourceNotFoundException.class);
    }

    @Test
    void simuler_shouldDelegateToSimulationServiceWithDemandeAndClientData() {
        DemandeCredit d = demande(1L, Statut.BROUILLON);
        d.setClient(client);
        d.setMontantDemande(new BigDecimal("5000"));
        d.setDureeMois(24);
        d.setTauxFictif(new BigDecimal("5"));
        when(demandeCreditRepository.findById(1L)).thenReturn(Optional.of(d));
        SimulationResultDto expected = new SimulationResultDto(BigDecimal.TEN, BigDecimal.ONE, ScoreSimplifie.BON);
        when(simulationService.simuler(new BigDecimal("5000"), 24, new BigDecimal("5"),
                client.getChargesMensuelles(), client.getRevenuMensuel())).thenReturn(expected);

        SimulationResultDto result = demandeCreditService.simuler(1L);

        assertThat(result).isEqualTo(expected);
    }

    private DemandeCredit demande(Long id, Statut statut) {
        DemandeCredit d = new DemandeCredit();
        d.setId(id);
        d.setClient(client);
        d.setMontantDemande(new BigDecimal("5000"));
        d.setDureeMois(24);
        d.setTauxFictif(new BigDecimal("5"));
        d.setStatut(statut);
        return d;
    }
}

