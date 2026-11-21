package br.pucpr.biblioteca.dominio.livro;

import br.pucpr.biblioteca.dominio.Emprestimo;

/** PADRÃO STATE — ConcreteState RENOVADO (entry / estenderVencimento(30 dias)). */
public final class Renovado extends EstadoLivro {
    static final Renovado INSTANCIA = new Renovado();

    private Renovado() { }

    @Override
    public CodigoEstadoLivro codigo() { return CodigoEstadoLivro.RENOVADO; }

    /** Ação de entrada do estado. */
    void entry(Emprestimo emprestimo) {
        emprestimo.estenderVencimento(Emprestimo.DIAS_POR_RENOVACAO);
    }

    @Override
    public void renovarEmprestimo(Livro livro, Emprestimo emprestimo) {
        // T5 — autotransição com guarda [numRenovacoes < 2], avaliada antes de mexer no vencimento
        if (emprestimo.getNumRenovacoes() >= Emprestimo.MAX_RENOVACOES) {
            throw new TransicaoInvalidaException(
                    "Renovação recusada: limite de " + Emprestimo.MAX_RENOVACOES + " renovações atingido");
        }
        livro.mudarPara(this);                            // autotransição: reentra no estado
        entry(emprestimo);
    }

    @Override
    public void registrarDevolucao(Livro livro) {         // T6
        livro.mudarPara(Devolvido.INSTANCIA);
    }
}
