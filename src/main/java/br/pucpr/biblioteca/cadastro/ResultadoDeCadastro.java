package br.pucpr.biblioteca.cadastro;

/** Resultado do passo "apresentar": a entidade, se foi criada agora e a mensagem para a tela. */
public record ResultadoDeCadastro<T>(T entidade, boolean criada, String mensagem) {
}
