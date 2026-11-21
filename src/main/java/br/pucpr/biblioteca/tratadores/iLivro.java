package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.cadastro.ResultadoDeCadastro;
import br.pucpr.biblioteca.dominio.livro.Livro;

import java.util.Optional;

public interface iLivro {
    ResultadoDeCadastro<Livro> inserirLivro(String titulo);
    Optional<Livro> consultarLivro(int idLivro);
    boolean atualizarLivro(int idLivro, String titulo);
    boolean excluirLivro(int idLivro);
}
