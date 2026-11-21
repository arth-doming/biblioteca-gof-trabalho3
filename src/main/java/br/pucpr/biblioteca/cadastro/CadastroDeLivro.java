package br.pucpr.biblioteca.cadastro;

import br.pucpr.biblioteca.acesso.Acervo;
import br.pucpr.biblioteca.dados.UnidadeDeTrabalho;
import br.pucpr.biblioteca.dominio.livro.Livro;

import java.util.Optional;

/** PADRÃO TEMPLATE METHOD — ConcreteClass (regra 1.1). */
public class CadastroDeLivro extends RotinaDeCadastro<CadastroDeLivro.Dados, Livro> {
    public record Dados(String titulo) { }

    private final Acervo acervo;

    public CadastroDeLivro(UnidadeDeTrabalho uow, Acervo acervo) {
        super(uow);
        this.acervo = acervo;
    }

    @Override
    protected Optional<Livro> buscarExistente(Dados d) {
        return vazio(d.titulo()) ? Optional.empty() : acervo.consultarPorTitulo(d.titulo().trim());
    }

    @Override
    protected void validar(Dados d) {
        exigir(!vazio(d.titulo()), "Título é obrigatório");
    }

    @Override
    protected Livro criar(Dados d) {
        return new Livro(acervo.proximoIdLivro(), d.titulo().trim());   // T1: nasce LIVRE
    }

    @Override
    protected void armazenar(Livro livro) {
        acervo.adicionarLivro(livro);
    }
}
