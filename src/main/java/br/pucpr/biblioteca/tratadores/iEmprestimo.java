package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.circulacao.ComprovanteDeEmprestimo;
import br.pucpr.biblioteca.circulacao.ComprovanteDeRenovacao;
import br.pucpr.biblioteca.circulacao.ComprovanteDeReserva;

import java.util.List;
import java.util.Optional;

/** Serviços f1 a f4 do Estudante. */
public interface iEmprestimo {
    ComprovanteDeReserva solicitarReserva(int idLivro, String matricula);
    Optional<ComprovanteDeReserva> consultarReservas(int idLivro, String matricula);
    List<ComprovanteDeReserva> consultarReservas(String matricula);
    ComprovanteDeEmprestimo efetivarEmprestimo(int idLivro, String matricula);
    ComprovanteDeRenovacao renovarEmprestimo(int idLivro, String matricula);
}
