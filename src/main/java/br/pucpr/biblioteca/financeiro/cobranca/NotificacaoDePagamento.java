package br.pucpr.biblioteca.financeiro.cobranca;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Notificação de pagamento já traduzida para a linguagem da instituição. */
public record NotificacaoDePagamento(String numeroDocumento, BigDecimal valorPago,
                                     SituacaoDoPagamento situacao, LocalDate dataPagamento) {
}
