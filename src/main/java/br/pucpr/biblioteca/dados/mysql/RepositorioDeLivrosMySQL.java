package br.pucpr.biblioteca.dados.mysql;

import br.pucpr.biblioteca.dados.RepositorioDeLivros;
import br.pucpr.biblioteca.dominio.livro.CodigoEstadoLivro;
import br.pucpr.biblioteca.dominio.livro.Livro;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** PADRÃO ABSTRACT FACTORY — produto concreto. O estado é gravado na coluna "estado" como texto. */
public final class RepositorioDeLivrosMySQL implements RepositorioDeLivros {
    private final UnidadeDeTrabalhoMySQL uow;

    RepositorioDeLivrosMySQL(UnidadeDeTrabalhoMySQL uow) {
        this.uow = uow;
    }

    public UnidadeDeTrabalhoMySQL unidadeDeTrabalho() { return uow; }

    @Override
    public Optional<Livro> buscar(int idLivro) {
        return uow.executar(() -> Jdbc.primeiro(uow.conexao(),
                "SELECT id_livro, titulo, estado FROM livro WHERE id_livro = ?",
                RepositorioDeLivrosMySQL::mapear, idLivro));
    }

    @Override
    public Optional<Livro> buscarPorTitulo(String titulo) {
        return uow.executar(() -> Jdbc.primeiro(uow.conexao(),
                "SELECT id_livro, titulo, estado FROM livro WHERE LOWER(titulo) = LOWER(?)",
                RepositorioDeLivrosMySQL::mapear, titulo));
    }

    @Override
    public List<Livro> listar() {
        return uow.executar(() -> Jdbc.lista(uow.conexao(),
                "SELECT id_livro, titulo, estado FROM livro ORDER BY id_livro",
                RepositorioDeLivrosMySQL::mapear));
    }

    @Override
    public void salvar(Livro livro) {
        uow.executar(() -> {
            int n = Jdbc.atualizar(uow.conexao(), "UPDATE livro SET titulo = ?, estado = ? WHERE id_livro = ?",
                    livro.getTitulo(), livro.getEstado().name(), livro.getIdLivro());
            if (n == 0) {
                Jdbc.atualizar(uow.conexao(), "INSERT INTO livro (id_livro, titulo, estado) VALUES (?, ?, ?)",
                        livro.getIdLivro(), livro.getTitulo(), livro.getEstado().name());
            }
        });
    }

    @Override
    public void remover(int idLivro) {
        uow.executar(() -> { Jdbc.atualizar(uow.conexao(), "DELETE FROM livro WHERE id_livro = ?", idLivro); });
    }

    @Override
    public int proximoId() {
        return uow.executar(() -> Jdbc.primeiro(uow.conexao(),
                "SELECT COALESCE(MAX(id_livro), 0) + 1 FROM livro", rs -> rs.getInt(1)).orElse(1));
    }

    private static Livro mapear(ResultSet rs) throws SQLException {
        return Livro.reconstituir(rs.getInt("id_livro"), rs.getString("titulo"),
                CodigoEstadoLivro.valueOf(rs.getString("estado")));
    }
}
