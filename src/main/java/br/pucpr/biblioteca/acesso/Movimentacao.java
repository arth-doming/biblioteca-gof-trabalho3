package br.pucpr.biblioteca.acesso;

import br.pucpr.biblioteca.dados.RepositorioDeMovimentacao;
import br.pucpr.biblioteca.dominio.Devolucao;
import br.pucpr.biblioteca.dominio.Emprestimo;
import br.pucpr.biblioteca.dominio.Reserva;
import br.pucpr.biblioteca.dominio.Transacao;

import java.util.List;
import java.util.Optional;

/** Ponto interno único de acesso às transações (Trabalho 02). Só a fachada de circulação o usa. */
public class Movimentacao {
    private final RepositorioDeMovimentacao repositorio;

    public Movimentacao(RepositorioDeMovimentacao repositorio) {
        this.repositorio = repositorio;
    }

    public void adicionar(Transacao transacao) { repositorio.adicionar(transacao); }
    public void atualizar(Transacao transacao) { repositorio.atualizar(transacao); }
    public void remover(Transacao transacao) { repositorio.remover(transacao); }

    public Optional<Reserva> consultarReservaAtiva(int idLivro) { return repositorio.buscarReservaAtivaDoLivro(idLivro); }
    public Optional<Emprestimo> consultarEmprestimo(int idLivro, String matricula) { return repositorio.buscarEmprestimo(idLivro, matricula); }
    public Optional<Devolucao> consultarDevolucao(int idLivro, String matricula) { return repositorio.buscarUltimaDevolucao(idLivro, matricula); }
    public List<Reserva> consultarReservas(String matricula) { return repositorio.listarReservas(matricula); }
}
