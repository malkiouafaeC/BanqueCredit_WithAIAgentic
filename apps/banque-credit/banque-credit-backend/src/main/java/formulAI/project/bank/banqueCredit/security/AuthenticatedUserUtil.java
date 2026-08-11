package formulAI.project.bank.banqueCredit.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Petit utilitaire pour recuperer l'identite de l'utilisateur authentifie courant (auteur HistoriqueDecision). */
public final class AuthenticatedUserUtil {

    private AuthenticatedUserUtil() {
    }

    public static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Aucun utilisateur authentifie dans le contexte de securite");
        }
        return authentication.getName();
    }
}

