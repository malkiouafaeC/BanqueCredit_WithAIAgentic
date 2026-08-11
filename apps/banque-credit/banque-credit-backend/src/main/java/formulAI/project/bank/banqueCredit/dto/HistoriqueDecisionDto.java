package formulAI.project.bank.banqueCredit.dto;

import formulAI.project.bank.banqueCredit.model.Statut;

import java.math.BigDecimal;
import java.time.Instant;

/** Entree d'historique de transition (US-016). */
public class HistoriqueDecisionDto {

    private Long id;
    private Statut ancienStatut;
    private Statut nouveauStatut;
    private String commentaire;
    private BigDecimal tauxEndettementSnapshot;
    private String auteur;
    private Instant date;

    public HistoriqueDecisionDto() {}

    public HistoriqueDecisionDto(Long id, Statut ancienStatut, Statut nouveauStatut, String commentaire,
                                  BigDecimal tauxEndettementSnapshot, String auteur, Instant date) {
        this.id = id;
        this.ancienStatut = ancienStatut;
        this.nouveauStatut = nouveauStatut;
        this.commentaire = commentaire;
        this.tauxEndettementSnapshot = tauxEndettementSnapshot;
        this.auteur = auteur;
        this.date = date;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Statut getAncienStatut() { return ancienStatut; }
    public void setAncienStatut(Statut ancienStatut) { this.ancienStatut = ancienStatut; }

    public Statut getNouveauStatut() { return nouveauStatut; }
    public void setNouveauStatut(Statut nouveauStatut) { this.nouveauStatut = nouveauStatut; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public BigDecimal getTauxEndettementSnapshot() { return tauxEndettementSnapshot; }
    public void setTauxEndettementSnapshot(BigDecimal tauxEndettementSnapshot) { this.tauxEndettementSnapshot = tauxEndettementSnapshot; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }
}

