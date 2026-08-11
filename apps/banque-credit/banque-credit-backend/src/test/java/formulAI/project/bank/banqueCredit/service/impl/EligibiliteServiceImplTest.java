package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.exception.EligibiliteNonRespecteeException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * QA-TASK-004 - Unitaire pur de EligibiliteServiceImpl (pure function, pas de mock necessaire).
 * Business rule : RG-BANK-06 (conditions cumulatives d'eligibilite, bornes inclusives 35%/1500/50000).
 *
 * Note (deviation documentee, cf plan de tests §1.3 et DecisionControllerIntegrationTest) :
 * la combinaison simultanee des 3 bornes exactes de l'AC-013-1 (35% ET 1500 ET 50000) est
 * mathematiquement infaisable sous les contraintes RG-BANK-02/03/04 (mensualite minimale a
 * montant=50000/duree=84/taux=0 donne deja ~39.7% de taux d'endettement avec revenu=1500).
 * Chaque borne est donc verifiee independamment ici, avec les 2 autres parametres tres larges,
 * ce qui est suffisant pour couvrir RG-BANK-06 sans introduire un cas de test irrealiste.
 */
class EligibiliteServiceImplTest {

    private final EligibiliteServiceImpl eligibiliteService = new EligibiliteServiceImpl();

    @Test
    void verifierEligibilite_shouldNotThrow_whenTauxEndettementExactement35() {
        assertThatCode(() -> eligibiliteService.verifierEligibilite(new BigDecimal("35"), new BigDecimal("100000"), new BigDecimal("2000")))
                .doesNotThrowAnyException();
    }

    @Test
    void verifierEligibilite_shouldThrow_whenTauxEndettement35Point01() {
        assertThatThrownBy(() -> eligibiliteService.verifierEligibilite(new BigDecimal("35.01"), new BigDecimal("100000"), new BigDecimal("2000")))
                .isInstanceOf(EligibiliteNonRespecteeException.class)
                .hasMessageContaining("taux d'endettement");
    }

    @Test
    void verifierEligibilite_shouldNotThrow_whenRevenuExactement1500() {
        assertThatCode(() -> eligibiliteService.verifierEligibilite(BigDecimal.ZERO, new BigDecimal("1500"), new BigDecimal("2000")))
                .doesNotThrowAnyException();
    }

    @Test
    void verifierEligibilite_shouldThrow_whenRevenu1499() {
        assertThatThrownBy(() -> eligibiliteService.verifierEligibilite(BigDecimal.ZERO, new BigDecimal("1499"), new BigDecimal("2000")))
                .isInstanceOf(EligibiliteNonRespecteeException.class)
                .hasMessageContaining("revenu");
    }

    @Test
    void verifierEligibilite_shouldNotThrow_whenMontantExactement50000() {
        assertThatCode(() -> eligibiliteService.verifierEligibilite(BigDecimal.ZERO, new BigDecimal("100000"), new BigDecimal("50000")))
                .doesNotThrowAnyException();
    }

    @Test
    void verifierEligibilite_shouldThrow_whenMontant50001() {
        assertThatThrownBy(() -> eligibiliteService.verifierEligibilite(BigDecimal.ZERO, new BigDecimal("100000"), new BigDecimal("50001")))
                .isInstanceOf(EligibiliteNonRespecteeException.class)
                .hasMessageContaining("montant");
    }

    @Test
    void verifierEligibilite_shouldThrow_whenOnlyMontantConditionFails_evenIfOthersConform_E13() {
        // E13 : une seule condition non respectee suffit a bloquer l'acceptation.
        assertThatThrownBy(() -> eligibiliteService.verifierEligibilite(new BigDecimal("10"), new BigDecimal("5000"), new BigDecimal("50001")))
                .isInstanceOf(EligibiliteNonRespecteeException.class);
    }

    @Test
    void verifierEligibilite_shouldThrowOnFirstFailingCondition_whenMultipleConditionsFail() {
        // taux > 35 ET revenu < 1500 : le taux est verifie en premier -> message taux.
        assertThatThrownBy(() -> eligibiliteService.verifierEligibilite(new BigDecimal("50"), new BigDecimal("1000"), new BigDecimal("2000")))
                .isInstanceOf(EligibiliteNonRespecteeException.class)
                .hasMessageContaining("taux d'endettement");
    }
}

