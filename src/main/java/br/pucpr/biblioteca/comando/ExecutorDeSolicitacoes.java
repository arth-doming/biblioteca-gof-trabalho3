package br.pucpr.biblioteca.comando;

import br.pucpr.biblioteca.circulacao.FachadaDeCirculacao;
import br.pucpr.biblioteca.dominio.RegraDeNegocioException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * PADRÃO COMMAND (R6) — Invoker.
 * Garante que um mesmo identificador seja executado no máximo uma vez. O registro usa
 * putIfAbsent atômico: com duas submissões simultâneas, só a primeira coloca sua tarefa
 * no mapa e a executa; a outra recebe a tarefa já registrada e espera o mesmo resultado
 * (ou a mesma exceção).
 * Recusas de regra de negócio ficam registradas (repetir dá a mesma recusa). Falhas técnicas
 * (ex.: banco indisponível) liberam o identificador, para que a mesma solicitação possa ser
 * reenviada depois (decisão D10).
 */
public class ExecutorDeSolicitacoes {
    private final FachadaDeCirculacao receptor;
    private final Map<String, FutureTask<Object>> executadas = new ConcurrentHashMap<>();

    public ExecutorDeSolicitacoes(FachadaDeCirculacao receptor) {
        this.receptor = receptor;
    }

    @SuppressWarnings("unchecked")
    public <R> R executar(SolicitacaoDeCirculacao<R> solicitacao) {
        FutureTask<Object> nova = new FutureTask<>(() -> solicitacao.executar(receptor));
        FutureTask<Object> registrada = executadas.putIfAbsent(solicitacao.getId(), nova);
        FutureTask<Object> tarefa = registrada == null ? nova : registrada;
        if (registrada == null) {
            nova.run();
        }
        try {
            return (R) tarefa.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Execução interrompida", e);
        } catch (ExecutionException e) {
            Throwable causa = e.getCause();
            if (!(causa instanceof RegraDeNegocioException)) {
                executadas.remove(solicitacao.getId(), tarefa);     // falha técnica: permite nova tentativa
            }
            if (causa instanceof RuntimeException re) throw re;
            if (causa instanceof Error er) throw er;
            throw new IllegalStateException(causa);
        }
    }

    public boolean jaExecutada(String idSolicitacao) {
        return executadas.containsKey(idSolicitacao);
    }
}
