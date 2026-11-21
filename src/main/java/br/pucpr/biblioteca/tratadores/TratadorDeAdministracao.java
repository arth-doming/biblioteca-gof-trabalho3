package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.consulta.Medidor;
import br.pucpr.biblioteca.financeiro.multa.ConfiguracaoAdministrativa;

import java.util.Set;

/** Configuração administrativa: política de multa vigente e métricas de consulta. */
public class TratadorDeAdministracao {
    private final ConfiguracaoAdministrativa configuracao;
    private final Medidor medidor;

    public TratadorDeAdministracao(ConfiguracaoAdministrativa configuracao, Medidor medidor) {
        this.configuracao = configuracao;
        this.medidor = medidor;
    }

    public void definirPoliticaDeMulta(String nome) { configuracao.definirPoliticaVigente(nome); }

    public String politicaDeMultaVigente() { return configuracao.politicaVigente().nome(); }

    public Set<String> politicasDeMulta() { return configuracao.politicasDisponiveis(); }

    public String metricasDeConsulta() { return medidor == null ? "medição desativada" : medidor.toString(); }
}
