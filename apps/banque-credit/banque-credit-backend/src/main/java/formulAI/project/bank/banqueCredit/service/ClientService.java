package formulAI.project.bank.banqueCredit.service;

import formulAI.project.bank.banqueCredit.dto.ClientCreateDto;
import formulAI.project.bank.banqueCredit.dto.ClientDetailDto;
import formulAI.project.bank.banqueCredit.dto.ClientDto;

import java.util.List;

public interface ClientService {

    ClientDto creer(ClientCreateDto request);

    List<ClientDto> lister();

    ClientDetailDto consulterDetail(Long id);
}

