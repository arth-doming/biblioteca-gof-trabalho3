package br.pucpr.biblioteca.dominio;

public class Estudante extends Papel {
    public Estudante(String matricula, Pessoa pessoa) {
        super(matricula, pessoa);
    }

    public String getMatricula() { return getId(); }

    @Override
    public String toString() {
        return "Estudante " + getMatricula() + " - " + getPessoa();
    }
}
