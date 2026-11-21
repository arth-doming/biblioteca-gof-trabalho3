package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.circulacao.ComprovanteDeDevolucao;
import br.pucpr.biblioteca.circulacao.ComprovanteDeSituacao;
import br.pucpr.biblioteca.financeiro.multa.Boleto;

import java.util.Optional;

/** Serviços f5 e f6 do Atendente + eventos de inspeção e reparo (Seção 12 do Trabalho 02). */
public interface iDevolucao {
    ComprovanteDeDevolucao registrarDevolucao(int idLivro, String matricula, String idAtendente);
    Optional<Boleto> emitirBoletoDeMulta(int idLivro, String matricula);
    ComprovanteDeSituacao inspecionar(int idLivro, boolean bomEstado);
    ComprovanteDeSituacao enviarParaReparo(int idLivro);
    ComprovanteDeSituacao retornarDoReparo(int idLivro);
    ComprovanteDeSituacao avaliarReparo(int idLivro, boolean reparavel);
}
