package br.pucpr.biblioteca.cadastro;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;
import br.pucpr.biblioteca.dominio.Pessoa;

import java.util.Optional;

/** PADRÃO TEMPLATE METHOD — ConcreteClass (regra 2.1). */
public class CadastroDePessoa extends RotinaDeCadastro<CadastroDePessoa.Dados, Pessoa> {
    public record Dados(String nome, String sobrenome, String cpf, String email) { }

    private final Relacionamento relacionamento;

    public CadastroDePessoa(UnidadeDeTrabalho uow, Relacionamento relacionamento) {
        super(uow);
        this.relacionamento = relacionamento;
    }

    @Override
    protected Optional<Pessoa> buscarExistente(Dados d) {
        return Cpf.valido(d.cpf()) ? relacionamento.consultarPessoa(Cpf.normalizar(d.cpf())) : Optional.empty();
    }

    @Override
    protected void validar(Dados d) {
        exigir(Cpf.valido(d.cpf()), "CPF deve ter 11 dígitos");
        exigir(!vazio(d.nome()), "Nome é obrigatório");
    }

    @Override
    protected Pessoa criar(Dados d) {
        return new Pessoa(Cpf.normalizar(d.cpf()), d.nome().trim(),
                d.sobrenome() == null ? "" : d.sobrenome().trim(), d.email());
    }

    @Override
    protected void armazenar(Pessoa p) {
        relacionamento.adicionar(p);
    }
}
