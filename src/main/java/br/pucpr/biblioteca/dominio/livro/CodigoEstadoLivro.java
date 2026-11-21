package br.pucpr.biblioteca.dominio.livro;

/**
 * Código gravado na persistência (era o enum EstadoLivro do Trabalho 02).
 * O nome EstadoLivro passou para a hierarquia do padrão State.
 */
public enum CodigoEstadoLivro {
    LIVRE, RESERVADO, EMPRESTADO, RENOVADO, DEVOLVIDO, REPARANDO, INCINERADO
}
