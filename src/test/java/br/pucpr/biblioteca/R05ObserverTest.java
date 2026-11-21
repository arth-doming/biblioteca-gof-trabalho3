package br.pucpr.biblioteca;

import br.pucpr.biblioteca.circulacao.ComprovanteDeDevolucao;
import br.pucpr.biblioteca.devolucao.EventoDevolucao;
import br.pucpr.biblioteca.devolucao.OuvinteDeDevolucao;
import br.pucpr.biblioteca.dominio.Emprestimo;
import br.pucpr.biblioteca.dominio.livro.CodigoEstadoLivro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** R5 — Reações independentes à devolução (Observer). */
class R05ObserverTest extends CenarioBase {

    @Test
    void R05_devolucaoAtrasadaNotificaOuvintes() {
        emprestar(idLivro, MATRICULA);
        relogio.avancarDias(Emprestimo.PRAZO_EMPRESTIMO_DIAS + 5);

        ComprovanteDeDevolucao c = sistema.tratadorDeDevolucao().registrarDevolucao(idLivro, MATRICULA, ATENDENTE);

        assertEquals(5, c.diasAtraso());
        assertTrue(sistema.tratadorDeDevolucao().livrosAguardandoInspecao().contains(idLivro), "inspeção");
        assertEquals(new BigDecimal("5.00"),
                sistema.tratadorDeMulta().consultarMulta(MATRICULA).get(0).getValor(), "multa");
        assertEquals(1, sistema.tratadorDeDevolucao().indicadores().devolucoesAtrasadas(), "indicadores");
        assertEquals(1, email.enviadas().size(), "e-mail");
        assertEquals(CodigoEstadoLivro.DEVOLVIDO,
                sistema.tratadorDeAcervo().consultarLivro(idLivro).orElseThrow().getEstado());
    }

    @Test
    void R05_devolucaoNoPrazoSemMulta() {
        emprestar(idLivro, MATRICULA);
        relogio.avancarDias(3);
        sistema.tratadorDeDevolucao().registrarDevolucao(idLivro, MATRICULA, ATENDENTE);

        assertTrue(sistema.tratadorDeMulta().consultarMulta(MATRICULA).isEmpty());
        assertEquals(1, sistema.tratadorDeDevolucao().indicadores().devolucoes());
        assertEquals(0, sistema.tratadorDeDevolucao().indicadores().devolucoesAtrasadas());
        assertEquals(CodigoEstadoLivro.DEVOLVIDO,
                sistema.tratadorDeAcervo().consultarLivro(idLivro).orElseThrow().getEstado());
    }

    @Test
    void R05_falhaDoEmailNaoAfetaDemaisNemDesfazDevolucao() {
        email.simularFalha(true);
        emprestar(idLivro, MATRICULA);
        relogio.avancarDias(Emprestimo.PRAZO_EMPRESTIMO_DIAS + 2);

        ComprovanteDeDevolucao c = sistema.tratadorDeDevolucao().registrarDevolucao(idLivro, MATRICULA, ATENDENTE);

        assertTrue(c.falhasDeOuvintes().containsKey("email"));
        assertEquals(1, sistema.tratadorDeMulta().consultarMulta(MATRICULA).size());
        assertEquals(1, sistema.tratadorDeDevolucao().indicadores().devolucoes());
        assertEquals(CodigoEstadoLivro.DEVOLVIDO,
                sistema.tratadorDeAcervo().consultarLivro(idLivro).orElseThrow().getEstado());
    }

    @Test
    void R05_novoOuvinteSemAlterarTratador() {
        List<EventoDevolucao> recebidos = new ArrayList<>();
        sistema.publicador().registrar(new OuvinteDeDevolucao() {
            public String nome() { return "auditoria"; }
            public void aoDevolver(EventoDevolucao e) { recebidos.add(e); }
        });
        emprestar(idLivro, MATRICULA);
        sistema.tratadorDeDevolucao().registrarDevolucao(idLivro, MATRICULA, ATENDENTE);
        assertEquals(1, recebidos.size());
        assertEquals(MATRICULA, recebidos.get(0).matricula());
    }
}
