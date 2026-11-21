package br.pucpr.biblioteca.dominio;

/** Violação de uma regra do domínio. A operação é recusada sem efeitos colaterais. */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
