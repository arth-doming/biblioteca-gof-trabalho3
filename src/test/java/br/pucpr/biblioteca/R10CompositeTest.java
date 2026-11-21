package br.pucpr.biblioteca;

import br.pucpr.biblioteca.dominio.livro.CodigoEstadoLivro;
import br.pucpr.biblioteca.dominio.livro.Livro;
import br.pucpr.biblioteca.organizacao.GrupoAcervo;
import br.pucpr.biblioteca.organizacao.ItemAcervo;
import br.pucpr.biblioteca.organizacao.OrganizacaoDoAcervo;
import br.pucpr.biblioteca.organizacao.OrganizacaoInvalidaException;
import br.pucpr.biblioteca.organizacao.TipoGrupo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** R10 — Organização hierárquica do acervo (Composite). */
class R10CompositeTest extends CenarioBase {

    @Test
    void R10_unidadeComDuasSecoesTotalizaCinco() {
        GrupoAcervo unidade = new GrupoAcervo("Campus Curitiba", TipoGrupo.UNIDADE);
        GrupoAcervo exatas = new GrupoAcervo("Exatas", TipoGrupo.SECAO);
        GrupoAcervo humanas = new GrupoAcervo("Humanas", TipoGrupo.SECAO);
        unidade.adicionar(exatas);
        unidade.adicionar(humanas);
        for (int id : new int[]{1, 2, 3}) exatas.adicionar(new ItemAcervo(id));
        for (int id : new int[]{4, 5}) humanas.adicionar(new ItemAcervo(id));

        assertEquals(5, unidade.contarLivros());
        assertEquals(3, exatas.contarLivros());
        assertEquals(List.of(1, 2, 3, 4, 5), unidade.listarLivros());
    }

    @Test
    void R10_moverLivroNaoAlteraEstado() {
        emprestar(idLivro, MATRICULA);
        GrupoAcervo unidade = new GrupoAcervo("Campus", TipoGrupo.UNIDADE);
        GrupoAcervo a = new GrupoAcervo("A", TipoGrupo.SECAO);
        GrupoAcervo b = new GrupoAcervo("B", TipoGrupo.SECAO);
        GrupoAcervo b1 = new GrupoAcervo("B1", TipoGrupo.SUBSECAO);
        unidade.adicionar(a);
        unidade.adicionar(b);
        b.adicionar(b1);
        a.adicionar(new ItemAcervo(idLivro));
        Livro antes = sistema.tratadorDeAcervo().consultarLivro(idLivro).orElseThrow();

        new OrganizacaoDoAcervo(unidade).moverLivro(idLivro, b1);

        Livro depois = sistema.tratadorDeAcervo().consultarLivro(idLivro).orElseThrow();
        assertEquals(antes.getIdLivro(), depois.getIdLivro());
        assertEquals(CodigoEstadoLivro.EMPRESTADO, depois.getEstado());
        assertEquals(0, a.contarLivros());
        assertEquals(1, b.contarLivros());
        // o empréstimo continua o mesmo: ainda é possível renová-lo
        assertEquals(1, sistema.tratadorDeEmprestimo().renovarEmprestimo(idLivro, MATRICULA).numRenovacoes());
    }

    @Test
    void R10_recusaCicloELivroEmDoisGrupos() {
        GrupoAcervo unidade = new GrupoAcervo("U", TipoGrupo.UNIDADE);
        GrupoAcervo secao = new GrupoAcervo("S", TipoGrupo.SECAO);
        GrupoAcervo sub = new GrupoAcervo("SS", TipoGrupo.SUBSECAO);
        unidade.adicionar(secao);
        secao.adicionar(sub);

        assertThrows(OrganizacaoInvalidaException.class, () -> sub.adicionar(unidade), "ciclo");
        assertThrows(OrganizacaoInvalidaException.class, () -> secao.adicionar(secao), "ciclo direto");

        secao.adicionar(new ItemAcervo(7));
        assertThrows(OrganizacaoInvalidaException.class, () -> sub.adicionar(new ItemAcervo(7)), "livro duplicado");
        ItemAcervo item = new ItemAcervo(8);
        sub.adicionar(item);
        assertThrows(OrganizacaoInvalidaException.class, () -> secao.adicionar(item), "item já tem grupo");
        assertEquals(2, unidade.contarLivros());
    }
}
