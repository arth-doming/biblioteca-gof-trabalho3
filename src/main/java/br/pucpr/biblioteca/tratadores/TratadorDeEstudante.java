package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.cadastro.CadastroDeEstudante;
import br.pucpr.biblioteca.cadastro.Cpf;
import br.pucpr.biblioteca.cadastro.ResultadoDeCadastro;
import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;
import br.pucpr.biblioteca.dominio.Estudante;

import java.util.Optional;

public class TratadorDeEstudante implements iEstudante {
    private final CadastroDeEstudante cadastro;
    private final Relacionamento relacionamento;
    private final UnidadeDeTrabalho uow;

    public TratadorDeEstudante(CadastroDeEstudante cadastro, Relacionamento relacionamento, UnidadeDeTrabalho uow) {
        this.cadastro = cadastro;
        this.relacionamento = relacionamento;
        this.uow = uow;
    }

    @Override
    public ResultadoDeCadastro<Estudante> inserirEstudante(String cpf, String matricula) {
        return cadastro.incluir(new CadastroDeEstudante.Dados(cpf, matricula));
    }

    @Override
    public Optional<Estudante> consultarEstudante(String matricula) {
        return relacionamento.consultarEstudante(matricula);
    }

    /** Regra 3.2: só atualiza se estudante e pessoa estiverem cadastrados. */
    @Override
    public boolean atualizarEstudante(String matricula, String cpf) {
        return uow.executar(() -> {
            Optional<Estudante> e = relacionamento.consultarEstudante(matricula);
            var p = relacionamento.consultarPessoa(Cpf.normalizar(cpf));
            if (e.isEmpty() || p.isEmpty()) return false;
            e.get().vincular(p.get());
            relacionamento.adicionar(e.get());
            return true;
        });
    }

    @Override
    public boolean excluirEstudante(String matricula) {
        return uow.executar(() -> relacionamento.consultarEstudante(matricula).map(e -> {
            relacionamento.remover(e);
            return true;
        }).orElse(false));
    }
}
