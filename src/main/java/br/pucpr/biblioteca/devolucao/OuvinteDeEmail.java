package br.pucpr.biblioteca.devolucao;

/** PADRÃO OBSERVER — ConcreteObserver: envia o recibo da devolução ao estudante. */
public class OuvinteDeEmail implements OuvinteDeDevolucao {
    private final ServicoDeEmail email;

    public OuvinteDeEmail(ServicoDeEmail email) { this.email = email; }

    @Override
    public String nome() { return "email"; }

    @Override
    public void aoDevolver(EventoDevolucao e) {
        String corpo = "Recebemos o livro \"" + e.tituloLivro() + "\" em " + e.dataDevolucao() + "."
                + (e.emAtraso() ? " Houve " + e.diasAtraso() + " dia(s) de atraso; o boleto da multa segue em separado." : "");
        email.enviar(e.emailEstudante(), "Devolução registrada", corpo);
    }
}
