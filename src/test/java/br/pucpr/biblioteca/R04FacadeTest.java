package br.pucpr.biblioteca;

import br.pucpr.biblioteca.acesso.Acervo;
import br.pucpr.biblioteca.acesso.Movimentacao;
import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.cadastro.CadastroDeEstudante;
import br.pucpr.biblioteca.cadastro.CadastroDePessoa;
import br.pucpr.biblioteca.circulacao.ComprovanteDeEmprestimo;
import br.pucpr.biblioteca.circulacao.FachadaDeCirculacao;
import br.pucpr.biblioteca.dados.memoria.FabricaEmMemoria;
import br.pucpr.biblioteca.devolucao.PublicadorDeDevolucao;
import br.pucpr.biblioteca.dominio.Emprestimo;
import br.pucpr.biblioteca.dominio.Transacao;
import br.pucpr.biblioteca.dominio.livro.CodigoEstadoLivro;
import br.pucpr.biblioteca.dominio.livro.Livro;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/** R4 — Entrada única para as operações de circulação (Facade). */
class R04FacadeTest extends CenarioBase {

    @Test
    void R04_efetivarEmprestimoPelaFachada() {
        var emp = sistema.tratadorDeEmprestimo();
        emp.solicitarReserva(idLivro, MATRICULA);
        assertTrue(emp.consultarReservas(idLivro, MATRICULA).isPresent());

        ComprovanteDeEmprestimo c = emp.efetivarEmprestimo(idLivro, MATRICULA);

        assertTrue(emp.consultarReservas(idLivro, MATRICULA).isEmpty(), "reserva ativa removida");
        assertTrue(c.idEmprestimo() > 0, "empréstimo criado");
        assertEquals(HOJE.plusDays(Emprestimo.PRAZO_EMPRESTIMO_DIAS), c.dataVencimento());
        assertEquals(CodigoEstadoLivro.EMPRESTADO,
                sistema.tratadorDeAcervo().consultarLivro(idLivro).orElseThrow().getEstado());
        // o que volta para a interface é um comprovante imutável, não uma coleção do domínio
        assertTrue(ComprovanteDeEmprestimo.class.isRecord());
        assertFalse(Collection.class.isAssignableFrom(ComprovanteDeEmprestimo.class));
    }

    @Test
    void R04_emprestimoSemReservaDoMesmoEstudanteRecusado() {
        sistema.tratadorDeEmprestimo().solicitarReserva(idLivro, MATRICULA);
        assertThrows(RuntimeException.class,
                () -> sistema.tratadorDeEmprestimo().efetivarEmprestimo(idLivro, OUTRA_MATRICULA));
        assertTrue(sistema.tratadorDeEmprestimo().consultarReservas(idLivro, MATRICULA).isPresent());
    }

    @Test
    void R04_falhaNoMeioDaOperacaoDesfazTudo() {
        FabricaEmMemoria f = new FabricaEmMemoria();
        Acervo acervo = new Acervo(f.criarRepositorioDeLivros());
        Relacionamento rel = new Relacionamento(f.criarRepositorioDeRelacionamento());
        // Movimentacao que falha ao gravar o empréstimo (passo 3 de 4)
        Movimentacao mov = new Movimentacao(f.criarRepositorioDeMovimentacao()) {
            @Override
            public void adicionar(Transacao t) {
                if (t instanceof Emprestimo) throw new IllegalStateException("disco cheio");
                super.adicionar(t);
            }
        };
        new CadastroDePessoa(f.unidadeDeTrabalho(), rel).incluir(new CadastroDePessoa.Dados("Ana", "S", "11144477735", "a@x"));
        new CadastroDeEstudante(f.unidadeDeTrabalho(), rel).incluir(new CadastroDeEstudante.Dados("11144477735", "E1"));
        acervo.adicionarLivro(new Livro(1, "Livro"));
        FachadaDeCirculacao fachada = new FachadaDeCirculacao(acervo, rel, mov, f.unidadeDeTrabalho(),
                new PublicadorDeDevolucao(), relogio);
        fachada.solicitarReserva(1, "E1");

        assertThrows(IllegalStateException.class, () -> fachada.efetivarEmprestimo(1, "E1"));

        assertEquals(CodigoEstadoLivro.RESERVADO, acervo.consultarLivro(1).orElseThrow().getEstado());
        assertTrue(fachada.consultarReserva(1, "E1").isPresent(), "reserva removida foi restaurada");
    }
}
