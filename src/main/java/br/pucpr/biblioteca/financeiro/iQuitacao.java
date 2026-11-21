package br.pucpr.biblioteca.financeiro;

import br.pucpr.biblioteca.financeiro.cobranca.NotificacaoDePagamento;

public interface iQuitacao {
    /** @return true se a notificação quitou a multa; false se ignorada (repetida, desconhecida ou não paga). */
    boolean registrarQuitacao(NotificacaoDePagamento notificacao);
}
