package br.pucpr.biblioteca.devolucao;

/** PADRÃO OBSERVER (R5) — Observer. */
public interface OuvinteDeDevolucao {
    String nome();

    void aoDevolver(EventoDevolucao evento);
}
