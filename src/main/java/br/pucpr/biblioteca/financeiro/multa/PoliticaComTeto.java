package br.pucpr.biblioteca.financeiro.multa;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** PADRÃO STRATEGY — ConcreteStrategy: R$ 1,00 por dia, limitado a R$ 20,00. */
public class PoliticaComTeto implements PoliticaDeMulta {
    public static final String NOME = "COM_TETO";
    public static final BigDecimal TETO = new BigDecimal("20.00");

    @Override
    public String nome() { return NOME; }

    @Override
    public BigDecimal calcular(long diasAtraso) {
        BigDecimal valor = VALOR_DIA.multiply(BigDecimal.valueOf(Math.max(0, diasAtraso)));
        return valor.min(TETO).setScale(2, RoundingMode.HALF_EVEN);
    }
}
