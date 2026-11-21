package br.pucpr.biblioteca.devolucao;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Guarda as mensagens em vez de enviá-las. Pode ser configurado para falhar (cenário do R5). */
public class ServicoDeEmailSimulado implements ServicoDeEmail {
    public record Mensagem(String destinatario, String assunto, String corpo) { }

    private final List<Mensagem> enviadas = new CopyOnWriteArrayList<>();
    private volatile boolean falhar;

    public void simularFalha(boolean falhar) { this.falhar = falhar; }

    @Override
    public void enviar(String destinatario, String assunto, String corpo) {
        if (falhar) throw new IllegalStateException("Servidor SMTP indisponível");
        enviadas.add(new Mensagem(destinatario, assunto, corpo));
    }

    public List<Mensagem> enviadas() { return List.copyOf(enviadas); }
}
