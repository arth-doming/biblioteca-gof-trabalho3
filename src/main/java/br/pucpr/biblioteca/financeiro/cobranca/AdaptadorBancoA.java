package br.pucpr.biblioteca.financeiro.cobranca;

import br.pucpr.biblioteca.financeiro.multa.Boleto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * PADRÃO ADAPTER (R3) — Adapter do banco A. Todas as conversões do banco A ficam aqui:
 * reais -> centavos, CPF -> só dígitos, LocalDate -> AAAAMMDD, nossoNumero <-> número do documento,
 * código de situação -> SituacaoDoPagamento.
 */
public class AdaptadorBancoA implements ContratoDeCobranca {
    private static final DateTimeFormatter AAAAMMDD = DateTimeFormatter.BASIC_ISO_DATE;

    private final BancoA banco;
    private final Map<String, String> documentoPorNossoNumero = new ConcurrentHashMap<>();
    private volatile Consumer<NotificacaoDePagamento> destino = n -> { };

    public AdaptadorBancoA(BancoA banco) {
        this.banco = banco;
        banco.cadastrarWebhook(this::traduzir);
    }

    @Override
    public Boleto registrarCobranca(SolicitacaoDeCobranca s) {
        long centavos = s.valor().movePointRight(2).setScale(0, RoundingMode.HALF_EVEN).longValueExact();
        String documento = s.cpfPagador().replaceAll("\\D", "");
        int vencimento = Integer.parseInt(s.vencimento().format(AAAAMMDD));
        String nossoNumero = banco.incluirTitulo(centavos, documento, vencimento);
        documentoPorNossoNumero.put(nossoNumero, s.numeroDocumento());
        return new Boleto("BOL-" + nossoNumero, s.numeroDocumento(), s.valor(), s.vencimento(),
                banco(), nossoNumero, "001-" + nossoNumero + "-" + centavos);
    }

    @Override
    public void aoReceberPagamento(Consumer<NotificacaoDePagamento> destino) {
        this.destino = destino;
    }

    @Override
    public String banco() { return "A"; }

    private void traduzir(BancoA.AvisoDeLiquidacao aviso) {
        String documento = documentoPorNossoNumero.get(aviso.nossoNumero());
        if (documento == null) return;                     // título desconhecido: ignora
        SituacaoDoPagamento situacao = aviso.codigoSituacao() == BancoA.LIQUIDADO
                ? SituacaoDoPagamento.PAGO : SituacaoDoPagamento.PENDENTE;
        BigDecimal valor = BigDecimal.valueOf(aviso.centavosPagos()).movePointLeft(2);
        LocalDate data = LocalDate.parse(String.valueOf(aviso.dataAAAAMMDD()), AAAAMMDD);
        destino.accept(new NotificacaoDePagamento(documento, valor, situacao, data));
    }
}
