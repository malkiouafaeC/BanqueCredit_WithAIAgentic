package formulAI.project.bank.banqueCredit.exception;

/** RG-BANK-06 - Conditions cumulatives d'eligibilite non respectees (422). */
public class EligibiliteNonRespecteeException extends RuntimeException {
    public EligibiliteNonRespecteeException(String message) {
        super(message);
    }
}

