package br.pucpr.biblioteca.financeiro;

import br.pucpr.biblioteca.financeiro.multa.Boleto;
import br.pucpr.biblioteca.financeiro.multa.Multa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Interface do componente Financeiro usada pela Biblioteca (Trabalho 02). */
public interface iMulta {
    /** Emite multa e boleto se houver atraso com valor positivo; caso contrário, vazio. */
    Optional<Boleto> emitirMulta(int idLivro, String matricula, LocalDate dataVencimento, LocalDate dataDevolucao);

    List<Multa> consultarMulta(String matricula);

    /** Segunda via do boleto da multa mais recente do livro/estudante. */
    Optional<Boleto> consultarBoleto(int idLivro, String matricula);
}
