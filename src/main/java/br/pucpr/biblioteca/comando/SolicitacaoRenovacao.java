package br.pucpr.biblioteca.comando;

import br.pucpr.biblioteca.circulacao.ComprovanteDeRenovacao;
import br.pucpr.biblioteca.circulacao.FachadaDeCirculacao;

import java.time.Instant;

/** PADRÃO COMMAND — ConcreteCommand de renovação. */
public class SolicitacaoRenovacao extends SolicitacaoDeCirculacao<ComprovanteDeRenovacao> {
    private final int idLivro;
    private final String matricula;

    public SolicitacaoRenovacao(String id, String autor, Instant criadaEm, int idLivro, String matricula) {
        super(id, autor, criadaEm);
        this.idLivro = idLivro;
        this.matricula = matricula;
    }

    @Override
    public ComprovanteDeRenovacao executar(FachadaDeCirculacao receptor) {
        return receptor.renovarEmprestimo(idLivro, matricula);
    }
}
