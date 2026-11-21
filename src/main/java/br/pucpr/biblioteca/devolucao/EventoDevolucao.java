package br.pucpr.biblioteca.devolucao;

import br.pucpr.biblioteca.financeiro.TratadorDeMulta;

import java.time.LocalDate;

/**
 * PADRÃO OBSERVER (R5) — dados do evento.
 * Leva tudo o que os ouvintes precisam (vencimento, data de devolução, estudante), para que
 * nenhum ouvinte consulte a Movimentação depois — quando o empréstimo já foi removido.
 */
public record EventoDevolucao(long idDevolucao, int idLivro, String tituloLivro,
                              String matricula, String emailEstudante, String idAtendente,
                              LocalDate dataVencimento, LocalDate dataDevolucao) {

    public long diasAtraso() {
        return TratadorDeMulta.diasDeAtraso(dataVencimento, dataDevolucao);
    }

    public boolean emAtraso() {
        return diasAtraso() > 0;
    }
}
