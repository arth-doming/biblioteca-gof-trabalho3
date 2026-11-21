package br.pucpr.biblioteca.financeiro.multa;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** PADRÃO STRATEGY — ConcreteStrategy: os dois primeiros dias de atraso não são cobrados. */
public class PoliticaComTolerancia implements PoliticaDeMulta {
    public static final String NOME = "COM_TOLERANCIA";
    public static final int DIAS_TOLERANCIA = 2;

    @Override
    public String nome() { return NOME; }

    @Override
    public BigDecimal calcular(long diasAtraso) {
        long cobrados = Math.max(0, diasAtraso - DIAS_TOLERANCIA);
        return VALOR_DIA.multiply(BigDecimal.valueOf(cobrados)).setScale(2, RoundingMode.HALF_EVEN);
    }
}
