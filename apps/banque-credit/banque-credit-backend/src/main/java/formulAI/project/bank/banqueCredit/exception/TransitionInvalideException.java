package formulAI.project.bank.banqueCredit.exception;

/** RG-BANK-05 - Transition de statut interdite (409). */
public class TransitionInvalideException extends RuntimeException {
    public TransitionInvalideException(String message) {
        super(message);
    }
}

