package br.pucpr.biblioteca.devolucao;

import java.util.concurrent.atomic.AtomicLong;

public class IndicadoresDeCirculacao {
    private final AtomicLong devolucoes = new AtomicLong();
    private final AtomicLong devolucoesAtrasadas = new AtomicLong();
    private final AtomicLong diasDeAtrasoAcumulados = new AtomicLong();

    public void registrar(EventoDevolucao e) {
        devolucoes.incrementAndGet();
        if (e.emAtraso()) {
            devolucoesAtrasadas.incrementAndGet();
            diasDeAtrasoAcumulados.addAndGet(e.diasAtraso());
        }
    }

    public long devolucoes() { return devolucoes.get(); }
    public long devolucoesAtrasadas() { return devolucoesAtrasadas.get(); }
    public long diasDeAtrasoAcumulados() { return diasDeAtrasoAcumulados.get(); }

    @Override
    public String toString() {
        return "devoluções=" + devolucoes() + ", atrasadas=" + devolucoesAtrasadas()
                + ", dias de atraso=" + diasDeAtrasoAcumulados();
    }
}
