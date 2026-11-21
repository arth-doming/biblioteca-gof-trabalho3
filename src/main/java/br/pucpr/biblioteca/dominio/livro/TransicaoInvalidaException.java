package br.pucpr.biblioteca.dominio.livro;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;

/** Evento recebido por um estado que não o aceita (ou guarda falsa). O livro não é alterado. */
public class TransicaoInvalidaException extends RegraDeNegocioException {
    public TransicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
