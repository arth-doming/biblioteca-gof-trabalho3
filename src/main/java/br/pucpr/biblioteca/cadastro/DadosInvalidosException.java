package br.pucpr.biblioteca.cadastro;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;

public class DadosInvalidosException extends RegraDeNegocioException {
    public DadosInvalidosException(String mensagem) { super(mensagem); }
}
