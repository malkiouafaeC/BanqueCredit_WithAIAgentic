package formulAI.project.bank.banqueCredit.exception;

/** Erreur de validation metier generique (champ optionnel) -> 400. */
public class ValidationMetierException extends RuntimeException {

    private final String champ;

    public ValidationMetierException(String message) {
        super(message);
        this.champ = null;
    }

    public ValidationMetierException(String message, String champ) {
        super(message);
        this.champ = champ;
    }

    public String getChamp() {
        return champ;
    }
}

