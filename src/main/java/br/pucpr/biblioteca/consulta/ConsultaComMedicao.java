package br.pucpr.biblioteca.consulta;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/** PADRÃO DECORATOR — ConcreteDecorator de medição. Mede toda chamada, venha ou não do cache. */
public class ConsultaComMedicao extends DecoradorDeConsulta {
    private final Medidor medidor;

    public ConsultaComMedicao(ConsultaAcervo interna, Medidor medidor) {
        super(interna);
        this.medidor = medidor;
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(int idLivro) {
        return medir("consultarMetadados", () -> super.consultarMetadados(idLivro));
    }

    @Override
    public List<MetadadosLivro> buscarPorTitulo(String trecho) {
        return medir("buscarPorTitulo", () -> super.buscarPorTitulo(trecho));
    }

    private <T> T medir(String operacao, Supplier<T> chamada) {
        long inicio = System.nanoTime();
        try {
            return chamada.get();
        } finally {
            medidor.registrar(operacao, System.nanoTime() - inicio);
        }
    }
}
