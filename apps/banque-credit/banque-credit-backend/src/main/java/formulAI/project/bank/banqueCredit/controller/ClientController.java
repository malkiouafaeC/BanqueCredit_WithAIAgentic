package formulAI.project.bank.banqueCredit.controller;

import formulAI.project.bank.banqueCredit.dto.ClientCreateDto;
import formulAI.project.bank.banqueCredit.dto.ClientDetailDto;
import formulAI.project.bank.banqueCredit.dto.ClientDto;
import formulAI.project.bank.banqueCredit.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
@Tag(name = "Clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @Operation(summary = "Creer un client (CONSEILLER)")
    @ApiResponse(responseCode = "201", description = "Client cree")
    @ApiResponse(responseCode = "400", description = "Validation echouee")
    @ApiResponse(responseCode = "403", description = "Role non autorise")
    public ResponseEntity<ClientDto> creer(@Valid @RequestBody ClientCreateDto request) {
        ClientDto created = clientService.creer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Operation(summary = "Lister les clients (CONSEILLER, RESPONSABLE_CREDIT)")
    public ResponseEntity<List<ClientDto>> lister() {
        return ResponseEntity.ok(clientService.lister());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le detail d'un client avec ses demandes")
    @ApiResponse(responseCode = "404", description = "Client introuvable")
    public ResponseEntity<ClientDetailDto> consulterDetail(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.consulterDetail(id));
    }
}

