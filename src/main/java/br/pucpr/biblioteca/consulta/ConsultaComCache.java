package br.pucpr.biblioteca.consulta;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PADRÃO DECORATOR — ConcreteDecorator de cache.
 * Guarda só MetadadosLivro (id e título). Se o armazenamento do cache falhar, a consulta
 * segue para o objeto interno: o cache é otimização, nunca condição para responder.
 */
public class ConsultaComCache extends DecoradorDeConsulta {
    private static final Logger LOG = Logger.getLogger(ConsultaComCache.class.getName());

    private final ArmazenamentoDeCache cache;
    private final AtomicLong acertos = new AtomicLong();
    private final AtomicLong falhas = new AtomicLong();

    public ConsultaComCache(ConsultaAcervo interna, ArmazenamentoDeCache cache) {
        super(interna);
        this.cache = cache;
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(int idLivro) {
        try {
            Optional<MetadadosLivro> emCache = cache.ler(idLivro);
            if (emCache.isPresent()) {
                acertos.incrementAndGet();
                return emCache;
            }
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Cache indisponível, consultando a origem: " + e.getMessage());
        }
        Optional<MetadadosLivro> resultado = super.consultarMetadados(idLivro);
        try {
            resultado.ifPresent(cache::gravar);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Falha ao gravar no cache: " + e.getMessage());
        }
        falhas.incrementAndGet();
        return resultado;
    }

    public void invalidar(int idLivro) {
        try {
            cache.invalidar(idLivro);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Falha ao invalidar cache: " + e.getMessage());
        }
    }

    public long acertos() { return acertos.get(); }

    public long faltas() { return falhas.get(); }
}
