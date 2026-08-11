package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.dto.DashboardDto;
import formulAI.project.bank.banqueCredit.model.Client;
import formulAI.project.bank.banqueCredit.model.DemandeCredit;
import formulAI.project.bank.banqueCredit.model.ScoreSimplifie;
import formulAI.project.bank.banqueCredit.model.Statut;
import formulAI.project.bank.banqueCredit.repository.DemandeCreditRepository;
import formulAI.project.bank.banqueCredit.service.SimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * QA-TASK-005 - Unitaire pur (Mockito) de DashboardServiceImpl.
 * Couvre US-017/AC-017-1/AC-017-4 et AC-ARCH-006 (Q4 : exclusion BROUILLON/ANNULEE des agregats).
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private DemandeCreditRepository demandeCreditRepository;
    @Mock
    private SimulationService simulationService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setRevenuMensuel(new BigDecimal("2000"));
        client.setChargesMensuelles(new BigDecimal("300"));
    }

    @Test
    void consulterSynthese_shouldReturnNeutralValues_whenNoData_AC017_4() {
        when(demandeCreditRepository.findByStatut(any())).thenReturn(List.of());
        when(demandeCreditRepository.findByStatutNotIn(any())).thenReturn(List.of());

        DashboardDto result = dashboardService.consulterSynthese();

        assertThat(result.getNbSoumises()).isZero();
        assertThat(result.getNbEnAnalyse()).isZero();
        assertThat(result.getNbAcceptees()).isZero();
        assertThat(result.getNbRefusees()).isZero();
        assertThat(result.getMontantTotalDemande()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTauxMoyenEndettement()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void consulterSynthese_shouldReturnExactCounters_whenDemandesSpreadAcrossStatuts_AC017_1() {
        when(demandeCreditRepository.findByStatut(Statut.SOUMISE)).thenReturn(List.of(demande(Statut.SOUMISE)));
        when(demandeCreditRepository.findByStatut(Statut.EN_ANALYSE)).thenReturn(List.of(demande(Statut.EN_ANALYSE), demande(Statut.EN_ANALYSE)));
        when(demandeCreditRepository.findByStatut(Statut.ACCEPTEE)).thenReturn(List.of(demande(Statut.ACCEPTEE)));
        when(demandeCreditRepository.findByStatut(Statut.REFUSEE)).thenReturn(List.of());
        when(demandeCreditRepository.findByStatutNotIn(any())).thenReturn(List.of());

        DashboardDto result = dashboardService.consulterSynthese();

        assertThat(result.getNbSoumises()).isEqualTo(1);
        assertThat(result.getNbEnAnalyse()).isEqualTo(2);
        assertThat(result.getNbAcceptees()).isEqualTo(1);
        assertThat(result.getNbRefusees()).isZero();
    }

    @Test
    void consulterSynthese_shouldExcludeBrouillonAndAnnulee_fromAggregates_ACARCH006() {
        when(demandeCreditRepository.findByStatut(any())).thenReturn(List.of());
        when(demandeCreditRepository.findByStatutNotIn(any())).thenReturn(List.of());

        dashboardService.consulterSynthese();

        ArgumentCaptor<List<Statut>> captor = ArgumentCaptor.forClass(List.class);
        verify(demandeCreditRepository).findByStatutNotIn(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrder(Statut.BROUILLON, Statut.ANNULEE);
    }

    @Test
    void consulterSynthese_shouldComputeSumAndRoundedAverage_whenPerimetreNonEmpty() {
        DemandeCredit d1 = demande(Statut.SOUMISE);
        d1.setMontantDemande(new BigDecimal("5000"));
        DemandeCredit d2 = demande(Statut.ACCEPTEE);
        d2.setMontantDemande(new BigDecimal("3000"));

        when(demandeCreditRepository.findByStatut(any())).thenReturn(List.of());
        when(demandeCreditRepository.findByStatutNotIn(any())).thenReturn(List.of(d1, d2));
        when(simulationService.simuler(eq(new BigDecimal("5000")), any(), any(), any(), any()))
                .thenReturn(new SimulationResultDto(BigDecimal.TEN, new BigDecimal("20"), ScoreSimplifie.EXCELLENT));
        when(simulationService.simuler(eq(new BigDecimal("3000")), any(), any(), any(), any()))
                .thenReturn(new SimulationResultDto(BigDecimal.TEN, new BigDecimal("31"), ScoreSimplifie.BON));

        DashboardDto result = dashboardService.consulterSynthese();

        assertThat(result.getMontantTotalDemande()).isEqualByComparingTo("8000");
        assertThat(result.getTauxMoyenEndettement()).isEqualByComparingTo("25.50");
    }

    private DemandeCredit demande(Statut statut) {
        DemandeCredit d = new DemandeCredit();
        d.setClient(client);
        d.setMontantDemande(new BigDecimal("5000"));
        d.setDureeMois(24);
        d.setTauxFictif(new BigDecimal("5"));
        d.setStatut(statut);
        return d;
    }
}

