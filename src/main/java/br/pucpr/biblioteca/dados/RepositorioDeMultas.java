package br.pucpr.biblioteca.dados;

import br.pucpr.biblioteca.financeiro.multa.Boleto;
import br.pucpr.biblioteca.financeiro.multa.Multa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PADRÃO ABSTRACT FACTORY — produto abstrato (multas do Financeiro, decisão D8).
 * Multa e boleto são gravados juntos; a quitação é atômica para resistir a notificações repetidas.
 */
public interface RepositorioDeMultas {
    String proximoNumero();
    void salvar(Multa multa, Boleto boleto);
    Optional<Multa> buscar(String numero);
    List<Multa> listarPorMatricula(String matricula);
    Optional<Boleto> boletoDaMulta(String numero);

    /** Marca como quitada só se ainda estiver em aberto. @return true se quitou agora. */
    boolean quitar(String numero, LocalDate dataPagamento);
}
