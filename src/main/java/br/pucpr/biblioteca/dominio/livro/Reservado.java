package br.pucpr.biblioteca.dominio.livro;

/** PADRÃO STATE — ConcreteState RESERVADO. */
public final class Reservado extends EstadoLivro {
    static final Reservado INSTANCIA = new Reservado();

    private Reservado() { }

    @Override
    public CodigoEstadoLivro codigo() { return CodigoEstadoLivro.RESERVADO; }

    @Override
    public void efetivarEmprestimo(Livro livro, boolean reservaDoMesmoEstudante) {
        if (!reservaDoMesmoEstudante) {                   // guarda [reserva do mesmo estudante]
            throw new TransicaoInvalidaException("Empréstimo recusado: reserva pertence a outro estudante");
        }
        livro.mudarPara(Emprestado.INSTANCIA);
    }
}
