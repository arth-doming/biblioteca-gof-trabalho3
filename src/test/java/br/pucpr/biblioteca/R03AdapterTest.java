package br.pucpr.biblioteca;

import br.pucpr.biblioteca.config.RelogioSimulado;
import br.pucpr.biblioteca.config.Sistema;
import br.pucpr.biblioteca.financeiro.multa.Boleto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** R3 — Integração com bancos incompatíveis (Adapter). */
class R03AdapterTest extends CenarioBase {

    private Sistema instalacaoComBanco(String banco) {
        Sistema s = new Sistema(Sistema.Opcoes.teste().comRelogio(new RelogioSimulado(HOJE)).comBanco(banco));
        s.tratadorDePessoa().inserirPessoa("Ana", "Souza", "11144477735", "ana@pucpr.edu.br");
        s.tratadorDeEstudante().inserirEstudante("11144477735", MATRICULA);
        return s;
    }

    @Test
    void R03_seteReaisConvertidosPorBanco() {
        Sistema a = instalacaoComBanco("A");
        Sistema b = instalacaoComBanco("B");

        // mesma solicitação (7 dias de atraso, política original = R$ 7,00) nas duas instalações
        Boleto boletoA = a.tratadorDeMulta().emitirMulta(1, MATRICULA, HOJE, HOJE.plusDays(7)).orElseThrow();
        Boleto boletoB = b.tratadorDeMulta().emitirMulta(1, MATRICULA, HOJE, HOJE.plusDays(7)).orElseThrow();

        assertEquals(700L, a.bancoA().titulosRecebidos().get(0).valorEmCentavos());
        assertEquals("11144477735", a.bancoA().titulosRecebidos().get(0).documentoPagador());
        assertEquals("7,00", b.bancoB().pedidosRecebidos().get(0).valor());
        assertEquals("111.444.777-35", b.bancoB().pedidosRecebidos().get(0).cpfFormatado());

        // para o serviço de multas os dois boletos falam a mesma língua
        assertEquals(new BigDecimal("7.00"), boletoA.valor());
        assertEquals(new BigDecimal("7.00"), boletoB.valor());
        assertEquals("A", boletoA.banco());
        assertEquals("B", boletoB.banco());
        assertTrue(a.bancoB().pedidosRecebidos().isEmpty());
        assertTrue(b.bancoA().titulosRecebidos().isEmpty());
    }

    @Test
    void R03_notificacaoRepetidaNaoQuitaDuasVezesBancoA() {
        Sistema a = instalacaoComBanco("A");
        Boleto boleto = a.tratadorDeMulta().emitirMulta(1, MATRICULA, HOJE, HOJE.plusDays(7)).orElseThrow();
        a.bancoA().simularLiquidacao(boleto.identificadorNoBanco(), 700, 20261201);
        a.bancoA().simularLiquidacao(boleto.identificadorNoBanco(), 700, 20261202);

        var multa = a.tratadorDeMulta().buscar(boleto.numeroMulta()).orElseThrow();
        assertTrue(multa.isQuitada());
        assertEquals(java.time.LocalDate.of(2026, 12, 1), multa.getDataPagamento()); // primeira notificação vale
    }

    @Test
    void R03_notificacaoRepetidaNaoQuitaDuasVezesBancoB() {
        Sistema b = instalacaoComBanco("B");
        Boleto boleto = b.tratadorDeMulta().emitirMulta(1, MATRICULA, HOJE, HOJE.plusDays(7)).orElseThrow();
        var traducao = new br.pucpr.biblioteca.financeiro.cobranca.NotificacaoDePagamento(boleto.numeroMulta(),
                new BigDecimal("7.00"), br.pucpr.biblioteca.financeiro.cobranca.SituacaoDoPagamento.PAGO, HOJE);
        b.bancoB().simularPagamento(boleto.identificadorNoBanco(), "7,00", "2026-12-01");
        assertFalse(b.tratadorDeMulta().registrarQuitacao(traducao), "segunda quitação deve ser ignorada");
        assertTrue(b.tratadorDeMulta().buscar(boleto.numeroMulta()).orElseThrow().isQuitada());
    }

    @Test
    void R03_multaGravadaNaFamiliaDeDadosSobreviveAoServico() {
        // D8: a multa fica no RepositorioDeMultas da fábrica, não na memória do TratadorDeMulta
        var fabrica = sistema.fabrica();
        var rel = new br.pucpr.biblioteca.acesso.Relacionamento(fabrica.criarRepositorioDeRelacionamento());
        Boleto boleto = sistema.tratadorDeMulta().emitirMulta(idLivro, MATRICULA, HOJE, HOJE.plusDays(4)).orElseThrow();

        var outroServico = new br.pucpr.biblioteca.financeiro.TratadorDeMulta(sistema.configuracao(),
                new br.pucpr.biblioteca.financeiro.cobranca.AdaptadorBancoA(new br.pucpr.biblioteca.financeiro.cobranca.BancoA()),
                rel, fabrica.criarRepositorioDeMultas(), fabrica.unidadeDeTrabalho(), relogio);
        var multa = outroServico.buscar(boleto.numeroMulta()).orElseThrow();
        assertEquals(new BigDecimal("4.00"), multa.getValor());
        assertEquals(boleto, outroServico.consultarBoleto(idLivro, MATRICULA).orElseThrow());
    }
}
