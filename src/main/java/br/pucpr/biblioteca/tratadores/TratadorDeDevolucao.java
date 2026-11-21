package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.circulacao.ComprovanteDeDevolucao;
import br.pucpr.biblioteca.circulacao.ComprovanteDeSituacao;
import br.pucpr.biblioteca.circulacao.FachadaDeCirculacao;
import br.pucpr.biblioteca.devolucao.FilaDeInspecao;
import br.pucpr.biblioteca.devolucao.IndicadoresDeCirculacao;
import br.pucpr.biblioteca.financeiro.iMulta;
import br.pucpr.biblioteca.financeiro.multa.Boleto;

import java.util.Optional;
import java.util.Set;

/** Tratador do Atendente (f5, f6, inspeção e reparo). Só delega. */
public class TratadorDeDevolucao implements iDevolucao {
    private final FachadaDeCirculacao fachada;
    private final iMulta multas;
    private final FilaDeInspecao filaDeInspecao;
    private final IndicadoresDeCirculacao indicadores;

    public TratadorDeDevolucao(FachadaDeCirculacao fachada, iMulta multas,
                               FilaDeInspecao filaDeInspecao, IndicadoresDeCirculacao indicadores) {
        this.fachada = fachada;
        this.multas = multas;
        this.filaDeInspecao = filaDeInspecao;
        this.indicadores = indicadores;
    }

    @Override
    public ComprovanteDeDevolucao registrarDevolucao(int idLivro, String matricula, String idAtendente) {
        return fachada.registrarDevolucao(idLivro, matricula, idAtendente);
    }

    /**
     * Regra 10: com devolução atrasada registrada, entrega a 2ª via do boleto. Se o ouvinte de
     * multa tiver falhado na devolução, a multa é emitida agora.
     */
    @Override
    public Optional<Boleto> emitirBoletoDeMulta(int idLivro, String matricula) {
        Optional<Boleto> existente = multas.consultarBoleto(idLivro, matricula);
        if (existente.isPresent()) return existente;
        return fachada.consultarDevolucao(idLivro, matricula)
                .filter(d -> d.diasAtraso() > 0)
                .flatMap(d -> multas.emitirMulta(idLivro, matricula, d.dataVencimento(), d.dataDevolucao()));
    }

    @Override
    public ComprovanteDeSituacao inspecionar(int idLivro, boolean bomEstado) {
        ComprovanteDeSituacao c = fachada.inspecionar(idLivro, bomEstado);
        filaDeInspecao.concluir(idLivro);
        return c;
    }

    @Override
    public ComprovanteDeSituacao enviarParaReparo(int idLivro) { return fachada.enviarParaReparo(idLivro); }

    @Override
    public ComprovanteDeSituacao retornarDoReparo(int idLivro) { return fachada.retornarDoReparo(idLivro); }

    @Override
    public ComprovanteDeSituacao avaliarReparo(int idLivro, boolean reparavel) { return fachada.avaliarReparo(idLivro, reparavel); }

    public Set<Integer> livrosAguardandoInspecao() { return filaDeInspecao.pendentes(); }

    public IndicadoresDeCirculacao indicadores() { return indicadores; }
}
