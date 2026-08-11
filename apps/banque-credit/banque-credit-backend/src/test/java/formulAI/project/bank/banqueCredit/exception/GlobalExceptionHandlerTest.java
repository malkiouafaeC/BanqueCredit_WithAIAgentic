package formulAI.project.bank.banqueCredit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void transitionInvalideRetourne409() {
        ResponseEntity<formulAI.project.bank.banqueCredit.dto.ErrorResponseDto> resp =
                handler.handleTransitionInvalide(new TransitionInvalideException("Transition invalide"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody().getCode()).isEqualTo("TRANSITION_INVALIDE");
    }

    @Test
    void eligibiliteNonRespecteeRetourne422() {
        var resp = handler.handleEligibiliteNonRespectee(new EligibiliteNonRespecteeException("non eligible"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resp.getBody().getCode()).isEqualTo("ELIGIBILITE_NON_RESPECTEE");
    }

    @Test
    void commentaireObligatoireRetourne400() {
        var resp = handler.handleCommentaireObligatoire(new CommentaireObligatoireException("commentaire obligatoire"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().getCode()).isEqualTo("COMMENTAIRE_OBLIGATOIRE");
    }

    @Test
    void validationMetierRetourne400() {
        var resp = handler.handleValidationMetier(new ValidationMetierException("hors bornes", "montantDemande"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().getCode()).isEqualTo("VALEUR_HORS_BORNES");
        assertThat(resp.getBody().getChamp()).isEqualTo("montantDemande");
    }

    @Test
    void ressourceNotFoundRetourne404() {
        var resp = handler.handleRessourceNotFound(new RessourceNotFoundException("introuvable"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody().getCode()).isEqualTo("RESSOURCE_INTROUVABLE");
    }

    @Test
    void roleNonAutoriseRetourne403() {
        var resp = handler.handleRoleNonAutorise(new RoleNonAutoriseException("role non autorise"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody().getCode()).isEqualTo("ROLE_NON_AUTORISE");
    }

    @Test
    void accessDeniedRetourne403() {
        var resp = handler.handleAccessDenied(new AccessDeniedException("denied"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resp.getBody().getCode()).isEqualTo("ROLE_NON_AUTORISE");
    }

    @Test
    void badCredentialsRetourne401() {
        var resp = handler.handleBadCredentials(new BadCredentialsException("bad"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody().getCode()).isEqualTo("IDENTIFIANTS_INVALIDES");
    }
}

