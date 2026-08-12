package formulAI.project.bank.banqueCredit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * ENT-001 - Client fictif porteur de demandes de credit.
 * Trace : RG-BANK-01, US-003..006.
 */
@Entity
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String nom;

    @Email(message = "Le format de l'email est invalide")
    private String email;

    @NotNull(message = "Le revenu mensuel est obligatoire")
    @DecimalMin(value = "0.01", message = "Le revenu mensuel doit etre strictement superieur a 0")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal revenuMensuel;

    @NotNull(message = "Les charges mensuelles sont obligatoires")
    @DecimalMin(value = "0.00", message = "Les charges mensuelles doivent etre superieures ou egales a 0")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal chargesMensuelles;

    private String situationProfessionnelle;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<DemandeCredit> demandes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
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

    public List<DemandeCredit> getDemandes() { return demandes; }
    public void setDemandes(List<DemandeCredit> demandes) { this.demandes = demandes; }
}

