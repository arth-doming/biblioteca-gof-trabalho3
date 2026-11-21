package br.pucpr.biblioteca.cadastro;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;
import br.pucpr.biblioteca.dominio.Atendente;
import br.pucpr.biblioteca.dominio.Pessoa;

import java.util.Optional;

/** PADRÃO TEMPLATE METHOD — ConcreteClass (regra 4.1). Usa o gancho verificarPreRequisitos. */
public class CadastroDeAtendente extends RotinaDeCadastro<CadastroDeAtendente.Dados, Atendente> {
    public record Dados(String cpf, String idAtendente) { }

    private final Relacionamento relacionamento;

    public CadastroDeAtendente(UnidadeDeTrabalho uow, Relacionamento relacionamento) {
        super(uow);
        this.relacionamento = relacionamento;
    }

    @Override
    protected Optional<Atendente> buscarExistente(Dados d) {
        return vazio(d.idAtendente()) ? Optional.empty() : relacionamento.consultarAtendente(d.idAtendente().trim());
    }

    @Override
    protected void validar(Dados d) {
        exigir(!vazio(d.idAtendente()), "Identificador do atendente é obrigatório");
        exigir(Cpf.valido(d.cpf()), "CPF deve ter 11 dígitos");
    }

    @Override
    protected void verificarPreRequisitos(Dados d) {
        if (relacionamento.consultarPessoa(Cpf.normalizar(d.cpf())).isEmpty()) {
            throw new PreRequisitoNaoAtendidoException(
                    "Pessoa com CPF " + d.cpf() + " não cadastrada: cadastre a pessoa antes do atendente");
        }
    }

    @Override
    protected Atendente criar(Dados d) {
        Pessoa pessoa = relacionamento.consultarPessoa(Cpf.normalizar(d.cpf())).orElseThrow();
        return new Atendente(d.idAtendente().trim(), pessoa);
    }

    @Override
    protected void armazenar(Atendente a) {
        relacionamento.adicionar(a);
    }
}
