package br.pucpr.biblioteca.circulacao;

import java.time.LocalDate;

public record ComprovanteDeRenovacao(long idEmprestimo, int idLivro, String matricula,
                                     LocalDate novoVencimento, int numRenovacoes) {
    @Override
    public String toString() {
        return "Renovação do empréstimo #" + idEmprestimo + " | livro " + idLivro + " | novo vencimento "
                + novoVencimento + " | renovações: " + numRenovacoes;
    }
}
