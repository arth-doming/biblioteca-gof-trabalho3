package br.pucpr.biblioteca.financeiro.multa;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Boleto devolvido pelo contrato de cobrança, já na linguagem da instituição. */
public record Boleto(String numero, String numeroMulta, BigDecimal valor, LocalDate vencimento,
                     String banco, String identificadorNoBanco, String linhaDigitavel) {
}
