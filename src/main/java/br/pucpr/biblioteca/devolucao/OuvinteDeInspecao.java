package br.pucpr.biblioteca.devolucao;

/** PADRÃO OBSERVER — ConcreteObserver: põe o livro na fila de inspeção. */
public class OuvinteDeInspecao implements OuvinteDeDevolucao {
    private final FilaDeInspecao fila;

    public OuvinteDeInspecao(FilaDeInspecao fila) { this.fila = fila; }

    @Override
    public String nome() { return "inspecao"; }

    @Override
    public void aoDevolver(EventoDevolucao e) { fila.adicionar(e.idLivro()); }
}
