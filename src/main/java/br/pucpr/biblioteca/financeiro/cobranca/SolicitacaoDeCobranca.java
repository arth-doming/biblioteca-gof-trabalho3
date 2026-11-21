package br.pucpr.biblioteca.financeiro.cobranca;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Pedido de cobrança na linguagem da instituição (valor em reais, CPF só com dígitos). */
public record SolicitacaoDeCobranca(String numeroDocumento, BigDecimal valor, String cpfPagador,
                                    String nomePagador, LocalDate vencimento) {
}
