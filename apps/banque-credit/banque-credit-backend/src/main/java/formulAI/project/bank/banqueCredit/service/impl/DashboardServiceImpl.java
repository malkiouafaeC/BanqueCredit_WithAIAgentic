package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.dto.DashboardDto;
import formulAI.project.bank.banqueCredit.model.DemandeCredit;
import formulAI.project.bank.banqueCredit.model.Statut;
import formulAI.project.bank.banqueCredit.repository.DemandeCreditRepository;
import formulAI.project.bank.banqueCredit.service.DashboardService;
import formulAI.project.bank.banqueCredit.service.SimulationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * US-017 - Q4/AC-ARCH-006 : les agregats (montant total, taux moyen) excluent
 * explicitement BROUILLON et ANNULEE.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final List<Statut> EXCLUS_AGREGATS = List.of(Statut.BROUILLON, Statut.ANNULEE);

    private final DemandeCreditRepository demandeCreditRepository;
    private final SimulationService simulationService;

    public DashboardServiceImpl(DemandeCreditRepository demandeCreditRepository, SimulationService simulationService) {
        this.demandeCreditRepository = demandeCreditRepository;
        this.simulationService = simulationService;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDto consulterSynthese() {
        long nbSoumises = demandeCreditRepository.findByStatut(Statut.SOUMISE).size();
        long nbEnAnalyse = demandeCreditRepository.findByStatut(Statut.EN_ANALYSE).size();
        long nbAcceptees = demandeCreditRepository.findByStatut(Statut.ACCEPTEE).size();
        long nbRefusees = demandeCreditRepository.findByStatut(Statut.REFUSEE).size();

        List<DemandeCredit> perimetreAgregats = demandeCreditRepository.findByStatutNotIn(EXCLUS_AGREGATS);

        BigDecimal montantTotal = perimetreAgregats.stream()
                .map(DemandeCredit::getMontantDemande)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tauxMoyen = BigDecimal.ZERO;
        if (!perimetreAgregats.isEmpty()) {
            BigDecimal sommeTaux = perimetreAgregats.stream()
                    .map(d -> simulationService.simuler(d.getMontantDemande(), d.getDureeMois(), d.getTauxFictif(),
                                    d.getClient().getChargesMensuelles(), d.getClient().getRevenuMensuel())
                            .getTauxEndettement())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            tauxMoyen = sommeTaux.divide(new BigDecimal(perimetreAgregats.size()), 2, RoundingMode.HALF_UP);
        }

        return new DashboardDto(nbSoumises, nbEnAnalyse, nbAcceptees, nbRefusees, montantTotal, tauxMoyen);
    }
}

