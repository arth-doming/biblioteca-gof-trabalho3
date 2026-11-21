package br.pucpr.biblioteca.devolucao;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Livros devolvidos aguardando a decisão da inspeção (o livro fica DEVOLVIDO até lá). */
public class FilaDeInspecao {
    private final Set<Integer> pendentes = Collections.synchronizedSet(new LinkedHashSet<>());

    public void adicionar(int idLivro) { pendentes.add(idLivro); }
    public void concluir(int idLivro) { pendentes.remove(idLivro); }
    public boolean contem(int idLivro) { return pendentes.contains(idLivro); }

    public Set<Integer> pendentes() {
        synchronized (pendentes) {
            return Set.copyOf(pendentes);
        }
    }
}
