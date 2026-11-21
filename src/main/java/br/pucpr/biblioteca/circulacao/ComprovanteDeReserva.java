package br.pucpr.biblioteca.circulacao;

import java.time.LocalDate;

public record ComprovanteDeReserva(long idReserva, int idLivro, String tituloLivro, String matricula, LocalDate data) {
    @Override
    public String toString() {
        return "Reserva #" + idReserva + " | livro " + idLivro + " \"" + tituloLivro + "\" | estudante " + matricula + " | " + data;
    }
}
