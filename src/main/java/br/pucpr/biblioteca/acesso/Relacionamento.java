package br.pucpr.biblioteca.acesso;

import br.pucpr.biblioteca.dados.RepositorioDeRelacionamento;
import br.pucpr.biblioteca.dominio.Atendente;
import br.pucpr.biblioteca.dominio.Estudante;
import br.pucpr.biblioteca.dominio.Pessoa;

import java.util.Optional;

/** Ponto interno de acesso a pessoas, estudantes e atendentes (Trabalho 02). */
public class Relacionamento {
    private final RepositorioDeRelacionamento repositorio;

    public Relacionamento(RepositorioDeRelacionamento repositorio) {
        this.repositorio = repositorio;
    }

    public void adicionar(Pessoa pessoa) { repositorio.salvarPessoa(pessoa); }
    public void remover(Pessoa pessoa) { repositorio.removerPessoa(pessoa.getCpf()); }
    public Optional<Pessoa> consultarPessoa(String cpf) { return repositorio.buscarPessoa(cpf); }

    public void adicionar(Estudante estudante) { repositorio.salvarEstudante(estudante); }
    public void remover(Estudante estudante) { repositorio.removerEstudante(estudante.getMatricula()); }
    public Optional<Estudante> consultarEstudante(String matricula) { return repositorio.buscarEstudante(matricula); }

    public void adicionar(Atendente atendente) { repositorio.salvarAtendente(atendente); }
    public void remover(Atendente atendente) { repositorio.removerAtendente(atendente.getIdAtendente()); }
    public Optional<Atendente> consultarAtendente(String idAtendente) { return repositorio.buscarAtendente(idAtendente); }
}
