package formulAI.project.bank.banqueCredit.dto;

import formulAI.project.bank.banqueCredit.model.ScoreSimplifie;
import formulAI.project.bank.banqueCredit.model.Statut;

import java.math.BigDecimal;
import java.time.Instant;

/** Representation complete d'une DemandeCredit (inclut simulation recalculee, DEC-ENT-002). */
public class DemandeCreditDto {

    private Long id;
    private Long clientId;
    private String clientNom;
    private BigDecimal montantDemande;
    private Integer dureeMois;
    private BigDecimal tauxFictif;
    private Statut statut;
    private BigDecimal mensualiteEstimee;
    private BigDecimal tauxEndettement;
    private ScoreSimplifie scoreSimplifie;
    private String commentaireDecision;
    private Instant dateSoumission;
    private Instant dateDecision;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }

    public BigDecimal getMontantDemande() { return montantDemande; }
    public void setMontantDemande(BigDecimal montantDemande) { this.montantDemande = montantDemande; }

    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }

    public BigDecimal getTauxFictif() { return tauxFictif; }
    public void setTauxFictif(BigDecimal tauxFictif) { this.tauxFictif = tauxFictif; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public BigDecimal getMensualiteEstimee() { return mensualiteEstimee; }
    public void setMensualiteEstimee(BigDecimal mensualiteEstimee) { this.mensualiteEstimee = mensualiteEstimee; }

    public BigDecimal getTauxEndettement() { return tauxEndettement; }
    public void setTauxEndettement(BigDecimal tauxEndettement) { this.tauxEndettement = tauxEndettement; }

    public ScoreSimplifie getScoreSimplifie() { return scoreSimplifie; }
    public void setScoreSimplifie(ScoreSimplifie scoreSimplifie) { this.scoreSimplifie = scoreSimplifie; }

    public String getCommentaireDecision() { return commentaireDecision; }
    public void setCommentaireDecision(String commentaireDecision) { this.commentaireDecision = commentaireDecision; }

    public Instant getDateSoumission() { return dateSoumission; }
    public void setDateSoumission(Instant dateSoumission) { this.dateSoumission = dateSoumission; }

    public Instant getDateDecision() { return dateDecision; }
    public void setDateDecision(Instant dateDecision) { this.dateDecision = dateDecision; }
}

