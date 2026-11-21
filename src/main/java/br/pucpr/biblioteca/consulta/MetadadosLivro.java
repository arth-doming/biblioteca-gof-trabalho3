package br.pucpr.biblioteca.consulta;

/** Somente metadados — sem estado. Por isso o cache nunca tem como "autorizar" uma reserva. */
public record MetadadosLivro(int idLivro, String titulo) {
}
