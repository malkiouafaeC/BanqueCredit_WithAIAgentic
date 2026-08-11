package formulAI.project.bank.banqueCredit.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * DEC-ENT-003 - Table de transitions de statut autorisees (RG-BANK-05, architecture §4.4),
 * codee en dur (pas de state-machine externe : 6 statuts / 6 transitions ne le justifient pas).
 * Methode dediee testable independamment : verifierTransitionAutorisee(statutActuel, statutCible, role).
 */
public final class TransitionRules {

    private static final Map<Statut, Map<Statut, Role>> TRANSITIONS = new EnumMap<>(Statut.class);

    static {
        Map<Statut, Role> fromBrouillon = new EnumMap<>(Statut.class);
        fromBrouillon.put(Statut.SOUMISE, Role.CONSEILLER);
        fromBrouillon.put(Statut.ANNULEE, Role.CONSEILLER);
        TRANSITIONS.put(Statut.BROUILLON, fromBrouillon);

        Map<Statut, Role> fromSoumise = new EnumMap<>(Statut.class);
        fromSoumise.put(Statut.EN_ANALYSE, Role.RESPONSABLE_CREDIT);
        fromSoumise.put(Statut.ANNULEE, Role.CONSEILLER);
        TRANSITIONS.put(Statut.SOUMISE, fromSoumise);

        Map<Statut, Role> fromEnAnalyse = new EnumMap<>(Statut.class);
        fromEnAnalyse.put(Statut.ACCEPTEE, Role.RESPONSABLE_CREDIT);
        fromEnAnalyse.put(Statut.REFUSEE, Role.RESPONSABLE_CREDIT);
        TRANSITIONS.put(Statut.EN_ANALYSE, fromEnAnalyse);

        // ACCEPTEE, REFUSEE, ANNULEE sont terminaux : aucune transition sortante.
        TRANSITIONS.put(Statut.ACCEPTEE, new EnumMap<>(Statut.class));
        TRANSITIONS.put(Statut.REFUSEE, new EnumMap<>(Statut.class));
        TRANSITIONS.put(Statut.ANNULEE, new EnumMap<>(Statut.class));
    }

    private TransitionRules() {
    }

    /**
     * Verifie si la transition statutActuel -> statutCible est autorisee pour le role donne.
     */
    public static boolean verifierTransitionAutorisee(Statut statutActuel, Statut statutCible, Role role) {
        if (statutActuel == null || statutCible == null || role == null) {
            return false;
        }
        Map<Statut, Role> cibles = TRANSITIONS.get(statutActuel);
        if (cibles == null) {
            return false;
        }
        Role roleRequis = cibles.get(statutCible);
        return roleRequis != null && roleRequis == role;
    }

    /**
     * Indique si la transition est structurellement autorisee (independamment du role),
     * utile pour distinguer TRANSITION_INVALIDE (409) de ROLE_NON_AUTORISE (403).
     */
    public static boolean transitionExiste(Statut statutActuel, Statut statutCible) {
        if (statutActuel == null || statutCible == null) {
            return false;
        }
        Map<Statut, Role> cibles = TRANSITIONS.get(statutActuel);
        return cibles != null && cibles.containsKey(statutCible);
    }

    public static Set<Statut> statutsTerminaux() {
        return EnumSet.of(Statut.ACCEPTEE, Statut.REFUSEE, Statut.ANNULEE);
    }
}

