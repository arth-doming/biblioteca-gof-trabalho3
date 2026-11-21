package br.pucpr.biblioteca.organizacao;

import java.util.List;

/** PADRÃO COMPOSITE — Leaf. Referencia o livro apenas pelo identificador. */
public final class ItemAcervo extends ComponenteAcervo {
    private final int idLivro;

    public ItemAcervo(int idLivro) {
        this.idLivro = idLivro;
    }

    public int getIdLivro() { return idLivro; }

    @Override
    public String nome() { return "livro " + idLivro; }

    @Override
    public int contarLivros() { return 1; }

    @Override
    public List<Integer> listarLivros() { return List.of(idLivro); }
}
