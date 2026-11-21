package br.pucpr.biblioteca.dados;

import br.pucpr.biblioteca.dominio.livro.Livro;

import java.util.List;
import java.util.Optional;

/** PADRÃO ABSTRACT FACTORY — produto abstrato. Grava o estado do Livro como CodigoEstadoLivro. */
public interface RepositorioDeLivros {
    Optional<Livro> buscar(int idLivro);
    Optional<Livro> buscarPorTitulo(String titulo);
    List<Livro> listar();
    void salvar(Livro livro);
    void remover(int idLivro);
    int proximoId();
}
