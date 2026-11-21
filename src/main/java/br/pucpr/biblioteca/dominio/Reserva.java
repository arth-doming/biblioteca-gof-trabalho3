package br.pucpr.biblioteca.dominio;

import java.time.LocalDate;

public class Reserva extends Transacao {
    public Reserva(long id, int idLivro, String matricula, LocalDate data) {
        super(id, idLivro, matricula, null, data);
    }
}
