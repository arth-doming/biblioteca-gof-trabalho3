package br.pucpr.biblioteca.dominio;

import java.util.Objects;

/** Toda pessoa tem nome, sobrenome e CPF (Trabalho 01). O e-mail é usado no envio do boleto. */
public class Pessoa {
    private final String cpf;
    private String nome;
    private String sobrenome;
    private String email;

    public Pessoa(String cpf, String nome, String sobrenome, String email) {
        this.cpf = Objects.requireNonNull(cpf, "cpf");
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email = email;
    }

    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public String getSobrenome() { return sobrenome; }
    public String getEmail() { return email; }

    public void atualizar(String nome, String sobrenome, String email) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        if (email != null) this.email = email;
    }

    @Override
    public String toString() {
        return nome + " " + sobrenome + " (CPF " + cpf + ")";
    }
}
