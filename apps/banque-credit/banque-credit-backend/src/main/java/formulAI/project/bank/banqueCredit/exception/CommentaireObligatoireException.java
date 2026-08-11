package formulAI.project.bank.banqueCredit.exception;

/** RG-BANK-07 - Commentaire obligatoire absent/vide pour un refus (400). */
public class CommentaireObligatoireException extends RuntimeException {
    public CommentaireObligatoireException(String message) {
        super(message);
    }
}

