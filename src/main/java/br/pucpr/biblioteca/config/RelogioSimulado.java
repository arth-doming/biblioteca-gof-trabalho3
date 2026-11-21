package br.pucpr.biblioteca.config;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Relógio que pode ser adiantado (testes e demonstração de atraso). */
public class RelogioSimulado extends Clock {
    private volatile Instant agora;
    private final ZoneId zona;

    public RelogioSimulado(LocalDate inicio) {
        this(inicio.atStartOfDay(ZoneOffset.UTC).plusHours(12).toInstant(), ZoneOffset.UTC);
    }

    private RelogioSimulado(Instant agora, ZoneId zona) {
        this.agora = agora;
        this.zona = zona;
    }

    public void avancarDias(long dias) {
        agora = agora.plusSeconds(dias * 86_400);
    }

    @Override
    public ZoneId getZone() { return zona; }

    @Override
    public Clock withZone(ZoneId zone) { return new RelogioSimulado(agora, zone); }

    @Override
    public Instant instant() { return agora; }
}
