package br.pucpr.biblioteca.dados;

import br.pucpr.biblioteca.dominio.Devolucao;
import br.pucpr.biblioteca.dominio.Emprestimo;
import br.pucpr.biblioteca.dominio.Reserva;
import br.pucpr.biblioteca.dominio.Transacao;

import java.util.List;
import java.util.Optional;

/** PADRÃO ABSTRACT FACTORY — produto abstrato. */
public interface RepositorioDeMovimentacao {
    void adicionar(Transacao transacao);
    void atualizar(Transacao transacao);
    void remover(Transacao transacao);
    Optional<Reserva> buscarReservaAtivaDoLivro(int idLivro);
    Optional<Emprestimo> buscarEmprestimo(int idLivro, String matricula);
    Optional<Devolucao> buscarUltimaDevolucao(int idLivro, String matricula);
    List<Reserva> listarReservas(String matricula);
}
