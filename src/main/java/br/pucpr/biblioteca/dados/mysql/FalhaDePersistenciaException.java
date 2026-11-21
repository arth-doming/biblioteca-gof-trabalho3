package br.pucpr.biblioteca.dados.mysql;

public class FalhaDePersistenciaException extends RuntimeException {
    public FalhaDePersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
