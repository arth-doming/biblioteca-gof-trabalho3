package br.pucpr.biblioteca.circulacao;

import java.time.LocalDate;

public record ComprovanteDeEmprestimo(long idEmprestimo, int idLivro, String tituloLivro, String matricula,
                                      LocalDate data, LocalDate dataVencimento) {
    @Override
    public String toString() {
        return "Empréstimo #" + idEmprestimo + " | livro " + idLivro + " \"" + tituloLivro + "\" | estudante "
                + matricula + " | vence em " + dataVencimento;
    }
}
