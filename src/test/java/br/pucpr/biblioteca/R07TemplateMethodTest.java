package br.pucpr.biblioteca;

import br.pucpr.biblioteca.acesso.Relacionamento;
import br.pucpr.biblioteca.cadastro.CadastroDeEstudante;
import br.pucpr.biblioteca.cadastro.PreRequisitoNaoAtendidoException;
import br.pucpr.biblioteca.cadastro.ResultadoDeCadastro;
import br.pucpr.biblioteca.cadastro.RotinaDeCadastro;
import br.pucpr.biblioteca.dados.memoria.FabricaEmMemoria;
import br.pucpr.biblioteca.dominio.Estudante;
import br.pucpr.biblioteca.dominio.Pessoa;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/** R7 — Rotina comum para cadastros (Template Method). */
class R07TemplateMethodTest extends CenarioBase {

    @Test
    void R07_estudanteDuplicadoRetornaExistente() {
        ResultadoDeCadastro<Estudante> r = sistema.tratadorDeEstudante().inserirEstudante("11144477735", MATRICULA);
        assertFalse(r.criada());
        assertEquals(MATRICULA, r.entidade().getMatricula());
        assertEquals("11144477735", r.entidade().getPessoa().getCpf());
    }

    @Test
    void R07_cpfInexistenteFalhaAntesDeGravar() {
        FabricaEmMemoria f = new FabricaEmMemoria();
        int[] gravacoes = {0};
        Relacionamento rel = new Relacionamento(f.criarRepositorioDeRelacionamento()) {
            @Override public void adicionar(Estudante e) { gravacoes[0]++; super.adicionar(e); }
            @Override public void adicionar(Pessoa p) { gravacoes[0]++; super.adicionar(p); }
        };
        CadastroDeEstudante cadastro = new CadastroDeEstudante(f.unidadeDeTrabalho(), rel);

        assertThrows(PreRequisitoNaoAtendidoException.class,
                () -> cadastro.incluir(new CadastroDeEstudante.Dados("99988877766", "E999")));
        assertEquals(0, gravacoes[0]);
        assertTrue(rel.consultarEstudante("E999").isEmpty());
    }

    @Test
    void R07_metodoModeloNaoPodeSerSobrescrito() throws Exception {
        assertTrue(Modifier.isFinal(RotinaDeCadastro.class.getMethod("incluir", Object.class).getModifiers()));
    }

    @Test
    void R07_atendenteComCpfInexistenteTambemFalha() {
        assertThrows(PreRequisitoNaoAtendidoException.class,
                () -> sistema.tratadorDeAtendente().inserirAtendente("55566677788", "AT99"));
        assertTrue(sistema.tratadorDeAtendente().consultarAtendente("AT99").isEmpty());
    }
}
