package br.pucpr.biblioteca.dados.mysql;

import br.pucpr.biblioteca.dados.FabricaDeAcessoADados;
import br.pucpr.biblioteca.dados.RepositorioDeLivros;
import br.pucpr.biblioteca.dados.RepositorioDeMovimentacao;
import br.pucpr.biblioteca.dados.RepositorioDeMultas;
import br.pucpr.biblioteca.dados.RepositorioDeRelacionamento;
import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;

/**
 * PADRÃO ABSTRACT FACTORY (R8) — ConcreteFactory da família relacional (perfil produção).
 * Os repositórios só aceitam UnidadeDeTrabalhoMySQL no construtor (pacote), então não há como
 * montar um repositório MySQL com a unidade de trabalho em memória, nem o contrário.
 */
public final class FabricaMySQL implements FabricaDeAcessoADados {
    public static final String FAMILIA = "mysql";

    private final UnidadeDeTrabalhoMySQL uow;
    private RepositorioDeLivrosMySQL livros;
    private RepositorioDeMovimentacaoMySQL movimentacao;
    private RepositorioDeRelacionamentoMySQL relacionamento;
    private RepositorioDeMultasMySQL multas;

    public FabricaMySQL(ConfiguracaoMySQL configuracao) {
        this.uow = new UnidadeDeTrabalhoMySQL(configuracao);
    }

    @Override
    public String familia() { return FAMILIA; }

    @Override
    public UnidadeDeTrabalho unidadeDeTrabalho() { return uow; }

    @Override
    public synchronized RepositorioDeLivros criarRepositorioDeLivros() {
        if (livros == null) livros = new RepositorioDeLivrosMySQL(uow);
        return livros;
    }

    @Override
    public synchronized RepositorioDeMovimentacao criarRepositorioDeMovimentacao() {
        if (movimentacao == null) movimentacao = new RepositorioDeMovimentacaoMySQL(uow);
        return movimentacao;
    }

    @Override
    public synchronized RepositorioDeRelacionamento criarRepositorioDeRelacionamento() {
        if (relacionamento == null) relacionamento = new RepositorioDeRelacionamentoMySQL(uow);
        return relacionamento;
    }

    @Override
    public synchronized RepositorioDeMultas criarRepositorioDeMultas() {
        if (multas == null) multas = new RepositorioDeMultasMySQL(uow);
        return multas;
    }
}
