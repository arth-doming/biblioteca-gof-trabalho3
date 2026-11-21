package br.pucpr.biblioteca.devolucao;

import java.util.List;
import java.util.Map;

/** Quem foi notificado e quem falhou (a falha não interrompe os demais). */
public record ResultadoDaPublicacao(List<String> notificados, Map<String, String> falhas) {
    public boolean semFalhas() { return falhas.isEmpty(); }
}
