package br.pucpr.biblioteca.consulta;

import br.pucpr.biblioteca.acesso.Acervo;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** PADRÃO DECORATOR — ConcreteComponent: a implementação original, sem medição nem cache. */
public class ConsultaAcervoOriginal implements ConsultaAcervo {
    private final Acervo acervo;

    public ConsultaAcervoOriginal(Acervo acervo) {
        this.acervo = acervo;
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(int idLivro) {
        return acervo.consultarLivro(idLivro).map(l -> new MetadadosLivro(l.getIdLivro(), l.getTitulo()));
    }

    @Override
    public List<MetadadosLivro> buscarPorTitulo(String trecho) {
        String t = trecho == null ? "" : trecho.toLowerCase(Locale.ROOT);
        return acervo.listarLivros().stream()
                .filter(l -> l.getTitulo().toLowerCase(Locale.ROOT).contains(t))
                .map(l -> new MetadadosLivro(l.getIdLivro(), l.getTitulo()))
                .toList();
    }
}
