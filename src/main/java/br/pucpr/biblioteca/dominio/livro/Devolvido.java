package br.pucpr.biblioteca.dominio.livro;

/**
 * PADRÃO STATE — ConcreteState DEVOLVIDO.
 * O livro só sai daqui pela decisão da inspeção; não pode ser reservado antes disso.
 */
public final class Devolvido extends EstadoLivro {
    static final Devolvido INSTANCIA = new Devolvido();

    private Devolvido() { }

    @Override
    public CodigoEstadoLivro codigo() { return CodigoEstadoLivro.DEVOLVIDO; }

    @Override
    public void inspecionar(Livro livro, boolean bomEstado) {
        if (bomEstado) {
            livro.mudarPara(Livre.INSTANCIA);            // T7 [livro em bom estado] / setEstado(LIVRE)
        } else {
            livro.mudarPara(Reparando.INSTANCIA);        // T8 [livro danificado]
        }
    }
}
