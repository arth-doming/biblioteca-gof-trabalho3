package br.pucpr.biblioteca.financeiro.cobranca;

import br.pucpr.biblioteca.financeiro.multa.Boleto;

import java.util.function.Consumer;

/**
 * PADRÃO ADAPTER (R3) — Target: contrato institucional de cobrança.
 * TratadorDeMulta conhece só esta interface; nunca pergunta qual banco está em uso.
 */
public interface ContratoDeCobranca {

    Boleto registrarCobranca(SolicitacaoDeCobranca solicitacao);

    /** Registra quem recebe as notificações de pagamento já traduzidas. */
    void aoReceberPagamento(Consumer<NotificacaoDePagamento> destino);

    String banco();
}
