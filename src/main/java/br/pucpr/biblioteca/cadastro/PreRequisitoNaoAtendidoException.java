package br.pucpr.biblioteca.cadastro;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;

public class PreRequisitoNaoAtendidoException extends RegraDeNegocioException {
    public PreRequisitoNaoAtendidoException(String mensagem) { super(mensagem); }
}
