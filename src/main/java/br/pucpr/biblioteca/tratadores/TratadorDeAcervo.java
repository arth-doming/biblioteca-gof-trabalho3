package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.acesso.Acervo;
import br.pucpr.biblioteca.cadastro.CadastroDeLivro;
import br.pucpr.biblioteca.cadastro.ResultadoDeCadastro;
import br.pucpr.biblioteca.consulta.ConsultaAcervo;
import br.pucpr.biblioteca.consulta.MetadadosLivro;
import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;
import br.pucpr.biblioteca.dominio.livro.Livro;

import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;

/** Manter Livro (Administrador). Delegação para Acervo; consultas de metadados via ConsultaAcervo decorada. */
public class TratadorDeAcervo implements iLivro {
    private final CadastroDeLivro cadastro;
    private final Acervo acervo;
    private final UnidadeDeTrabalho uow;
    private final ConsultaAcervo consulta;
    private final IntConsumer aoAlterarLivro;

    public TratadorDeAcervo(CadastroDeLivro cadastro, Acervo acervo, UnidadeDeTrabalho uow,
                            ConsultaAcervo consulta, IntConsumer aoAlterarLivro) {
        this.cadastro = cadastro;
        this.acervo = acervo;
        this.uow = uow;
        this.consulta = consulta;
        this.aoAlterarLivro = aoAlterarLivro;
    }

    @Override
    public ResultadoDeCadastro<Livro> inserirLivro(String titulo) {
        return cadastro.incluir(new CadastroDeLivro.Dados(titulo));
    }

    /** Livro com o estado atual (lido do Acervo, nunca do cache). */
    @Override
    public Optional<Livro> consultarLivro(int idLivro) {
        return acervo.consultarLivro(idLivro);
    }

    @Override
    public boolean atualizarLivro(int idLivro, String titulo) {
        boolean ok = uow.executar(() -> acervo.consultarLivro(idLivro).map(l -> {
            l.atualizarTitulo(titulo);
            acervo.atualizarLivro(l);
            return true;
        }).orElse(false));
        if (ok) aoAlterarLivro.accept(idLivro);
        return ok;
    }

    /** T11: só LIVRE ou INCINERADO aceitam excluirLivro (o estado decide). */
    @Override
    public boolean excluirLivro(int idLivro) {
        boolean ok = uow.executar(() -> acervo.consultarLivro(idLivro).map(l -> {
            l.excluirLivro();
            acervo.removerLivro(idLivro);
            return true;
        }).orElse(false));
        if (ok) aoAlterarLivro.accept(idLivro);
        return ok;
    }

    public List<Livro> listarLivros() {
        return acervo.listarLivros();
    }

    public Optional<MetadadosLivro> consultarMetadados(int idLivro) {
        return consulta.consultarMetadados(idLivro);
    }

    public List<MetadadosLivro> buscarPorTitulo(String trecho) {
        return consulta.buscarPorTitulo(trecho);
    }
}
