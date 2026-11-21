package br.pucpr.biblioteca.dominio.livro;

/** PADRÃO STATE — ConcreteState LIVRE. */
public final class Livre extends EstadoLivro {
    static final Livre INSTANCIA = new Livre();

    private Livre() { }

    @Override
    public CodigoEstadoLivro codigo() { return CodigoEstadoLivro.LIVRE; }

    @Override
    public void solicitarReserva(Livro livro, boolean estudanteCadastrado) {
        if (!estudanteCadastrado) {                       // guarda [estudante cadastrado]
            throw new TransicaoInvalidaException("Reserva recusada: estudante não cadastrado");
        }
        livro.mudarPara(Reservado.INSTANCIA);
    }

    @Override
    public void enviarParaReparo(Livro livro) {           // T9 [dano identificado no acervo]
        livro.mudarPara(Reparando.INSTANCIA);
    }

    @Override
    public void excluirLivro(Livro livro) {               // T11
        livro.marcarExcluido();
    }

    @Override
    public boolean aceitaReserva() { return true; }
}
