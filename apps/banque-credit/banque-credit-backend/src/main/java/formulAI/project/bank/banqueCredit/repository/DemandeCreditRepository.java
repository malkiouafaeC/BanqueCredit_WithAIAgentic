package formulAI.project.bank.banqueCredit.repository;

import formulAI.project.bank.banqueCredit.model.DemandeCredit;
import formulAI.project.bank.banqueCredit.model.Statut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandeCreditRepository extends JpaRepository<DemandeCredit, Long> {

    List<DemandeCredit> findByStatut(Statut statut);

    List<DemandeCredit> findByClientId(Long clientId);

    List<DemandeCredit> findByStatutIn(List<Statut> statuts);

    List<DemandeCredit> findByStatutNotIn(List<Statut> statuts);
}

