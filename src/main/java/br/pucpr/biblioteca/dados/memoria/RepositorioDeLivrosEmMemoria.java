package br.pucpr.biblioteca.dados.memoria;

import br.pucpr.biblioteca.dados.RepositorioDeLivros;
import br.pucpr.biblioteca.dominio.livro.CodigoEstadoLivro;
import br.pucpr.biblioteca.dominio.livro.Livro;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/** PADRÃO ABSTRACT FACTORY — produto concreto. Só aceita a unidade de trabalho em memória. */
public final class RepositorioDeLivrosEmMemoria implements RepositorioDeLivros {
    private record LivroRegistro(int id, String titulo, CodigoEstadoLivro estado) { }

    private final ArmazemEmMemoria<Integer, LivroRegistro> tabela;
    private final AtomicInteger sequencia = new AtomicInteger();

    RepositorioDeLivrosEmMemoria(UnidadeDeTrabalhoEmMemoria uow) {
        this.tabela = new ArmazemEmMemoria<>(uow);
    }

    @Override
    public Optional<Livro> buscar(int idLivro) {
        return tabela.ler(idLivro).map(RepositorioDeLivrosEmMemoria::reconstituir);
    }

    @Override
    public Optional<Livro> buscarPorTitulo(String titulo) {
        return tabela.filtrar(r -> r.titulo().equalsIgnoreCase(titulo)).stream()
                .findFirst().map(RepositorioDeLivrosEmMemoria::reconstituir);
    }

    @Override
    public List<Livro> listar() {
        return tabela.filtrar(r -> true).stream()
                .sorted(Comparator.comparingInt(LivroRegistro::id))
                .map(RepositorioDeLivrosEmMemoria::reconstituir).toList();
    }

    @Override
    public void salvar(Livro livro) {
        sequencia.accumulateAndGet(livro.getIdLivro(), Math::max);
        tabela.gravar(livro.getIdLivro(),
                new LivroRegistro(livro.getIdLivro(), livro.getTitulo(), livro.getEstado()));
    }

    @Override
    public void remover(int idLivro) {
        tabela.apagar(idLivro);
    }

    @Override
    public int proximoId() {
        return sequencia.incrementAndGet();
    }

    private static Livro reconstituir(LivroRegistro r) {
        return Livro.reconstituir(r.id(), r.titulo(), r.estado());
    }
}
