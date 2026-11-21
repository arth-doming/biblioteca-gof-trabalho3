package br.pucpr.biblioteca.financeiro.multa;

import java.math.BigDecimal;

/**
 * PADRÃO STRATEGY (R2) — Strategy.
 * Calcula o valor da multa a partir dos dias de atraso (sempre >= 0).
 * Para incluir uma política nova basta criar outra classe que implemente esta interface
 * e registrá-la na ConfiguracaoAdministrativa; TratadorDeMulta não muda.
 */
public interface PoliticaDeMulta {
    BigDecimal VALOR_DIA = new BigDecimal("1.00");

    /** Nome gravado na multa (politicaAplicada). */
    String nome();

    BigDecimal calcular(long diasAtraso);
}
