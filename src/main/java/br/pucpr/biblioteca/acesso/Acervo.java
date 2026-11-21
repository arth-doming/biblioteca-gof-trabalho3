package br.pucpr.biblioteca.acesso;

import br.pucpr.biblioteca.dados.RepositorioDeLivros;
import br.pucpr.biblioteca.dominio.livro.Livro;

import java.util.List;
import java.util.Optional;

/** Ponto interno de acesso aos livros (Trabalho 02). Os tratadores chegam aqui por delegação. */
public class Acervo {
    private final RepositorioDeLivros livros;

    public Acervo(RepositorioDeLivros livros) {
        this.livros = livros;
    }

    public void adicionarLivro(Livro livro) { livros.salvar(livro); }
    public void atualizarLivro(Livro livro) { livros.salvar(livro); }
    public void removerLivro(int idLivro) { livros.remover(idLivro); }
    public Optional<Livro> consultarLivro(int idLivro) { return livros.buscar(idLivro); }
    public Optional<Livro> consultarPorTitulo(String titulo) { return livros.buscarPorTitulo(titulo); }
    public List<Livro> listarLivros() { return livros.listar(); }
    public int proximoIdLivro() { return livros.proximoId(); }
}
