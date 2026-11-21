package br.pucpr.biblioteca.comando;

import br.pucpr.biblioteca.circulacao.ComprovanteDeReserva;
import br.pucpr.biblioteca.circulacao.FachadaDeCirculacao;

import java.time.Instant;

/** PADRÃO COMMAND — ConcreteCommand de reserva. */
public class SolicitacaoReserva extends SolicitacaoDeCirculacao<ComprovanteDeReserva> {
    private final int idLivro;
    private final String matricula;

    public SolicitacaoReserva(String id, String autor, Instant criadaEm, int idLivro, String matricula) {
        super(id, autor, criadaEm);
        this.idLivro = idLivro;
        this.matricula = matricula;
    }

    @Override
    public ComprovanteDeReserva executar(FachadaDeCirculacao receptor) {
        return receptor.solicitarReserva(idLivro, matricula);
    }
}
