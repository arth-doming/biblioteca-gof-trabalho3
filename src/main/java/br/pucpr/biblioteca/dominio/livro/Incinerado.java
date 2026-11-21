package br.pucpr.biblioteca.dominio.livro;

/** PADRÃO STATE — ConcreteState INCINERADO (entry / registrarIncineracao). */
public final class Incinerado extends EstadoLivro {
    static final Incinerado INSTANCIA = new Incinerado();

    private Incinerado() { }

    @Override
    public CodigoEstadoLivro codigo() { return CodigoEstadoLivro.INCINERADO; }

    void entry(Livro livro) {
        livro.registrarIncineracao();
    }

    @Override
    public void excluirLivro(Livro livro) {              // T11
        livro.marcarExcluido();
    }
}
