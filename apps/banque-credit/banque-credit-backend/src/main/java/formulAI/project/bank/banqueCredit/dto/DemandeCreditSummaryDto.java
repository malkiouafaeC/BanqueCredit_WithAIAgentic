package formulAI.project.bank.banqueCredit.dto;

import formulAI.project.bank.banqueCredit.model.Statut;

import java.math.BigDecimal;

/** Resume d'une DemandeCredit pour affichage dans le detail client / listes. */
public class DemandeCreditSummaryDto {

    private Long id;
    private BigDecimal montantDemande;
    private Integer dureeMois;
    private Statut statut;

    public DemandeCreditSummaryDto() {}

    public DemandeCreditSummaryDto(Long id, BigDecimal montantDemande, Integer dureeMois, Statut statut) {
        this.id = id;
        this.montantDemande = montantDemande;
        this.dureeMois = dureeMois;
        this.statut = statut;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getMontantDemande() { return montantDemande; }
    public void setMontantDemande(BigDecimal montantDemande) { this.montantDemande = montantDemande; }

    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }
}

