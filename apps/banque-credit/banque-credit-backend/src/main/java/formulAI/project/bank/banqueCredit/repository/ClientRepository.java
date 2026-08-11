package formulAI.project.bank.banqueCredit.repository;

import formulAI.project.bank.banqueCredit.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}

