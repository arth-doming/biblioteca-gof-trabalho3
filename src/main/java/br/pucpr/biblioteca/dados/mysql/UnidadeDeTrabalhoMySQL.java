package br.pucpr.biblioteca.dados.mysql;

import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

/**
 * PADRÃO ABSTRACT FACTORY — produto concreto (família relacional).
 * Uma conexão JDBC por transação, guardada em ThreadLocal: todos os repositórios MySQL que
 * recebem esta unidade escrevem na mesma conexão, logo no mesmo COMMIT/ROLLBACK.
 * A conexão só é aberta quando a primeira transação começa.
 */
public final class UnidadeDeTrabalhoMySQL implements UnidadeDeTrabalho {
    private final ConfiguracaoMySQL configuracao;
    private final ThreadLocal<Connection> conexaoAtual = new ThreadLocal<>();
    private volatile boolean esquemaCriado;

    public UnidadeDeTrabalhoMySQL(ConfiguracaoMySQL configuracao) {
        this.configuracao = configuracao;
    }

    @Override
    public <T> T executar(Supplier<T> operacao) {
        if (emAndamento()) {
            return operacao.get();
        }
        try (Connection c = DriverManager.getConnection(
                configuracao.url(), configuracao.usuario(), configuracao.senha())) {
            criarEsquemaSeNecessario(c);
            c.setAutoCommit(false);
            conexaoAtual.set(c);
            try {
                T resultado = operacao.get();
                c.commit();
                return resultado;
            } catch (RuntimeException | Error e) {
                c.rollback();
                throw e;
            } finally {
                conexaoAtual.remove();
            }
        } catch (SQLException e) {
            throw new FalhaDePersistenciaException("Falha na transação MySQL", e);
        }
    }

    @Override
    public boolean emAndamento() {
        return conexaoAtual.get() != null;
    }

    @Override
    public String familia() {
        return FabricaMySQL.FAMILIA;
    }

    /** Conexão da transação corrente; usada apenas pelos repositórios MySQL. */
    Connection conexao() {
        Connection c = conexaoAtual.get();
        if (c == null) throw new IllegalStateException("Nenhuma transação aberta");
        return c;
    }

    private void criarEsquemaSeNecessario(Connection c) throws SQLException {
        if (esquemaCriado) return;
        synchronized (this) {
            if (esquemaCriado) return;
            try (Statement st = c.createStatement()) {
                for (String ddl : EsquemaMySQL.DDL) st.execute(ddl);
            }
            esquemaCriado = true;
        }
    }
}
