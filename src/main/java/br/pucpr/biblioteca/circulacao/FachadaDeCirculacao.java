package br.pucpr.biblioteca.circulacao;

import br.pucpr.biblioteca.acesso.Acervo;
import br.pucpr.biblioteca.acesso.Movimentacao;
import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;
import br.pucpr.biblioteca.devolucao.EventoDevolucao;
import br.pucpr.biblioteca.devolucao.PublicadorDeDevolucao;
import br.pucpr.biblioteca.devolucao.ResultadoDaPublicacao;
import br.pucpr.biblioteca.dominio.Atendente;
import br.pucpr.biblioteca.dominio.Devolucao;
import br.pucpr.biblioteca.dominio.Emprestimo;
import br.pucpr.biblioteca.dominio.Estudante;
import br.pucpr.biblioteca.dominio.RegraDeNegocioException;
import br.pucpr.biblioteca.dominio.Reserva;
import br.pucpr.biblioteca.dominio.livro.CodigoEstadoLivro;
import br.pucpr.biblioteca.dominio.livro.Livro;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * PADRÃO FACADE (R4) — Facade.
 * Entrada única das operações de circulação. Coordena Relacionamento, Movimentacao, Acervo
 * e Livro dentro de UMA unidade de trabalho e devolve comprovantes (records imutáveis),
 * nunca as coleções internas. Se qualquer passo falhar, a unidade de trabalho desfaz todos.
 * O estado atual do Livro vem sempre do Acervo (nunca do cache de consultas).
 */
public class FachadaDeCirculacao {
    private final Acervo acervo;
    private final Relacionamento relacionamento;
    private final Movimentacao movimentacao;
    private final UnidadeDeTrabalho uow;
    private final PublicadorDeDevolucao publicador;
    private final Clock relogio;

    public FachadaDeCirculacao(Acervo acervo, Relacionamento relacionamento, Movimentacao movimentacao,
                               UnidadeDeTrabalho uow, PublicadorDeDevolucao publicador, Clock relogio) {
        this.acervo = acervo;
        this.relacionamento = relacionamento;
        this.movimentacao = movimentacao;
        this.uow = uow;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    /** f1 — regra 5: livro cadastrado, estudante cadastrado e livro LIVRE (decidido pelo estado). */
    public ComprovanteDeReserva solicitarReserva(int idLivro, String matricula) {
        return uow.executar(() -> {
            Livro livro = livroCadastrado(idLivro);
            boolean estudanteCadastrado = relacionamento.consultarEstudante(matricula).isPresent();
            livro.solicitarReserva(estudanteCadastrado);                 // 1. State valida e transita
            Reserva reserva = new Reserva(0, idLivro, matricula, hoje());
            movimentacao.adicionar(reserva);                             // 2. registra a reserva
            acervo.atualizarLivro(livro);                                // 3. grava o novo estado
            return comprovante(reserva, livro);
        });
    }

    /** f2 — regra 6. */
    public Optional<ComprovanteDeReserva> consultarReserva(int idLivro, String matricula) {
        return uow.executar(() -> movimentacao.consultarReservaAtiva(idLivro)
                .filter(r -> r.getMatricula().equals(matricula))
                .map(r -> comprovante(r, livroCadastrado(idLivro))));
    }

    public List<ComprovanteDeReserva> consultarReservas(String matricula) {
        return uow.executar(() -> movimentacao.consultarReservas(matricula).stream()
                .map(r -> comprovante(r, livroCadastrado(r.getIdLivro()))).toList());
    }

    /** f3 — regra 7: valida a reserva, remove a reserva ativa, cria o empréstimo e atualiza o Livro. */
    public ComprovanteDeEmprestimo efetivarEmprestimo(int idLivro, String matricula) {
        return uow.executar(() -> {
            Livro livro = livroCadastrado(idLivro);
            Optional<Reserva> reserva = movimentacao.consultarReservaAtiva(idLivro);
            boolean doMesmoEstudante = reserva.map(r -> r.getMatricula().equals(matricula)).orElse(false);
            livro.efetivarEmprestimo(doMesmoEstudante);                  // 1. State: RESERVADO -> EMPRESTADO
            movimentacao.remover(reserva.orElseThrow());                 // 2. remove a reserva ativa
            Emprestimo emprestimo = Emprestimo.novo(idLivro, matricula, hoje());
            movimentacao.adicionar(emprestimo);                          // 3. cria o empréstimo
            acervo.atualizarLivro(livro);                                // 4. grava o Livro
            return new ComprovanteDeEmprestimo(emprestimo.getId(), idLivro, livro.getTitulo(), matricula,
                    emprestimo.getData(), emprestimo.getDataVencimento());
        });
    }

    /** f4 — regra 8: guarda numRenovacoes < 2 avaliada pelo estado antes de mexer no vencimento. */
    public ComprovanteDeRenovacao renovarEmprestimo(int idLivro, String matricula) {
        return uow.executar(() -> {
            Livro livro = livroCadastrado(idLivro);
            Emprestimo emprestimo = movimentacao.consultarEmprestimo(idLivro, matricula)
                    .orElseThrow(() -> new RegraDeNegocioException("Não há empréstimo do livro " + idLivro
                            + " para o estudante " + matricula));
            livro.renovarEmprestimo(emprestimo);                         // State: guarda + estenderVencimento
            movimentacao.atualizar(emprestimo);
            acervo.atualizarLivro(livro);
            return new ComprovanteDeRenovacao(emprestimo.getId(), idLivro, matricula,
                    emprestimo.getDataVencimento(), emprestimo.getNumRenovacoes());
        });
    }

    /**
     * f5 — regra 9: insere a Devolução, remove o Empréstimo, Livro vai a DEVOLVIDO.
     * O evento é publicado só depois que a transação foi confirmada.
     */
    public ComprovanteDeDevolucao registrarDevolucao(int idLivro, String matricula, String idAtendente) {
        EventoDevolucao evento = uow.executar(() -> {
            Atendente atendente = relacionamento.consultarAtendente(idAtendente)
                    .orElseThrow(() -> new RegraDeNegocioException("Atendente não cadastrado: " + idAtendente));
            Estudante estudante = relacionamento.consultarEstudante(matricula)
                    .orElseThrow(() -> new RegraDeNegocioException("Estudante não cadastrado: " + matricula));
            Livro livro = livroCadastrado(idLivro);
            Emprestimo emprestimo = movimentacao.consultarEmprestimo(idLivro, matricula)
                    .orElseThrow(() -> new RegraDeNegocioException("Não há empréstimo do livro " + idLivro
                            + " para o estudante " + matricula));
            livro.registrarDevolucao();                                  // State: -> DEVOLVIDO
            Devolucao devolucao = new Devolucao(0, idLivro, matricula, atendente.getIdAtendente(),
                    hoje(), emprestimo.getDataVencimento());
            movimentacao.adicionar(devolucao);
            movimentacao.remover(emprestimo);
            acervo.atualizarLivro(livro);
            return new EventoDevolucao(devolucao.getId(), idLivro, livro.getTitulo(), matricula,
                    estudante.getPessoa().getEmail(), idAtendente,
                    devolucao.getDataVencimento(), devolucao.getDataDevolucao());
        });
        ResultadoDaPublicacao resultado = publicador.publicar(evento);   // após a confirmação
        return new ComprovanteDeDevolucao(evento.idDevolucao(), idLivro, matricula, idAtendente,
                evento.dataVencimento(), evento.dataDevolucao(), evento.diasAtraso(), resultado.falhas());
    }

    /** Dados da última devolução (usado para a 2ª via do boleto — regra 10). */
    public Optional<ComprovanteDeDevolucao> consultarDevolucao(int idLivro, String matricula) {
        return uow.executar(() -> movimentacao.consultarDevolucao(idLivro, matricula)
                .map(d -> new ComprovanteDeDevolucao(d.getId(), idLivro, matricula, d.getIdAtendente(),
                        d.getDataVencimento(), d.getDataDevolucao(),
                        d.diasDeAtraso(),
                        Map.of())));
    }

    // ----- eventos de inspeção e reparo (Seção 12 do Trabalho 02) -----
    public ComprovanteDeSituacao inspecionar(int idLivro, boolean bomEstado) {
        return transitar(idLivro, l -> l.inspecionar(bomEstado));
    }

    public ComprovanteDeSituacao enviarParaReparo(int idLivro) {
        return transitar(idLivro, Livro::enviarParaReparo);
    }

    public ComprovanteDeSituacao retornarDoReparo(int idLivro) {
        return transitar(idLivro, Livro::retornarDoReparo);
    }

    public ComprovanteDeSituacao avaliarReparo(int idLivro, boolean reparavel) {
        return transitar(idLivro, l -> l.avaliarReparo(reparavel));
    }

    private ComprovanteDeSituacao transitar(int idLivro, Consumer<Livro> evento) {
        return uow.executar(() -> {
            Livro livro = livroCadastrado(idLivro);
            CodigoEstadoLivro antes = livro.getEstado();
            evento.accept(livro);
            acervo.atualizarLivro(livro);
            return new ComprovanteDeSituacao(idLivro, livro.getTitulo(), antes, livro.getEstado());
        });
    }

    private Livro livroCadastrado(int idLivro) {
        return acervo.consultarLivro(idLivro)
                .orElseThrow(() -> new RegraDeNegocioException("Livro não cadastrado: " + idLivro));
    }

    private ComprovanteDeReserva comprovante(Reserva r, Livro livro) {
        return new ComprovanteDeReserva(r.getId(), r.getIdLivro(), livro.getTitulo(), r.getMatricula(), r.getData());
    }

    private LocalDate hoje() {
        return LocalDate.now(relogio);
    }
}
