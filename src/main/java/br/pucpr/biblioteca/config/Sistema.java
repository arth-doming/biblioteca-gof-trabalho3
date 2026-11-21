package br.pucpr.biblioteca.config;

import br.pucpr.biblioteca.acesso.Acervo;
import br.pucpr.biblioteca.acesso.Movimentacao;
import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.cadastro.CadastroDeAtendente;
import br.pucpr.biblioteca.cadastro.CadastroDeEstudante;
import br.pucpr.biblioteca.cadastro.CadastroDeLivro;
import br.pucpr.biblioteca.cadastro.CadastroDePessoa;
import br.pucpr.biblioteca.circulacao.FachadaDeCirculacao;
import br.pucpr.biblioteca.comando.ExecutorDeSolicitacoes;
import br.pucpr.biblioteca.consulta.CacheEmMemoria;
import br.pucpr.biblioteca.consulta.ConsultaAcervo;
import br.pucpr.biblioteca.consulta.Medidor;
import br.pucpr.biblioteca.consulta.MontadorDeConsulta;
import br.pucpr.biblioteca.dados.FabricaDeAcessoADados;
import br.pucpr.biblioteca.dados.Perfil;
import br.pucpr.biblioteca.dados.SelecaoDeFabrica;
import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;
import br.pucpr.biblioteca.dados.mysql.ConfiguracaoMySQL;
import br.pucpr.biblioteca.devolucao.FilaDeInspecao;
import br.pucpr.biblioteca.devolucao.IndicadoresDeCirculacao;
import br.pucpr.biblioteca.devolucao.OuvinteDeEmail;
import br.pucpr.biblioteca.devolucao.OuvinteDeIndicadores;
import br.pucpr.biblioteca.devolucao.OuvinteDeInspecao;
import br.pucpr.biblioteca.devolucao.OuvinteDeMulta;
import br.pucpr.biblioteca.devolucao.PublicadorDeDevolucao;
import br.pucpr.biblioteca.devolucao.ServicoDeEmail;
import br.pucpr.biblioteca.devolucao.ServicoDeEmailSimulado;
import br.pucpr.biblioteca.financeiro.TratadorDeMulta;
import br.pucpr.biblioteca.financeiro.cobranca.AdaptadorBancoA;
import br.pucpr.biblioteca.financeiro.cobranca.AdaptadorBancoB;
import br.pucpr.biblioteca.financeiro.cobranca.BancoA;
import br.pucpr.biblioteca.financeiro.cobranca.BancoB;
import br.pucpr.biblioteca.financeiro.cobranca.ContratoDeCobranca;
import br.pucpr.biblioteca.financeiro.multa.ConfiguracaoAdministrativa;
import br.pucpr.biblioteca.tratadores.TratadorDeAcervo;
import br.pucpr.biblioteca.tratadores.TratadorDeAdministracao;
import br.pucpr.biblioteca.tratadores.TratadorDeAtendente;
import br.pucpr.biblioteca.tratadores.TratadorDeDevolucao;
import br.pucpr.biblioteca.tratadores.TratadorDeEmprestimo;
import br.pucpr.biblioteca.tratadores.TratadorDeEstudante;
import br.pucpr.biblioteca.tratadores.TratadorDePessoa;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;

/**
 * Raiz de composição: o único lugar que conhece as classes concretas e liga tudo.
 * Aqui (e só aqui) se escolhe: a família de dados pelo perfil (R8), o banco da instalação (R3),
 * a política de multa inicial (R2), os decoradores da consulta (R9) e os ouvintes da devolução (R5).
 */
public final class Sistema {

    /** Opções da instalação. */
    public record Opcoes(Perfil perfil, Clock relogio, String banco, String politicaDeMulta,
                         boolean cache, boolean medicao, ServicoDeEmail email, ConfiguracaoMySQL mysql) {

        public static Opcoes teste() {
            return new Opcoes(Perfil.TESTE, new RelogioSimulado(LocalDate.of(2026, 11, 23)), "A",
                    "ORIGINAL", true, true, new ServicoDeEmailSimulado(), ConfiguracaoMySQL.padrao());
        }

        public Opcoes comPerfil(Perfil p) { return new Opcoes(p, relogio, banco, politicaDeMulta, cache, medicao, email, mysql); }
        public Opcoes comRelogio(Clock c) { return new Opcoes(perfil, c, banco, politicaDeMulta, cache, medicao, email, mysql); }
        public Opcoes comBanco(String b) { return new Opcoes(perfil, relogio, b, politicaDeMulta, cache, medicao, email, mysql); }
        public Opcoes comPolitica(String p) { return new Opcoes(perfil, relogio, banco, p, cache, medicao, email, mysql); }
        public Opcoes comEmail(ServicoDeEmail e) { return new Opcoes(perfil, relogio, banco, politicaDeMulta, cache, medicao, e, mysql); }
        public Opcoes comDecoradores(boolean c, boolean m) { return new Opcoes(perfil, relogio, banco, politicaDeMulta, c, m, email, mysql); }
    }

    private final Opcoes opcoes;
    private final FabricaDeAcessoADados fabrica;
    private final BancoA bancoA = new BancoA();
    private final BancoB bancoB = new BancoB();
    private final ConfiguracaoAdministrativa configuracao = new ConfiguracaoAdministrativa();
    private final PublicadorDeDevolucao publicador = new PublicadorDeDevolucao();
    private final FilaDeInspecao filaDeInspecao = new FilaDeInspecao();
    private final IndicadoresDeCirculacao indicadores = new IndicadoresDeCirculacao();
    private final Medidor medidor;

    private final TratadorDeAcervo tratadorDeAcervo;
    private final TratadorDePessoa tratadorDePessoa;
    private final TratadorDeEstudante tratadorDeEstudante;
    private final TratadorDeAtendente tratadorDeAtendente;
    private final TratadorDeEmprestimo tratadorDeEmprestimo;
    private final TratadorDeDevolucao tratadorDeDevolucao;
    private final TratadorDeMulta tratadorDeMulta;
    private final TratadorDeAdministracao tratadorDeAdministracao;

    public Sistema(Opcoes opcoes) {
        this.opcoes = opcoes;
        Clock relogio = opcoes.relogio();

        // R8 — a família é escolhida UMA vez, aqui.
        this.fabrica = SelecaoDeFabrica.para(opcoes.perfil(), opcoes.mysql());
        UnidadeDeTrabalho uow = fabrica.unidadeDeTrabalho();
        Acervo acervo = new Acervo(fabrica.criarRepositorioDeLivros());
        Relacionamento relacionamento = new Relacionamento(fabrica.criarRepositorioDeRelacionamento());
        Movimentacao movimentacao = new Movimentacao(fabrica.criarRepositorioDeMovimentacao());

        // R2 + R3 — política vigente e banco da instalação.
        configuracao.definirPoliticaVigente(opcoes.politicaDeMulta());
        ContratoDeCobranca cobranca = "B".equalsIgnoreCase(opcoes.banco())
                ? new AdaptadorBancoB(bancoB) : new AdaptadorBancoA(bancoA);
        this.tratadorDeMulta = new TratadorDeMulta(configuracao, cobranca, relacionamento,
                fabrica.criarRepositorioDeMultas(), uow, relogio);

        // R5 — ouvintes da devolução.
        publicador.registrar(new OuvinteDeInspecao(filaDeInspecao));
        publicador.registrar(new OuvinteDeMulta(tratadorDeMulta));
        publicador.registrar(new OuvinteDeIndicadores(indicadores));
        publicador.registrar(new OuvinteDeEmail(opcoes.email()));

        // R4 + R6
        FachadaDeCirculacao fachada = new FachadaDeCirculacao(acervo, relacionamento, movimentacao, uow, publicador, relogio);
        ExecutorDeSolicitacoes executor = new ExecutorDeSolicitacoes(fachada);

        // R9 — cadeia de decoradores da consulta.
        CacheEmMemoria cache = opcoes.cache() ? new CacheEmMemoria(relogio, Duration.ofMinutes(5)) : null;
        this.medidor = opcoes.medicao() ? new Medidor() : null;
        ConsultaAcervo consulta = MontadorDeConsulta.montar(acervo, cache, medidor);

        // R7 — rotinas de cadastro.
        this.tratadorDeAcervo = new TratadorDeAcervo(new CadastroDeLivro(uow, acervo), acervo, uow, consulta,
                id -> { if (cache != null) cache.invalidar(id); });
        this.tratadorDePessoa = new TratadorDePessoa(new CadastroDePessoa(uow, relacionamento), relacionamento, uow);
        this.tratadorDeEstudante = new TratadorDeEstudante(new CadastroDeEstudante(uow, relacionamento), relacionamento, uow);
        this.tratadorDeAtendente = new TratadorDeAtendente(new CadastroDeAtendente(uow, relacionamento), relacionamento, uow);
        this.tratadorDeEmprestimo = new TratadorDeEmprestimo(fachada, executor, relogio);
        this.tratadorDeDevolucao = new TratadorDeDevolucao(fachada, tratadorDeMulta, filaDeInspecao, indicadores);
        this.tratadorDeAdministracao = new TratadorDeAdministracao(configuracao, medidor);
    }

    public TratadorDeAcervo tratadorDeAcervo() { return tratadorDeAcervo; }
    public TratadorDePessoa tratadorDePessoa() { return tratadorDePessoa; }
    public TratadorDeEstudante tratadorDeEstudante() { return tratadorDeEstudante; }
    public TratadorDeAtendente tratadorDeAtendente() { return tratadorDeAtendente; }
    public TratadorDeEmprestimo tratadorDeEmprestimo() { return tratadorDeEmprestimo; }
    public TratadorDeDevolucao tratadorDeDevolucao() { return tratadorDeDevolucao; }
    public TratadorDeMulta tratadorDeMulta() { return tratadorDeMulta; }
    public TratadorDeAdministracao tratadorDeAdministracao() { return tratadorDeAdministracao; }

    // ----- infraestrutura exposta para testes e para a simulação na demonstração -----
    public Opcoes opcoes() { return opcoes; }
    public FabricaDeAcessoADados fabrica() { return fabrica; }
    public PublicadorDeDevolucao publicador() { return publicador; }
    public ConfiguracaoAdministrativa configuracao() { return configuracao; }
    public BancoA bancoA() { return bancoA; }
    public BancoB bancoB() { return bancoB; }
    public Medidor medidor() { return medidor; }
}
