package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.model.ScoreSimplifie;
import formulAI.project.bank.banqueCredit.service.SimulationService;
import org.springframework.stereotype.Service;
import formulAI.project.bank.banqueCredit.dto.SimulationResultDto;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * RG-BANK-04/RG-BANK-08 - Implementation normative, sans reinterpretation.
 */
@Service
public class SimulationServiceImpl implements SimulationService {

    private static final MathContext MC = new MathContext(20);
    private static final BigDecimal CENT = new BigDecimal("100");
    private static final BigDecimal DOUZE = new BigDecimal("12");

    @Override
    public BigDecimal calculerMensualite(BigDecimal montantDemande, Integer dureeMois, BigDecimal tauxFictif) {
        if (tauxFictif.compareTo(BigDecimal.ZERO) == 0) {
            // Cas particulier : amortissement lineaire sans interet.
            return montantDemande.divide(new BigDecimal(dureeMois), 2, RoundingMode.HALF_UP);
        }
        BigDecimal tauxMensuel = tauxFictif.divide(CENT, MC).divide(DOUZE, MC);
        BigDecimal base = BigDecimal.ONE.add(tauxMensuel, MC);
        BigDecimal basePowN = base.pow(dureeMois, MC);
        BigDecimal inversePow = BigDecimal.ONE.divide(basePowN, MC);
        BigDecimal denominateur = BigDecimal.ONE.subtract(inversePow, MC);
        BigDecimal mensualite = montantDemande.multiply(tauxMensuel, MC).divide(denominateur, MC);
        return mensualite.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculerTauxEndettement(BigDecimal chargesMensuelles, BigDecimal mensualiteEstimee, BigDecimal revenuMensuel) {
        BigDecimal ratio = chargesMensuelles.add(mensualiteEstimee, MC)
                .divide(revenuMensuel, MC)
                .multiply(CENT, MC);
        return ratio.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public ScoreSimplifie calculerScore(BigDecimal tauxEndettementPourcentage) {
        if (tauxEndettementPourcentage.compareTo(new BigDecimal("20")) <= 0) {
            return ScoreSimplifie.EXCELLENT;
        }
        if (tauxEndettementPourcentage.compareTo(new BigDecimal("35")) <= 0) {
            return ScoreSimplifie.BON;
        }
        if (tauxEndettementPourcentage.compareTo(new BigDecimal("50")) <= 0) {
            return ScoreSimplifie.MOYEN;
        }
        return ScoreSimplifie.FAIBLE;
    }

    @Override
    public SimulationResultDto simuler(BigDecimal montantDemande, Integer dureeMois, BigDecimal tauxFictif,
                                        BigDecimal chargesMensuelles, BigDecimal revenuMensuel) {
        BigDecimal mensualite = calculerMensualite(montantDemande, dureeMois, tauxFictif);
        BigDecimal tauxEndettement = calculerTauxEndettement(chargesMensuelles, mensualite, revenuMensuel);
        ScoreSimplifie score = calculerScore(tauxEndettement);
        return new SimulationResultDto(mensualite, tauxEndettement, score);
    }
}

