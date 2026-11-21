package br.pucpr.biblioteca.cadastro;

import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;

import java.util.Optional;

/**
 * PADRÃO TEMPLATE METHOD (R7) — AbstractClass.
 * incluir() é o método-modelo: é final, então nenhuma subclasse muda a ordem
 * buscar existente -> validar -> verificar pré-requisitos -> criar -> armazenar -> apresentar.
 * Tudo roda numa unidade de trabalho: se validar ou verificarPreRequisitos falhar, nada foi
 * gravado; se armazenar falhar no meio, a unidade de trabalho desfaz o que já foi escrito.
 *
 * @param <D> dados de entrada do cadastro
 * @param <T> entidade cadastrada
 */
public abstract class RotinaDeCadastro<D, T> {
    private final UnidadeDeTrabalho uow;

    protected RotinaDeCadastro(UnidadeDeTrabalho uow) {
        this.uow = uow;
    }

    public final ResultadoDeCadastro<T> incluir(D dados) {
        return uow.executar(() -> {
            Optional<T> existente = buscarExistente(dados);
            if (existente.isPresent()) {
                return apresentar(existente.get(), false);       // regra x.1: mostra o cadastro existente
            }
            validar(dados);
            verificarPreRequisitos(dados);
            T nova = criar(dados);
            armazenar(nova);
            return apresentar(nova, true);
        });
    }

    // ----- passos variáveis (primitivas) -----
    protected abstract Optional<T> buscarExistente(D dados);

    protected abstract void validar(D dados);

    protected abstract T criar(D dados);

    protected abstract void armazenar(T entidade);

    // ----- ganchos (hooks): têm comportamento padrão e podem ser sobrescritos -----

    /** Gancho: por padrão não há pré-requisito. Estudante e Atendente exigem a Pessoa. */
    protected void verificarPreRequisitos(D dados) {
    }

    /** Gancho: por padrão descreve a entidade com toString(). */
    protected String descrever(T entidade) {
        return String.valueOf(entidade);
    }

    protected ResultadoDeCadastro<T> apresentar(T entidade, boolean criada) {
        String mensagem = (criada ? "Cadastrado: " : "Já cadastrado: ") + descrever(entidade);
        return new ResultadoDeCadastro<>(entidade, criada, mensagem);
    }

    protected static void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new DadosInvalidosException(mensagem);
    }

    protected static boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
