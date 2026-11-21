package br.pucpr.biblioteca.cadastro;

/** Normaliza CPF para 11 dígitos. */
public final class Cpf {
    private Cpf() { }

    public static String normalizar(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    public static boolean valido(String cpf) {
        String d = normalizar(cpf);
        return d != null && d.length() == 11;
    }
}
