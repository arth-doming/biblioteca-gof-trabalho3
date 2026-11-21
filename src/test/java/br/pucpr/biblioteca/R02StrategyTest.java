package br.pucpr.biblioteca;

import br.pucpr.biblioteca.financeiro.multa.Multa;
import br.pucpr.biblioteca.financeiro.multa.PoliticaComTeto;
import br.pucpr.biblioteca.financeiro.multa.PoliticaComTolerancia;
import br.pucpr.biblioteca.financeiro.multa.PoliticaDeMulta;
import br.pucpr.biblioteca.financeiro.multa.PoliticaOriginal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** R2 — Políticas de cálculo de multa (Strategy). */
class R02StrategyTest extends CenarioBase {

    @Test
    void R02_cincoDiasOriginalETolerancia() {
        assertEquals(new BigDecimal("5.00"), new PoliticaOriginal().calcular(5));
        assertEquals(new BigDecimal("3.00"), new PoliticaComTolerancia().calcular(5));
    }

    @Test
    void R02_semAtrasoMultaZero() {
        for (PoliticaDeMulta p : List.of(new PoliticaOriginal(), new PoliticaComTeto(), new PoliticaComTolerancia())) {
            assertEquals(0, BigDecimal.ZERO.compareTo(p.calcular(0)), p.nome());
        }
        assertTrue(sistema.tratadorDeMulta().emitirMulta(idLivro, MATRICULA, HOJE, HOJE).isEmpty());
        assertTrue(sistema.tratadorDeMulta().emitirMulta(idLivro, MATRICULA, HOJE, HOJE.minusDays(3)).isEmpty());
    }

    @Test
    void R02_tetoDeVinteReais() {
        assertEquals(new BigDecimal("20.00"), new PoliticaComTeto().calcular(45));
        assertEquals(new BigDecimal("7.00"), new PoliticaComTeto().calcular(7));
    }

    @Test
    void R02_politicaVigenteVemDaConfiguracaoEMultaGravaPolitica() {
        sistema.tratadorDeAdministracao().definirPoliticaDeMulta(PoliticaComTolerancia.NOME);
        sistema.tratadorDeMulta().emitirMulta(idLivro, MATRICULA, HOJE, HOJE.plusDays(5));
        Multa m = sistema.tratadorDeMulta().consultarMulta(MATRICULA).get(0);
        assertEquals(new BigDecimal("3.00"), m.getValor());
        assertEquals(PoliticaComTolerancia.NOME, m.getPoliticaAplicada());
    }

    @Test
    void R02_trocaDePoliticaNaoRecalculaMultaEmitida() {
        sistema.tratadorDeMulta().emitirMulta(idLivro, MATRICULA, HOJE, HOJE.plusDays(5));
        sistema.tratadorDeAdministracao().definirPoliticaDeMulta(PoliticaComTolerancia.NOME);
        Multa emitida = sistema.tratadorDeMulta().consultarMulta(MATRICULA).get(0);
        assertEquals(new BigDecimal("5.00"), emitida.getValor());
        assertEquals(PoliticaOriginal.NOME, emitida.getPoliticaAplicada());
    }

    @Test
    void R02_novaPoliticaSemAlterarServico() {
        // extensão ao vivo: uma política nova é só mais uma classe registrada na configuração
        PoliticaDeMulta dobro = new PoliticaDeMulta() {
            public String nome() { return "DOBRO"; }
            public BigDecimal calcular(long dias) { return new PoliticaOriginal().calcular(dias).multiply(BigDecimal.valueOf(2)); }
        };
        sistema.configuracao().registrar(dobro);
        sistema.tratadorDeAdministracao().definirPoliticaDeMulta("DOBRO");
        sistema.tratadorDeMulta().emitirMulta(idLivro, MATRICULA, HOJE, HOJE.plusDays(5));
        assertEquals(new BigDecimal("10.00"), sistema.tratadorDeMulta().consultarMulta(MATRICULA).get(0).getValor());
    }
}
