package br.pucpr.biblioteca.dominio.livro;

import br.pucpr.biblioteca.dominio.Emprestimo;

import java.util.Objects;

/**
 * PADRÃO STATE (R1) — papel: Context.
 * Livro não testa a própria situação com condicionais: cada evento é delegado
 * ao objeto de estado atual, que decide a transição ou recusa o evento.
 */
public class Livro {
    private final int idLivro;
    private String titulo;
    private EstadoLivro estado;
    private boolean excluido;
    private boolean incineracaoRegistrada;

    /** T1: / inserirLivro -> LIVRE */
    public Livro(int idLivro, String titulo) {
        this(idLivro, titulo, CodigoEstadoLivro.LIVRE);
    }

    private Livro(int idLivro, String titulo, CodigoEstadoLivro codigo) {
        this.idLivro = idLivro;
        this.titulo = Objects.requireNonNull(titulo, "titulo");
        this.estado = EstadoLivro.de(codigo);
        this.incineracaoRegistrada = codigo == CodigoEstadoLivro.INCINERADO;
    }

    /** Usado pelos repositórios: o estado é gravado como código e reconstituído aqui. */
    public static Livro reconstituir(int idLivro, String titulo, CodigoEstadoLivro codigo) {
        return new Livro(idLivro, titulo, codigo);
    }

    // ----- eventos (delegação ao estado) -----
    public void solicitarReserva(boolean estudanteCadastrado) { estado.solicitarReserva(this, estudanteCadastrado); }
    public void efetivarEmprestimo(boolean reservaDoMesmoEstudante) { estado.efetivarEmprestimo(this, reservaDoMesmoEstudante); }
    public void renovarEmprestimo(Emprestimo emprestimo) { estado.renovarEmprestimo(this, emprestimo); }
    public void registrarDevolucao() { estado.registrarDevolucao(this); }
    public void inspecionar(boolean bomEstado) { estado.inspecionar(this, bomEstado); }
    public void enviarParaReparo() { estado.enviarParaReparo(this); }
    public void retornarDoReparo() { estado.retornarDoReparo(this); }
    public void avaliarReparo(boolean reparavel) { estado.avaliarReparo(this, reparavel); }
    public void excluirLivro() { estado.excluirLivro(this); }

    // ----- chamados apenas pelos estados (pacote) -----
    void mudarPara(EstadoLivro novo) { this.estado = novo; }
    void marcarExcluido() { this.excluido = true; }
    void registrarIncineracao() { this.incineracaoRegistrada = true; }

    // ----- consultas -----
    public int getIdLivro() { return idLivro; }
    public String getTitulo() { return titulo; }
    public CodigoEstadoLivro getEstado() { return estado.codigo(); }
    public boolean isExcluido() { return excluido; }
    public boolean isIncineracaoRegistrada() { return incineracaoRegistrada; }
    public boolean estaLivre() { return estado.aceitaReserva(); }

    public void atualizarTitulo(String titulo) {
        this.titulo = Objects.requireNonNull(titulo, "titulo");
    }

    @Override
    public String toString() {
        return "Livro " + idLivro + " \"" + titulo + "\" [" + getEstado() + "]";
    }
}
