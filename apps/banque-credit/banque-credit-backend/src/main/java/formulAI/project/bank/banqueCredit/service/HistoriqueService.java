package formulAI.project.bank.banqueCredit.service;

import formulAI.project.bank.banqueCredit.dto.HistoriqueDecisionDto;

import java.util.List;

public interface HistoriqueService {

    List<HistoriqueDecisionDto> listerParDemande(Long demandeId);
}

