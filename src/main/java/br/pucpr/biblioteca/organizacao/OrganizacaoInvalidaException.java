package br.pucpr.biblioteca.organizacao;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;

public class OrganizacaoInvalidaException extends RegraDeNegocioException {
    public OrganizacaoInvalidaException(String mensagem) { super(mensagem); }
}
