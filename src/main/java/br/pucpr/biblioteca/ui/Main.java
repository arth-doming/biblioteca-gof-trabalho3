package br.pucpr.biblioteca.ui;

import br.pucpr.biblioteca.comando.SolicitacaoRenovacao;
import br.pucpr.biblioteca.config.RelogioSimulado;
import br.pucpr.biblioteca.config.Sistema;
import br.pucpr.biblioteca.dados.Perfil;
import br.pucpr.biblioteca.dados.mysql.ConfiguracaoMySQL;
import br.pucpr.biblioteca.devolucao.ServicoDeEmailSimulado;
import br.pucpr.biblioteca.dominio.RegraDeNegocioException;
import br.pucpr.biblioteca.tratadores.TratadorDeAcervo;
import br.pucpr.biblioteca.tratadores.TratadorDeAdministracao;
import br.pucpr.biblioteca.tratadores.TratadorDeDevolucao;
import br.pucpr.biblioteca.tratadores.TratadorDeEmprestimo;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

/**
 * Interface de linha de comando. Só conversa com os tratadores; não há regra de domínio aqui.
 * (O "avançar dias" mexe no relógio simulado, que é infraestrutura de demonstração.)
 *
 * Uso: mvn -q compile exec:java                      (menu interativo, perfil teste)
 *      mvn -q compile exec:java -Dexec.args="--demo" (fluxo completo automático)
 *      mvn -q compile exec:java -Dperfil=producao    (família MySQL)
 */
public class Main {
    private static final PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

    private final Sistema sistema;
    private final RelogioSimulado relogio;
    private final ServicoDeEmailSimulado email;

    Main(Perfil perfil, String banco, String politica) {
        this.relogio = new RelogioSimulado(LocalDate.now());
        this.email = new ServicoDeEmailSimulado();
        this.sistema = new Sistema(Sistema.Opcoes.teste()
                .comPerfil(perfil).comRelogio(relogio).comBanco(banco).comPolitica(politica).comEmail(email));
    }

    public static void main(String[] args) {
        Perfil perfil = Perfil.de(System.getProperty("perfil", "teste"));
        String banco = System.getProperty("banco", "A");
        String politica = System.getProperty("politica", "ORIGINAL");
        Main app = new Main(perfil, banco, politica);
        out.println("Biblioteca iB2B — perfil " + perfil + ", banco " + banco + ", política " + politica
                + (perfil == Perfil.PRODUCAO ? " (" + ConfiguracaoMySQL.padrao().url() + ")" : ""));
        boolean demo = args.length > 0 && args[0].equals("--demo");
        if (demo) app.demo();
        else app.menu(new Scanner(System.in, StandardCharsets.UTF_8));
    }

    // ------------------------------------------------------------ demonstração
    void demo() {
        TratadorDeEmprestimo emp = sistema.tratadorDeEmprestimo();
        TratadorDeDevolucao dev = sistema.tratadorDeDevolucao();
        TratadorDeAcervo acervo = sistema.tratadorDeAcervo();

        titulo("Cadastros (Template Method)");
        passo(() -> sistema.tratadorDePessoa().inserirPessoa("Ana", "Souza", "111.444.777-35", "ana@pucpr.edu.br").mensagem());
        passo(() -> sistema.tratadorDePessoa().inserirPessoa("Carlos", "Lima", "222.333.444-05", "carlos@ib2b.com").mensagem());
        passo(() -> sistema.tratadorDeEstudante().inserirEstudante("11144477735", "E001").mensagem());
        passo(() -> sistema.tratadorDeEstudante().inserirEstudante("11144477735", "E001").mensagem());
        passo(() -> sistema.tratadorDeAtendente().inserirAtendente("22233344405", "AT01").mensagem());
        int id = acervo.inserirLivro("Padrões de Projeto (GoF)").entidade().getIdLivro();
        out.println("  Livro cadastrado: " + acervo.consultarLivro(id).orElseThrow());

        titulo("Reserva, empréstimo e renovações (Command + Facade + State)");
        passo(() -> emp.solicitarReserva(id, "E001"));
        passo(() -> emp.efetivarEmprestimo(id, "E001"));
        SolicitacaoRenovacao r1 = new SolicitacaoRenovacao(UUID.randomUUID().toString(), "E001", relogio.instant(), id, "E001");
        passo(() -> emp.submeter(r1));
        passo(() -> "mesma solicitação reenviada -> " + emp.submeter(r1) + " (não renovou de novo)");
        passo(() -> emp.renovarEmprestimo(id, "E001"));
        passo(() -> emp.renovarEmprestimo(id, "E001"));   // terceira: recusada

        titulo("Devolução atrasada (Observer + Strategy + Adapter)");
        avancar(14 + 30 + 30 + 5);
        passo(() -> dev.registrarDevolucao(id, "E001", "AT01"));
        out.println("  Estado do livro: " + acervo.consultarLivro(id).orElseThrow().getEstado());
        passo(() -> "Boleto: " + dev.emitirBoletoDeMulta(id, "E001").map(Object::toString).orElse("sem multa"));
        passo(() -> sistema.tratadorDeMulta().consultarMulta("E001"));
        out.println("  Indicadores: " + dev.indicadores());
        out.println("  E-mails enviados: " + email.enviadas().size());
        out.println("  Título no banco A: " + sistema.bancoA().titulosRecebidos());
        passo(() -> emp.solicitarReserva(id, "E001"));    // DEVOLVIDO: recusada até a inspeção
        passo(() -> dev.inspecionar(id, true));
        passo(() -> emp.solicitarReserva(id, "E001"));

        titulo("Consulta com cache e medição (Decorator)");
        acervo.consultarMetadados(id);
        acervo.consultarMetadados(id);
        out.println("  " + sistema.tratadorDeAdministracao().metricasDeConsulta());
    }

    // ------------------------------------------------------------ menu
    void menu(Scanner in) {
        TratadorDeEmprestimo emp = sistema.tratadorDeEmprestimo();
        TratadorDeDevolucao dev = sistema.tratadorDeDevolucao();
        TratadorDeAcervo acervo = sistema.tratadorDeAcervo();
        TratadorDeAdministracao adm = sistema.tratadorDeAdministracao();
        while (true) {
            out.println();
            out.println("Hoje (simulado): " + LocalDate.now(relogio));
            out.println(" 1 Inserir pessoa       2 Inserir estudante    3 Inserir atendente   4 Inserir livro");
            out.println(" 5 Listar livros        6 Solicitar reserva    7 Consultar reservas  8 Efetivar empréstimo");
            out.println(" 9 Renovar empréstimo  10 Registrar devolução 11 Boleto de multa    12 Inspecionar livro");
            out.println("13 Multas do estudante 14 Política de multa   15 Avançar dias       16 Indicadores");
            out.println(" 0 Sair");
            String op = ler(in, "Opção");
            try {
                switch (op) {
                    case "1" -> out.println(sistema.tratadorDePessoa().inserirPessoa(ler(in, "Nome"), ler(in, "Sobrenome"),
                            ler(in, "CPF"), ler(in, "E-mail")).mensagem());
                    case "2" -> out.println(sistema.tratadorDeEstudante().inserirEstudante(ler(in, "CPF"), ler(in, "Matrícula")).mensagem());
                    case "3" -> out.println(sistema.tratadorDeAtendente().inserirAtendente(ler(in, "CPF"), ler(in, "Id atendente")).mensagem());
                    case "4" -> out.println(acervo.inserirLivro(ler(in, "Título")).mensagem());
                    case "5" -> acervo.listarLivros().forEach(l -> out.println("  " + l));
                    case "6" -> out.println(emp.solicitarReserva(lerInt(in, "Id livro"), ler(in, "Matrícula")));
                    case "7" -> emp.consultarReservas(ler(in, "Matrícula")).forEach(r -> out.println("  " + r));
                    case "8" -> out.println(emp.efetivarEmprestimo(lerInt(in, "Id livro"), ler(in, "Matrícula")));
                    case "9" -> out.println(emp.renovarEmprestimo(lerInt(in, "Id livro"), ler(in, "Matrícula")));
                    case "10" -> out.println(dev.registrarDevolucao(lerInt(in, "Id livro"), ler(in, "Matrícula"), ler(in, "Id atendente")));
                    case "11" -> out.println(dev.emitirBoletoDeMulta(lerInt(in, "Id livro"), ler(in, "Matrícula"))
                            .map(Object::toString).orElse("Sem multa para essa devolução"));
                    case "12" -> out.println(dev.inspecionar(lerInt(in, "Id livro"), ler(in, "Bom estado? (s/n)").startsWith("s")));
                    case "13" -> sistema.tratadorDeMulta().consultarMulta(ler(in, "Matrícula")).forEach(m -> out.println("  " + m));
                    case "14" -> {
                        out.println("Vigente: " + adm.politicaDeMultaVigente() + " | disponíveis: " + adm.politicasDeMulta());
                        adm.definirPoliticaDeMulta(ler(in, "Nova política"));
                    }
                    case "15" -> avancar(lerInt(in, "Dias"));
                    case "16" -> out.println(dev.indicadores() + " | consulta: " + adm.metricasDeConsulta());
                    case "0" -> { return; }
                    default -> out.println("Opção inválida");
                }
            } catch (RegraDeNegocioException e) {
                out.println("Recusado: " + e.getMessage());
            } catch (NumberFormatException e) {
                out.println("Número inválido");
            }
        }
    }

    // ------------------------------------------------------------ utilitários
    private void avancar(int dias) {
        relogio.avancarDias(dias);
        out.println("  (relógio avançado " + dias + " dias -> " + LocalDate.now(relogio) + ")");
    }

    private static void titulo(String t) {
        out.println();
        out.println("== " + t);
    }

    private static void passo(java.util.function.Supplier<Object> acao) {
        try {
            out.println("  " + acao.get());
        } catch (RegraDeNegocioException e) {
            out.println("  Recusado: " + e.getMessage());
        }
    }

    private static String ler(Scanner in, String rotulo) {
        out.print(rotulo + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : "0";
    }

    private static int lerInt(Scanner in, String rotulo) {
        return Integer.parseInt(ler(in, rotulo));
    }
}
