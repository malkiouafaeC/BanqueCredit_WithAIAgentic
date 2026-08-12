package formulAI.project.bank.banqueCredit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * ENT-003 - Historique horodate de chaque transition de statut d'une DemandeCredit.
 * Trace : RG-BANK-05, RG-BANK-07, US-016, Q2 (DEC-ENT-005).
 */
@Entity
@Table(name = "historique_decision")
public class HistoriqueDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "demande_credit_id", nullable = false)
    private DemandeCredit demandeCredit;

    /** Nullable uniquement pour la 1ere entree a la creation de la demande (Q2). */
    @Enumerated(EnumType.STRING)
    private Statut ancienStatut;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut nouveauStatut;

    private String commentaire;

    /** Snapshot du taux d'endettement au moment de la decision (EN_ANALYSE -> ACCEPTEE/REFUSEE). */
    @Column(precision = 7, scale = 4)
    private BigDecimal tauxEndettementSnapshot;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "auteur_id", nullable = false)
    private User auteur;

    @Column(nullable = false)
    private Instant date;

    @PrePersist
    public void prePersist() {
        if (date == null) {
            date = Instant.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DemandeCredit getDemandeCredit() { return demandeCredit; }
    public void setDemandeCredit(DemandeCredit demandeCredit) { this.demandeCredit = demandeCredit; }

    public Statut getAncienStatut() { return ancienStatut; }
    public void setAncienStatut(Statut ancienStatut) { this.ancienStatut = ancienStatut; }

    public Statut getNouveauStatut() { return nouveauStatut; }
    public void setNouveauStatut(Statut nouveauStatut) { this.nouveauStatut = nouveauStatut; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public BigDecimal getTauxEndettementSnapshot() { return tauxEndettementSnapshot; }
    public void setTauxEndettementSnapshot(BigDecimal tauxEndettementSnapshot) { this.tauxEndettementSnapshot = tauxEndettementSnapshot; }

    public User getAuteur() { return auteur; }
    public void setAuteur(User auteur) { this.auteur = auteur; }

    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }
}

