package br.pucpr.biblioteca.consulta;

import java.util.List;
import java.util.Optional;

/** PADRÃO DECORATOR (R9) — Component: contrato de consulta ao acervo. */
public interface ConsultaAcervo {
    Optional<MetadadosLivro> consultarMetadados(int idLivro);

    List<MetadadosLivro> buscarPorTitulo(String trecho);
}
