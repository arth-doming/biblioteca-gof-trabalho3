package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.cadastro.CadastroDeAtendente;
import br.pucpr.biblioteca.cadastro.Cpf;
import br.pucpr.biblioteca.cadastro.ResultadoDeCadastro;
import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;
import br.pucpr.biblioteca.dominio.Atendente;

import java.util.Optional;

public class TratadorDeAtendente implements iAtendente {
    private final CadastroDeAtendente cadastro;
    private final Relacionamento relacionamento;
    private final UnidadeDeTrabalho uow;

    public TratadorDeAtendente(CadastroDeAtendente cadastro, Relacionamento relacionamento, UnidadeDeTrabalho uow) {
        this.cadastro = cadastro;
        this.relacionamento = relacionamento;
        this.uow = uow;
    }

    @Override
    public ResultadoDeCadastro<Atendente> inserirAtendente(String cpf, String idAtendente) {
        return cadastro.incluir(new CadastroDeAtendente.Dados(cpf, idAtendente));
    }

    @Override
    public Optional<Atendente> consultarAtendente(String idAtendente) {
        return relacionamento.consultarAtendente(idAtendente);
    }

    /** Regra 4.2: só atualiza se atendente e pessoa estiverem cadastrados. */
    @Override
    public boolean atualizarAtendente(String idAtendente, String cpf) {
        return uow.executar(() -> {
            Optional<Atendente> e = relacionamento.consultarAtendente(idAtendente);
            var p = relacionamento.consultarPessoa(Cpf.normalizar(cpf));
            if (e.isEmpty() || p.isEmpty()) return false;
            e.get().vincular(p.get());
            relacionamento.adicionar(e.get());
            return true;
        });
    }

    @Override
    public boolean excluirAtendente(String idAtendente) {
        return uow.executar(() -> relacionamento.consultarAtendente(idAtendente).map(e -> {
            relacionamento.remover(e);
            return true;
        }).orElse(false));
    }
}
