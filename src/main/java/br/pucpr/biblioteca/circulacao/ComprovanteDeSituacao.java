package br.pucpr.biblioteca.circulacao;

import br.pucpr.biblioteca.dominio.livro.CodigoEstadoLivro;

public record ComprovanteDeSituacao(int idLivro, String tituloLivro, CodigoEstadoLivro estadoAnterior,
                                    CodigoEstadoLivro estadoAtual) {
    @Override
    public String toString() {
        return "Livro " + idLivro + " \"" + tituloLivro + "\": " + estadoAnterior + " -> " + estadoAtual;
    }
}
