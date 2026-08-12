package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.exception.EligibiliteNonRespecteeException;
import formulAI.project.bank.banqueCredit.service.EligibiliteService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * RG-BANK-06 - Blocage strict (Q3) : les 3 conditions doivent etre toutes vraies
 * pour permettre l'acceptation. Bornes inclusives (35% / 1500 / 50000).
 */
@Service
public class EligibiliteServiceImpl implements EligibiliteService {

    private static final BigDecimal TAUX_MAX = new BigDecimal("35");
    private static final BigDecimal REVENU_MIN = new BigDecimal("1500");
    private static final BigDecimal MONTANT_MAX = new BigDecimal("50000");

    @Override
    public void verifierEligibilite(BigDecimal tauxEndettement, BigDecimal revenuMensuel, BigDecimal montantDemande) {
        if (tauxEndettement.compareTo(TAUX_MAX) > 0) {
            throw new EligibiliteNonRespecteeException("Acceptation impossible : taux d'endettement superieur a 35%");
        }
        if (revenuMensuel.compareTo(REVENU_MIN) < 0) {
            throw new EligibiliteNonRespecteeException("Acceptation impossible : revenu mensuel inferieur a 1500");
        }
        if (montantDemande.compareTo(MONTANT_MAX) > 0) {
            throw new EligibiliteNonRespecteeException("Acceptation impossible : montant superieur a 50000");
        }
    }
}

