package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.circulacao.ComprovanteDeEmprestimo;
import br.pucpr.biblioteca.circulacao.ComprovanteDeRenovacao;
import br.pucpr.biblioteca.circulacao.ComprovanteDeReserva;
import br.pucpr.biblioteca.circulacao.FachadaDeCirculacao;
import br.pucpr.biblioteca.comando.ExecutorDeSolicitacoes;
import br.pucpr.biblioteca.comando.SolicitacaoDeCirculacao;
import br.pucpr.biblioteca.comando.SolicitacaoRenovacao;
import br.pucpr.biblioteca.comando.SolicitacaoReserva;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Tratador do Estudante (f1–f4). Não tem regra de domínio: delega à fachada (FACADE) e,
 * para reserva e renovação, encaminha a solicitação ao executor (COMMAND).
 */
public class TratadorDeEmprestimo implements iEmprestimo {
    private final FachadaDeCirculacao fachada;
    private final ExecutorDeSolicitacoes executor;
    private final Clock relogio;

    public TratadorDeEmprestimo(FachadaDeCirculacao fachada, ExecutorDeSolicitacoes executor, Clock relogio) {
        this.fachada = fachada;
        this.executor = executor;
        this.relogio = relogio;
    }

    /** Entrada usada pelas telas: a tela cria a solicitação (com id próprio) e a submete. */
    public <R> R submeter(SolicitacaoDeCirculacao<R> solicitacao) {
        return executor.executar(solicitacao);
    }

    @Override
    public ComprovanteDeReserva solicitarReserva(int idLivro, String matricula) {
        return submeter(new SolicitacaoReserva(UUID.randomUUID().toString(), matricula, relogio.instant(),
                idLivro, matricula));
    }

    @Override
    public Optional<ComprovanteDeReserva> consultarReservas(int idLivro, String matricula) {
        return fachada.consultarReserva(idLivro, matricula);
    }

    @Override
    public List<ComprovanteDeReserva> consultarReservas(String matricula) {
        return fachada.consultarReservas(matricula);
    }

    @Override
    public ComprovanteDeEmprestimo efetivarEmprestimo(int idLivro, String matricula) {
        return fachada.efetivarEmprestimo(idLivro, matricula);
    }

    @Override
    public ComprovanteDeRenovacao renovarEmprestimo(int idLivro, String matricula) {
        return submeter(new SolicitacaoRenovacao(UUID.randomUUID().toString(), matricula, relogio.instant(),
                idLivro, matricula));
    }
}
