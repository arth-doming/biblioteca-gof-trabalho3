package br.pucpr.biblioteca.financeiro.multa;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** PADRÃO STRATEGY — ConcreteStrategy: R$ 1,00 por dia de atraso. */
public class PoliticaOriginal implements PoliticaDeMulta {
    public static final String NOME = "ORIGINAL";

    @Override
    public String nome() { return NOME; }

    @Override
    public BigDecimal calcular(long diasAtraso) {
        long dias = Math.max(0, diasAtraso);
        return VALOR_DIA.multiply(BigDecimal.valueOf(dias)).setScale(2, RoundingMode.HALF_EVEN);
    }
}
