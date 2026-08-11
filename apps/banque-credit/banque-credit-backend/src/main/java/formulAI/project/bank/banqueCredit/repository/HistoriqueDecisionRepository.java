package formulAI.project.bank.banqueCredit.repository;

import formulAI.project.bank.banqueCredit.model.HistoriqueDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueDecisionRepository extends JpaRepository<HistoriqueDecision, Long> {

    List<HistoriqueDecision> findByDemandeCreditIdOrderByDateAsc(Long demandeCreditId);
}

