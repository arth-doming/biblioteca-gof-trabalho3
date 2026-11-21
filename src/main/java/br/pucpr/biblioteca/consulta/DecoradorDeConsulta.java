package br.pucpr.biblioteca.consulta;

import java.util.List;
import java.util.Optional;

/** PADRÃO DECORATOR — Decorator abstrato: repassa tudo para o objeto embrulhado. */
public abstract class DecoradorDeConsulta implements ConsultaAcervo {
    protected final ConsultaAcervo interna;

    protected DecoradorDeConsulta(ConsultaAcervo interna) {
        this.interna = interna;
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(int idLivro) {
        return interna.consultarMetadados(idLivro);
    }

    @Override
    public List<MetadadosLivro> buscarPorTitulo(String trecho) {
        return interna.buscarPorTitulo(trecho);
    }
}
