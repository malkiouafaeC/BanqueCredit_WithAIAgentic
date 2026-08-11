package formulAI.project.bank.banqueCredit.service.impl;

import formulAI.project.bank.banqueCredit.model.ScoreSimplifie;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SimulationServiceImplTest {

    private final SimulationServiceImpl service = new SimulationServiceImpl();

    @Test
    void calculerMensualiteCasTauxZeroEstAmortissementLineaire() {
        BigDecimal mensualite = service.calculerMensualite(new BigDecimal("12000"), 12, BigDecimal.ZERO);
        assertThat(mensualite).isEqualByComparingTo("1000.00");
    }

    @Test
    void calculerMensualiteFormuleAmortissementStandard() {
        // montant=10000, taux=12%, duree=12 -> tauxMensuel=0.01 ; formule normative RG-BANK-04
        BigDecimal mensualite = service.calculerMensualite(new BigDecimal("10000"), 12, new BigDecimal("12"));
        // Valeur de reference calculee via la formule d'amortissement standard : ~888.49
        assertThat(mensualite.doubleValue()).isCloseTo(888.49, within(0.05));
    }

    @Test
    void calculerTauxEndettementPourcentage() {
        BigDecimal taux = service.calculerTauxEndettement(new BigDecimal("300"), new BigDecimal("400"), new BigDecimal("2000"));
        assertThat(taux).isEqualByComparingTo("35.00");
    }

    @Test
    void scoreExcellentQuandTauxInferieurOuEgal20() {
        assertThat(service.calculerScore(new BigDecimal("20.00"))).isEqualTo(ScoreSimplifie.EXCELLENT);
        assertThat(service.calculerScore(new BigDecimal("0.00"))).isEqualTo(ScoreSimplifie.EXCELLENT);
    }

    @Test
    void scoreBonQuandTauxEntre20Exclu35Inclu() {
        assertThat(service.calculerScore(new BigDecimal("20.01"))).isEqualTo(ScoreSimplifie.BON);
        assertThat(service.calculerScore(new BigDecimal("35.00"))).isEqualTo(ScoreSimplifie.BON);
    }

    @Test
    void scoreMoyenQuandTauxEntre35Exclu50Inclu() {
        assertThat(service.calculerScore(new BigDecimal("35.01"))).isEqualTo(ScoreSimplifie.MOYEN);
        assertThat(service.calculerScore(new BigDecimal("50.00"))).isEqualTo(ScoreSimplifie.MOYEN);
    }

    @Test
    void scoreFaibleQuandTauxSuperieur50() {
        assertThat(service.calculerScore(new BigDecimal("50.01"))).isEqualTo(ScoreSimplifie.FAIBLE);
        assertThat(service.calculerScore(new BigDecimal("120.00"))).isEqualTo(ScoreSimplifie.FAIBLE);
    }
}

