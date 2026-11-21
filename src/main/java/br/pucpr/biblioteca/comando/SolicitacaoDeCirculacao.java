package br.pucpr.biblioteca.comando;

import br.pucpr.biblioteca.circulacao.FachadaDeCirculacao;

import java.time.Instant;
import java.util.Objects;

/**
 * PADRÃO COMMAND (R6) — Command.
 * Uma solicitação de circulação vira objeto com identificador, autor e instante de criação.
 * Quem cria: a tela (cliente). Quem invoca: ExecutorDeSolicitacoes. Quem executa a regra:
 * a FachadaDeCirculacao (receptor), que delega ao Livro/estado — as regras ficam no domínio.
 * Não há desfazer: reserva e renovação têm efeitos de negócio que só se revertem por outro
 * evento do domínio (ex.: devolução), não por "undo".
 */
public abstract class SolicitacaoDeCirculacao<R> {
    private final String id;
    private final String autor;
    private final Instant criadaEm;

    protected SolicitacaoDeCirculacao(String id, String autor, Instant criadaEm) {
        this.id = Objects.requireNonNull(id, "id");
        this.autor = Objects.requireNonNull(autor, "autor");
        this.criadaEm = Objects.requireNonNull(criadaEm, "criadaEm");
    }

    public String getId() { return id; }
    public String getAutor() { return autor; }
    public Instant getCriadaEm() { return criadaEm; }

    public abstract R executar(FachadaDeCirculacao receptor);
}
