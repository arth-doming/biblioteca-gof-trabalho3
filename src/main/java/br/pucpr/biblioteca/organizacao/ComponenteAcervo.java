package br.pucpr.biblioteca.organizacao;

import java.util.List;

/**
 * PADRÃO COMPOSITE (R10) — Component.
 * Grupos (unidade, seção, subseção) e itens respondem às mesmas operações.
 */
public abstract class ComponenteAcervo {
    private GrupoAcervo pai;

    public abstract String nome();

    public abstract int contarLivros();

    public abstract List<Integer> listarLivros();

    public GrupoAcervo getPai() { return pai; }

    void definirPai(GrupoAcervo pai) { this.pai = pai; }

    public GrupoAcervo raiz() {
        ComponenteAcervo c = this;
        while (c.pai != null) c = c.pai;
        return c instanceof GrupoAcervo g ? g : null;
    }
}
