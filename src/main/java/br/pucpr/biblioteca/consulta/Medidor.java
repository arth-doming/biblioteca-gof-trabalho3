package br.pucpr.biblioteca.consulta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Coleta número de chamadas e tempo total por operação. */
public class Medidor {
    private final Map<String, AtomicLong> chamadas = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> nanos = new ConcurrentHashMap<>();

    public void registrar(String operacao, long duracaoNanos) {
        chamadas.computeIfAbsent(operacao, k -> new AtomicLong()).incrementAndGet();
        nanos.computeIfAbsent(operacao, k -> new AtomicLong()).addAndGet(duracaoNanos);
    }

    public long chamadas(String operacao) {
        AtomicLong c = chamadas.get(operacao);
        return c == null ? 0 : c.get();
    }

    public long nanosAcumulados(String operacao) {
        AtomicLong n = nanos.get(operacao);
        return n == null ? 0 : n.get();
    }

    @Override
    public String toString() {
        return "chamadas=" + chamadas;
    }
}
