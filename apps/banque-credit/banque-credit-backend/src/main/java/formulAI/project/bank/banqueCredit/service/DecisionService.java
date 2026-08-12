package formulAI.project.bank.banqueCredit.service;

import formulAI.project.bank.banqueCredit.dto.DemandeCreditDto;

/**
 * DEC-006/DEC-009 - Toutes les transitions de statut d'une DemandeCredit.
 * Chaque methode est transactionnelle et cree une HistoriqueDecision de maniere atomique.
 */
public interface DecisionService {

    DemandeCreditDto soumettre(Long demandeId);

    DemandeCreditDto annuler(Long demandeId);

    DemandeCreditDto analyser(Long demandeId);

    DemandeCreditDto accepter(Long demandeId);

    DemandeCreditDto refuser(Long demandeId, String commentaire);
}

