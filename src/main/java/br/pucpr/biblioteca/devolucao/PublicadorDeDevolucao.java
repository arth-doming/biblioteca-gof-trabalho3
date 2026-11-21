package br.pucpr.biblioteca.devolucao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PADRÃO OBSERVER (R5) — Subject.
 * Chamado pela fachada só DEPOIS da confirmação da devolução. Cada ouvinte roda isolado
 * em try/catch: uma falha é registrada e os seguintes continuam; a devolução já está gravada.
 */
public class PublicadorDeDevolucao {
    private static final Logger LOG = Logger.getLogger(PublicadorDeDevolucao.class.getName());
    private final List<OuvinteDeDevolucao> ouvintes = new CopyOnWriteArrayList<>();

    public void registrar(OuvinteDeDevolucao ouvinte) { ouvintes.add(ouvinte); }

    public void remover(OuvinteDeDevolucao ouvinte) { ouvintes.remove(ouvinte); }

    public List<String> ouvintesRegistrados() {
        return ouvintes.stream().map(OuvinteDeDevolucao::nome).toList();
    }

    public ResultadoDaPublicacao publicar(EventoDevolucao evento) {
        List<String> notificados = new ArrayList<>();
        Map<String, String> falhas = new LinkedHashMap<>();
        for (OuvinteDeDevolucao o : ouvintes) {
            try {
                o.aoDevolver(evento);
                notificados.add(o.nome());
            } catch (RuntimeException e) {
                falhas.put(o.nome(), e.getMessage());
                LOG.log(Level.WARNING, "Ouvinte " + o.nome() + " falhou: " + e.getMessage());
            }
        }
        return new ResultadoDaPublicacao(List.copyOf(notificados), Map.copyOf(falhas));
    }
}
