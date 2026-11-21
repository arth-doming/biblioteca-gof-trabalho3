package br.pucpr.biblioteca.dados.mysql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Utilitário mínimo de JDBC usado pelos repositórios MySQL. */
final class Jdbc {
    private Jdbc() { }

    @FunctionalInterface
    interface Mapeador<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    static <T> Optional<T> primeiro(Connection c, String sql, Mapeador<T> m, Object... params) {
        List<T> l = lista(c, sql, m, params);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }

    static <T> List<T> lista(Connection c, String sql, Mapeador<T> m, Object... params) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            vincular(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) out.add(m.mapear(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new FalhaDePersistenciaException(sql, e);
        }
    }

    static int atualizar(Connection c, String sql, Object... params) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            vincular(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new FalhaDePersistenciaException(sql, e);
        }
    }

    static long inserirComChave(Connection c, String sql, Object... params) {
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            vincular(ps, params);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new FalhaDePersistenciaException(sql, e);
        }
    }

    static LocalDate data(ResultSet rs, String coluna) throws SQLException {
        Date d = rs.getDate(coluna);
        return d == null ? null : d.toLocalDate();
    }

    private static void vincular(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p instanceof LocalDate ld) ps.setDate(i + 1, Date.valueOf(ld));
            else ps.setObject(i + 1, p);
        }
    }
}
