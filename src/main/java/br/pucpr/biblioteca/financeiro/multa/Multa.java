package br.pucpr.biblioteca.financeiro.multa;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Multa emitida pelo Financeiro (pendência P1). Valor e política são congelados na emissão. */
public class Multa {
    private final String numero;
    private final int idLivro;
    private final String matricula;
    private final long diasAtraso;
    private final BigDecimal valor;
    private final String politicaAplicada;
    private final LocalDate dataEmissao;
    private boolean quitada;
    private LocalDate dataPagamento;

    public Multa(String numero, int idLivro, String matricula, long diasAtraso,
                 BigDecimal valor, String politicaAplicada, LocalDate dataEmissao) {
        this.numero = numero;
        this.idLivro = idLivro;
        this.matricula = matricula;
        this.diasAtraso = diasAtraso;
        this.valor = valor;
        this.politicaAplicada = politicaAplicada;
        this.dataEmissao = dataEmissao;
    }

    /** Usado pelos repositórios para recriar uma multa gravada. */
    public static Multa reconstituir(String numero, int idLivro, String matricula, long diasAtraso, BigDecimal valor,
                                     String politicaAplicada, LocalDate dataEmissao, boolean quitada, LocalDate dataPagamento) {
        Multa m = new Multa(numero, idLivro, matricula, diasAtraso, valor, politicaAplicada, dataEmissao);
        m.quitada = quitada;
        m.dataPagamento = dataPagamento;
        return m;
    }

    public String getNumero() { return numero; }
    public int getIdLivro() { return idLivro; }
    public String getMatricula() { return matricula; }
    public long getDiasAtraso() { return diasAtraso; }
    public BigDecimal getValor() { return valor; }
    public String getPoliticaAplicada() { return politicaAplicada; }
    public LocalDate getDataEmissao() { return dataEmissao; }
    public boolean isQuitada() { return quitada; }
    public LocalDate getDataPagamento() { return dataPagamento; }

    /** @return false se já estava quitada (notificação repetida). */
    public synchronized boolean quitar(LocalDate dataPagamento) {
        if (quitada) return false;
        this.quitada = true;
        this.dataPagamento = dataPagamento;
        return true;
    }

    @Override
    public String toString() {
        return "Multa " + numero + ": R$ " + valor + " (" + diasAtraso + " dias, política " + politicaAplicada
                + (quitada ? ", quitada" : ", em aberto") + ")";
    }
}
