package formulAI.project.bank.banqueCredit.controller;

import formulAI.project.bank.banqueCredit.dto.AuthRequestDto;
import formulAI.project.bank.banqueCredit.dto.AuthResponseDto;
import formulAI.project.bank.banqueCredit.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentification")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur (CONSEILLER ou RESPONSABLE_CREDIT)")
    @ApiResponse(responseCode = "200", description = "Authentification reussie")
    @ApiResponse(responseCode = "401", description = "Identifiants invalides")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

