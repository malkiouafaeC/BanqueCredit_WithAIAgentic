package formulAI.project.bank.banqueCredit.controller;

import formulAI.project.bank.banqueCredit.dto.*;
import formulAI.project.bank.banqueCredit.model.Statut;
import formulAI.project.bank.banqueCredit.service.DemandeCreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/demandes")
@Tag(name = "Demandes de credit")
public class DemandeCreditController {

    private final DemandeCreditService demandeCreditService;

    public DemandeCreditController(DemandeCreditService demandeCreditService) {
        this.demandeCreditService = demandeCreditService;
    }

    @PostMapping
    @Operation(summary = "Creer une demande de credit en brouillon (CONSEILLER)")
    public ResponseEntity<DemandeCreditDto> creer(@Valid @RequestBody DemandeCreditCreateDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(demandeCreditService.creer(request));
    }

    @GetMapping
    @Operation(summary = "Lister les demandes de credit (filtres optionnels statut/clientId)")
    public ResponseEntity<List<DemandeCreditSummaryDto>> lister(@RequestParam(required = false) Statut statut,
                                                                 @RequestParam(required = false) Long clientId) {
        return ResponseEntity.ok(demandeCreditService.lister(statut, clientId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le detail d'une demande de credit (avec simulation recalculee)")
    public ResponseEntity<DemandeCreditDto> consulter(@PathVariable Long id) {
        return ResponseEntity.ok(demandeCreditService.consulter(id));
    }

    @PostMapping("/{id}/simuler")
    @Operation(summary = "Simuler une demande de credit (mensualite, taux d'endettement, score)")
    public ResponseEntity<SimulationResultDto> simuler(@PathVariable Long id) {
        return ResponseEntity.ok(demandeCreditService.simuler(id));
    }
}

