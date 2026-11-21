package br.pucpr.biblioteca.financeiro.multa;

import br.pucpr.biblioteca.dominio.RegraDeNegocioException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Configuração administrativa que define a política de multa vigente (contexto do Strategy).
 * A troca de política afeta apenas multas emitidas depois dela: cada Multa grava valor e
 * nome da política no momento da emissão.
 */
public class ConfiguracaoAdministrativa {
    private final Map<String, PoliticaDeMulta> politicas = new LinkedHashMap<>();
    private volatile PoliticaDeMulta vigente;

    public ConfiguracaoAdministrativa() {
        registrar(new PoliticaOriginal());
        registrar(new PoliticaComTeto());
        registrar(new PoliticaComTolerancia());
        vigente = politicas.get(PoliticaOriginal.NOME);
    }

    public synchronized void registrar(PoliticaDeMulta politica) {
        politicas.put(politica.nome(), politica);
    }

    public synchronized void definirPoliticaVigente(String nome) {
        PoliticaDeMulta p = politicas.get(nome);
        if (p == null) throw new RegraDeNegocioException("Política de multa desconhecida: " + nome);
        vigente = p;
    }

    public PoliticaDeMulta politicaVigente() { return vigente; }

    public synchronized Set<String> politicasDisponiveis() { return Set.copyOf(politicas.keySet()); }
}
