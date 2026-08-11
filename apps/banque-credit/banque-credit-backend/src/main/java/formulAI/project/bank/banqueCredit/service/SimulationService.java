package formulAI.project.bank.banqueCredit.service;

import formulAI.project.bank.banqueCredit.model.ScoreSimplifie;

import java.math.BigDecimal;

/**
 * DEC-ENT-002/RG-BANK-04/RG-BANK-08 - Calculs purs (aucune dependance persistance),
 * formule normative implementee telle quelle.
 */
public interface SimulationService {

    /**
     * Mensualite estimee (amortissement standard). Cas particulier tauxFictif = 0 : amortissement lineaire.
     */
    BigDecimal calculerMensualite(BigDecimal montantDemande, Integer dureeMois, BigDecimal tauxFictif);

    /**
     * Taux d'endettement en pourcentage : (chargesMensuelles + mensualiteEstimee) / revenuMensuel * 100.
     */
    BigDecimal calculerTauxEndettement(BigDecimal chargesMensuelles, BigDecimal mensualiteEstimee, BigDecimal revenuMensuel);

    /**
     * Score simplifie par bandes (RG-BANK-08), a partir du taux d'endettement en pourcentage.
     */
    ScoreSimplifie calculerScore(BigDecimal tauxEndettementPourcentage);

    /**
     * Calcule l'ensemble du resultat de simulation pour une demande + son client.
     */
    SimulationResultDto simuler(BigDecimal montantDemande, Integer dureeMois, BigDecimal tauxFictif,
                                 BigDecimal chargesMensuelles, BigDecimal revenuMensuel);
}

