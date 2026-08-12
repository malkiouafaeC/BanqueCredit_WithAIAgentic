package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.dto.ClientCreateDto;
import formulAI.project.bank.banqueCredit.dto.ClientDetailDto;
import formulAI.project.bank.banqueCredit.dto.ClientDto;
import formulAI.project.bank.banqueCredit.dto.DemandeCreditSummaryDto;
import formulAI.project.bank.banqueCredit.exception.RessourceNotFoundException;
import formulAI.project.bank.banqueCredit.model.Client;
import formulAI.project.bank.banqueCredit.model.DemandeCredit;
import formulAI.project.bank.banqueCredit.repository.ClientRepository;
import formulAI.project.bank.banqueCredit.service.ClientService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @PreAuthorize("hasRole('CONSEILLER')")
    @Transactional
    public ClientDto creer(ClientCreateDto request) {
        Client client = new Client();
        client.setNom(request.getNom());
        client.setEmail(request.getEmail());
        client.setRevenuMensuel(request.getRevenuMensuel());
        client.setChargesMensuelles(request.getChargesMensuelles());
        client.setSituationProfessionnelle(request.getSituationProfessionnelle());
        Client saved = clientRepository.save(client);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDto> lister() {
        return clientRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDetailDto consulterDetail(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Client introuvable : id=" + id));
        List<DemandeCreditSummaryDto> demandes = client.getDemandes().stream()
                .map(this::toSummary)
                .toList();
        return new ClientDetailDto(client.getId(), client.getNom(), client.getEmail(), client.getRevenuMensuel(),
                client.getChargesMensuelles(), client.getSituationProfessionnelle(), client.getCreatedAt(), demandes);
    }

    private ClientDto toDto(Client client) {
        return new ClientDto(client.getId(), client.getNom(), client.getEmail(), client.getRevenuMensuel(),
                client.getChargesMensuelles(), client.getSituationProfessionnelle(), client.getCreatedAt());
    }

    private DemandeCreditSummaryDto toSummary(DemandeCredit demande) {
        return new DemandeCreditSummaryDto(demande.getId(), demande.getMontantDemande(), demande.getDureeMois(), demande.getStatut());
    }
}

