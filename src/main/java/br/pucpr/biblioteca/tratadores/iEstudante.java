package br.pucpr.biblioteca.tratadores;

import br.pucpr.biblioteca.cadastro.ResultadoDeCadastro;
import br.pucpr.biblioteca.dominio.Estudante;

import java.util.Optional;

public interface iEstudante {
    ResultadoDeCadastro<Estudante> inserirEstudante(String cpf, String matricula);
    Optional<Estudante> consultarEstudante(String matricula);
    boolean atualizarEstudante(String matricula, String cpf);
    boolean excluirEstudante(String matricula);
}
