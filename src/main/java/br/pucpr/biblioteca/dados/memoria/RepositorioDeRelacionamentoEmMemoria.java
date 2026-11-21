package br.pucpr.biblioteca.dados.memoria;

import br.pucpr.biblioteca.dados.RepositorioDeRelacionamento;
import br.pucpr.biblioteca.dominio.Atendente;
import br.pucpr.biblioteca.dominio.Estudante;
import br.pucpr.biblioteca.dominio.Pessoa;

import java.util.Optional;

/** PADRÃO ABSTRACT FACTORY — produto concreto. */
public final class RepositorioDeRelacionamentoEmMemoria implements RepositorioDeRelacionamento {
    private record PessoaRegistro(String cpf, String nome, String sobrenome, String email) { }
    private record PapelRegistro(String id, String cpf) { }

    private final ArmazemEmMemoria<String, PessoaRegistro> pessoas;
    private final ArmazemEmMemoria<String, PapelRegistro> estudantes;
    private final ArmazemEmMemoria<String, PapelRegistro> atendentes;

    RepositorioDeRelacionamentoEmMemoria(UnidadeDeTrabalhoEmMemoria uow) {
        this.pessoas = new ArmazemEmMemoria<>(uow);
        this.estudantes = new ArmazemEmMemoria<>(uow);
        this.atendentes = new ArmazemEmMemoria<>(uow);
    }

    @Override
    public void salvarPessoa(Pessoa p) {
        pessoas.gravar(p.getCpf(), new PessoaRegistro(p.getCpf(), p.getNome(), p.getSobrenome(), p.getEmail()));
    }

    @Override
    public void removerPessoa(String cpf) { pessoas.apagar(cpf); }

    @Override
    public Optional<Pessoa> buscarPessoa(String cpf) {
        return pessoas.ler(cpf).map(r -> new Pessoa(r.cpf(), r.nome(), r.sobrenome(), r.email()));
    }

    @Override
    public void salvarEstudante(Estudante e) {
        estudantes.gravar(e.getMatricula(), new PapelRegistro(e.getMatricula(), e.getPessoa().getCpf()));
    }

    @Override
    public void removerEstudante(String matricula) { estudantes.apagar(matricula); }

    @Override
    public Optional<Estudante> buscarEstudante(String matricula) {
        return estudantes.ler(matricula).flatMap(r ->
                buscarPessoa(r.cpf()).map(p -> new Estudante(r.id(), p)));
    }

    @Override
    public void salvarAtendente(Atendente a) {
        atendentes.gravar(a.getIdAtendente(), new PapelRegistro(a.getIdAtendente(), a.getPessoa().getCpf()));
    }

    @Override
    public void removerAtendente(String idAtendente) { atendentes.apagar(idAtendente); }

    @Override
    public Optional<Atendente> buscarAtendente(String idAtendente) {
        return atendentes.ler(idAtendente).flatMap(r ->
                buscarPessoa(r.cpf()).map(p -> new Atendente(r.id(), p)));
    }
}
