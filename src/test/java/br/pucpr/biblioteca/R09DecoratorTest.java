package br.pucpr.biblioteca;

import br.pucpr.biblioteca.acesso.Acervo;
import br.pucpr.biblioteca.consulta.ArmazenamentoDeCache;
import br.pucpr.biblioteca.consulta.ConsultaAcervo;
import br.pucpr.biblioteca.consulta.ConsultaComCache;
import br.pucpr.biblioteca.consulta.Medidor;
import br.pucpr.biblioteca.consulta.MetadadosLivro;
import br.pucpr.biblioteca.consulta.MontadorDeConsulta;
import br.pucpr.biblioteca.dados.memoria.FabricaEmMemoria;
import br.pucpr.biblioteca.dominio.livro.CodigoEstadoLivro;
import br.pucpr.biblioteca.dominio.livro.Livro;
import br.pucpr.biblioteca.dominio.livro.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** R9 — Serviço de consulta com recursos opcionais (Decorator). */
class R09DecoratorTest extends CenarioBase {

    @Test
    void R09_segundaConsultaUsaCacheEMede() {
        Acervo acervo = new Acervo(new FabricaEmMemoria().criarRepositorioDeLivros()) {
            int leituras;
            @Override public Optional<Livro> consultarLivro(int id) { leituras++; return super.consultarLivro(id); }
            @Override public String toString() { return String.valueOf(leituras); }
        };
        acervo.adicionarLivro(new Livro(1, "Refactoring"));
        Medidor medidor = new Medidor();
        ConsultaAcervo consulta = MontadorDeConsulta.montar(acervo, new br.pucpr.biblioteca.consulta.CacheEmMemoria(
                relogio, java.time.Duration.ofMinutes(5)), medidor);

        assertEquals("Refactoring", consulta.consultarMetadados(1).orElseThrow().titulo());
        assertEquals("Refactoring", consulta.consultarMetadados(1).orElseThrow().titulo());

        assertEquals("1", acervo.toString(), "a segunda consulta veio do cache");
        assertEquals(2, medidor.chamadas("consultarMetadados"), "e mesmo assim foi medida");
    }

    @Test
    void R09_cacheNaoAutorizaReserva() {
        // metadados vão para o cache enquanto o livro está LIVRE
        assertTrue(sistema.tratadorDeAcervo().consultarMetadados(idLivro).isPresent());
        emprestar(idLivro, MATRICULA);                                  // deixa de estar LIVRE
        assertTrue(sistema.tratadorDeAcervo().consultarMetadados(idLivro).isPresent()); // cache ainda responde

        assertThrows(TransicaoInvalidaException.class,
                () -> sistema.tratadorDeEmprestimo().solicitarReserva(idLivro, OUTRA_MATRICULA));
        assertEquals(CodigoEstadoLivro.EMPRESTADO,
                sistema.tratadorDeAcervo().consultarLivro(idLivro).orElseThrow().getEstado());
        assertEquals(2, MetadadosLivro.class.getRecordComponents().length, "cache só guarda id e título");
    }

    @Test
    void R09_falhaDoCacheConsultaOrigem() {
        Acervo acervo = new Acervo(new FabricaEmMemoria().criarRepositorioDeLivros());
        acervo.adicionarLivro(new Livro(1, "DDD"));
        ArmazenamentoDeCache quebrado = new ArmazenamentoDeCache() {
            public Optional<MetadadosLivro> ler(int id) { throw new IllegalStateException("cache fora"); }
            public void gravar(MetadadosLivro m) { throw new IllegalStateException("cache fora"); }
            public void invalidar(int id) { }
        };
        ConsultaComCache consulta = (ConsultaComCache) MontadorDeConsulta.montar(acervo, quebrado, null);
        assertEquals("DDD", consulta.consultarMetadados(1).orElseThrow().titulo());
    }
}
