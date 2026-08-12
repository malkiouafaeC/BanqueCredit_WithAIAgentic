package formulAI.project.bank.banqueCredit.dto;

import java.time.Instant;
import java.util.List;

/** Corps de reponse d'erreur standardise (DEC-005). */
public class ErrorResponseDto {

    private String code;
    private String message;
    private String champ;
    private Instant timestamp;
    /** Renseigne uniquement pour les erreurs de validation multi-champs (AC-004-4). */
    private List<ChampErreur> erreurs;

    public ErrorResponseDto() {}

    public ErrorResponseDto(String code, String message, String champ, Instant timestamp) {
        this.code = code;
        this.message = message;
        this.champ = champ;
        this.timestamp = timestamp;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getChamp() { return champ; }
    public void setChamp(String champ) { this.champ = champ; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public List<ChampErreur> getErreurs() { return erreurs; }
    public void setErreurs(List<ChampErreur> erreurs) { this.erreurs = erreurs; }

    public static class ChampErreur {
        private String champ;
        private String message;

        public ChampErreur() {}

        public ChampErreur(String champ, String message) {
            this.champ = champ;
            this.message = message;
        }

        public String getChamp() { return champ; }
        public void setChamp(String champ) { this.champ = champ; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}

