package br.pucpr.biblioteca.devolucao;

import br.pucpr.biblioteca.financeiro.iMulta;

/** PADRÃO OBSERVER — ConcreteObserver: emite multa e boleto quando há atraso (ação de T6). */
public class OuvinteDeMulta implements OuvinteDeDevolucao {
    private final iMulta multas;

    public OuvinteDeMulta(iMulta multas) { this.multas = multas; }

    @Override
    public String nome() { return "multa"; }

    @Override
    public void aoDevolver(EventoDevolucao e) {
        if (e.emAtraso()) {
            multas.emitirMulta(e.idLivro(), e.matricula(), e.dataVencimento(), e.dataDevolucao());
        }
    }
}
