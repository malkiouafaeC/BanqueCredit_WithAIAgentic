package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.dto.DemandeCreditDto;
import formulAI.project.bank.banqueCredit.exception.CommentaireObligatoireException;
import formulAI.project.bank.banqueCredit.exception.RessourceNotFoundException;
import formulAI.project.bank.banqueCredit.exception.RoleNonAutoriseException;
import formulAI.project.bank.banqueCredit.exception.TransitionInvalideException;
import formulAI.project.bank.banqueCredit.model.*;
import formulAI.project.bank.banqueCredit.repository.DemandeCreditRepository;
import formulAI.project.bank.banqueCredit.repository.HistoriqueDecisionRepository;
import formulAI.project.bank.banqueCredit.repository.UserRepository;
import formulAI.project.bank.banqueCredit.security.AuthenticatedUserUtil;
import formulAI.project.bank.banqueCredit.service.DecisionService;
import formulAI.project.bank.banqueCredit.service.EligibiliteService;
import formulAI.project.bank.banqueCredit.service.SimulationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import formulAI.project.bank.banqueCredit.dto.SimulationResultDto;

import java.time.Instant;

/**
 * DEC-006 - Chaque transition de statut + creation HistoriqueDecision est atomique (@Transactional).
 * DEC-009 - Role verifie a la fois par @PreAuthorize (1ere ligne, 403 avant meme l'execution de la
 * methode) et par TransitionRules en interne (2e ligne, defense en profondeur) : verifierTransitionEtRole
 * distingue 409 TRANSITION_INVALIDE (transition impossible pour tout role) de 403 ROLE_NON_AUTORISE
 * (transition possible mais role courant incorrect).
 */
@Service
public class DecisionServiceImpl implements DecisionService {

    private final DemandeCreditRepository demandeCreditRepository;
    private final HistoriqueDecisionRepository historiqueDecisionRepository;
    private final UserRepository userRepository;
    private final SimulationService simulationService;
    private final EligibiliteService eligibiliteService;

    public DecisionServiceImpl(DemandeCreditRepository demandeCreditRepository,
                                HistoriqueDecisionRepository historiqueDecisionRepository,
                                UserRepository userRepository,
                                SimulationService simulationService,
                                EligibiliteService eligibiliteService) {
        this.demandeCreditRepository = demandeCreditRepository;
        this.historiqueDecisionRepository = historiqueDecisionRepository;
        this.userRepository = userRepository;
        this.simulationService = simulationService;
        this.eligibiliteService = eligibiliteService;
    }

    @Override
    @PreAuthorize("hasRole('CONSEILLER')")
    @Transactional
    public DemandeCreditDto soumettre(Long demandeId) {
        DemandeCredit demande = findOrThrow(demandeId);
        User auteur = verifierTransitionEtRole(demande.getStatut(), Statut.SOUMISE,
                "Transition invalide : seule une demande en Brouillon peut etre soumise");
        Statut ancien = demande.getStatut();
        demande.setStatut(Statut.SOUMISE);
        demande.setDateSoumission(Instant.now());
        demandeCreditRepository.save(demande);
        enregistrerHistorique(demande, ancien, Statut.SOUMISE, null, null, auteur);
        return toDto(demande);
    }

    @Override
    @PreAuthorize("hasRole('CONSEILLER')")
    @Transactional
    public DemandeCreditDto annuler(Long demandeId) {
        DemandeCredit demande = findOrThrow(demandeId);
        User auteur = verifierTransitionEtRole(demande.getStatut(), Statut.ANNULEE,
                "Annulation impossible : la demande est deja en analyse ou decidee");
        Statut ancien = demande.getStatut();
        demande.setStatut(Statut.ANNULEE);
        demandeCreditRepository.save(demande);
        enregistrerHistorique(demande, ancien, Statut.ANNULEE, null, null, auteur);
        return toDto(demande);
    }

    @Override
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    @Transactional
    public DemandeCreditDto analyser(Long demandeId) {
        DemandeCredit demande = findOrThrow(demandeId);
        User auteur = verifierTransitionEtRole(demande.getStatut(), Statut.EN_ANALYSE,
                "Transition invalide : seule une demande Soumise peut passer en analyse");
        Statut ancien = demande.getStatut();
        demande.setStatut(Statut.EN_ANALYSE);
        demandeCreditRepository.save(demande);
        enregistrerHistorique(demande, ancien, Statut.EN_ANALYSE, null, null, auteur);
        return toDto(demande);
    }

    @Override
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    @Transactional
    public DemandeCreditDto accepter(Long demandeId) {
        DemandeCredit demande = findOrThrow(demandeId);
        User auteur = verifierTransitionEtRole(demande.getStatut(), Statut.ACCEPTEE,
                "Transition invalide : seule une demande En analyse peut etre acceptee");
        SimulationResultDto simulation = simuler(demande);
        eligibiliteService.verifierEligibilite(simulation.getTauxEndettement(), demande.getClient().getRevenuMensuel(),
                demande.getMontantDemande());

        Statut ancien = demande.getStatut();
        demande.setStatut(Statut.ACCEPTEE);
        demande.setDateDecision(Instant.now());
        demandeCreditRepository.save(demande);
        enregistrerHistorique(demande, ancien, Statut.ACCEPTEE, null, simulation.getTauxEndettement(), auteur);
        return toDto(demande);
    }

    @Override
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    @Transactional
    public DemandeCreditDto refuser(Long demandeId, String commentaire) {
        DemandeCredit demande = findOrThrow(demandeId);
        User auteur = verifierTransitionEtRole(demande.getStatut(), Statut.REFUSEE,
                "Transition invalide : seule une demande En analyse peut etre refusee");
        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new CommentaireObligatoireException("Un commentaire est obligatoire pour refuser une demande");
        }
        SimulationResultDto simulation = simuler(demande);

        Statut ancien = demande.getStatut();
        demande.setStatut(Statut.REFUSEE);
        demande.setCommentaireDecision(commentaire);
        demande.setDateDecision(Instant.now());
        demandeCreditRepository.save(demande);
        enregistrerHistorique(demande, ancien, Statut.REFUSEE, commentaire, simulation.getTauxEndettement(), auteur);
        return toDto(demande);
    }

    private SimulationResultDto simuler(DemandeCredit demande) {
        return simulationService.simuler(demande.getMontantDemande(), demande.getDureeMois(), demande.getTauxFictif(),
                demande.getClient().getChargesMensuelles(), demande.getClient().getRevenuMensuel());
    }

    /**
     * DEC-009 - Verifie via TransitionRules que statutActuel -> statutCible existe structurellement
     * (sinon 409 TRANSITION_INVALIDE, messageTransitionInvalide preserve tel quel pour les AC),
     * puis que le role de l'utilisateur courant est autorise pour cette transition precise
     * (sinon 403 ROLE_NON_AUTORISE). Retourne l'auteur pour eviter un second lookup dans l'historique.
     */
    private User verifierTransitionEtRole(Statut statutActuel, Statut statutCible, String messageTransitionInvalide) {
        if (!TransitionRules.transitionExiste(statutActuel, statutCible)) {
            throw new TransitionInvalideException(messageTransitionInvalide);
        }
        User auteur = currentUser();
        if (!TransitionRules.verifierTransitionAutorisee(statutActuel, statutCible, auteur.getRole())) {
            throw new RoleNonAutoriseException(
                    "Role non autorise pour la transition " + statutActuel + " -> " + statutCible);
        }
        return auteur;
    }

    private void enregistrerHistorique(DemandeCredit demande, Statut ancien, Statut nouveau, String commentaire,
                                        java.math.BigDecimal tauxEndettementSnapshot, User auteur) {
        HistoriqueDecision historique = new HistoriqueDecision();
        historique.setDemandeCredit(demande);
        historique.setAncienStatut(ancien);
        historique.setNouveauStatut(nouveau);
        historique.setCommentaire(commentaire);
        historique.setTauxEndettementSnapshot(tauxEndettementSnapshot);
        historique.setAuteur(auteur);
        historiqueDecisionRepository.save(historique);
    }

    private User currentUser() {
        String username = AuthenticatedUserUtil.currentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur authentifie introuvable : " + username));
    }

    private DemandeCredit findOrThrow(Long id) {
        return demandeCreditRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Demande de credit introuvable : id=" + id));
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

        SimulationResultDto simulation = simuler(demande);
        dto.setMensualiteEstimee(simulation.getMensualiteEstimee());
        dto.setTauxEndettement(simulation.getTauxEndettement());
        dto.setScoreSimplifie(simulation.getScoreSimplifie());
        return dto;
    }
}

