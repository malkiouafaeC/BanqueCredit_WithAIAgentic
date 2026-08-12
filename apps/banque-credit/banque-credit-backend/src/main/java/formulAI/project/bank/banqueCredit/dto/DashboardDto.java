package formulAI.project.bank.banqueCredit.dto;

import java.math.BigDecimal;

/** Synthese agence (US-017, Q4 : agregats hors BROUILLON/ANNULEE). */
public class DashboardDto {

    private long nbSoumises;
    private long nbEnAnalyse;
    private long nbAcceptees;
    private long nbRefusees;
    private BigDecimal montantTotalDemande;
    private BigDecimal tauxMoyenEndettement;

    public DashboardDto() {}

    public DashboardDto(long nbSoumises, long nbEnAnalyse, long nbAcceptees, long nbRefusees,
                         BigDecimal montantTotalDemande, BigDecimal tauxMoyenEndettement) {
        this.nbSoumises = nbSoumises;
        this.nbEnAnalyse = nbEnAnalyse;
        this.nbAcceptees = nbAcceptees;
        this.nbRefusees = nbRefusees;
        this.montantTotalDemande = montantTotalDemande;
        this.tauxMoyenEndettement = tauxMoyenEndettement;
    }

    public long getNbSoumises() { return nbSoumises; }
    public void setNbSoumises(long nbSoumises) { this.nbSoumises = nbSoumises; }

    public long getNbEnAnalyse() { return nbEnAnalyse; }
    public void setNbEnAnalyse(long nbEnAnalyse) { this.nbEnAnalyse = nbEnAnalyse; }

    public long getNbAcceptees() { return nbAcceptees; }
    public void setNbAcceptees(long nbAcceptees) { this.nbAcceptees = nbAcceptees; }

    public long getNbRefusees() { return nbRefusees; }
    public void setNbRefusees(long nbRefusees) { this.nbRefusees = nbRefusees; }

    public BigDecimal getMontantTotalDemande() { return montantTotalDemande; }
    public void setMontantTotalDemande(BigDecimal montantTotalDemande) { this.montantTotalDemande = montantTotalDemande; }

    public BigDecimal getTauxMoyenEndettement() { return tauxMoyenEndettement; }
    public void setTauxMoyenEndettement(BigDecimal tauxMoyenEndettement) { this.tauxMoyenEndettement = tauxMoyenEndettement; }
}

