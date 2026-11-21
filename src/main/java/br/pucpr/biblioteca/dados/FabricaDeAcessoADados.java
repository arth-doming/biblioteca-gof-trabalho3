package br.pucpr.biblioteca.dados;

/**
 * PADRÃO ABSTRACT FACTORY (R8) — AbstractFactory.
 * Cria uma família compatível: a unidade de trabalho e os repositórios que a compartilham.
 * Cada fábrica concreta só sabe criar produtos da própria família.
 */
public interface FabricaDeAcessoADados {
    String familia();
    UnidadeDeTrabalho unidadeDeTrabalho();
    RepositorioDeLivros criarRepositorioDeLivros();
    RepositorioDeMovimentacao criarRepositorioDeMovimentacao();
    RepositorioDeRelacionamento criarRepositorioDeRelacionamento();
    RepositorioDeMultas criarRepositorioDeMultas();
}
