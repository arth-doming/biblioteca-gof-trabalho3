package br.pucpr.biblioteca.dados;

/** Perfil de execução. Decide, uma única vez, qual família de acesso a dados é usada. */
public enum Perfil {
    TESTE, PRODUCAO;

    public static Perfil de(String texto) {
        if (texto == null || texto.isBlank()) return TESTE;
        return switch (texto.trim().toLowerCase()) {
            case "producao", "produção", "prod" -> PRODUCAO;
            default -> TESTE;
        };
    }
}
