package formulAI.project.bank.banqueCredit.dto;

import formulAI.project.bank.banqueCredit.model.ScoreSimplifie;

import java.math.BigDecimal;

/** Resultat de simulation (RG-BANK-04, RG-BANK-08). */
public class SimulationResultDto {

    private BigDecimal mensualiteEstimee;
    private BigDecimal tauxEndettement;
    private ScoreSimplifie scoreSimplifie;

    public SimulationResultDto() {}

    public SimulationResultDto(BigDecimal mensualiteEstimee, BigDecimal tauxEndettement, ScoreSimplifie scoreSimplifie) {
        this.mensualiteEstimee = mensualiteEstimee;
        this.tauxEndettement = tauxEndettement;
        this.scoreSimplifie = scoreSimplifie;
    }

    public BigDecimal getMensualiteEstimee() { return mensualiteEstimee; }
    public void setMensualiteEstimee(BigDecimal mensualiteEstimee) { this.mensualiteEstimee = mensualiteEstimee; }

    public BigDecimal getTauxEndettement() { return tauxEndettement; }
    public void setTauxEndettement(BigDecimal tauxEndettement) { this.tauxEndettement = tauxEndettement; }

    public ScoreSimplifie getScoreSimplifie() { return scoreSimplifie; }
    public void setScoreSimplifie(ScoreSimplifie scoreSimplifie) { this.scoreSimplifie = scoreSimplifie; }
}

