package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.cadastro.ResultadoDeCadastro;
import br.pucpr.biblioteca.dominio.Atendente;

import java.util.Optional;

public interface iAtendente {
    ResultadoDeCadastro<Atendente> inserirAtendente(String cpf, String idAtendente);
    Optional<Atendente> consultarAtendente(String idAtendente);
    boolean atualizarAtendente(String idAtendente, String cpf);
    boolean excluirAtendente(String idAtendente);
}
