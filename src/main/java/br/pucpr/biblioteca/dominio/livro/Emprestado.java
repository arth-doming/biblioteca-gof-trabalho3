package br.pucpr.biblioteca.dominio.livro;

import br.pucpr.biblioteca.dominio.Emprestimo;

/** PADRÃO STATE — ConcreteState EMPRESTADO. */
public final class Emprestado extends EstadoLivro {
    static final Emprestado INSTANCIA = new Emprestado();

    private Emprestado() { }

    @Override
    public CodigoEstadoLivro codigo() { return CodigoEstadoLivro.EMPRESTADO; }

    @Override
    public void renovarEmprestimo(Livro livro, Emprestimo emprestimo) {
        // T4 — guarda avaliada ANTES de qualquer alteração
        if (emprestimo.getNumRenovacoes() != 0) {
            throw new TransicaoInvalidaException("Renovação recusada: numRenovacoes deveria ser 0");
        }
        livro.mudarPara(Renovado.INSTANCIA);
        Renovado.INSTANCIA.entry(emprestimo);             // entry / estenderVencimento(30 dias)
    }

    @Override
    public void registrarDevolucao(Livro livro) {         // T6 (a multa, se houver atraso, é reação ao evento)
        livro.mudarPara(Devolvido.INSTANCIA);
    }
}
