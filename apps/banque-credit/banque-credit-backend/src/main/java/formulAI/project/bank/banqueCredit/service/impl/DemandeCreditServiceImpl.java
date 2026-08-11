package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.dto.*;
import formulAI.project.bank.banqueCredit.exception.RessourceNotFoundException;
import formulAI.project.bank.banqueCredit.model.*;
import formulAI.project.bank.banqueCredit.repository.ClientRepository;
import formulAI.project.bank.banqueCredit.repository.DemandeCreditRepository;
import formulAI.project.bank.banqueCredit.repository.HistoriqueDecisionRepository;
import formulAI.project.bank.banqueCredit.repository.UserRepository;
import formulAI.project.bank.banqueCredit.security.AuthenticatedUserUtil;
import formulAI.project.bank.banqueCredit.service.DemandeCreditService;
import formulAI.project.bank.banqueCredit.service.SimulationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DemandeCreditServiceImpl implements DemandeCreditService {

    private final DemandeCreditRepository demandeCreditRepository;
    private final ClientRepository clientRepository;
    private final HistoriqueDecisionRepository historiqueDecisionRepository;
    private final UserRepository userRepository;
    private final SimulationService simulationService;

    public DemandeCreditServiceImpl(DemandeCreditRepository demandeCreditRepository,
                                     ClientRepository clientRepository,
                                     HistoriqueDecisionRepository historiqueDecisionRepository,
                                     UserRepository userRepository,
                                     SimulationService simulationService) {
        this.demandeCreditRepository = demandeCreditRepository;
        this.clientRepository = clientRepository;
        this.historiqueDecisionRepository = historiqueDecisionRepository;
        this.userRepository = userRepository;
        this.simulationService = simulationService;
    }

    @Override
    @PreAuthorize("hasRole('CONSEILLER')")
    @Transactional
    public DemandeCreditDto creer(DemandeCreditCreateDto request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RessourceNotFoundException("Client introuvable : id=" + request.getClientId()));

        DemandeCredit demande = new DemandeCredit();
        demande.setClient(client);
        demande.setMontantDemande(request.getMontantDemande());
        demande.setDureeMois(request.getDureeMois());
        demande.setTauxFictif(request.getTauxFictif());
        demande.setStatut(Statut.BROUILLON);
        DemandeCredit saved = demandeCreditRepository.save(demande);

        User auteur = currentUser();
        HistoriqueDecision historique = new HistoriqueDecision();
        historique.setDemandeCredit(saved);
        historique.setAncienStatut(null);
        historique.setNouveauStatut(Statut.BROUILLON);
        historique.setAuteur(auteur);
        historiqueDecisionRepository.save(historique);

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeCreditSummaryDto> lister(Statut statut, Long clientId) {
        List<DemandeCredit> demandes;
        if (statut != null && clientId != null) {
            demandes = demandeCreditRepository.findByClientId(clientId).stream()
                    .filter(d -> d.getStatut() == statut)
                    .toList();
        } else if (statut != null) {
            demandes = demandeCreditRepository.findByStatut(statut);
        } else if (clientId != null) {
            demandes = demandeCreditRepository.findByClientId(clientId);
        } else {
            demandes = demandeCreditRepository.findAll();
        }
        return demandes.stream()
                .map(d -> new DemandeCreditSummaryDto(d.getId(), d.getMontantDemande(), d.getDureeMois(), d.getStatut()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DemandeCreditDto consulter(Long id) {
        DemandeCredit demande = findOrThrow(id);
        return toDto(demande);
    }

    @Override
    @Transactional(readOnly = true)
    public SimulationResultDto simuler(Long id) {
        DemandeCredit demande = findOrThrow(id);
        return simulationService.simuler(demande.getMontantDemande(), demande.getDureeMois(), demande.getTauxFictif(),
                demande.getClient().getChargesMensuelles(), demande.getClient().getRevenuMensuel());
    }

    private DemandeCredit findOrThrow(Long id) {
        return demandeCreditRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Demande de credit introuvable : id=" + id));
    }

    private User currentUser() {
        String username = AuthenticatedUserUtil.currentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur authentifie introuvable : " + username));
    }

    private DemandeCreditDto toDto(DemandeCredit demande) {
        DemandeCreditDto dto = new DemandeCreditDto();
        dto.setId(demande.getId());
        dto.setClientId(demande.getClient().getId());
        dto.setClientNom(demande.getClient().getNom());
        dto.setMontantDemande(demande.getMontantDemande());
        dto.setDureeMois(demande.getDureeMois());
        dto.setTauxFictif(demande.getTauxFictif());
        dto.setStatut(demande.getStatut());
        dto.setCommentaireDecision(demande.getCommentaireDecision());
        dto.setDateSoumission(demande.getDateSoumission());
        dto.setDateDecision(demande.getDateDecision());

        SimulationResultDto simulation = simulationService.simuler(demande.getMontantDemande(), demande.getDureeMois(),
                demande.getTauxFictif(), demande.getClient().getChargesMensuelles(), demande.getClient().getRevenuMensuel());
        dto.setMensualiteEstimee(simulation.getMensualiteEstimee());
        dto.setTauxEndettement(simulation.getTauxEndettement());
        dto.setScoreSimplifie(simulation.getScoreSimplifie());
        return dto;
    }
}

