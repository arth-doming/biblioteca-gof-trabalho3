package br.pucpr.biblioteca.dados.memoria;

import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * PADRÃO ABSTRACT FACTORY — produto concreto (família em memória).
 * Cada escrita feita durante a transação registra a ação que a desfaz; em caso de falha,
 * as ações são executadas em ordem inversa. Transações são serializadas por uma trava.
 */
public final class UnidadeDeTrabalhoEmMemoria implements UnidadeDeTrabalho {
    private final ReentrantLock trava = new ReentrantLock();
    private final ThreadLocal<Deque<Runnable>> desfazer = new ThreadLocal<>();

    @Override
    public <T> T executar(Supplier<T> operacao) {
        if (emAndamento()) {
            return operacao.get();                      // participa da transação aberta
        }
        trava.lock();
        desfazer.set(new ArrayDeque<>());
        try {
            T resultado = operacao.get();
            return resultado;                           // confirmar = descartar o log de desfazer
        } catch (RuntimeException | Error e) {
            Deque<Runnable> log = desfazer.get();
            while (!log.isEmpty()) {
                log.pop().run();
            }
            throw e;
        } finally {
            desfazer.remove();
            trava.unlock();
        }
    }

    @Override
    public boolean emAndamento() {
        return desfazer.get() != null;
    }

    @Override
    public String familia() {
        return FabricaEmMemoria.FAMILIA;
    }

    /** Chamado pelos repositórios em memória antes de cada escrita. */
    void aoDesfazer(Runnable acao) {
        Deque<Runnable> log = desfazer.get();
        if (log != null) {
            log.push(acao);
        }
    }
}
