package br.pucpr.biblioteca;

import br.pucpr.biblioteca.config.RelogioSimulado;
import br.pucpr.biblioteca.config.Sistema;
import br.pucpr.biblioteca.dados.FabricaDeAcessoADados;
import br.pucpr.biblioteca.dados.Perfil;
import br.pucpr.biblioteca.dados.SelecaoDeFabrica;
import br.pucpr.biblioteca.dados.memoria.FabricaEmMemoria;
import br.pucpr.biblioteca.dados.memoria.RepositorioDeLivrosEmMemoria;
import br.pucpr.biblioteca.dados.memoria.RepositorioDeMovimentacaoEmMemoria;
import br.pucpr.biblioteca.dados.memoria.RepositorioDeMultasEmMemoria;
import br.pucpr.biblioteca.dados.memoria.RepositorioDeRelacionamentoEmMemoria;
import br.pucpr.biblioteca.dados.memoria.UnidadeDeTrabalhoEmMemoria;
import br.pucpr.biblioteca.dados.mysql.ConfiguracaoMySQL;
import br.pucpr.biblioteca.dados.mysql.FabricaMySQL;
import br.pucpr.biblioteca.dados.mysql.RepositorioDeLivrosMySQL;
import br.pucpr.biblioteca.dados.mysql.RepositorioDeMovimentacaoMySQL;
import br.pucpr.biblioteca.dados.mysql.RepositorioDeMultasMySQL;
import br.pucpr.biblioteca.dados.mysql.RepositorioDeRelacionamentoMySQL;
import br.pucpr.biblioteca.dados.mysql.UnidadeDeTrabalhoMySQL;
import br.pucpr.biblioteca.dominio.livro.CodigoEstadoLivro;
import br.pucpr.biblioteca.dominio.livro.Livro;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** R8 — Famílias compatíveis de acesso aos dados (Abstract Factory). */
class R08AbstractFactoryTest {

    private static final ConfiguracaoMySQL NAO_CONECTA =
            new ConfiguracaoMySQL("jdbc:mysql://host-inexistente:3306/x", "u", "s");

    @Test
    void R08_perfilTesteSomenteMemoria() {
        FabricaDeAcessoADados f = SelecaoDeFabrica.para(Perfil.TESTE, NAO_CONECTA);
        assertInstanceOf(FabricaEmMemoria.class, f);
        assertInstanceOf(UnidadeDeTrabalhoEmMemoria.class, f.unidadeDeTrabalho());
        assertInstanceOf(RepositorioDeLivrosEmMemoria.class, f.criarRepositorioDeLivros());
        assertInstanceOf(RepositorioDeMovimentacaoEmMemoria.class, f.criarRepositorioDeMovimentacao());
        assertInstanceOf(RepositorioDeRelacionamentoEmMemoria.class, f.criarRepositorioDeRelacionamento());
        assertInstanceOf(RepositorioDeMultasEmMemoria.class, f.criarRepositorioDeMultas());

        Sistema s = new Sistema(Sistema.Opcoes.teste().comRelogio(new RelogioSimulado(LocalDate.of(2026, 11, 23))));
        assertEquals(FabricaEmMemoria.FAMILIA, s.fabrica().familia());
    }

    @Test
    void R08_perfilProducaoMesmaUnidadeDeTrabalho() {
        FabricaDeAcessoADados f = SelecaoDeFabrica.para(Perfil.PRODUCAO, NAO_CONECTA);
        assertInstanceOf(FabricaMySQL.class, f);
        var uow = assertInstanceOf(UnidadeDeTrabalhoMySQL.class, f.unidadeDeTrabalho());
        var livros = assertInstanceOf(RepositorioDeLivrosMySQL.class, f.criarRepositorioDeLivros());
        var mov = assertInstanceOf(RepositorioDeMovimentacaoMySQL.class, f.criarRepositorioDeMovimentacao());
        var rel = assertInstanceOf(RepositorioDeRelacionamentoMySQL.class, f.criarRepositorioDeRelacionamento());
        assertSame(uow, livros.unidadeDeTrabalho());
        assertSame(uow, mov.unidadeDeTrabalho());
        assertSame(uow, rel.unidadeDeTrabalho());
        var multas = assertInstanceOf(RepositorioDeMultasMySQL.class, f.criarRepositorioDeMultas());
        assertSame(uow, multas.unidadeDeTrabalho());
        assertFalse(uow.emAndamento(), "nenhuma conexão é aberta só por criar a família");
    }

    @Test
    void R08_unidadeDeTrabalhoEmMemoriaDesfazTudoEmFalha() {
        FabricaEmMemoria f = new FabricaEmMemoria();
        var livros = f.criarRepositorioDeLivros();
        livros.salvar(new Livro(1, "A"));
        assertThrows(IllegalStateException.class, () -> f.unidadeDeTrabalho().executar(() -> {
            livros.salvar(Livro.reconstituir(1, "A", CodigoEstadoLivro.RESERVADO));
            livros.salvar(new Livro(2, "B"));
            throw new IllegalStateException("falha");
        }));
        assertEquals(CodigoEstadoLivro.LIVRE, livros.buscar(1).orElseThrow().getEstado());
        assertTrue(livros.buscar(2).isEmpty());
    }

    /** Integração real com MySQL: rode com -Dmysql.integracao=true (ver README). */
    @Test
    void R08_integracaoMySQLReconstituiEstado() {
        assumeTrue(System.getProperty("mysql.integracao") != null, "MySQL não habilitado (-Dmysql.integracao=true)");
        FabricaDeAcessoADados f = SelecaoDeFabrica.para(Perfil.PRODUCAO, ConfiguracaoMySQL.paraTestes());
        var livros = f.criarRepositorioDeLivros();
        int id = 900_000 + (int) (System.nanoTime() % 1000);
        f.unidadeDeTrabalho().executar(() -> livros.salvar(Livro.reconstituir(id, "Integração", CodigoEstadoLivro.RENOVADO)));
        assertEquals(CodigoEstadoLivro.RENOVADO, livros.buscar(id).orElseThrow().getEstado());
        livros.remover(id);
    }
}
