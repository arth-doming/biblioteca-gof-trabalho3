package br.pucpr.biblioteca.organizacao;

/**
 * Operações sobre a árvore. Mover só mexe nos nós do Composite: o Livro (id, estado) e a
 * Movimentacao (empréstimos) não são tocados, porque o item guarda apenas o idLivro.
 */
public class OrganizacaoDoAcervo {
    private final GrupoAcervo raiz;

    public OrganizacaoDoAcervo(GrupoAcervo raiz) {
        this.raiz = raiz;
    }

    public GrupoAcervo raiz() { return raiz; }

    public void moverLivro(int idLivro, GrupoAcervo destino) {
        if (destino.raiz() != raiz) {
            throw new OrganizacaoInvalidaException("Destino " + destino.nome() + " não pertence a esta organização");
        }
        ItemAcervo item = raiz.localizarItem(idLivro)
                .orElseThrow(() -> new OrganizacaoInvalidaException("Livro " + idLivro + " não está organizado"));
        GrupoAcervo origem = item.getPai();
        origem.remover(item);
        try {
            destino.adicionar(item);
        } catch (RuntimeException e) {
            origem.adicionar(item);           // volta para onde estava
            throw e;
        }
    }
}
