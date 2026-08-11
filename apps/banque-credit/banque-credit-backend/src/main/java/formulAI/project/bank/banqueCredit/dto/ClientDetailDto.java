package formulAI.project.bank.banqueCredit.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Detail client incluant ses demandes de credit associees (US-006). */
public class ClientDetailDto {

    private Long id;
    private String nom;
    private String email;
    private BigDecimal revenuMensuel;
    private BigDecimal chargesMensuelles;
    private String situationProfessionnelle;
    private Instant createdAt;
    private List<DemandeCreditSummaryDto> demandes;

    public ClientDetailDto() {}

    public ClientDetailDto(Long id, String nom, String email, BigDecimal revenuMensuel, BigDecimal chargesMensuelles,
                            String situationProfessionnelle, Instant createdAt, List<DemandeCreditSummaryDto> demandes) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.revenuMensuel = revenuMensuel;
        this.chargesMensuelles = chargesMensuelles;
        this.situationProfessionnelle = situationProfessionnelle;
        this.createdAt = createdAt;
        this.demandes = demandes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<DemandeCreditSummaryDto> getDemandes() { return demandes; }
    public void setDemandes(List<DemandeCreditSummaryDto> demandes) { this.demandes = demandes; }
}

