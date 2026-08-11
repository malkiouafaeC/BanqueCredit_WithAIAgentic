package formulAI.project.bank.banqueCredit.exception;

/** Ressource introuvable (404). */
public class RessourceNotFoundException extends RuntimeException {
    public RessourceNotFoundException(String message) {
        super(message);
    }
}

