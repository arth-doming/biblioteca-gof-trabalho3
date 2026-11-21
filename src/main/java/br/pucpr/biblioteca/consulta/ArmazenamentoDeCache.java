package br.pucpr.biblioteca.consulta;

import java.util.Optional;

/** Onde o cache guarda os metadados (em memória aqui; poderia ser Redis). Pode falhar. */
public interface ArmazenamentoDeCache {
    Optional<MetadadosLivro> ler(int idLivro);

    void gravar(MetadadosLivro metadados);

    void invalidar(int idLivro);
}
