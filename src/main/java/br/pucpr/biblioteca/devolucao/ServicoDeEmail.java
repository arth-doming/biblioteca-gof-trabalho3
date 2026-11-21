package br.pucpr.biblioteca.devolucao;

public interface ServicoDeEmail {
    void enviar(String destinatario, String assunto, String corpo);
}
