package br.pucpr.biblioteca.consulta;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Cache com validade (TTL), para que um título alterado não fique velho para sempre. */
public class CacheEmMemoria implements ArmazenamentoDeCache {
    private record Entrada(MetadadosLivro valor, Instant expiraEm) { }

    private final Map<Integer, Entrada> entradas = new ConcurrentHashMap<>();
    private final Clock relogio;
    private final Duration validade;

    public CacheEmMemoria(Clock relogio, Duration validade) {
        this.relogio = relogio;
        this.validade = validade;
    }

    @Override
    public Optional<MetadadosLivro> ler(int idLivro) {
        Entrada e = entradas.get(idLivro);
        if (e == null) return Optional.empty();
        if (relogio.instant().isAfter(e.expiraEm())) {
            entradas.remove(idLivro);
            return Optional.empty();
        }
        return Optional.of(e.valor());
    }

    @Override
    public void gravar(MetadadosLivro m) {
        entradas.put(m.idLivro(), new Entrada(m, relogio.instant().plus(validade)));
    }

    @Override
    public void invalidar(int idLivro) {
        entradas.remove(idLivro);
    }
}
