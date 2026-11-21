package br.pucpr.biblioteca.dominio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Devolucao extends Transacao {
    private final LocalDate dataVencimento;

    public Devolucao(long id, int idLivro, String matricula, String idAtendente,
                     LocalDate dataDevolucao, LocalDate dataVencimento) {
        super(id, idLivro, matricula, idAtendente, dataDevolucao);
        this.dataVencimento = dataVencimento;
    }

    public LocalDate getDataDevolucao() { return getData(); }
    public LocalDate getDataVencimento() { return dataVencimento; }

    public boolean emAtraso() {
        return diasDeAtraso() > 0;
    }

    /** Dias corridos entre vencimento e devolução, nunca negativo. */
    public long diasDeAtraso() {
        return Math.max(0, ChronoUnit.DAYS.between(dataVencimento, getDataDevolucao()));
    }
}
