package br.pucpr.biblioteca.dados;

import br.pucpr.biblioteca.dados.memoria.FabricaEmMemoria;
import br.pucpr.biblioteca.dados.mysql.ConfiguracaoMySQL;
import br.pucpr.biblioteca.dados.mysql.FabricaMySQL;

/**
 * Único ponto em que a família é escolhida (chamado só pela raiz de composição, Sistema).
 * O resto do sistema enxerga apenas FabricaDeAcessoADados.
 * Observação: o Oracle é réplica de leitura mantida fora da aplicação; nenhuma fábrica
 * cria conexões de escrita para ele.
 */
public final class SelecaoDeFabrica {
    private SelecaoDeFabrica() { }

    public static FabricaDeAcessoADados para(Perfil perfil, ConfiguracaoMySQL mysql) {
        return switch (perfil) {
            case TESTE -> new FabricaEmMemoria();
            case PRODUCAO -> new FabricaMySQL(mysql);
        };
    }
}
