package br.pucpr.biblioteca.dados.memoria;

import br.pucpr.biblioteca.dados.RepositorioDeMovimentacao;
import br.pucpr.biblioteca.dominio.Devolucao;
import br.pucpr.biblioteca.dominio.Emprestimo;
import br.pucpr.biblioteca.dominio.Reserva;
import br.pucpr.biblioteca.dominio.Transacao;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** PADRÃO ABSTRACT FACTORY — produto concreto. */
public final class RepositorioDeMovimentacaoEmMemoria implements RepositorioDeMovimentacao {
    private enum Tipo { RESERVA, EMPRESTIMO, DEVOLUCAO }

    private record TransacaoRegistro(long id, Tipo tipo, int idLivro, String matricula, String idAtendente,
                                     LocalDate data, LocalDate dataVencimento, int numRenovacoes) { }

    private final ArmazemEmMemoria<Long, TransacaoRegistro> tabela;
    private final AtomicLong sequencia = new AtomicLong();

    RepositorioDeMovimentacaoEmMemoria(UnidadeDeTrabalhoEmMemoria uow) {
        this.tabela = new ArmazemEmMemoria<>(uow);
    }

    @Override
    public void adicionar(Transacao t) {
        if (t.getId() == 0) t.atribuirId(sequencia.incrementAndGet());
        tabela.gravar(t.getId(), registro(t));
    }

    @Override
    public void atualizar(Transacao t) {
        tabela.gravar(t.getId(), registro(t));
    }

    @Override
    public void remover(Transacao t) {
        tabela.apagar(t.getId());
    }

    @Override
    public Optional<Reserva> buscarReservaAtivaDoLivro(int idLivro) {
        return tabela.filtrar(r -> r.tipo() == Tipo.RESERVA && r.idLivro() == idLivro).stream()
                .findFirst().map(r -> (Reserva) reconstituir(r));
    }

    @Override
    public Optional<Emprestimo> buscarEmprestimo(int idLivro, String matricula) {
        return tabela.filtrar(r -> r.tipo() == Tipo.EMPRESTIMO && r.idLivro() == idLivro
                        && r.matricula().equals(matricula)).stream()
                .findFirst().map(r -> (Emprestimo) reconstituir(r));
    }

    @Override
    public Optional<Devolucao> buscarUltimaDevolucao(int idLivro, String matricula) {
        return tabela.filtrar(r -> r.tipo() == Tipo.DEVOLUCAO && r.idLivro() == idLivro
                        && r.matricula().equals(matricula)).stream()
                .max(Comparator.comparingLong(TransacaoRegistro::id))
                .map(r -> (Devolucao) reconstituir(r));
    }

    @Override
    public List<Reserva> listarReservas(String matricula) {
        return tabela.filtrar(r -> r.tipo() == Tipo.RESERVA && r.matricula().equals(matricula)).stream()
                .sorted(Comparator.comparingLong(TransacaoRegistro::id))
                .map(r -> (Reserva) reconstituir(r)).toList();
    }

    private static TransacaoRegistro registro(Transacao t) {
        if (t instanceof Emprestimo e) {
            return new TransacaoRegistro(e.getId(), Tipo.EMPRESTIMO, e.getIdLivro(), e.getMatricula(), null,
                    e.getData(), e.getDataVencimento(), e.getNumRenovacoes());
        }
        if (t instanceof Devolucao d) {
            return new TransacaoRegistro(d.getId(), Tipo.DEVOLUCAO, d.getIdLivro(), d.getMatricula(),
                    d.getIdAtendente(), d.getData(), d.getDataVencimento(), 0);
        }
        return new TransacaoRegistro(t.getId(), Tipo.RESERVA, t.getIdLivro(), t.getMatricula(), null,
                t.getData(), null, 0);
    }

    private static Transacao reconstituir(TransacaoRegistro r) {
        return switch (r.tipo()) {
            case RESERVA -> new Reserva(r.id(), r.idLivro(), r.matricula(), r.data());
            case EMPRESTIMO -> new Emprestimo(r.id(), r.idLivro(), r.matricula(), r.data(),
                    r.dataVencimento(), r.numRenovacoes());
            case DEVOLUCAO -> new Devolucao(r.id(), r.idLivro(), r.matricula(), r.idAtendente(),
                    r.data(), r.dataVencimento());
        };
    }
}
