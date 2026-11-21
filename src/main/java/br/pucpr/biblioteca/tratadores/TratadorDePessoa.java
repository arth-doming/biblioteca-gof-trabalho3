package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.cadastro.CadastroDePessoa;
import br.pucpr.biblioteca.cadastro.Cpf;
import br.pucpr.biblioteca.cadastro.ResultadoDeCadastro;
import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;
import br.pucpr.biblioteca.dominio.Pessoa;

import java.util.Optional;

public class TratadorDePessoa implements iPessoa {
    private final CadastroDePessoa cadastro;
    private final Relacionamento relacionamento;
    private final UnidadeDeTrabalho uow;

    public TratadorDePessoa(CadastroDePessoa cadastro, Relacionamento relacionamento, UnidadeDeTrabalho uow) {
        this.cadastro = cadastro;
        this.relacionamento = relacionamento;
        this.uow = uow;
    }

    @Override
    public ResultadoDeCadastro<Pessoa> inserirPessoa(String nome, String sobrenome, String cpf, String email) {
        return cadastro.incluir(new CadastroDePessoa.Dados(nome, sobrenome, cpf, email));
    }

    @Override
    public Optional<Pessoa> consultarPessoa(String cpf) {
        return relacionamento.consultarPessoa(Cpf.normalizar(cpf));
    }

    @Override
    public boolean atualizarPessoa(String cpf, String nome, String sobrenome, String email) {
        return uow.executar(() -> relacionamento.consultarPessoa(Cpf.normalizar(cpf)).map(p -> {
            p.atualizar(nome, sobrenome, email);
            relacionamento.adicionar(p);
            return true;
        }).orElse(false));
    }

    @Override
    public boolean excluirPessoa(String cpf) {
        return uow.executar(() -> relacionamento.consultarPessoa(Cpf.normalizar(cpf)).map(p -> {
            relacionamento.remover(p);
            return true;
        }).orElse(false));
    }
}
