package formulAI.project.bank.banqueCredit.service;

import formulAI.project.bank.banqueCredit.dto.DemandeCreditCreateDto;
import formulAI.project.bank.banqueCredit.dto.DemandeCreditDto;
import formulAI.project.bank.banqueCredit.dto.DemandeCreditSummaryDto;
import formulAI.project.bank.banqueCredit.model.Statut;
import formulAI.project.bank.banqueCredit.dto.SimulationResultDto;

import java.util.List;

public interface DemandeCreditService {

    DemandeCreditDto creer(DemandeCreditCreateDto request);

    List<DemandeCreditSummaryDto> lister(Statut statut, Long clientId);

    DemandeCreditDto consulter(Long id);

    SimulationResultDto simuler(Long id);
}

