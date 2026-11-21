package br.pucpr.biblioteca;

import br.pucpr.biblioteca.config.RelogioSimulado;
import br.pucpr.biblioteca.config.Sistema;
import br.pucpr.biblioteca.devolucao.ServicoDeEmailSimulado;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;

/** Monta um Sistema no perfil de teste (família em memória) com dados mínimos. */
public abstract class CenarioBase {
    protected static final String MATRICULA = "E001";
    protected static final String OUTRA_MATRICULA = "E002";
    protected static final String ATENDENTE = "AT01";
    protected static final LocalDate HOJE = LocalDate.of(2026, 11, 23);

    protected RelogioSimulado relogio;
    protected ServicoDeEmailSimulado email;
    protected Sistema sistema;
    protected int idLivro;

    @BeforeEach
    void montarCenario() {
        relogio = new RelogioSimulado(HOJE);
        email = new ServicoDeEmailSimulado();
        sistema = new Sistema(opcoes(Sistema.Opcoes.teste().comRelogio(relogio).comEmail(email)));
        sistema.tratadorDePessoa().inserirPessoa("Ana", "Souza", "11144477735", "ana@pucpr.edu.br");
        sistema.tratadorDePessoa().inserirPessoa("Bruno", "Alves", "33322211100", "bruno@pucpr.edu.br");
        sistema.tratadorDePessoa().inserirPessoa("Carlos", "Lima", "22233344405", "carlos@ib2b.com");
        sistema.tratadorDeEstudante().inserirEstudante("11144477735", MATRICULA);
        sistema.tratadorDeEstudante().inserirEstudante("33322211100", OUTRA_MATRICULA);
        sistema.tratadorDeAtendente().inserirAtendente("22233344405", ATENDENTE);
        idLivro = sistema.tratadorDeAcervo().inserirLivro("Padrões de Projeto").entidade().getIdLivro();
    }

    /** Subclasses podem trocar opções (banco, política, decoradores). */
    protected Sistema.Opcoes opcoes(Sistema.Opcoes base) {
        return base;
    }

    protected void emprestar(int idLivro, String matricula) {
        sistema.tratadorDeEmprestimo().solicitarReserva(idLivro, matricula);
        sistema.tratadorDeEmprestimo().efetivarEmprestimo(idLivro, matricula);
    }
}
