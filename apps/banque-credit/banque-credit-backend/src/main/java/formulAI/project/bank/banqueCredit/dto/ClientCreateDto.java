package formulAI.project.bank.banqueCredit.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Requete de creation d'un client (RG-BANK-01). */
public class ClientCreateDto {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @Email(message = "Le format de l'email est invalide")
    private String email;

    @NotNull(message = "Le revenu mensuel est obligatoire")
    @DecimalMin(value = "0.01", message = "Le revenu mensuel doit etre strictement superieur a 0")
    private BigDecimal revenuMensuel;

    @NotNull(message = "Les charges mensuelles sont obligatoires")
    @DecimalMin(value = "0.00", message = "Les charges mensuelles doivent etre superieures ou egales a 0")
    private BigDecimal chargesMensuelles;

    private String situationProfessionnelle;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getRevenuMensuel() { return revenuMensuel; }
    public void setRevenuMensuel(BigDecimal revenuMensuel) { this.revenuMensuel = revenuMensuel; }

    public BigDecimal getChargesMensuelles() { return chargesMensuelles; }
    public void setChargesMensuelles(BigDecimal chargesMensuelles) { this.chargesMensuelles = chargesMensuelles; }

    public String getSituationProfessionnelle() { return situationProfessionnelle; }
    public void setSituationProfessionnelle(String situationProfessionnelle) { this.situationProfessionnelle = situationProfessionnelle; }
}

