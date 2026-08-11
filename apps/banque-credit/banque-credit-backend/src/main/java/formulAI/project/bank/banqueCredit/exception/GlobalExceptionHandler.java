package formulAI.project.bank.banqueCredit.exception;

import formulAI.project.bank.banqueCredit.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * DEC-005 - Traduction centralisee des exceptions metier/techniques vers un corps HTTP standardise.
 * Aucune stack trace n'est exposee au client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponseDto.ChampErreur> erreurs = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponseDto.ChampErreur(fe.getField(), fe.getDefaultMessage()))
                .toList();
        ErrorResponseDto body = new ErrorResponseDto("CHAMP_OBLIGATOIRE",
                "Un ou plusieurs champs sont invalides", null, Instant.now());
        body.setErreurs(erreurs);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(TransitionInvalideException.class)
    public ResponseEntity<ErrorResponseDto> handleTransitionInvalide(TransitionInvalideException ex) {
        return build(HttpStatus.CONFLICT, "TRANSITION_INVALIDE", ex.getMessage(), null);
    }

    @ExceptionHandler(EligibiliteNonRespecteeException.class)
    public ResponseEntity<ErrorResponseDto> handleEligibiliteNonRespectee(EligibiliteNonRespecteeException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "ELIGIBILITE_NON_RESPECTEE", ex.getMessage(), null);
    }

    @ExceptionHandler(CommentaireObligatoireException.class)
    public ResponseEntity<ErrorResponseDto> handleCommentaireObligatoire(CommentaireObligatoireException ex) {
        return build(HttpStatus.BAD_REQUEST, "COMMENTAIRE_OBLIGATOIRE", ex.getMessage(), "commentaire");
    }

    @ExceptionHandler(ValidationMetierException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationMetier(ValidationMetierException ex) {
        return build(HttpStatus.BAD_REQUEST, "VALEUR_HORS_BORNES", ex.getMessage(), ex.getChamp());
    }

    @ExceptionHandler(RessourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleRessourceNotFound(RessourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "RESSOURCE_INTROUVABLE", ex.getMessage(), null);
    }

    @ExceptionHandler(RoleNonAutoriseException.class)
    public ResponseEntity<ErrorResponseDto> handleRoleNonAutorise(RoleNonAutoriseException ex) {
        return build(HttpStatus.FORBIDDEN, "ROLE_NON_AUTORISE", ex.getMessage(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "ROLE_NON_AUTORISE", "Acces refuse : role non autorise pour cette action", null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "IDENTIFIANTS_INVALIDES", "Identifiants invalides", null);
    }

    private ResponseEntity<ErrorResponseDto> build(HttpStatus status, String code, String message, String champ) {
        return ResponseEntity.status(status).body(new ErrorResponseDto(code, message, champ, Instant.now()));
    }
}

