package br.pucpr.biblioteca.dados.memoria;

import br.pucpr.biblioteca.dados.FabricaDeAcessoADados;
import br.pucpr.biblioteca.dados.RepositorioDeLivros;
import br.pucpr.biblioteca.dados.RepositorioDeMovimentacao;
import br.pucpr.biblioteca.dados.RepositorioDeMultas;
import br.pucpr.biblioteca.dados.RepositorioDeRelacionamento;
import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;

/**
 * PADRÃO ABSTRACT FACTORY (R8) — ConcreteFactory da família em memória (perfil teste).
 * Todos os repositórios recebem a MESMA UnidadeDeTrabalhoEmMemoria.
 */
public final class FabricaEmMemoria implements FabricaDeAcessoADados {
    public static final String FAMILIA = "memoria";

    private final UnidadeDeTrabalhoEmMemoria uow = new UnidadeDeTrabalhoEmMemoria();
    private RepositorioDeLivrosEmMemoria livros;
    private RepositorioDeMovimentacaoEmMemoria movimentacao;
    private RepositorioDeRelacionamentoEmMemoria relacionamento;
    private RepositorioDeMultasEmMemoria multas;

    @Override
    public String familia() { return FAMILIA; }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() { return uow; }

    @Override
    public synchronized RepositorioDeLivros criarRepositorioDeLivros() {
        if (livros == null) livros = new RepositorioDeLivrosEmMemoria(uow);
        return livros;
    }

    @Override
    public synchronized RepositorioDeMovimentacao criarRepositorioDeMovimentacao() {
        if (movimentacao == null) movimentacao = new RepositorioDeMovimentacaoEmMemoria(uow);
        return movimentacao;
    }

    @Override
    public synchronized RepositorioDeRelacionamento criarRepositorioDeRelacionamento() {
        if (relacionamento == null) relacionamento = new RepositorioDeRelacionamentoEmMemoria(uow);
        return relacionamento;
    }

    @Override
    public synchronized RepositorioDeMultas criarRepositorioDeMultas() {
        if (multas == null) multas = new RepositorioDeMultasEmMemoria(uow);
        return multas;
    }
}
