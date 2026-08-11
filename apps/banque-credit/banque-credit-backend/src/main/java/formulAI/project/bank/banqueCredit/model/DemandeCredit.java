package formulAI.project.bank.banqueCredit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * ENT-002 - Demande de credit associee a un Client.
 * Trace : RG-BANK-02..08, US-007..015.
 * Note DEC-ENT-002 : mensualiteEstimee/tauxEndettement/scoreSimplifie ne sont PAS persistes
 * (recalcules a la volee depuis montantDemande/dureeMois/tauxFictif/client).
 */
@Entity
@Table(name = "demande_credit")
public class DemandeCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le client est obligatoire")
    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull(message = "Le montant demande est obligatoire")
    @DecimalMin(value = "1000.00", inclusive = false, message = "Le montant doit etre strictement superieur a 1000")
    @DecimalMax(value = "100000.00", inclusive = true, message = "Le montant doit etre inferieur ou egal a 100000")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal montantDemande;

    @NotNull(message = "La duree est obligatoire")
    @Min(value = 12, message = "La duree doit etre comprise entre 12 et 84 mois")
    @Max(value = 84, message = "La duree doit etre comprise entre 12 et 84 mois")
    @Column(nullable = false)
    private Integer dureeMois;

    @NotNull(message = "Le taux fictif est obligatoire")
    @DecimalMin(value = "0.00", inclusive = true, message = "Le taux fictif doit etre superieur ou egal a 0")
    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal tauxFictif;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.BROUILLON;

    private String commentaireDecision;

    private Instant dateSoumission;

    private Instant dateDecision;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public BigDecimal getMontantDemande() { return montantDemande; }
    public void setMontantDemande(BigDecimal montantDemande) { this.montantDemande = montantDemande; }

    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }

    public BigDecimal getTauxFictif() { return tauxFictif; }
    public void setTauxFictif(BigDecimal tauxFictif) { this.tauxFictif = tauxFictif; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public String getCommentaireDecision() { return commentaireDecision; }
    public void setCommentaireDecision(String commentaireDecision) { this.commentaireDecision = commentaireDecision; }

    public Instant getDateSoumission() { return dateSoumission; }
    public void setDateSoumission(Instant dateSoumission) { this.dateSoumission = dateSoumission; }

    public Instant getDateDecision() { return dateDecision; }
    public void setDateDecision(Instant dateDecision) { this.dateDecision = dateDecision; }
}

