package br.pucpr.biblioteca;

import br.pucpr.biblioteca.circulacao.ComprovanteDeRenovacao;
import br.pucpr.biblioteca.dominio.Emprestimo;
import br.pucpr.biblioteca.dominio.livro.CodigoEstadoLivro;
import br.pucpr.biblioteca.dominio.livro.Livro;
import br.pucpr.biblioteca.dominio.livro.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** R1 — Ciclo de vida do livro (State). */
class R01StateTest extends CenarioBase {

    @Test
    void R01_terceiraRenovacaoRecusada() {
        emprestar(idLivro, MATRICULA);
        var emp = sistema.tratadorDeEmprestimo();
        emp.renovarEmprestimo(idLivro, MATRICULA);
        ComprovanteDeRenovacao segunda = emp.renovarEmprestimo(idLivro, MATRICULA);
        assertEquals(2, segunda.numRenovacoes());

        assertThrows(TransicaoInvalidaException.class, () -> emp.renovarEmprestimo(idLivro, MATRICULA));

        // vencimento não mudou: renovar de novo ainda é recusado e o livro continua RENOVADO
        LocalDate vencimentoEsperado = HOJE.plusDays(Emprestimo.PRAZO_EMPRESTIMO_DIAS + 60);
        assertEquals(vencimentoEsperado, segunda.novoVencimento());
        assertEquals(CodigoEstadoLivro.RENOVADO, estado());
        relogio.avancarDias(Emprestimo.PRAZO_EMPRESTIMO_DIAS + 60);
        var devolucao = sistema.tratadorDeDevolucao().registrarDevolucao(idLivro, MATRICULA, ATENDENTE);
        assertEquals(vencimentoEsperado, devolucao.dataVencimento());
        assertEquals(0, devolucao.diasAtraso());
    }

    @Test
    void R01_terceiraRenovacaoNaoAlteraVencimentoNoObjeto() {
        Livro livro = new Livro(10, "X");
        Emprestimo e = Emprestimo.novo(10, MATRICULA, HOJE);
        livro.solicitarReserva(true);
        livro.efetivarEmprestimo(true);
        livro.renovarEmprestimo(e);
        livro.renovarEmprestimo(e);
        LocalDate antes = e.getDataVencimento();
        assertThrows(TransicaoInvalidaException.class, () -> livro.renovarEmprestimo(e));
        assertEquals(antes, e.getDataVencimento());
        assertEquals(2, e.getNumRenovacoes());
    }

    @Test
    void R01_livroDevolvidoNaoPodeSerReservado() {
        emprestar(idLivro, MATRICULA);
        sistema.tratadorDeDevolucao().registrarDevolucao(idLivro, MATRICULA, ATENDENTE);
        assertEquals(CodigoEstadoLivro.DEVOLVIDO, estado());

        assertThrows(TransicaoInvalidaException.class,
                () -> sistema.tratadorDeEmprestimo().solicitarReserva(idLivro, OUTRA_MATRICULA));
        assertEquals(CodigoEstadoLivro.DEVOLVIDO, estado());
        assertTrue(sistema.tratadorDeEmprestimo().consultarReservas(OUTRA_MATRICULA).isEmpty());

        sistema.tratadorDeDevolucao().inspecionar(idLivro, true);
        assertEquals(CodigoEstadoLivro.LIVRE, estado());
        assertNotNull(sistema.tratadorDeEmprestimo().solicitarReserva(idLivro, OUTRA_MATRICULA));
    }

    @Test
    void R01_eventoNaoAceitoNaoAlteraLivro() {
        Livro livro = new Livro(1, "X");
        assertThrows(TransicaoInvalidaException.class, livro::registrarDevolucao);
        assertThrows(TransicaoInvalidaException.class, () -> livro.efetivarEmprestimo(true));
        assertThrows(TransicaoInvalidaException.class, () -> livro.solicitarReserva(false));
        assertEquals(CodigoEstadoLivro.LIVRE, livro.getEstado());
    }

    @Test
    void R01_laudoIrrecuperavelLevaReparandoAIncinerado() {
        var dev = sistema.tratadorDeDevolucao();
        emprestar(idLivro, MATRICULA);
        dev.registrarDevolucao(idLivro, MATRICULA, ATENDENTE);
        dev.inspecionar(idLivro, false);
        assertEquals(CodigoEstadoLivro.REPARANDO, estado());
        dev.avaliarReparo(idLivro, false);
        assertEquals(CodigoEstadoLivro.INCINERADO, estado());
        assertTrue(sistema.tratadorDeAcervo().consultarLivro(idLivro).orElseThrow().isIncineracaoRegistrada());
        assertTrue(sistema.tratadorDeAcervo().excluirLivro(idLivro));
    }

    @Test
    void R01_estadoGravadoEReconstituido() {
        sistema.tratadorDeEmprestimo().solicitarReserva(idLivro, MATRICULA);
        Livro relido = sistema.tratadorDeAcervo().consultarLivro(idLivro).orElseThrow();
        assertEquals(CodigoEstadoLivro.RESERVADO, relido.getEstado());
        // o objeto reconstituído responde aos eventos conforme o estado gravado
        assertThrows(TransicaoInvalidaException.class, () -> relido.solicitarReserva(true));
    }

    private CodigoEstadoLivro estado() {
        return sistema.tratadorDeAcervo().consultarLivro(idLivro).orElseThrow().getEstado();
    }
}
