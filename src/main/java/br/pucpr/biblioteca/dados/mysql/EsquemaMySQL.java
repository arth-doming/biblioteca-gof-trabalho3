package br.pucpr.biblioteca.dados.mysql;

import java.util.List;

/** DDL executado automaticamente na primeira conexão (o mesmo está em src/main/resources/schema-mysql.sql). */
final class EsquemaMySQL {
    private EsquemaMySQL() { }

    static final List<String> DDL = List.of(
            """
            CREATE TABLE IF NOT EXISTS livro (
              id_livro INT PRIMARY KEY,
              titulo   VARCHAR(200) NOT NULL,
              estado   VARCHAR(20)  NOT NULL
            )""",
            """
            CREATE TABLE IF NOT EXISTS pessoa (
              cpf       VARCHAR(14) PRIMARY KEY,
              nome      VARCHAR(100) NOT NULL,
              sobrenome VARCHAR(100),
              email     VARCHAR(150)
            )""",
            """
            CREATE TABLE IF NOT EXISTS estudante (
              matricula VARCHAR(20) PRIMARY KEY,
              cpf       VARCHAR(14) NOT NULL,
              FOREIGN KEY (cpf) REFERENCES pessoa(cpf)
            )""",
            """
            CREATE TABLE IF NOT EXISTS atendente (
              id_atendente VARCHAR(20) PRIMARY KEY,
              cpf          VARCHAR(14) NOT NULL,
              FOREIGN KEY (cpf) REFERENCES pessoa(cpf)
            )""",
            """
            CREATE TABLE IF NOT EXISTS transacao (
              id              BIGINT AUTO_INCREMENT PRIMARY KEY,
              tipo            VARCHAR(12) NOT NULL,
              id_livro        INT NOT NULL,
              matricula       VARCHAR(20) NOT NULL,
              id_atendente    VARCHAR(20),
              data            DATE NOT NULL,
              data_vencimento DATE,
              num_renovacoes  INT NOT NULL DEFAULT 0
            )""",
            """
            CREATE TABLE IF NOT EXISTS multa (
              numero            VARCHAR(20) PRIMARY KEY,
              id_livro          INT NOT NULL,
              matricula         VARCHAR(20) NOT NULL,
              dias_atraso       INT NOT NULL,
              valor             DECIMAL(10,2) NOT NULL,
              politica_aplicada VARCHAR(40) NOT NULL,
              data_emissao      DATE NOT NULL,
              quitada           BOOLEAN NOT NULL DEFAULT FALSE,
              data_pagamento    DATE,
              boleto_numero     VARCHAR(60),
              boleto_vencimento DATE,
              banco             VARCHAR(5),
              id_no_banco       VARCHAR(60),
              linha_digitavel   VARCHAR(120)
            )""");
}
