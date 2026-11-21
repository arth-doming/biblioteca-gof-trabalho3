package br.pucpr.biblioteca.dados.memoria;

import br.pucpr.biblioteca.dados.RepositorioDeMultas;
import br.pucpr.biblioteca.financeiro.multa.Boleto;
import br.pucpr.biblioteca.financeiro.multa.Multa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** PADRÃO ABSTRACT FACTORY — produto concreto. */
public final class RepositorioDeMultasEmMemoria implements RepositorioDeMultas {
    private record MultaRegistro(String numero, int idLivro, String matricula, long diasAtraso, BigDecimal valor,
                                 String politica, LocalDate emissao, boolean quitada, LocalDate pagamento,
                                 Boleto boleto) { }

    private final ArmazemEmMemoria<String, MultaRegistro> tabela;
    private final UnidadeDeTrabalhoEmMemoria uow;
    private final AtomicLong sequencia = new AtomicLong();

    RepositorioDeMultasEmMemoria(UnidadeDeTrabalhoEmMemoria uow) {
        this.uow = uow;
        this.tabela = new ArmazemEmMemoria<>(uow);
    }

    @Override
    public String proximoNumero() {
        return String.format("M-%06d", sequencia.incrementAndGet());
    }

    @Override
    public void salvar(Multa m, Boleto b) {
        tabela.gravar(m.getNumero(), new MultaRegistro(m.getNumero(), m.getIdLivro(), m.getMatricula(),
                m.getDiasAtraso(), m.getValor(), m.getPoliticaAplicada(), m.getDataEmissao(),
                m.isQuitada(), m.getDataPagamento(), b));
    }

    @Override
    public Optional<Multa> buscar(String numero) {
        return tabela.ler(numero).map(RepositorioDeMultasEmMemoria::reconstituir);
    }

    @Override
    public List<Multa> listarPorMatricula(String matricula) {
        return tabela.filtrar(r -> r.matricula().equals(matricula)).stream()
                .sorted(Comparator.comparing(MultaRegistro::numero))
                .map(RepositorioDeMultasEmMemoria::reconstituir).toList();
    }

    @Override
    public Optional<Boleto> boletoDaMulta(String numero) {
        return tabela.ler(numero).map(MultaRegistro::boleto);
    }

    /** A leitura e a escrita rodam na mesma unidade de trabalho, que serializa as transações. */
    @Override
    public boolean quitar(String numero, LocalDate dataPagamento) {
        return uow.executar(() -> {
            Optional<MultaRegistro> r = tabela.ler(numero);
            if (r.isEmpty() || r.get().quitada()) return false;
            MultaRegistro a = r.get();
            tabela.gravar(numero, new MultaRegistro(a.numero(), a.idLivro(), a.matricula(), a.diasAtraso(), a.valor(),
                    a.politica(), a.emissao(), true, dataPagamento, a.boleto()));
            return true;
        });
    }

    private static Multa reconstituir(MultaRegistro r) {
        return Multa.reconstituir(r.numero(), r.idLivro(), r.matricula(), r.diasAtraso(), r.valor(), r.politica(),
                r.emissao(), r.quitada(), r.pagamento());
    }
}
