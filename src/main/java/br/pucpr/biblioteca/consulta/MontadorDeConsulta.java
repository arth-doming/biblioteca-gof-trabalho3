package br.pucpr.biblioteca.consulta;

import br.pucpr.biblioteca.acesso.Acervo;

/**
 * Monta a cadeia de decoradores conforme a instalação.
 * Ordem: Medicao( Cache( Original ) ). A medição fica por fora para contar TODAS as
 * consultas, inclusive as respondidas pelo cache. Se fosse Cache( Medicao( Original ) ),
 * os acertos do cache não seriam medidos.
 */
public final class MontadorDeConsulta {
    private MontadorDeConsulta() { }

    public static ConsultaAcervo montar(Acervo acervo, ArmazenamentoDeCache cacheOuNull, Medidor medidorOuNull) {
        ConsultaAcervo consulta = new ConsultaAcervoOriginal(acervo);
        if (cacheOuNull != null) consulta = new ConsultaComCache(consulta, cacheOuNull);
        if (medidorOuNull != null) consulta = new ConsultaComMedicao(consulta, medidorOuNull);
        return consulta;
    }
}
