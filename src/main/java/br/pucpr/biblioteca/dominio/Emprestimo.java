package br.pucpr.biblioteca.dominio;

import java.time.LocalDate;

public class Emprestimo extends Transacao {
    public static final int PRAZO_EMPRESTIMO_DIAS = 14;
    public static final int DIAS_POR_RENOVACAO = 30;
    public static final int MAX_RENOVACOES = 2;

    private LocalDate dataVencimento;
    private int numRenovacoes;

    public Emprestimo(long id, int idLivro, String matricula, LocalDate data,
                      LocalDate dataVencimento, int numRenovacoes) {
        super(id, idLivro, matricula, null, data);
        this.dataVencimento = dataVencimento;
        this.numRenovacoes = numRenovacoes;
    }

    public static Emprestimo novo(int idLivro, String matricula, LocalDate hoje) {
        return new Emprestimo(0, idLivro, matricula, hoje, hoje.plusDays(PRAZO_EMPRESTIMO_DIAS), 0);
    }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public int getNumRenovacoes() { return numRenovacoes; }

    /** Ação de entrada do estado RENOVADO: + dias no vencimento e numRenovacoes++. */
    public void estenderVencimento(int dias) {
        this.dataVencimento = dataVencimento.plusDays(dias);
        this.numRenovacoes++;
    }
}
