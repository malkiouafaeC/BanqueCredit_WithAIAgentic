package formulAI.project.bank.banqueCredit.exception;

/** Role non autorise pour l'action demandee (403). */
public class RoleNonAutoriseException extends RuntimeException {
    public RoleNonAutoriseException(String message) {
        super(message);
    }
}

