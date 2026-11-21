package br.pucpr.biblioteca.dados.mysql;

import br.pucpr.biblioteca.dados.RepositorioDeRelacionamento;
import br.pucpr.biblioteca.dominio.Atendente;
import br.pucpr.biblioteca.dominio.Estudante;
import br.pucpr.biblioteca.dominio.Pessoa;

import java.util.Optional;

/** PADRÃO ABSTRACT FACTORY — produto concreto. */
public final class RepositorioDeRelacionamentoMySQL implements RepositorioDeRelacionamento {
    private final UnidadeDeTrabalhoMySQL uow;

    RepositorioDeRelacionamentoMySQL(UnidadeDeTrabalhoMySQL uow) {
        this.uow = uow;
    }

    public UnidadeDeTrabalhoMySQL unidadeDeTrabalho() { return uow; }

    @Override
    public void salvarPessoa(Pessoa p) {
        uow.executar(() -> {
            int n = Jdbc.atualizar(uow.conexao(),
                    "UPDATE pessoa SET nome = ?, sobrenome = ?, email = ? WHERE cpf = ?",
                    p.getNome(), p.getSobrenome(), p.getEmail(), p.getCpf());
            if (n == 0) {
                Jdbc.atualizar(uow.conexao(), "INSERT INTO pessoa (cpf, nome, sobrenome, email) VALUES (?, ?, ?, ?)",
                        p.getCpf(), p.getNome(), p.getSobrenome(), p.getEmail());
            }
        });
    }

    @Override
    public void removerPessoa(String cpf) {
        uow.executar(() -> { Jdbc.atualizar(uow.conexao(), "DELETE FROM pessoa WHERE cpf = ?", cpf); });
    }

    @Override
    public Optional<Pessoa> buscarPessoa(String cpf) {
        return uow.executar(() -> Jdbc.primeiro(uow.conexao(),
                "SELECT cpf, nome, sobrenome, email FROM pessoa WHERE cpf = ?",
                rs -> new Pessoa(rs.getString("cpf"), rs.getString("nome"),
                        rs.getString("sobrenome"), rs.getString("email")), cpf));
    }

    @Override
    public void salvarEstudante(Estudante e) {
        uow.executar(() -> {
            int n = Jdbc.atualizar(uow.conexao(), "UPDATE estudante SET cpf = ? WHERE matricula = ?",
                    e.getPessoa().getCpf(), e.getMatricula());
            if (n == 0) {
                Jdbc.atualizar(uow.conexao(), "INSERT INTO estudante (matricula, cpf) VALUES (?, ?)",
                        e.getMatricula(), e.getPessoa().getCpf());
            }
        });
    }

    @Override
    public void removerEstudante(String matricula) {
        uow.executar(() -> { Jdbc.atualizar(uow.conexao(), "DELETE FROM estudante WHERE matricula = ?", matricula); });
    }

    @Override
    public Optional<Estudante> buscarEstudante(String matricula) {
        return uow.executar(() -> Jdbc.primeiro(uow.conexao(),
                "SELECT matricula, cpf FROM estudante WHERE matricula = ?",
                rs -> new String[]{rs.getString(1), rs.getString(2)}, matricula)
                .flatMap(r -> buscarPessoa(r[1]).map(p -> new Estudante(r[0], p))));
    }

    @Override
    public void salvarAtendente(Atendente a) {
        uow.executar(() -> {
            int n = Jdbc.atualizar(uow.conexao(), "UPDATE atendente SET cpf = ? WHERE id_atendente = ?",
                    a.getPessoa().getCpf(), a.getIdAtendente());
            if (n == 0) {
                Jdbc.atualizar(uow.conexao(), "INSERT INTO atendente (id_atendente, cpf) VALUES (?, ?)",
                        a.getIdAtendente(), a.getPessoa().getCpf());
            }
        });
    }

    @Override
    public void removerAtendente(String idAtendente) {
        uow.executar(() -> { Jdbc.atualizar(uow.conexao(), "DELETE FROM atendente WHERE id_atendente = ?", idAtendente); });
    }

    @Override
    public Optional<Atendente> buscarAtendente(String idAtendente) {
        return uow.executar(() -> Jdbc.primeiro(uow.conexao(),
                "SELECT id_atendente, cpf FROM atendente WHERE id_atendente = ?",
                rs -> new String[]{rs.getString(1), rs.getString(2)}, idAtendente)
                .flatMap(r -> buscarPessoa(r[1]).map(p -> new Atendente(r[0], p))));
    }
}
