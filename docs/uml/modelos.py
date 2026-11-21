"""Especificação dos diagramas UML do Trabalho 3 (mesmos nomes do código)."""

# ---------------------------------------------------------------- classes
# cada diagrama: titulo, classes {nome: (estereotipo, papel, atributos, metodos)}, relacoes [(de, para, tipo, rotulo)]
# tipos: herda, realiza, usa, assoc, comp
CLASSES = {}

CLASSES["R01_State"] = ("R1 — Ciclo de vida do livro (State)", {
    "TratadorDeEmprestimo": ("", "Client", [], ["solicitarReserva(idLivro, matricula)", "renovarEmprestimo(idLivro, matricula)"]),
    "FachadaDeCirculacao": ("", "Client", [], ["solicitarReserva(...)", "efetivarEmprestimo(...)", "renovarEmprestimo(...)", "registrarDevolucao(...)", "inspecionar(idLivro, bomEstado)", "avaliarReparo(idLivro, reparavel)"]),
    "Livro": ("", "Context", ["- idLivro : int", "- titulo : String", "- estado : EstadoLivro"],
              ["+ solicitarReserva(estudanteCadastrado)", "+ efetivarEmprestimo(reservaDoMesmoEstudante)", "+ renovarEmprestimo(emprestimo)", "+ registrarDevolucao()", "+ inspecionar(bomEstado)", "+ enviarParaReparo()", "+ retornarDoReparo()", "+ avaliarReparo(reparavel)", "+ excluirLivro()", "~ mudarPara(estado)", "+ reconstituir(id, titulo, codigo) : Livro", "+ getEstado() : CodigoEstadoLivro"]),
    "EstadoLivro": ("abstract", "State", [], ["+ codigo() : CodigoEstadoLivro", "+ solicitarReserva(livro, ok)", "+ efetivarEmprestimo(livro, ok)", "+ renovarEmprestimo(livro, emp)", "+ registrarDevolucao(livro)", "+ inspecionar(livro, bomEstado)", "+ enviarParaReparo(livro)", "+ retornarDoReparo(livro)", "+ avaliarReparo(livro, reparavel)", "+ excluirLivro(livro)", "# recusar(evento)", "+ de(codigo) : EstadoLivro"]),
    "Livre": ("", "ConcreteState", [], ["solicitarReserva", "enviarParaReparo", "excluirLivro"]),
    "Reservado": ("", "ConcreteState", [], ["efetivarEmprestimo"]),
    "Emprestado": ("", "ConcreteState", [], ["renovarEmprestimo [numRenovacoes = 0]", "registrarDevolucao"]),
    "Renovado": ("", "ConcreteState", [], ["entry(emprestimo)", "renovarEmprestimo [numRenovacoes < 2]", "registrarDevolucao"]),
    "Devolvido": ("", "ConcreteState", [], ["inspecionar"]),
    "Reparando": ("", "ConcreteState", [], ["retornarDoReparo", "avaliarReparo"]),
    "Incinerado": ("", "ConcreteState", [], ["entry(livro)", "excluirLivro"]),
    "CodigoEstadoLivro": ("enumeration", "", ["LIVRE", "RESERVADO", "EMPRESTADO", "RENOVADO", "DEVOLVIDO", "REPARANDO", "INCINERADO"], []),
    "Emprestimo": ("", "", ["- dataVencimento : LocalDate", "- numRenovacoes : int"], ["+ estenderVencimento(dias)"]),
}, [
    ("TratadorDeEmprestimo", "FachadaDeCirculacao", "usa", ""),
    ("FachadaDeCirculacao", "Livro", "usa", ""),
    ("Livro", "EstadoLivro", "assoc", "estado 1"),
    ("Livre", "EstadoLivro", "herda", ""), ("Reservado", "EstadoLivro", "herda", ""),
    ("Emprestado", "EstadoLivro", "herda", ""), ("Renovado", "EstadoLivro", "herda", ""),
    ("Devolvido", "EstadoLivro", "herda", ""), ("Reparando", "EstadoLivro", "herda", ""),
    ("Incinerado", "EstadoLivro", "herda", ""),
    ("EstadoLivro", "CodigoEstadoLivro", "usa", "persistência"),
    ("Renovado", "Emprestimo", "usa", "estenderVencimento(30)"),
])

CLASSES["R02_Strategy"] = ("R2 — Políticas de multa (Strategy)", {
    "OuvinteDeMulta": ("", "Client", [], ["+ aoDevolver(evento)"]),
    "TratadorDeMulta": ("", "Context", ["- configuracao", "- cobranca : ContratoDeCobranca", "- multas : RepositorioDeMultas"], ["+ emitirMulta(idLivro, matricula, vencimento, devolucao) : Optional<Boleto>", "+ diasDeAtraso(vencimento, devolucao) : long"]),
    "ConfiguracaoAdministrativa": ("", "", ["- vigente : PoliticaDeMulta"], ["+ registrar(politica)", "+ definirPoliticaVigente(nome)", "+ politicaVigente() : PoliticaDeMulta"]),
    "PoliticaDeMulta": ("interface", "Strategy", [], ["+ nome() : String", "+ calcular(diasAtraso) : BigDecimal"]),
    "PoliticaOriginal": ("", "ConcreteStrategy", [], ["R$ 1,00 × dias"]),
    "PoliticaComTeto": ("", "ConcreteStrategy", ["TETO = 20.00"], ["min(dias × 1,00 ; 20,00)"]),
    "PoliticaComTolerancia": ("", "ConcreteStrategy", ["DIAS_TOLERANCIA = 2"], ["max(0, dias − 2) × 1,00"]),
    "Multa": ("", "", ["- valor : BigDecimal", "- diasAtraso : long", "- politicaAplicada : String", "- dataEmissao : LocalDate", "- quitada : boolean"], ["+ quitar(data) : boolean"]),
    "TratadorDeAdministracao": ("", "", [], ["+ definirPoliticaDeMulta(nome)"]),
}, [
    ("OuvinteDeMulta", "TratadorDeMulta", "usa", "iMulta"),
    ("TratadorDeMulta", "ConfiguracaoAdministrativa", "assoc", ""),
    ("ConfiguracaoAdministrativa", "PoliticaDeMulta", "assoc", "vigente"),
    ("PoliticaOriginal", "PoliticaDeMulta", "realiza", ""), ("PoliticaComTeto", "PoliticaDeMulta", "realiza", ""),
    ("PoliticaComTolerancia", "PoliticaDeMulta", "realiza", ""),
    ("TratadorDeMulta", "Multa", "assoc", "cria; grava via RepositorioDeMultas"),
    ("TratadorDeAdministracao", "ConfiguracaoAdministrativa", "usa", ""),
])

CLASSES["R03_Adapter"] = ("R3 — Bancos incompatíveis (Adapter)", {
    "TratadorDeMulta": ("", "Client", [], ["+ emitirMulta(...)", "+ registrarQuitacao(notificacao) : boolean"]),
    "ContratoDeCobranca": ("interface", "Target", [], ["+ registrarCobranca(SolicitacaoDeCobranca) : Boleto", "+ aoReceberPagamento(Consumer<NotificacaoDePagamento>)", "+ banco() : String"]),
    "AdaptadorBancoA": ("", "Adapter", ["- documentoPorNossoNumero : Map"], ["+ registrarCobranca(s) : reais→centavos, CPF→dígitos, data→AAAAMMDD", "- traduzir(AvisoDeLiquidacao)"]),
    "AdaptadorBancoB": ("", "Adapter", ["- documentoPorIdBoleto : Map"], ["+ registrarCobranca(s) : 7.00→\"7,00\", CPF formatado, data ISO", "- traduzir(EventoDePagamento)"]),
    "BancoA": ("", "Adaptee", [], ["+ incluirTitulo(centavos, documento, AAAAMMDD) : nossoNumero", "+ cadastrarWebhook(Consumer<AvisoDeLiquidacao>)"]),
    "BancoB": ("", "Adaptee", [], ["+ emitirBoleto(PedidoDeBoleto) : RespostaDeBoleto", "+ assinarEventos(Consumer<EventoDePagamento>)"]),
    "SolicitacaoDeCobranca": ("record", "", ["numeroDocumento, valor : BigDecimal,", "cpfPagador, nomePagador, vencimento"], []),
    "NotificacaoDePagamento": ("record", "", ["numeroDocumento, valorPago,", "situacao : SituacaoDoPagamento, dataPagamento"], []),
    "Sistema": ("", "raiz de composição", [], ["escolhe o adaptador pela opção 'banco'"]),
}, [
    ("TratadorDeMulta", "ContratoDeCobranca", "assoc", "cobranca"),
    ("AdaptadorBancoA", "ContratoDeCobranca", "realiza", ""), ("AdaptadorBancoB", "ContratoDeCobranca", "realiza", ""),
    ("AdaptadorBancoA", "BancoA", "assoc", ""), ("AdaptadorBancoB", "BancoB", "assoc", ""),
    ("ContratoDeCobranca", "SolicitacaoDeCobranca", "usa", ""), ("ContratoDeCobranca", "NotificacaoDePagamento", "usa", ""),
    ("Sistema", "AdaptadorBancoA", "usa", "cria"), ("Sistema", "AdaptadorBancoB", "usa", "cria"),
])

CLASSES["R04_Facade"] = ("R4 — Entrada única de circulação (Facade)", {
    "Main (CLI)": ("", "interface", [], []),
    "TratadorDeEmprestimo": ("", "Client", [], ["f1–f4"]),
    "TratadorDeDevolucao": ("", "Client", [], ["f5, f6, inspeção"]),
    "FachadaDeCirculacao": ("", "Facade", ["- uow : UnidadeDeTrabalho"], ["+ solicitarReserva(idLivro, matricula) : ComprovanteDeReserva", "+ consultarReserva(idLivro, matricula)", "+ efetivarEmprestimo(idLivro, matricula) : ComprovanteDeEmprestimo", "+ renovarEmprestimo(idLivro, matricula) : ComprovanteDeRenovacao", "+ registrarDevolucao(idLivro, matricula, idAtendente) : ComprovanteDeDevolucao", "+ inspecionar / enviarParaReparo / retornarDoReparo / avaliarReparo"]),
    "Relacionamento": ("", "subsistema", [], []),
    "Movimentacao": ("", "subsistema", [], []),
    "Acervo": ("", "subsistema", [], []),
    "Livro": ("", "subsistema", [], []),
    "UnidadeDeTrabalho": ("interface", "", [], ["+ executar(operacao)"]),
    "PublicadorDeDevolucao": ("", "", [], ["+ publicar(evento)"]),
    "ComprovanteDeEmprestimo": ("record", "", ["idEmprestimo, idLivro, tituloLivro,", "matricula, data, dataVencimento"], []),
}, [
    ("Main (CLI)", "TratadorDeEmprestimo", "usa", ""), ("Main (CLI)", "TratadorDeDevolucao", "usa", ""),
    ("TratadorDeEmprestimo", "FachadaDeCirculacao", "usa", ""), ("TratadorDeDevolucao", "FachadaDeCirculacao", "usa", ""),
    ("FachadaDeCirculacao", "Relacionamento", "assoc", ""), ("FachadaDeCirculacao", "Movimentacao", "assoc", ""),
    ("FachadaDeCirculacao", "Acervo", "assoc", ""), ("FachadaDeCirculacao", "Livro", "usa", ""),
    ("FachadaDeCirculacao", "UnidadeDeTrabalho", "assoc", ""), ("FachadaDeCirculacao", "PublicadorDeDevolucao", "assoc", ""),
    ("FachadaDeCirculacao", "ComprovanteDeEmprestimo", "usa", "devolve"),
])

CLASSES["R05_Observer"] = ("R5 — Reações independentes à devolução (Observer)", {
    "FachadaDeCirculacao": ("", "", [], ["+ registrarDevolucao(...) — publica após o commit"]),
    "PublicadorDeDevolucao": ("", "Subject", ["- ouvintes : List<OuvinteDeDevolucao>"], ["+ registrar(ouvinte)", "+ remover(ouvinte)", "+ publicar(evento) : ResultadoDaPublicacao"]),
    "OuvinteDeDevolucao": ("interface", "Observer", [], ["+ nome() : String", "+ aoDevolver(EventoDevolucao)"]),
    "EventoDevolucao": ("record", "", ["idDevolucao, idLivro, tituloLivro, matricula,", "emailEstudante, idAtendente,", "dataVencimento, dataDevolucao"], ["+ diasAtraso() : long", "+ emAtraso() : boolean"]),
    "OuvinteDeInspecao": ("", "ConcreteObserver", [], []),
    "OuvinteDeMulta": ("", "ConcreteObserver", [], []),
    "OuvinteDeIndicadores": ("", "ConcreteObserver", [], []),
    "OuvinteDeEmail": ("", "ConcreteObserver", [], []),
    "FilaDeInspecao": ("", "", [], []), "iMulta": ("interface", "", [], []),
    "IndicadoresDeCirculacao": ("", "", [], []), "ServicoDeEmail": ("interface", "", [], []),
}, [
    ("FachadaDeCirculacao", "PublicadorDeDevolucao", "assoc", ""),
    ("PublicadorDeDevolucao", "OuvinteDeDevolucao", "assoc", "0..*"),
    ("PublicadorDeDevolucao", "EventoDevolucao", "usa", ""),
    ("OuvinteDeInspecao", "OuvinteDeDevolucao", "realiza", ""), ("OuvinteDeMulta", "OuvinteDeDevolucao", "realiza", ""),
    ("OuvinteDeIndicadores", "OuvinteDeDevolucao", "realiza", ""), ("OuvinteDeEmail", "OuvinteDeDevolucao", "realiza", ""),
    ("OuvinteDeInspecao", "FilaDeInspecao", "assoc", ""), ("OuvinteDeMulta", "iMulta", "assoc", ""),
    ("OuvinteDeIndicadores", "IndicadoresDeCirculacao", "assoc", ""), ("OuvinteDeEmail", "ServicoDeEmail", "assoc", ""),
])

CLASSES["R06_Command"] = ("R6 — Solicitações de circulação como objetos (Command)", {
    "Main (CLI)": ("", "Client (tela)", [], ["cria a solicitação com id próprio"]),
    "TratadorDeEmprestimo": ("", "", [], ["+ submeter(solicitacao) : R", "+ solicitarReserva(idLivro, matricula)", "+ renovarEmprestimo(idLivro, matricula)"]),
    "ExecutorDeSolicitacoes": ("", "Invoker", ["- executadas : ConcurrentHashMap<String, FutureTask>"], ["+ executar(solicitacao) : R", "+ jaExecutada(id) : boolean"]),
    "SolicitacaoDeCirculacao<R>": ("abstract", "Command", ["- id : String", "- autor : String", "- criadaEm : Instant"], ["+ executar(receptor) : R"]),
    "SolicitacaoReserva": ("", "ConcreteCommand", ["- idLivro", "- matricula"], ["+ executar(receptor) : ComprovanteDeReserva"]),
    "SolicitacaoRenovacao": ("", "ConcreteCommand", ["- idLivro", "- matricula"], ["+ executar(receptor) : ComprovanteDeRenovacao"]),
    "FachadaDeCirculacao": ("", "Receiver", [], ["+ solicitarReserva(...)", "+ renovarEmprestimo(...)"]),
}, [
    ("Main (CLI)", "SolicitacaoRenovacao", "usa", "cria"), ("Main (CLI)", "TratadorDeEmprestimo", "usa", "submeter"),
    ("TratadorDeEmprestimo", "ExecutorDeSolicitacoes", "assoc", ""),
    ("ExecutorDeSolicitacoes", "SolicitacaoDeCirculacao<R>", "usa", ""),
    ("SolicitacaoReserva", "SolicitacaoDeCirculacao<R>", "herda", ""), ("SolicitacaoRenovacao", "SolicitacaoDeCirculacao<R>", "herda", ""),
    ("ExecutorDeSolicitacoes", "FachadaDeCirculacao", "assoc", "receptor"),
    ("SolicitacaoDeCirculacao<R>", "FachadaDeCirculacao", "usa", ""),
])

CLASSES["R07_TemplateMethod"] = ("R7 — Rotina comum para cadastros (Template Method)", {
    "RotinaDeCadastro<D,T>": ("abstract", "AbstractClass", ["- uow : UnidadeDeTrabalho"], ["+ incluir(dados) : ResultadoDeCadastro<T>  {final}", "# buscarExistente(dados) : Optional<T>  {abstract}", "# validar(dados)  {abstract}", "# verificarPreRequisitos(dados)  «gancho»", "# criar(dados) : T  {abstract}", "# armazenar(entidade)  {abstract}", "# apresentar(entidade, criada)  «gancho»", "# descrever(entidade)  «gancho»"]),
    "CadastroDeLivro": ("", "ConcreteClass", [], ["buscarExistente, validar, criar, armazenar"]),
    "CadastroDePessoa": ("", "ConcreteClass", [], ["buscarExistente, validar, criar, armazenar"]),
    "CadastroDeEstudante": ("", "ConcreteClass", [], ["+ verificarPreRequisitos: Pessoa cadastrada"]),
    "CadastroDeAtendente": ("", "ConcreteClass", [], ["+ verificarPreRequisitos: Pessoa cadastrada"]),
    "TratadorDeAcervo": ("", "", [], []), "TratadorDePessoa": ("", "", [], []),
    "TratadorDeEstudante": ("", "", [], []), "TratadorDeAtendente": ("", "", [], []),
    "iLivro": ("interface", "", [], []), "iPessoa": ("interface", "", [], []),
    "iEstudante": ("interface", "", [], []), "iAtendente": ("interface", "", [], []),
}, [
    ("CadastroDeLivro", "RotinaDeCadastro<D,T>", "herda", ""), ("CadastroDePessoa", "RotinaDeCadastro<D,T>", "herda", ""),
    ("CadastroDeEstudante", "RotinaDeCadastro<D,T>", "herda", ""), ("CadastroDeAtendente", "RotinaDeCadastro<D,T>", "herda", ""),
    ("TratadorDeAcervo", "CadastroDeLivro", "assoc", ""), ("TratadorDePessoa", "CadastroDePessoa", "assoc", ""),
    ("TratadorDeEstudante", "CadastroDeEstudante", "assoc", ""), ("TratadorDeAtendente", "CadastroDeAtendente", "assoc", ""),
    ("TratadorDeAcervo", "iLivro", "realiza", ""), ("TratadorDePessoa", "iPessoa", "realiza", ""),
    ("TratadorDeEstudante", "iEstudante", "realiza", ""), ("TratadorDeAtendente", "iAtendente", "realiza", ""),
])

CLASSES["R08_AbstractFactory"] = ("R8 — Famílias de acesso aos dados (Abstract Factory)", {
    "Sistema": ("", "Client", [], ["fabrica = SelecaoDeFabrica.para(perfil, mysql)"]),
    "SelecaoDeFabrica": ("", "", [], ["+ para(Perfil, ConfiguracaoMySQL) : FabricaDeAcessoADados"]),
    "FabricaDeAcessoADados": ("interface", "AbstractFactory", [], ["+ unidadeDeTrabalho() : UnidadeDeTrabalho", "+ criarRepositorioDeLivros()", "+ criarRepositorioDeMovimentacao()", "+ criarRepositorioDeRelacionamento()", "+ criarRepositorioDeMultas()"]),
    "FabricaEmMemoria": ("", "ConcreteFactory", [], []),
    "FabricaMySQL": ("", "ConcreteFactory", [], []),
    "UnidadeDeTrabalho": ("interface", "AbstractProduct", [], ["+ executar(operacao)"]),
    "RepositorioDeLivros": ("interface", "AbstractProduct", [], []),
    "RepositorioDeMovimentacao": ("interface", "AbstractProduct", [], []),
    "RepositorioDeRelacionamento": ("interface", "AbstractProduct", [], []),
    "RepositorioDeMultas": ("interface", "AbstractProduct", [], ["+ salvar(multa, boleto)", "+ quitar(numero, data) : boolean"]),
    "UnidadeDeTrabalhoEmMemoria": ("", "Product", [], ["log de desfazer"]),
    "Repositorio*EmMemoria": ("", "Product (×4)", [], ["construtor(UnidadeDeTrabalhoEmMemoria)"]),
    "UnidadeDeTrabalhoMySQL": ("", "Product", [], ["Connection por transação"]),
    "Repositorio*MySQL": ("", "Product (×4)", [], ["construtor(UnidadeDeTrabalhoMySQL)"]),
}, [
    ("Sistema", "SelecaoDeFabrica", "usa", ""), ("Sistema", "FabricaDeAcessoADados", "usa", ""),
    ("FabricaEmMemoria", "FabricaDeAcessoADados", "realiza", ""), ("FabricaMySQL", "FabricaDeAcessoADados", "realiza", ""),
    ("UnidadeDeTrabalhoEmMemoria", "UnidadeDeTrabalho", "realiza", ""), ("UnidadeDeTrabalhoMySQL", "UnidadeDeTrabalho", "realiza", ""),
    ("Repositorio*EmMemoria", "RepositorioDeLivros", "realiza", ""), ("Repositorio*MySQL", "RepositorioDeLivros", "realiza", ""),
    ("Repositorio*EmMemoria", "RepositorioDeMovimentacao", "realiza", ""), ("Repositorio*MySQL", "RepositorioDeMovimentacao", "realiza", ""),
    ("Repositorio*EmMemoria", "RepositorioDeRelacionamento", "realiza", ""), ("Repositorio*MySQL", "RepositorioDeRelacionamento", "realiza", ""),
    ("Repositorio*EmMemoria", "RepositorioDeMultas", "realiza", ""), ("Repositorio*MySQL", "RepositorioDeMultas", "realiza", ""),
    ("FabricaEmMemoria", "UnidadeDeTrabalhoEmMemoria", "usa", "cria"), ("FabricaEmMemoria", "Repositorio*EmMemoria", "usa", "cria"),
    ("FabricaMySQL", "UnidadeDeTrabalhoMySQL", "usa", "cria"), ("FabricaMySQL", "Repositorio*MySQL", "usa", "cria"),
    ("Repositorio*EmMemoria", "UnidadeDeTrabalhoEmMemoria", "assoc", "mesma uow"),
    ("Repositorio*MySQL", "UnidadeDeTrabalhoMySQL", "assoc", "mesma uow"),
])

CLASSES["R09_Decorator"] = ("R9 — Consulta com recursos opcionais (Decorator)", {
    "TratadorDeAcervo": ("", "Client", [], ["+ consultarMetadados(idLivro)", "+ buscarPorTitulo(trecho)"]),
    "ConsultaAcervo": ("interface", "Component", [], ["+ consultarMetadados(idLivro) : Optional<MetadadosLivro>", "+ buscarPorTitulo(trecho) : List<MetadadosLivro>"]),
    "ConsultaAcervoOriginal": ("", "ConcreteComponent", ["- acervo : Acervo"], []),
    "DecoradorDeConsulta": ("abstract", "Decorator", ["# interna : ConsultaAcervo"], []),
    "ConsultaComMedicao": ("", "ConcreteDecorator", ["- medidor : Medidor"], []),
    "ConsultaComCache": ("", "ConcreteDecorator", ["- cache : ArmazenamentoDeCache"], ["+ invalidar(idLivro)"]),
    "MontadorDeConsulta": ("", "", [], ["+ montar(acervo, cache, medidor)  → Medicao(Cache(Original))"]),
    "MetadadosLivro": ("record", "", ["idLivro, titulo  (sem estado)"], []),
}, [
    ("TratadorDeAcervo", "ConsultaAcervo", "assoc", ""),
    ("ConsultaAcervoOriginal", "ConsultaAcervo", "realiza", ""), ("DecoradorDeConsulta", "ConsultaAcervo", "realiza", ""),
    ("DecoradorDeConsulta", "ConsultaAcervo", "assoc", "interna 1"),
    ("ConsultaComMedicao", "DecoradorDeConsulta", "herda", ""), ("ConsultaComCache", "DecoradorDeConsulta", "herda", ""),
    ("MontadorDeConsulta", "ConsultaAcervo", "usa", "monta"), ("ConsultaAcervo", "MetadadosLivro", "usa", ""),
])

CLASSES["R10_Composite"] = ("R10 — Organização hierárquica do acervo (Composite)", {
    "OrganizacaoDoAcervo": ("", "Client", [], ["+ moverLivro(idLivro, destino)"]),
    "ComponenteAcervo": ("abstract", "Component", ["- pai : GrupoAcervo"], ["+ nome() : String", "+ contarLivros() : int", "+ listarLivros() : List<Integer>", "+ raiz() : GrupoAcervo"]),
    "ItemAcervo": ("", "Leaf", ["- idLivro : int"], ["contarLivros() = 1"]),
    "GrupoAcervo": ("", "Composite", ["- nome", "- tipo : TipoGrupo", "- filhos : List<ComponenteAcervo>"], ["+ adicionar(c)  recusa ciclo / livro em 2 grupos", "+ remover(c)", "+ localizarItem(idLivro)"]),
    "TipoGrupo": ("enumeration", "", ["UNIDADE", "SECAO", "SUBSECAO"], []),
}, [
    ("OrganizacaoDoAcervo", "GrupoAcervo", "assoc", "raiz"),
    ("ItemAcervo", "ComponenteAcervo", "herda", ""), ("GrupoAcervo", "ComponenteAcervo", "herda", ""),
    ("GrupoAcervo", "ComponenteAcervo", "agg", "filhos 0..*"), ("GrupoAcervo", "TipoGrupo", "usa", ""),
])

# ---------------------------------------------------------------- máquina de estados
ESTADOS = {
    "LIVRE": "", "RESERVADO": "", "EMPRESTADO": "",
    "RENOVADO": "entry / estenderVencimento(30 dias)",
    "DEVOLVIDO": "entry / registrarDataDevolucao", "REPARANDO": "do / reparar exemplar",
    "INCINERADO": "entry / registrarIncineracao",
}
TRANSICOES = [
    ("[*]", "LIVRE", "T1 / inserirLivro", "construtor Livro()"),
    ("LIVRE", "RESERVADO", "T2 solicitarReserva [estudante cadastrado]", "Livre.solicitarReserva"),
    ("RESERVADO", "EMPRESTADO", "T3 efetivarEmprestimo [reserva do mesmo estudante]", "Reservado.efetivarEmprestimo"),
    ("EMPRESTADO", "RENOVADO", "T4 renovarEmprestimo [numRenovacoes = 0]", "Emprestado.renovarEmprestimo"),
    ("RENOVADO", "RENOVADO", "T5 renovarEmprestimo [numRenovacoes < 2]", "Renovado.renovarEmprestimo"),
    ("EMPRESTADO", "DEVOLVIDO", "T6 registrarDevolucao / publicarEventoDevolucao", "Emprestado.registrarDevolucao; multa: OuvinteDeMulta se houver atraso"),
    ("RENOVADO", "DEVOLVIDO", "T6 registrarDevolucao / publicarEventoDevolucao", "Renovado.registrarDevolucao; multa: OuvinteDeMulta se houver atraso"),
    ("DEVOLVIDO", "LIVRE", "T7 inspecionar [bom estado]", "Devolvido.inspecionar"),
    ("DEVOLVIDO", "REPARANDO", "T8 inspecionar [danificado]", "Devolvido.inspecionar"),
    ("LIVRE", "REPARANDO", "T9 enviarParaReparo [dano no acervo]", "Livre.enviarParaReparo"),
    ("REPARANDO", "LIVRE", "T10 retornarDoReparo", "Reparando.retornarDoReparo"),
    ("REPARANDO", "INCINERADO", "avaliarReparo [laudo irrecuperável]", "Reparando.avaliarReparo"),
    ("LIVRE", "[final]", "T11 excluirLivro", "Livre.excluirLivro"),
    ("INCINERADO", "[final]", "T11 excluirLivro", "Incinerado.excluirLivro"),
]

# ---------------------------------------------------------------- sequências
# (titulo, participantes, mensagens) — mensagem: (de, para, texto) | ("note", texto) | ("ret", de, para, texto)
SEQUENCIAS = {}
SEQUENCIAS["S01_terceiraRenovacaoRecusada"] = ("R1 — Terceira renovação recusada (State)",
    ["Estudante", "TratadorDeEmprestimo", "FachadaDeCirculacao", "Movimentacao", "Livro", "Renovado", "Emprestimo"], [
    ("Estudante", "TratadorDeEmprestimo", "renovarEmprestimo(idLivro, matricula)"),
    ("TratadorDeEmprestimo", "FachadaDeCirculacao", "renovarEmprestimo(idLivro, matricula)  [via executor]"),
    ("FachadaDeCirculacao", "Movimentacao", "consultarEmprestimo(idLivro, matricula)"),
    ("ret", "Movimentacao", "FachadaDeCirculacao", "emprestimo (numRenovacoes = 2)"),
    ("FachadaDeCirculacao", "Livro", "renovarEmprestimo(emprestimo)"),
    ("Livro", "Renovado", "renovarEmprestimo(livro, emprestimo)"),
    ("Renovado", "Emprestimo", "getNumRenovacoes()"),
    ("ret", "Emprestimo", "Renovado", "2"),
    ("note", "guarda [numRenovacoes < 2] falsa: nada foi alterado"),
    ("ret", "Renovado", "FachadaDeCirculacao", "TransicaoInvalidaException"),
    ("note", "UnidadeDeTrabalho não confirma nada; vencimento continua igual"),
    ("ret", "FachadaDeCirculacao", "Estudante", "Recusado: limite de 2 renovações"),
])
SEQUENCIAS["S02_multaPorPolitica"] = ("R2 — Cálculo da multa pela política vigente (Strategy)",
    ["OuvinteDeMulta", "TratadorDeMulta", "ConfiguracaoAdministrativa", "PoliticaComTolerancia", "ContratoDeCobranca"], [
    ("OuvinteDeMulta", "TratadorDeMulta", "emitirMulta(idLivro, matricula, vencimento, devolucao)"),
    ("TratadorDeMulta", "TratadorDeMulta", "diasDeAtraso() = 5"),
    ("TratadorDeMulta", "ConfiguracaoAdministrativa", "politicaVigente()"),
    ("ret", "ConfiguracaoAdministrativa", "TratadorDeMulta", "PoliticaComTolerancia"),
    ("TratadorDeMulta", "PoliticaComTolerancia", "calcular(5)"),
    ("ret", "PoliticaComTolerancia", "TratadorDeMulta", "3.00"),
    ("TratadorDeMulta", "TratadorDeMulta", "new Multa(valor=3.00, politicaAplicada=COM_TOLERANCIA)"),
    ("TratadorDeMulta", "ContratoDeCobranca", "registrarCobranca(solicitacao)"),
    ("ret", "ContratoDeCobranca", "TratadorDeMulta", "Boleto"),
])
SEQUENCIAS["S03_seteReaisNosBancos"] = ("R3 — R$ 7,00 convertido por banco (Adapter)",
    ["TratadorDeMulta", "AdaptadorBancoA", "BancoA", "AdaptadorBancoB", "BancoB"], [
    ("note", "Instalação com banco A"),
    ("TratadorDeMulta", "AdaptadorBancoA", "registrarCobranca(valor = 7.00, cpf)"),
    ("AdaptadorBancoA", "BancoA", "incluirTitulo(700, \"11144477735\", 20261203)"),
    ("ret", "BancoA", "AdaptadorBancoA", "nossoNumero"),
    ("ret", "AdaptadorBancoA", "TratadorDeMulta", "Boleto(valor = 7.00)"),
    ("note", "Instalação com banco B — mesmo código no TratadorDeMulta"),
    ("TratadorDeMulta", "AdaptadorBancoB", "registrarCobranca(valor = 7.00, cpf)"),
    ("AdaptadorBancoB", "BancoB", "emitirBoleto(valor = \"7,00\", \"111.444.777-35\")"),
    ("ret", "BancoB", "AdaptadorBancoB", "idBoleto"),
    ("ret", "AdaptadorBancoB", "TratadorDeMulta", "Boleto(valor = 7.00)"),
    ("BancoA", "AdaptadorBancoA", "webhook(AvisoDeLiquidacao) ×2"),
    ("AdaptadorBancoA", "TratadorDeMulta", "registrarQuitacao(NotificacaoDePagamento)"),
    ("note", "2ª notificação: RepositorioDeMultas.quitar() — UPDATE ... WHERE quitada = FALSE — devolve false"),
])
SEQUENCIAS["S04_efetivarEmprestimo"] = ("R4 — Efetivar empréstimo pela fachada (Facade)",
    ["Estudante", "TratadorDeEmprestimo", "FachadaDeCirculacao", "UnidadeDeTrabalho", "Acervo", "Movimentacao", "Livro"], [
    ("Estudante", "TratadorDeEmprestimo", "efetivarEmprestimo(idLivro, matricula)"),
    ("TratadorDeEmprestimo", "FachadaDeCirculacao", "efetivarEmprestimo(idLivro, matricula)"),
    ("FachadaDeCirculacao", "UnidadeDeTrabalho", "executar(operacao)"),
    ("FachadaDeCirculacao", "Acervo", "consultarLivro(idLivro)"),
    ("FachadaDeCirculacao", "Movimentacao", "consultarReservaAtiva(idLivro)"),
    ("FachadaDeCirculacao", "Livro", "efetivarEmprestimo(reservaDoMesmoEstudante)"),
    ("note", "Reservado → Emprestado"),
    ("FachadaDeCirculacao", "Movimentacao", "remover(reserva)"),
    ("FachadaDeCirculacao", "Movimentacao", "adicionar(emprestimo)"),
    ("FachadaDeCirculacao", "Acervo", "atualizarLivro(livro)"),
    ("ret", "UnidadeDeTrabalho", "FachadaDeCirculacao", "commit (ou desfaz tudo se algum passo falhar)"),
    ("ret", "FachadaDeCirculacao", "Estudante", "ComprovanteDeEmprestimo"),
])
SEQUENCIAS["S05_devolucaoAtrasada"] = ("R5 — Devolução atrasada notifica ouvintes (Observer)",
    ["Atendente", "TratadorDeDevolucao", "FachadaDeCirculacao", "PublicadorDeDevolucao", "OuvinteDeInspecao", "OuvinteDeMulta", "OuvinteDeIndicadores", "OuvinteDeEmail"], [
    ("Atendente", "TratadorDeDevolucao", "registrarDevolucao(idLivro, matricula, idAtendente)"),
    ("TratadorDeDevolucao", "FachadaDeCirculacao", "registrarDevolucao(...)"),
    ("note", "transação: Livro → DEVOLVIDO, adicionar(devolucao), remover(emprestimo), commit"),
    ("FachadaDeCirculacao", "PublicadorDeDevolucao", "publicar(EventoDevolucao)"),
    ("PublicadorDeDevolucao", "OuvinteDeInspecao", "aoDevolver(e)  → fila de inspeção"),
    ("PublicadorDeDevolucao", "OuvinteDeMulta", "aoDevolver(e)  → emitirMulta (atraso)"),
    ("PublicadorDeDevolucao", "OuvinteDeIndicadores", "aoDevolver(e)"),
    ("PublicadorDeDevolucao", "OuvinteDeEmail", "aoDevolver(e)"),
    ("ret", "OuvinteDeEmail", "PublicadorDeDevolucao", "exceção capturada (os demais já rodaram)"),
    ("ret", "PublicadorDeDevolucao", "FachadaDeCirculacao", "ResultadoDaPublicacao"),
    ("ret", "FachadaDeCirculacao", "Atendente", "ComprovanteDeDevolucao (livro DEVOLVIDO)"),
])
SEQUENCIAS["S06_renovacaoRepetida"] = ("R6 — Mesma renovação enviada duas vezes (Command)",
    ["Tela", "TratadorDeEmprestimo", "ExecutorDeSolicitacoes", "SolicitacaoRenovacao", "FachadaDeCirculacao"], [
    ("Tela", "SolicitacaoRenovacao", "new(id = REQ-1, autor, criadaEm, idLivro, matricula)"),
    ("Tela", "TratadorDeEmprestimo", "submeter(solicitacao)"),
    ("TratadorDeEmprestimo", "ExecutorDeSolicitacoes", "executar(solicitacao)"),
    ("ExecutorDeSolicitacoes", "ExecutorDeSolicitacoes", "putIfAbsent(REQ-1, tarefa) = null"),
    ("ExecutorDeSolicitacoes", "SolicitacaoRenovacao", "executar(receptor)"),
    ("SolicitacaoRenovacao", "FachadaDeCirculacao", "renovarEmprestimo(idLivro, matricula)"),
    ("ret", "FachadaDeCirculacao", "Tela", "ComprovanteDeRenovacao (+30 dias)"),
    ("note", "Clique repetido"),
    ("Tela", "TratadorDeEmprestimo", "submeter(solicitacao)"),
    ("TratadorDeEmprestimo", "ExecutorDeSolicitacoes", "executar(solicitacao)"),
    ("ExecutorDeSolicitacoes", "ExecutorDeSolicitacoes", "putIfAbsent(REQ-1, ...) = tarefa existente"),
    ("ret", "ExecutorDeSolicitacoes", "Tela", "mesmo comprovante (não executa de novo)"),
])
SEQUENCIAS["S07_estudanteSemPessoa"] = ("R7 — Inclusão de estudante (Template Method)",
    ["Administrador", "TratadorDeEstudante", "CadastroDeEstudante", "Relacionamento"], [
    ("Administrador", "TratadorDeEstudante", "inserirEstudante(cpf, matricula)"),
    ("TratadorDeEstudante", "CadastroDeEstudante", "incluir(dados)  {final}"),
    ("CadastroDeEstudante", "Relacionamento", "consultarEstudante(matricula)  [buscarExistente]"),
    ("note", "se existir: apresentar(existente, false) e encerra"),
    ("CadastroDeEstudante", "CadastroDeEstudante", "validar(dados)"),
    ("CadastroDeEstudante", "Relacionamento", "consultarPessoa(cpf)  [verificarPreRequisitos]"),
    ("ret", "Relacionamento", "CadastroDeEstudante", "vazio"),
    ("ret", "CadastroDeEstudante", "Administrador", "PreRequisitoNaoAtendidoException — criar/armazenar não rodam"),
])
SEQUENCIAS["S08_familiaPorPerfil"] = ("R8 — Família escolhida pelo perfil (Abstract Factory)",
    ["Main", "Sistema", "SelecaoDeFabrica", "FabricaMySQL", "UnidadeDeTrabalhoMySQL", "RepositorioDeLivrosMySQL"], [
    ("Main", "Sistema", "new Sistema(opcoes{perfil = PRODUCAO})"),
    ("Sistema", "SelecaoDeFabrica", "para(PRODUCAO, mysql)"),
    ("SelecaoDeFabrica", "FabricaMySQL", "new FabricaMySQL(config)"),
    ("FabricaMySQL", "UnidadeDeTrabalhoMySQL", "new (sem abrir conexão)"),
    ("Sistema", "FabricaMySQL", "criarRepositorioDeLivros()"),
    ("FabricaMySQL", "RepositorioDeLivrosMySQL", "new(uow)"),
    ("note", "Movimentacao e Relacionamento recebem a MESMA uow"),
])
SEQUENCIAS["S09_cacheNaoAutoriza"] = ("R9 — Cache não autoriza reserva (Decorator)",
    ["Estudante", "TratadorDeAcervo", "ConsultaComMedicao", "ConsultaComCache", "TratadorDeEmprestimo", "FachadaDeCirculacao", "Acervo"], [
    ("Estudante", "TratadorDeAcervo", "consultarMetadados(id)"),
    ("TratadorDeAcervo", "ConsultaComMedicao", "consultarMetadados(id)"),
    ("ConsultaComMedicao", "ConsultaComCache", "consultarMetadados(id)  [mede]"),
    ("ret", "ConsultaComCache", "Estudante", "MetadadosLivro(id, titulo) do cache — sem estado"),
    ("Estudante", "TratadorDeEmprestimo", "solicitarReserva(id, matricula)"),
    ("TratadorDeEmprestimo", "FachadaDeCirculacao", "solicitarReserva(...)"),
    ("FachadaDeCirculacao", "Acervo", "consultarLivro(id)  — estado atual"),
    ("ret", "Acervo", "FachadaDeCirculacao", "Livro [EMPRESTADO]"),
    ("ret", "FachadaDeCirculacao", "Estudante", "Recusado pelo estado"),
])
SEQUENCIAS["S10_contarEMover"] = ("R10 — Contagem e movimentação na árvore (Composite)",
    ["Cliente", "unidade:GrupoAcervo", "exatas:GrupoAcervo", "humanas:GrupoAcervo", "OrganizacaoDoAcervo"], [
    ("Cliente", "unidade:GrupoAcervo", "contarLivros()"),
    ("unidade:GrupoAcervo", "exatas:GrupoAcervo", "contarLivros()"),
    ("ret", "exatas:GrupoAcervo", "unidade:GrupoAcervo", "3 (soma de ItemAcervo)"),
    ("unidade:GrupoAcervo", "humanas:GrupoAcervo", "contarLivros()"),
    ("ret", "humanas:GrupoAcervo", "unidade:GrupoAcervo", "2"),
    ("ret", "unidade:GrupoAcervo", "Cliente", "5"),
    ("Cliente", "OrganizacaoDoAcervo", "moverLivro(idLivro, humanas)"),
    ("OrganizacaoDoAcervo", "exatas:GrupoAcervo", "remover(item)"),
    ("OrganizacaoDoAcervo", "humanas:GrupoAcervo", "adicionar(item)"),
    ("note", "Livro e Movimentacao não são tocados: o item guarda só o idLivro"),
])
