package br.pucpr.biblioteca.dados.mysql;

import br.pucpr.biblioteca.dados.RepositorioDeMovimentacao;
import br.pucpr.biblioteca.dominio.Devolucao;
import br.pucpr.biblioteca.dominio.Emprestimo;
import br.pucpr.biblioteca.dominio.Reserva;
import br.pucpr.biblioteca.dominio.Transacao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** PADRÃO ABSTRACT FACTORY — produto concreto. */
public final class RepositorioDeMovimentacaoMySQL implements RepositorioDeMovimentacao {
    private static final String COLUNAS =
            "SELECT id, tipo, id_livro, matricula, id_atendente, data, data_vencimento, num_renovacoes FROM transacao ";
    private final UnidadeDeTrabalhoMySQL uow;

    RepositorioDeMovimentacaoMySQL(UnidadeDeTrabalhoMySQL uow) {
        this.uow = uow;
    }

    public UnidadeDeTrabalhoMySQL unidadeDeTrabalho() { return uow; }

    @Override
    public void adicionar(Transacao t) {
        uow.executar(() -> {
            long id = Jdbc.inserirComChave(uow.conexao(),
                    "INSERT INTO transacao (tipo, id_livro, matricula, id_atendente, data, data_vencimento, num_renovacoes) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    tipo(t), t.getIdLivro(), t.getMatricula(), t.getIdAtendente(), t.getData(),
                    vencimento(t), renovacoes(t));
            t.atribuirId(id);
        });
    }

    @Override
    public void atualizar(Transacao t) {
        uow.executar(() -> {
            Jdbc.atualizar(uow.conexao(),
                    "UPDATE transacao SET data_vencimento = ?, num_renovacoes = ? WHERE id = ?",
                    vencimento(t), renovacoes(t), t.getId());
        });
    }

    @Override
    public void remover(Transacao t) {
        uow.executar(() -> { Jdbc.atualizar(uow.conexao(), "DELETE FROM transacao WHERE id = ?", t.getId()); });
    }

    @Override
    public Optional<Reserva> buscarReservaAtivaDoLivro(int idLivro) {
        return uow.executar(() -> Jdbc.primeiro(uow.conexao(),
                COLUNAS + "WHERE tipo = 'RESERVA' AND id_livro = ?",
                rs -> (Reserva) mapear(rs), idLivro));
    }

    @Override
    public Optional<Emprestimo> buscarEmprestimo(int idLivro, String matricula) {
        return uow.executar(() -> Jdbc.primeiro(uow.conexao(),
                COLUNAS + "WHERE tipo = 'EMPRESTIMO' AND id_livro = ? AND matricula = ?",
                rs -> (Emprestimo) mapear(rs), idLivro, matricula));
    }

    @Override
    public Optional<Devolucao> buscarUltimaDevolucao(int idLivro, String matricula) {
        return uow.executar(() -> Jdbc.primeiro(uow.conexao(),
                COLUNAS + "WHERE tipo = 'DEVOLUCAO' AND id_livro = ? AND matricula = ? ORDER BY id DESC",
                rs -> (Devolucao) mapear(rs), idLivro, matricula));
    }

    @Override
    public List<Reserva> listarReservas(String matricula) {
        return uow.executar(() -> Jdbc.lista(uow.conexao(),
                COLUNAS + "WHERE tipo = 'RESERVA' AND matricula = ? ORDER BY id",
                rs -> (Reserva) mapear(rs), matricula));
    }

    private static String tipo(Transacao t) {
        if (t instanceof Emprestimo) return "EMPRESTIMO";
        if (t instanceof Devolucao) return "DEVOLUCAO";
        return "RESERVA";
    }

    private static Object vencimento(Transacao t) {
        if (t instanceof Emprestimo e) return e.getDataVencimento();
        if (t instanceof Devolucao d) return d.getDataVencimento();
        return null;
    }

    private static int renovacoes(Transacao t) {
        return t instanceof Emprestimo e ? e.getNumRenovacoes() : 0;
    }

    private static Transacao mapear(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        int idLivro = rs.getInt("id_livro");
        String matricula = rs.getString("matricula");
        return switch (rs.getString("tipo")) {
            case "EMPRESTIMO" -> new Emprestimo(id, idLivro, matricula, Jdbc.data(rs, "data"),
                    Jdbc.data(rs, "data_vencimento"), rs.getInt("num_renovacoes"));
            case "DEVOLUCAO" -> new Devolucao(id, idLivro, matricula, rs.getString("id_atendente"),
                    Jdbc.data(rs, "data"), Jdbc.data(rs, "data_vencimento"));
            default -> new Reserva(id, idLivro, matricula, Jdbc.data(rs, "data"));
        };
    }
}
