package br.pucpr.biblioteca.devolucao;

/** PADRÃO OBSERVER — ConcreteObserver: atualiza indicadores de circulação. */
public class OuvinteDeIndicadores implements OuvinteDeDevolucao {
    private final IndicadoresDeCirculacao indicadores;

    public OuvinteDeIndicadores(IndicadoresDeCirculacao indicadores) { this.indicadores = indicadores; }

    @Override
    public String nome() { return "indicadores"; }

    @Override
    public void aoDevolver(EventoDevolucao e) { indicadores.registrar(e); }
}
