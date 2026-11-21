package br.pucpr.biblioteca.dominio.livro;

import br.pucpr.biblioteca.dominio.Emprestimo;

/**
 * PADRÃO STATE (R1) — papel: State.
 * Cada evento da máquina "T02-1 Estados do Livro" é um método. A implementação padrão
 * recusa o evento; cada estado concreto sobrescreve somente as transições que saem dele.
 * Os estados não guardam dados (o contexto é o Livro), por isso cada um é um singleton.
 */
public abstract class EstadoLivro {

    public abstract CodigoEstadoLivro codigo();

    /** T2: LIVRE --solicitarReserva [estudante cadastrado]--> RESERVADO */
    public void solicitarReserva(Livro livro, boolean estudanteCadastrado) {
        throw recusar("solicitarReserva");
    }

    /** T3: RESERVADO --efetivarEmprestimo [reserva do mesmo estudante]--> EMPRESTADO */
    public void efetivarEmprestimo(Livro livro, boolean reservaDoMesmoEstudante) {
        throw recusar("efetivarEmprestimo");
    }

    /** T4/T5: renovarEmprestimo [numRenovacoes = 0] / [numRenovacoes < 2] */
    public void renovarEmprestimo(Livro livro, Emprestimo emprestimo) {
        throw recusar("renovarEmprestimo");
    }

    /** T6: EMPRESTADO|RENOVADO --registrarDevolucao--> DEVOLVIDO */
    public void registrarDevolucao(Livro livro) {
        throw recusar("registrarDevolucao");
    }

    /** T7/T8: DEVOLVIDO --inspecionar [bom estado]--> LIVRE | [danificado] --> REPARANDO */
    public void inspecionar(Livro livro, boolean bomEstado) {
        throw recusar("inspecionar");
    }

    /** T9: LIVRE --enviarParaReparo [dano identificado no acervo]--> REPARANDO */
    public void enviarParaReparo(Livro livro) {
        throw recusar("enviarParaReparo");
    }

    /** T10: REPARANDO --retornarDoReparo--> LIVRE */
    public void retornarDoReparo(Livro livro) {
        throw recusar("retornarDoReparo");
    }

    /** Laudo: REPARANDO --avaliarReparo [livro irreparável]--> INCINERADO */
    public void avaliarReparo(Livro livro, boolean reparavel) {
        throw recusar("avaliarReparo");
    }

    /** T11: LIVRE|INCINERADO --excluirLivro--> estado final */
    public void excluirLivro(Livro livro) {
        throw recusar("excluirLivro");
    }

    /** Usado por consultas; a decisão de reserva é sempre do método solicitarReserva. */
    public boolean aceitaReserva() {
        return false;
    }

    protected TransicaoInvalidaException recusar(String evento) {
        return new TransicaoInvalidaException(
                "Evento " + evento + " não é aceito no estado " + codigo());
    }

    /** Reconstitui o objeto de estado a partir do código gravado na persistência. */
    public static EstadoLivro de(CodigoEstadoLivro codigo) {
        return switch (codigo) {
            case LIVRE -> Livre.INSTANCIA;
            case RESERVADO -> Reservado.INSTANCIA;
            case EMPRESTADO -> Emprestado.INSTANCIA;
            case RENOVADO -> Renovado.INSTANCIA;
            case DEVOLVIDO -> Devolvido.INSTANCIA;
            case REPARANDO -> Reparando.INSTANCIA;
            case INCINERADO -> Incinerado.INSTANCIA;
        };
    }

    @Override
    public String toString() {
        return codigo().name();
    }
}
