package br.pucpr.biblioteca.dominio;

import java.util.Objects;

/** Papel assumido por uma pessoa. id = matrícula (Estudante) ou idAtendente (Atendente). */
public abstract class Papel {
    private final String id;
    private Pessoa pessoa;

    protected Papel(String id, Pessoa pessoa) {
        this.id = Objects.requireNonNull(id, "id");
        this.pessoa = Objects.requireNonNull(pessoa, "pessoa");
    }

    public String getId() { return id; }
    public Pessoa getPessoa() { return pessoa; }

    public void vincular(Pessoa pessoa) {
        this.pessoa = Objects.requireNonNull(pessoa, "pessoa");
    }
}
