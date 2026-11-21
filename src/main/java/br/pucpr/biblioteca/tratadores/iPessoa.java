package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.cadastro.ResultadoDeCadastro;
import br.pucpr.biblioteca.dominio.Pessoa;

import java.util.Optional;

public interface iPessoa {
    ResultadoDeCadastro<Pessoa> inserirPessoa(String nome, String sobrenome, String cpf, String email);
    Optional<Pessoa> consultarPessoa(String cpf);
    boolean atualizarPessoa(String cpf, String nome, String sobrenome, String email);
    boolean excluirPessoa(String cpf);
}
