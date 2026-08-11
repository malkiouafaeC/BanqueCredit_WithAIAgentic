package formulAI.project.bank.banqueCredit.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Couvre les 36 combinaisons (6 statuts source x 6 statuts cible) x 2 roles = 72 verifications,
 * incluant les 30 combinaisons du tableau architecture §4.4 (BE-CREDIT-002 DoD).
 */
class TransitionRulesTest {

    // Attendu : statut source -> (statut cible -> role autorise), absent = transition interdite pour tout role
    private static final Map<Statut, Map<Statut, Role>> EXPECTED = new EnumMap<>(Statut.class);

    static {
        Map<Statut, Role> fromBrouillon = new EnumMap<>(Statut.class);
        fromBrouillon.put(Statut.SOUMISE, Role.CONSEILLER);
        fromBrouillon.put(Statut.ANNULEE, Role.CONSEILLER);
        EXPECTED.put(Statut.BROUILLON, fromBrouillon);

        Map<Statut, Role> fromSoumise = new EnumMap<>(Statut.class);
        fromSoumise.put(Statut.EN_ANALYSE, Role.RESPONSABLE_CREDIT);
        fromSoumise.put(Statut.ANNULEE, Role.CONSEILLER);
        EXPECTED.put(Statut.SOUMISE, fromSoumise);

        Map<Statut, Role> fromEnAnalyse = new EnumMap<>(Statut.class);
        fromEnAnalyse.put(Statut.ACCEPTEE, Role.RESPONSABLE_CREDIT);
        fromEnAnalyse.put(Statut.REFUSEE, Role.RESPONSABLE_CREDIT);
        EXPECTED.put(Statut.EN_ANALYSE, fromEnAnalyse);

        EXPECTED.put(Statut.ACCEPTEE, new EnumMap<>(Statut.class));
        EXPECTED.put(Statut.REFUSEE, new EnumMap<>(Statut.class));
        EXPECTED.put(Statut.ANNULEE, new EnumMap<>(Statut.class));
    }

    @ParameterizedTest
    @EnumSource(Statut.class)
    void verifieToutesLesTransitionsPourChaqueStatutSource(Statut source) {
        for (Statut cible : Statut.values()) {
            Role roleAttendu = EXPECTED.get(source).get(cible);
            for (Role role : Role.values()) {
                boolean attendu = roleAttendu == role;
                boolean actuel = TransitionRules.verifierTransitionAutorisee(source, cible, role);
                assertThat(actuel)
                        .as("%s -> %s avec role %s", source, cible, role)
                        .isEqualTo(attendu);
            }
        }
    }

    @Test
    void statutsTerminauxNOntAucuneTransitionSortante() {
        for (Statut terminal : TransitionRules.statutsTerminaux()) {
            for (Statut cible : Statut.values()) {
                assertThat(TransitionRules.transitionExiste(terminal, cible)).isFalse();
            }
        }
    }

    @Test
    void transitionExisteDistingueDuRole() {
        assertThat(TransitionRules.transitionExiste(Statut.SOUMISE, Statut.EN_ANALYSE)).isTrue();
        assertThat(TransitionRules.verifierTransitionAutorisee(Statut.SOUMISE, Statut.EN_ANALYSE, Role.CONSEILLER)).isFalse();
        assertThat(TransitionRules.verifierTransitionAutorisee(Statut.SOUMISE, Statut.EN_ANALYSE, Role.RESPONSABLE_CREDIT)).isTrue();
    }

    @Test
    void gereLesParametresNulls() {
        assertThat(TransitionRules.verifierTransitionAutorisee(null, Statut.SOUMISE, Role.CONSEILLER)).isFalse();
        assertThat(TransitionRules.verifierTransitionAutorisee(Statut.BROUILLON, null, Role.CONSEILLER)).isFalse();
        assertThat(TransitionRules.verifierTransitionAutorisee(Statut.BROUILLON, Statut.SOUMISE, null)).isFalse();
    }
}

