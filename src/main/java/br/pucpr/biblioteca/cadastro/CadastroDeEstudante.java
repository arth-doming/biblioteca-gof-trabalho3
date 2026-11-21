package br.pucpr.biblioteca.cadastro;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;
import br.pucpr.biblioteca.dominio.Estudante;
import br.pucpr.biblioteca.dominio.Pessoa;

import java.util.Optional;

/** PADRÃO TEMPLATE METHOD — ConcreteClass (regra 3.1). Usa o gancho verificarPreRequisitos. */
public class CadastroDeEstudante extends RotinaDeCadastro<CadastroDeEstudante.Dados, Estudante> {
    public record Dados(String cpf, String matricula) { }

    private final Relacionamento relacionamento;

    public CadastroDeEstudante(UnidadeDeTrabalho uow, Relacionamento relacionamento) {
        super(uow);
        this.relacionamento = relacionamento;
    }

    @Override
    protected Optional<Estudante> buscarExistente(Dados d) {
        return vazio(d.matricula()) ? Optional.empty() : relacionamento.consultarEstudante(d.matricula().trim());
    }

    @Override
    protected void validar(Dados d) {
        exigir(!vazio(d.matricula()), "Matrícula é obrigatória");
        exigir(Cpf.valido(d.cpf()), "CPF deve ter 11 dígitos");
    }

    @Override
    protected void verificarPreRequisitos(Dados d) {
        if (relacionamento.consultarPessoa(Cpf.normalizar(d.cpf())).isEmpty()) {
            throw new PreRequisitoNaoAtendidoException(
                    "Pessoa com CPF " + d.cpf() + " não cadastrada: cadastre a pessoa antes do estudante");
        }
    }

    @Override
    protected Estudante criar(Dados d) {
        Pessoa pessoa = relacionamento.consultarPessoa(Cpf.normalizar(d.cpf())).orElseThrow();
        return new Estudante(d.matricula().trim(), pessoa);
    }

    @Override
    protected void armazenar(Estudante e) {
        relacionamento.adicionar(e);
    }
}
