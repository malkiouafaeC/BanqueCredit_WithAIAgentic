package formulAI.project.bank.banqueCredit.controller;

import formulAI.project.bank.banqueCredit.dto.DashboardDto;
import formulAI.project.bank.banqueCredit.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Consulter la synthese agence (compteurs, montant total, taux moyen d'endettement)")
    public ResponseEntity<DashboardDto> consulter() {
        return ResponseEntity.ok(dashboardService.consulterSynthese());
    }
}

