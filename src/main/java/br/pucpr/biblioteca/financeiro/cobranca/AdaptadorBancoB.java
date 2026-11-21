package br.pucpr.biblioteca.financeiro.cobranca;

import br.pucpr.biblioteca.financeiro.multa.Boleto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * PADRÃO ADAPTER (R3) — Adapter do banco B. Conversões do banco B:
 * BigDecimal -> "7,00", CPF -> 000.000.000-00, LocalDate -> ISO, idBoleto <-> número do documento,
 * status textual -> SituacaoDoPagamento.
 */
public class AdaptadorBancoB implements ContratoDeCobranca {
    private final BancoB banco;
    private final Map<String, String> documentoPorIdBoleto = new ConcurrentHashMap<>();
    private volatile Consumer<NotificacaoDePagamento> destino = n -> { };

    public AdaptadorBancoB(BancoB banco) {
        this.banco = banco;
        banco.assinarEventos(this::traduzir);
    }

    @Override
    public Boleto registrarCobranca(SolicitacaoDeCobranca s) {
        String valor = s.valor().setScale(2, RoundingMode.HALF_EVEN).toPlainString().replace('.', ',');
        BancoB.RespostaDeBoleto r = banco.emitirBoleto(new BancoB.PedidoDeBoleto(
                valor, formatarCpf(s.cpfPagador()), s.nomePagador(), s.vencimento().toString(), s.numeroDocumento()));
        documentoPorIdBoleto.put(r.idBoleto(), s.numeroDocumento());
        return new Boleto("BOL-" + r.idBoleto().substring(0, 8), s.numeroDocumento(), s.valor(), s.vencimento(),
                banco(), r.idBoleto(), r.codigoDeBarras());
    }

    @Override
    public void aoReceberPagamento(Consumer<NotificacaoDePagamento> destino) {
        this.destino = destino;
    }

    @Override
    public String banco() { return "B"; }

    private void traduzir(BancoB.EventoDePagamento e) {
        String documento = documentoPorIdBoleto.get(e.idBoleto());
        if (documento == null) return;
        SituacaoDoPagamento situacao = switch (e.status()) {
            case "PAGO" -> SituacaoDoPagamento.PAGO;
            case "CANCELADO" -> SituacaoDoPagamento.CANCELADO;
            default -> SituacaoDoPagamento.PENDENTE;
        };
        BigDecimal valor = new BigDecimal(e.valorPago().replace(".", "").replace(',', '.'));
        destino.accept(new NotificacaoDePagamento(documento, valor, situacao, LocalDate.parse(e.dataIso())));
    }

    static String formatarCpf(String cpf) {
        String d = cpf.replaceAll("\\D", "");
        if (d.length() != 11) return cpf;
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }
}
