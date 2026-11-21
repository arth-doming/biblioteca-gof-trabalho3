package br.pucpr.biblioteca.financeiro;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.dados.RepositorioDeMultas;
import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;
import br.pucpr.biblioteca.dominio.Estudante;
import br.pucpr.biblioteca.dominio.RegraDeNegocioException;
import br.pucpr.biblioteca.financeiro.cobranca.ContratoDeCobranca;
import br.pucpr.biblioteca.financeiro.cobranca.NotificacaoDePagamento;
import br.pucpr.biblioteca.financeiro.cobranca.SituacaoDoPagamento;
import br.pucpr.biblioteca.financeiro.cobranca.SolicitacaoDeCobranca;
import br.pucpr.biblioteca.financeiro.multa.Boleto;
import br.pucpr.biblioteca.financeiro.multa.ConfiguracaoAdministrativa;
import br.pucpr.biblioteca.financeiro.multa.Multa;
import br.pucpr.biblioteca.financeiro.multa.PoliticaDeMulta;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Serviço de multas do Financeiro (realiza iMulta e iQuitacao). Multas e boletos são gravados
 * pelo RepositorioDeMultas da família de dados escolhida pelo perfil (decisão D8).
 * STRATEGY (R2): papel Context — pede o valor à política vigente, sem saber qual é.
 * ADAPTER (R3): papel Client — fala só com ContratoDeCobranca; não há "if banco".
 */
public class TratadorDeMulta implements iMulta, iQuitacao {
    public static final int DIAS_PARA_PAGAR = 10;

    private final ConfiguracaoAdministrativa configuracao;
    private final ContratoDeCobranca cobranca;
    private final Relacionamento relacionamento;
    private final Clock relogio;

    private final RepositorioDeMultas multas;
    private final UnidadeDeTrabalho uow;

    public TratadorDeMulta(ConfiguracaoAdministrativa configuracao, ContratoDeCobranca cobranca,
                           Relacionamento relacionamento, RepositorioDeMultas multas,
                           UnidadeDeTrabalho uow, Clock relogio) {
        this.configuracao = configuracao;
        this.cobranca = cobranca;
        this.relacionamento = relacionamento;
        this.relogio = relogio;
        this.multas = multas;
        this.uow = uow;
        cobranca.aoReceberPagamento(this::registrarQuitacao);
    }

    /** Dias corridos entre o vencimento e a devolução; nunca negativo. */
    public static long diasDeAtraso(LocalDate vencimento, LocalDate devolucao) {
        return Math.max(0, ChronoUnit.DAYS.between(vencimento, devolucao));
    }

    @Override
    public Optional<Boleto> emitirMulta(int idLivro, String matricula, LocalDate dataVencimento, LocalDate dataDevolucao) {
        long dias = diasDeAtraso(dataVencimento, dataDevolucao);
        PoliticaDeMulta politica = configuracao.politicaVigente();   // lida UMA vez, na emissão
        BigDecimal valor = politica.calcular(dias);
        if (valor.signum() <= 0) {
            return Optional.empty();
        }
        Estudante estudante = relacionamento.consultarEstudante(matricula)
                .orElseThrow(() -> new RegraDeNegocioException("Estudante não cadastrado: " + matricula));

        LocalDate hoje = LocalDate.now(relogio);
        return uow.executar(() -> {
            String numero = multas.proximoNumero();
            Multa multa = new Multa(numero, idLivro, matricula, dias, valor, politica.nome(), hoje);
            Boleto boleto = cobranca.registrarCobranca(new SolicitacaoDeCobranca(
                    numero, valor, estudante.getPessoa().getCpf(),
                    estudante.getPessoa().getNome() + " " + estudante.getPessoa().getSobrenome(),
                    hoje.plusDays(DIAS_PARA_PAGAR)));
            multas.salvar(multa, boleto);          // multa e boleto gravados juntos
            return Optional.of(boleto);
        });
    }

    @Override
    public List<Multa> consultarMulta(String matricula) {
        return multas.listarPorMatricula(matricula);
    }

    @Override
    public Optional<Boleto> consultarBoleto(int idLivro, String matricula) {
        return consultarMulta(matricula).stream()
                .filter(m -> m.getIdLivro() == idLivro)
                .reduce((a, b) -> b)
                .flatMap(m -> multas.boletoDaMulta(m.getNumero()));
    }

    /** Idempotente: a segunda notificação do mesmo documento não gera segunda quitação. */
    @Override
    public boolean registrarQuitacao(NotificacaoDePagamento n) {
        if (n.situacao() != SituacaoDoPagamento.PAGO) return false;
        return multas.quitar(n.numeroDocumento(), n.dataPagamento());   // atômico no repositório
    }

    public Optional<Multa> buscar(String numeroMulta) {
        return multas.buscar(numeroMulta);
    }

    public String bancoEmUso() { return cobranca.banco(); }
}
