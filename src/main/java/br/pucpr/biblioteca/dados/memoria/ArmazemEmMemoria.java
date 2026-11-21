package br.pucpr.biblioteca.dados.memoria;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/** Tabela em memória com registros imutáveis e desfazer integrado à unidade de trabalho. */
final class ArmazemEmMemoria<K, R> {
    private final Map<K, R> registros = new ConcurrentHashMap<>();
    private final UnidadeDeTrabalhoEmMemoria uow;

    ArmazemEmMemoria(UnidadeDeTrabalhoEmMemoria uow) {
        this.uow = uow;
    }

    void gravar(K chave, R registro) {
        uow.executar(() -> {
            R anterior = registros.put(chave, registro);
            uow.aoDesfazer(() -> restaurar(chave, anterior));
        });
    }

    void apagar(K chave) {
        uow.executar(() -> {
            R anterior = registros.remove(chave);
            uow.aoDesfazer(() -> restaurar(chave, anterior));
        });
    }

    Optional<R> ler(K chave) {
        return Optional.ofNullable(registros.get(chave));
    }

    List<R> filtrar(Predicate<R> filtro) {
        List<R> lista = new ArrayList<>();
        for (R r : registros.values()) {
            if (filtro.test(r)) lista.add(r);
        }
        return lista;
    }

    private void restaurar(K chave, R anterior) {
        if (anterior == null) registros.remove(chave);
        else registros.put(chave, anterior);
    }
}
