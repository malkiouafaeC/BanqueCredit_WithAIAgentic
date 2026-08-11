package formulAI.project.bank.banqueCredit.controller;

import formulAI.project.bank.banqueCredit.dto.HistoriqueDecisionDto;
import formulAI.project.bank.banqueCredit.service.HistoriqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/demandes")
@Tag(name = "Historique")
public class HistoriqueController {

    private final HistoriqueService historiqueService;

    public HistoriqueController(HistoriqueService historiqueService) {
        this.historiqueService = historiqueService;
    }

    @GetMapping("/{id}/historique")
    @Operation(summary = "Consulter l'historique des transitions d'une demande (ordre chronologique)")
    public ResponseEntity<List<HistoriqueDecisionDto>> lister(@PathVariable Long id) {
        return ResponseEntity.ok(historiqueService.listerParDemande(id));
    }
}

