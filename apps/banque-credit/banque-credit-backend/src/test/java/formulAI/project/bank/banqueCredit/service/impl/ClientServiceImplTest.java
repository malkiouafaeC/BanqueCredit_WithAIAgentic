package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.dto.ClientCreateDto;
import formulAI.project.bank.banqueCredit.dto.ClientDetailDto;
import formulAI.project.bank.banqueCredit.dto.ClientDto;
import formulAI.project.bank.banqueCredit.exception.RessourceNotFoundException;
import formulAI.project.bank.banqueCredit.model.Client;
import formulAI.project.bank.banqueCredit.model.DemandeCredit;
import formulAI.project.bank.banqueCredit.model.Statut;
import formulAI.project.bank.banqueCredit.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * QA-TASK-001 - Unitaire pur (Mockito) de ClientServiceImpl, isole de la persistance reelle.
 * Business rule couverte : RG-BANK-01 (via le mapping de creation), US-005/US-006 (liste/detail).
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(1L);
        client.setNom("Dupont");
        client.setEmail("dupont@example.com");
        client.setRevenuMensuel(new BigDecimal("2000"));
        client.setChargesMensuelles(new BigDecimal("300"));
        client.setSituationProfessionnelle("Salarie");
        client.setCreatedAt(Instant.now());
    }

    @Test
    void creer_shouldMapRequestToEntityAndReturnDto_whenFieldsValid() {
        ClientCreateDto request = new ClientCreateDto();
        request.setNom("Dupont");
        request.setEmail("dupont@example.com");
        request.setRevenuMensuel(new BigDecimal("2000"));
        request.setChargesMensuelles(new BigDecimal("300"));
        request.setSituationProfessionnelle("Salarie");

        when(clientRepository.save(any(Client.class))).thenReturn(client);

        ClientDto result = clientService.creer(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNom()).isEqualTo("Dupont");
        assertThat(result.getRevenuMensuel()).isEqualByComparingTo("2000");
        assertThat(result.getChargesMensuelles()).isEqualByComparingTo("300");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void lister_shouldReturnEmptyList_whenNoClientsExist() {
        when(clientRepository.findAll()).thenReturn(List.of());

        List<ClientDto> result = clientService.lister();

        assertThat(result).isEmpty();
    }

    @Test
    void lister_shouldReturnAllClients_whenClientsExist() {
        when(clientRepository.findAll()).thenReturn(List.of(client));

        List<ClientDto> result = clientService.lister();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).isEqualTo("Dupont");
    }

    @Test
    void consulterDetail_shouldReturnClientWithDemandesSummary_whenClientExists() {
        DemandeCredit demande = new DemandeCredit();
        demande.setId(10L);
        demande.setMontantDemande(new BigDecimal("5000"));
        demande.setDureeMois(24);
        demande.setStatut(Statut.BROUILLON);
        client.setDemandes(List.of(demande));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        ClientDetailDto result = clientService.consulterDetail(1L);

        assertThat(result.getDemandes()).hasSize(1);
        assertThat(result.getDemandes().get(0).getStatut()).isEqualTo(Statut.BROUILLON);
    }

    @Test
    void consulterDetail_shouldReturnEmptyDemandesSection_whenClientHasNoDemande() {
        client.setDemandes(List.of());
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        ClientDetailDto result = clientService.consulterDetail(1L);

        assertThat(result.getDemandes()).isEmpty();
    }

    @Test
    void consulterDetail_shouldThrowRessourceNotFoundException_whenClientDoesNotExist() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.consulterDetail(99L))
                .isInstanceOf(RessourceNotFoundException.class);
    }
}

