package br.pucpr.biblioteca.dados.mysql;

import br.pucpr.biblioteca.dados.RepositorioDeMultas;
import br.pucpr.biblioteca.financeiro.multa.Boleto;
import br.pucpr.biblioteca.financeiro.multa.Multa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PADRÃO ABSTRACT FACTORY — produto concreto.
 * A quitação é um UPDATE condicional (quitada = FALSE): se duas notificações chegarem juntas,
 * só uma altera a linha; a outra recebe 0 linhas afetadas.
 */
public final class RepositorioDeMultasMySQL implements RepositorioDeMultas {
    private static final String COLUNAS = "SELECT numero, id_livro, matricula, dias_atraso, valor, politica_aplicada, "
            + "data_emissao, quitada, data_pagamento, boleto_numero, boleto_vencimento, banco, id_no_banco, linha_digitavel FROM multa ";
    private final UnidadeDeTrabalhoMySQL uow;

    RepositorioDeMultasMySQL(UnidadeDeTrabalhoMySQL uow) {
        this.uow = uow;
    }

    public UnidadeDeTrabalhoMySQL unidadeDeTrabalho() { return uow; }

    @Override
    public String proximoNumero() {
        long n = uow.executar(() -> Jdbc.primeiro(uow.conexao(), "SELECT COUNT(*) + 1 FROM multa",
                rs -> rs.getLong(1)).orElse(1L));
        return String.format("M-%06d", n);
    }

    @Override
    public void salvar(Multa m, Boleto b) {
        uow.executar(() -> {
            Jdbc.atualizar(uow.conexao(),
                    "INSERT INTO multa (numero, id_livro, matricula, dias_atraso, valor, politica_aplicada, data_emissao, "
                            + "quitada, data_pagamento, boleto_numero, boleto_vencimento, banco, id_no_banco, linha_digitavel) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    m.getNumero(), m.getIdLivro(), m.getMatricula(), m.getDiasAtraso(), m.getValor(),
                    m.getPoliticaAplicada(), m.getDataEmissao(), m.isQuitada(), m.getDataPagamento(),
                    b.numero(), b.vencimento(), b.banco(), b.identificadorNoBanco(), b.linhaDigitavel());
        });
    }

    @Override
    public Optional<Multa> buscar(String numero) {
        return uow.executar(() -> Jdbc.primeiro(uow.conexao(), COLUNAS + "WHERE numero = ?",
                RepositorioDeMultasMySQL::multa, numero));
    }

    @Override
    public List<Multa> listarPorMatricula(String matricula) {
        return uow.executar(() -> Jdbc.lista(uow.conexao(), COLUNAS + "WHERE matricula = ? ORDER BY numero",
                RepositorioDeMultasMySQL::multa, matricula));
    }

    @Override
    public Optional<Boleto> boletoDaMulta(String numero) {
        return uow.executar(() -> Jdbc.primeiro(uow.conexao(), COLUNAS + "WHERE numero = ?",
                rs -> new Boleto(rs.getString("boleto_numero"), rs.getString("numero"), rs.getBigDecimal("valor"),
                        Jdbc.data(rs, "boleto_vencimento"), rs.getString("banco"), rs.getString("id_no_banco"),
                        rs.getString("linha_digitavel")), numero));
    }

    @Override
    public boolean quitar(String numero, LocalDate dataPagamento) {
        return uow.executar(() -> Jdbc.atualizar(uow.conexao(),
                "UPDATE multa SET quitada = TRUE, data_pagamento = ? WHERE numero = ? AND quitada = FALSE",
                dataPagamento, numero) == 1);
    }

    private static Multa multa(ResultSet rs) throws SQLException {
        return Multa.reconstituir(rs.getString("numero"), rs.getInt("id_livro"), rs.getString("matricula"),
                rs.getLong("dias_atraso"), rs.getBigDecimal("valor"), rs.getString("politica_aplicada"),
                Jdbc.data(rs, "data_emissao"), rs.getBoolean("quitada"), Jdbc.data(rs, "data_pagamento"));
    }
}
