package br.pucpr.biblioteca.dados;

import java.util.function.Supplier;

/**
 * PADRÃO ABSTRACT FACTORY (R8) — produto abstrato "unidade de trabalho".
 * Delimita uma transação: tudo o que roda dentro de executar() é confirmado junto
 * ou desfeito junto. Chamadas aninhadas participam da transação já aberta.
 */
public interface UnidadeDeTrabalho {

    <T> T executar(Supplier<T> operacao);

    default void executar(Runnable operacao) {
        executar(() -> {
            operacao.run();
            return null;
        });
    }

    boolean emAndamento();

    String familia();
}
