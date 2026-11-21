package br.pucpr.biblioteca.dominio.livro;

/** PADRÃO STATE — ConcreteState REPARANDO (do / reparar exemplar). */
public final class Reparando extends EstadoLivro {
    static final Reparando INSTANCIA = new Reparando();

    private Reparando() { }

    @Override
    public CodigoEstadoLivro codigo() { return CodigoEstadoLivro.REPARANDO; }

    @Override
    public void retornarDoReparo(Livro livro) {          // T10 / setEstado(LIVRE)
        livro.mudarPara(Livre.INSTANCIA);
    }

    @Override
    public void avaliarReparo(Livro livro, boolean reparavel) {
        if (!reparavel) {                                // laudo irrecuperável
            livro.mudarPara(Incinerado.INSTANCIA);
            Incinerado.INSTANCIA.entry(livro);           // entry / registrarIncineracao
        }
        // laudo favorável: o exemplar continua em reparo (sem transição)
    }
}
