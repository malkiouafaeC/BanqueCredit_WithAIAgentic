package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.dto.HistoriqueDecisionDto;
import formulAI.project.bank.banqueCredit.exception.RessourceNotFoundException;
import formulAI.project.bank.banqueCredit.model.HistoriqueDecision;
import formulAI.project.bank.banqueCredit.repository.DemandeCreditRepository;
import formulAI.project.bank.banqueCredit.repository.HistoriqueDecisionRepository;
import formulAI.project.bank.banqueCredit.service.HistoriqueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoriqueServiceImpl implements HistoriqueService {

    private final HistoriqueDecisionRepository historiqueDecisionRepository;
    private final DemandeCreditRepository demandeCreditRepository;

    public HistoriqueServiceImpl(HistoriqueDecisionRepository historiqueDecisionRepository,
                                  DemandeCreditRepository demandeCreditRepository) {
        this.historiqueDecisionRepository = historiqueDecisionRepository;
        this.demandeCreditRepository = demandeCreditRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriqueDecisionDto> listerParDemande(Long demandeId) {
        if (!demandeCreditRepository.existsById(demandeId)) {
            throw new RessourceNotFoundException("Demande de credit introuvable : id=" + demandeId);
        }
        List<HistoriqueDecision> historique = historiqueDecisionRepository.findByDemandeCreditIdOrderByDateAsc(demandeId);
        return historique.stream().map(this::toDto).toList();
    }

    private HistoriqueDecisionDto toDto(HistoriqueDecision h) {
        return new HistoriqueDecisionDto(h.getId(), h.getAncienStatut(), h.getNouveauStatut(), h.getCommentaire(),
                h.getTauxEndettementSnapshot(), h.getAuteur().getUsername(), h.getDate());
    }
}

