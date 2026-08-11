package formulAI.project.bank.banqueCredit.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Requete de creation d'une DemandeCredit (RG-BANK-02, RG-BANK-03). */
public class DemandeCreditCreateDto {

    @NotNull(message = "Le client est obligatoire")
    private Long clientId;

    @NotNull(message = "Le montant demande est obligatoire")
    @DecimalMin(value = "1000.00", inclusive = false, message = "Le montant doit etre strictement superieur a 1000")
    @DecimalMax(value = "100000.00", inclusive = true, message = "Le montant doit etre inferieur ou egal a 100000")
    private BigDecimal montantDemande;

    @NotNull(message = "La duree est obligatoire")
    @Min(value = 12, message = "La duree doit etre comprise entre 12 et 84 mois")
    @Max(value = 84, message = "La duree doit etre comprise entre 12 et 84 mois")
    private Integer dureeMois;

    @NotNull(message = "Le taux fictif est obligatoire")
    @DecimalMin(value = "0.00", inclusive = true, message = "Le taux fictif doit etre superieur ou egal a 0")
    private BigDecimal tauxFictif;

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public BigDecimal getMontantDemande() { return montantDemande; }
    public void setMontantDemande(BigDecimal montantDemande) { this.montantDemande = montantDemande; }

    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }

    public BigDecimal getTauxFictif() { return tauxFictif; }
    public void setTauxFictif(BigDecimal tauxFictif) { this.tauxFictif = tauxFictif; }
}

