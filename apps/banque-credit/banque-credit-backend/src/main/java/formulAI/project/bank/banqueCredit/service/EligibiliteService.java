package formulAI.project.bank.banqueCredit.service;

/** RG-BANK-06 - Verification des 3 conditions cumulatives d'eligibilite a l'acceptation. */
public interface EligibiliteService {

    /**
     * Leve EligibiliteNonRespecteeException avec un message explicite si une condition echoue.
     */
    void verifierEligibilite(java.math.BigDecimal tauxEndettement, java.math.BigDecimal revenuMensuel, java.math.BigDecimal montantDemande);
}

