package br.pucpr.biblioteca.organizacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PADRÃO COMPOSITE (R10) — Composite.
 * contarLivros/listarLivros percorrem recursivamente os filhos (profundidade variável).
 * adicionar() recusa: componente que já tem pai, ciclo (o grupo ser ancestral de si mesmo)
 * e livro que já está em algum grupo da mesma árvore.
 */
public final class GrupoAcervo extends ComponenteAcervo {
    private final String nome;
    private final TipoGrupo tipo;
    private final List<ComponenteAcervo> filhos = new ArrayList<>();

    public GrupoAcervo(String nome, TipoGrupo tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    @Override
    public String nome() { return nome; }

    public TipoGrupo tipo() { return tipo; }

    public List<ComponenteAcervo> filhos() { return List.copyOf(filhos); }

    @Override
    public int contarLivros() {
        int total = 0;
        for (ComponenteAcervo f : filhos) total += f.contarLivros();
        return total;
    }

    @Override
    public List<Integer> listarLivros() {
        List<Integer> ids = new ArrayList<>();
        for (ComponenteAcervo f : filhos) ids.addAll(f.listarLivros());
        return ids;
    }

    public void adicionar(ComponenteAcervo componente) {
        if (componente.getPai() != null) {
            throw new OrganizacaoInvalidaException(componente.nome() + " já pertence ao grupo " + componente.getPai().nome());
        }
        if (componente == this || (componente instanceof GrupoAcervo g && g.ehAncestralDe(this))) {
            throw new OrganizacaoInvalidaException("Ciclo: " + componente.nome() + " não pode ficar dentro de " + nome);
        }
        GrupoAcervo raiz = raiz();
        for (int idLivro : componente.listarLivros()) {
            if (raiz.localizarItem(idLivro).isPresent()) {
                throw new OrganizacaoInvalidaException("Livro " + idLivro + " já pertence a outro grupo");
            }
        }
        filhos.add(componente);
        componente.definirPai(this);
    }

    public void remover(ComponenteAcervo componente) {
        if (filhos.remove(componente)) componente.definirPai(null);
    }

    public Optional<ItemAcervo> localizarItem(int idLivro) {
        for (ComponenteAcervo f : filhos) {
            if (f instanceof ItemAcervo i && i.getIdLivro() == idLivro) return Optional.of(i);
            if (f instanceof GrupoAcervo g) {
                Optional<ItemAcervo> achado = g.localizarItem(idLivro);
                if (achado.isPresent()) return achado;
            }
        }
        return Optional.empty();
    }

    /** true se este grupo está acima de "outro" na árvore (ou é ele). */
    private boolean ehAncestralDe(ComponenteAcervo outro) {
        for (ComponenteAcervo c = outro; c != null; c = c.getPai()) {
            if (c == this) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return tipo + " " + nome + " (" + contarLivros() + " livros)";
    }
}
