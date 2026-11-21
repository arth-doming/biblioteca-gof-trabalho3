package br.pucpr.biblioteca.dados;

import br.pucpr.biblioteca.dominio.Atendente;
import br.pucpr.biblioteca.dominio.Estudante;
import br.pucpr.biblioteca.dominio.Pessoa;

import java.util.Optional;

/** PADRÃO ABSTRACT FACTORY — produto abstrato. */
public interface RepositorioDeRelacionamento {
    void salvarPessoa(Pessoa pessoa);
    void removerPessoa(String cpf);
    Optional<Pessoa> buscarPessoa(String cpf);

    void salvarEstudante(Estudante estudante);
    void removerEstudante(String matricula);
    Optional<Estudante> buscarEstudante(String matricula);

    void salvarAtendente(Atendente atendente);
    void removerAtendente(String idAtendente);
    Optional<Atendente> buscarAtendente(String idAtendente);
}
