package br.pucpr.biblioteca.dados.mysql;

/**
 * Dados de conexão do perfil de produção. Padrão: usuário local "biblioteca"/"biblioteca",
 * criado por docs/mysql-criar-usuario.sql. Pode ser trocado com -Dmysql.url, -Dmysql.usuario, -Dmysql.senha.
 */
public record ConfiguracaoMySQL(String url, String usuario, String senha) {

    public static final String URL_PADRAO = "jdbc:mysql://localhost:3306/biblioteca?createDatabaseIfNotExist=true";
    public static final String URL_TESTE = "jdbc:mysql://localhost:3306/biblioteca_teste?createDatabaseIfNotExist=true";

    public static ConfiguracaoMySQL padrao() {
        return comUrl(System.getProperty("mysql.url", URL_PADRAO));
    }

    /** Usada pelo teste de integração (banco separado, para não misturar com o da demonstração). */
    public static ConfiguracaoMySQL paraTestes() {
        return comUrl(System.getProperty("mysql.url", URL_TESTE));
    }

    private static ConfiguracaoMySQL comUrl(String url) {
        return new ConfiguracaoMySQL(url,
                System.getProperty("mysql.usuario", "biblioteca"),
                System.getProperty("mysql.senha", "biblioteca"));
    }
}
