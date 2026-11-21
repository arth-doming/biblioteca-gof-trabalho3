package br.pucpr.biblioteca.financeiro.cobranca;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * PADRÃO ADAPTER — Adaptee (API simulada do banco A, incompatível com o contrato).
 * Valores em centavos (long), CPF só com dígitos, vencimento como inteiro AAAAMMDD,
 * situação numérica (1 = liquidado) e "nossoNumero" como identificador.
 */
public class BancoA {
    public record Titulo(String nossoNumero, long valorEmCentavos, String documentoPagador, int vencimentoAAAAMMDD) { }
    public record AvisoDeLiquidacao(String nossoNumero, long centavosPagos, int codigoSituacao, int dataAAAAMMDD) { }

    public static final int LIQUIDADO = 1;

    private final List<Titulo> titulosRecebidos = new ArrayList<>();
    private Consumer<AvisoDeLiquidacao> webhook = a -> { };
    private int sequencia;

    public synchronized String incluirTitulo(long valorEmCentavos, String documentoPagador, int vencimentoAAAAMMDD) {
        String nossoNumero = String.format("A%08d", ++sequencia);
        titulosRecebidos.add(new Titulo(nossoNumero, valorEmCentavos, documentoPagador, vencimentoAAAAMMDD));
        return nossoNumero;
    }

    public void cadastrarWebhook(Consumer<AvisoDeLiquidacao> webhook) {
        this.webhook = webhook;
    }

    /** Simula o banco avisando um pagamento (pode avisar mais de uma vez). */
    public void simularLiquidacao(String nossoNumero, long centavosPagos, int dataAAAAMMDD) {
        webhook.accept(new AvisoDeLiquidacao(nossoNumero, centavosPagos, LIQUIDADO, dataAAAAMMDD));
    }

    public synchronized List<Titulo> titulosRecebidos() { return List.copyOf(titulosRecebidos); }
}
