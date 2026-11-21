package br.pucpr.biblioteca.dominio;

public class Atendente extends Papel {
    public Atendente(String idAtendente, Pessoa pessoa) {
        super(idAtendente, pessoa);
    }

    public String getIdAtendente() { return getId(); }

    @Override
    public String toString() {
        return "Atendente " + getIdAtendente() + " - " + getPessoa();
    }
}
