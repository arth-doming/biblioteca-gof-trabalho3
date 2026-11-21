package br.pucpr.biblioteca;

import br.pucpr.biblioteca.circulacao.ComprovanteDeRenovacao;
import br.pucpr.biblioteca.comando.SolicitacaoRenovacao;
import br.pucpr.biblioteca.dominio.Emprestimo;
import br.pucpr.biblioteca.dominio.livro.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/** R6 — Solicitações de circulação como objetos (Command). */
class R06CommandTest extends CenarioBase {

    private SolicitacaoRenovacao renovacao(String id) {
        return new SolicitacaoRenovacao(id, MATRICULA, relogio.instant(), idLivro, MATRICULA);
    }

    @Test
    void R06_renovacaoRepetidaAcrescentaTrintaDias() {
        emprestar(idLivro, MATRICULA);
        var emp = sistema.tratadorDeEmprestimo();
        SolicitacaoRenovacao s = renovacao("REQ-1");

        ComprovanteDeRenovacao primeira = emp.submeter(s);
        ComprovanteDeRenovacao repetida = emp.submeter(s);

        assertEquals(HOJE.plusDays(Emprestimo.PRAZO_EMPRESTIMO_DIAS + 30), primeira.novoVencimento());
        assertEquals(primeira, repetida);
        assertEquals(1, repetida.numRenovacoes());
    }

    @Test
    void R06_solicitacoesDiferentesRespeitamLimiteDeDuas() {
        emprestar(idLivro, MATRICULA);
        var emp = sistema.tratadorDeEmprestimo();
        emp.submeter(renovacao("REQ-1"));
        emp.submeter(renovacao("REQ-2"));
        assertThrows(TransicaoInvalidaException.class, () -> emp.submeter(renovacao("REQ-3")));
    }

    @Test
    void R06_submissoesSimultaneasExecutamUmaVez() throws Exception {
        emprestar(idLivro, MATRICULA);
        var emp = sistema.tratadorDeEmprestimo();
        SolicitacaoRenovacao s = renovacao("REQ-CONC");
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<ComprovanteDeRenovacao>> futuros = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            futuros.add(pool.submit(() -> {
                largada.await();
                return emp.submeter(s);
            }));
        }
        largada.countDown();
        for (Future<ComprovanteDeRenovacao> f : futuros) {
            assertEquals(1, f.get().numRenovacoes());
        }
        pool.shutdown();
        // só uma renovação foi aplicada: ainda cabe exatamente mais uma
        assertEquals(2, emp.submeter(renovacao("REQ-OUTRA")).numRenovacoes());
    }

    @Test
    void R06_falhaTecnicaLiberaIdParaNovaTentativa() {
        // D10: erro técnico não "queima" o identificador; recusa de regra continua registrada
        emprestar(idLivro, MATRICULA);
        var emp = sistema.tratadorDeEmprestimo();
        int[] tentativas = {0};
        var instavel = new br.pucpr.biblioteca.comando.SolicitacaoDeCirculacao<ComprovanteDeRenovacao>(
                "REQ-INSTAVEL", MATRICULA, relogio.instant()) {
            @Override
            public ComprovanteDeRenovacao executar(br.pucpr.biblioteca.circulacao.FachadaDeCirculacao receptor) {
                if (tentativas[0]++ == 0) throw new IllegalStateException("banco indisponível");
                return receptor.renovarEmprestimo(idLivro, MATRICULA);
            }
        };
        assertThrows(IllegalStateException.class, () -> emp.submeter(instavel));
        assertEquals(1, emp.submeter(instavel).numRenovacoes(), "segunda tentativa executa");
        assertEquals(1, emp.submeter(instavel).numRenovacoes(), "terceira devolve o mesmo resultado");
        assertEquals(2, tentativas[0]);
    }
}
