package br.pucpr.biblioteca.dominio;

import java.time.LocalDate;

/** Reserva, empréstimo e devolução são casos particulares de transação (Trabalho 01). */
public abstract class Transacao {
    private long id;
    private final int idLivro;
    private final String matricula;
    private final String idAtendente;
    private final LocalDate data;

    protected Transacao(long id, int idLivro, String matricula, String idAtendente, LocalDate data) {
        this.id = id;
        this.idLivro = idLivro;
        this.matricula = matricula;
        this.idAtendente = idAtendente;
        this.data = data;
    }

    public long getId() { return id; }
    public int getIdLivro() { return idLivro; }
    public String getMatricula() { return matricula; }
    public String getIdAtendente() { return idAtendente; }
    public LocalDate getData() { return data; }

    /** Chamado pelo repositório quando a transação é gravada pela primeira vez. */
    public void atribuirId(long id) {
        if (this.id != 0 && this.id != id) {
            throw new IllegalStateException("Transação já possui id " + this.id);
        }
        this.id = id;
    }

    public boolean envolve(int idLivro, String matricula) {
        return this.idLivro == idLivro && this.matricula.equals(matricula);
    }
}
