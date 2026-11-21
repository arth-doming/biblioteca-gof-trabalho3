package br.pucpr.biblioteca.financeiro.cobranca;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * PADRÃO ADAPTER — Adaptee (API simulada do banco B, incompatível com o contrato e com o banco A).
 * Valor em texto com vírgula ("7,00"), CPF formatado (000.000.000-00), data ISO em texto,
 * situação textual ("PAGO") e identificador UUID gerado pelo banco.
 */
public class BancoB {
    public record PedidoDeBoleto(String valor, String cpfFormatado, String nome, String dataVencimentoIso, String seuNumero) { }
    public record RespostaDeBoleto(String idBoleto, String codigoDeBarras) { }
    public record EventoDePagamento(String idBoleto, String valorPago, String status, String dataIso) { }

    private final List<PedidoDeBoleto> pedidosRecebidos = new ArrayList<>();
    private Consumer<EventoDePagamento> ouvinte = e -> { };

    public synchronized RespostaDeBoleto emitirBoleto(PedidoDeBoleto pedido) {
        pedidosRecebidos.add(pedido);
        String id = UUID.randomUUID().toString();
        return new RespostaDeBoleto(id, "23790." + Math.abs(id.hashCode()));
    }

    public void assinarEventos(Consumer<EventoDePagamento> ouvinte) {
        this.ouvinte = ouvinte;
    }

    public void simularPagamento(String idBoleto, String valorPago, String dataIso) {
        ouvinte.accept(new EventoDePagamento(idBoleto, valorPago, "PAGO", dataIso));
    }

    public synchronized List<PedidoDeBoleto> pedidosRecebidos() { return List.copyOf(pedidosRecebidos); }
}
