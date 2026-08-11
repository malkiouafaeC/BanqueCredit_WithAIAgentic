package formulAI.project.bank.banqueCredit.service;

import formulAI.project.bank.banqueCredit.dto.AuthRequestDto;
import formulAI.project.bank.banqueCredit.dto.AuthResponseDto;

/** Contrat du service d'authentification (DEC-001). */
public interface AuthService {

    AuthResponseDto login(AuthRequestDto request);
}

