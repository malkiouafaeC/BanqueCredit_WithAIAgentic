package formulAI.project.bank.banqueCredit.controller;

import formulAI.project.bank.banqueCredit.dto.DecisionRefusDto;
import formulAI.project.bank.banqueCredit.dto.DemandeCreditDto;
import formulAI.project.bank.banqueCredit.service.DecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/demandes")
@Tag(name = "Decisions")
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @PostMapping("/{id}/soumettre")
    @Operation(summary = "Soumettre une demande Brouillon (CONSEILLER)")
    public ResponseEntity<DemandeCreditDto> soumettre(@PathVariable Long id) {
        return ResponseEntity.ok(decisionService.soumettre(id));
    }

    @PostMapping("/{id}/annuler")
    @Operation(summary = "Annuler une demande Brouillon ou Soumise (CONSEILLER)")
    public ResponseEntity<DemandeCreditDto> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(decisionService.annuler(id));
    }

    @PostMapping("/{id}/analyser")
    @Operation(summary = "Passer une demande Soumise en analyse (RESPONSABLE_CREDIT)")
    public ResponseEntity<DemandeCreditDto> analyser(@PathVariable Long id) {
        return ResponseEntity.ok(decisionService.analyser(id));
    }

    @PostMapping("/{id}/accepter")
    @Operation(summary = "Accepter une demande En analyse eligible (RESPONSABLE_CREDIT)")
    public ResponseEntity<DemandeCreditDto> accepter(@PathVariable Long id) {
        return ResponseEntity.ok(decisionService.accepter(id));
    }

    @PostMapping("/{id}/refuser")
    @Operation(summary = "Refuser une demande En analyse avec commentaire obligatoire (RESPONSABLE_CREDIT)")
    public ResponseEntity<DemandeCreditDto> refuser(@PathVariable Long id, @RequestBody(required = false) DecisionRefusDto request) {
        String commentaire = request != null ? request.getCommentaire() : null;
        return ResponseEntity.ok(decisionService.refuser(id, commentaire));
    }
}

