package formulAI.project.bank.banqueCredit.dto;

import jakarta.validation.constraints.NotBlank;

/** Requete de refus (commentaire obligatoire, RG-BANK-07). */
public class DecisionRefusDto {

    private String commentaire;

    public DecisionRefusDto() {}

    public DecisionRefusDto(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}

