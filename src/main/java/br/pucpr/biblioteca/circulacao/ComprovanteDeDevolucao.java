package br.pucpr.biblioteca.circulacao;

import java.time.LocalDate;
import java.util.Map;

public record ComprovanteDeDevolucao(long idDevolucao, int idLivro, String matricula, String idAtendente,
                                     LocalDate dataVencimento, LocalDate dataDevolucao, long diasAtraso,
                                     Map<String, String> falhasDeOuvintes) {
    @Override
    public String toString() {
        return "Devolução #" + idDevolucao + " | livro " + idLivro + " | estudante " + matricula
                + " | vencimento " + dataVencimento + " | devolvido " + dataDevolucao
                + (diasAtraso > 0 ? " | ATRASO de " + diasAtraso + " dia(s)" : " | no prazo")
                + (falhasDeOuvintes.isEmpty() ? "" : " | avisos: " + falhasDeOuvintes);
    }
}
