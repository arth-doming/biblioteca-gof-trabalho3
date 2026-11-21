# Tabela de rastreabilidade — Trabalho 3 (padrões GoF)

Pacote base: `br.pucpr.biblioteca` (abreviado como `…`). Testes em `src/test/java/br/pucpr/biblioteca`.
Diagramas: `docs/uml/Modelos_UML_Trabalho3.pdf` (classes de cada padrão, máquina de estados e sequências dos cenários).

| Req. | Situação | Padrão | Papéis → classes | Teste que comprova o cenário | Diagramas |
|---|---|---|---|---|---|
| R1 | Ciclo de vida do livro | **State** | Context: `…dominio.livro.Livro` · State: `EstadoLivro` · ConcreteState: `Livre`, `Reservado`, `Emprestado`, `Renovado`, `Devolvido`, `Reparando`, `Incinerado` · código persistido: `CodigoEstadoLivro` | `R01StateTest.R01_terceiraRenovacaoRecusada`, `R01_livroDevolvidoNaoPodeSerReservado` (+ `R01_eventoNaoAceitoNaoAlteraLivro`, `R01_laudoIrrecuperavelLevaReparandoAIncinerado`, `R01_estadoGravadoEReconstituido`) | R01_State, R01_MaquinaDeEstados, S01 |
| R2 | Políticas de multa | **Strategy** | Strategy: `…financeiro.multa.PoliticaDeMulta` · ConcreteStrategy: `PoliticaOriginal`, `PoliticaComTeto`, `PoliticaComTolerancia` · Context: `…financeiro.TratadorDeMulta` · configuração: `ConfiguracaoAdministrativa` · registro: `Multa` (valor + `politicaAplicada`) | `R02StrategyTest.R02_cincoDiasOriginalETolerancia`, `R02_semAtrasoMultaZero` (+ `R02_trocaDePoliticaNaoRecalculaMultaEmitida`, `R02_novaPoliticaSemAlterarServico`) | R02_Strategy, S02 |
| R3 | Bancos incompatíveis | **Adapter** | Target: `…financeiro.cobranca.ContratoDeCobranca` · Adapter: `AdaptadorBancoA`, `AdaptadorBancoB` · Adaptee: `BancoA`, `BancoB` · Client: `TratadorDeMulta` | `R03AdapterTest.R03_seteReaisConvertidosPorBanco`, `R03_notificacaoRepetidaNaoQuitaDuasVezesBancoA/B` (+ `R03_multaGravadaNaFamiliaDeDadosSobreviveAoServico`) | R03_Adapter, S03 |
| R4 | Entrada única de circulação | **Facade** | Facade: `…circulacao.FachadaDeCirculacao` · subsistema: `Acervo`, `Relacionamento`, `Movimentacao`, `Livro`, `UnidadeDeTrabalho` · Clients: `TratadorDeEmprestimo`, `TratadorDeDevolucao` · comprovantes: `Comprovante*` (records) | `R04FacadeTest.R04_efetivarEmprestimoPelaFachada` (+ `R04_falhaNoMeioDaOperacaoDesfazTudo`) | R04_Facade, S04 |
| R5 | Reações à devolução | **Observer** | Subject: `…devolucao.PublicadorDeDevolucao` · Observer: `OuvinteDeDevolucao` · ConcreteObserver: `OuvinteDeInspecao`, `OuvinteDeMulta`, `OuvinteDeIndicadores`, `OuvinteDeEmail` · evento: `EventoDevolucao` | `R05ObserverTest.R05_devolucaoAtrasadaNotificaOuvintes`, `R05_devolucaoNoPrazoSemMulta` (+ `R05_falhaDoEmailNaoAfetaDemaisNemDesfazDevolucao`, `R05_novoOuvinteSemAlterarTratador`) | R05_Observer, S05 |
| R6 | Solicitações como objetos | **Command** | Command: `…comando.SolicitacaoDeCirculacao` · ConcreteCommand: `SolicitacaoReserva`, `SolicitacaoRenovacao` · Invoker: `ExecutorDeSolicitacoes` · Receiver: `FachadaDeCirculacao` · Client: tela (`ui.Main`) via `TratadorDeEmprestimo.submeter` | `R06CommandTest.R06_renovacaoRepetidaAcrescentaTrintaDias`, `R06_solicitacoesDiferentesRespeitamLimiteDeDuas` (+ `R06_submissoesSimultaneasExecutamUmaVez`, `R06_falhaTecnicaLiberaIdParaNovaTentativa`) | R06_Command, S06 |
| R7 | Rotina comum de cadastro | **Template Method** | AbstractClass: `…cadastro.RotinaDeCadastro` (`incluir` é `final`) · ConcreteClass: `CadastroDeLivro`, `CadastroDePessoa`, `CadastroDeEstudante`, `CadastroDeAtendente` · gancho: `verificarPreRequisitos`, `apresentar`, `descrever` · interfaces preservadas: `iLivro`, `iPessoa`, `iEstudante`, `iAtendente` | `R07TemplateMethodTest.R07_estudanteDuplicadoRetornaExistente`, `R07_cpfInexistenteFalhaAntesDeGravar` (+ `R07_metodoModeloNaoPodeSerSobrescrito`) | R07_TemplateMethod, S07 |
| R8 | Famílias de acesso a dados | **Abstract Factory** | AbstractFactory: `…dados.FabricaDeAcessoADados` · ConcreteFactory: `FabricaEmMemoria`, `FabricaMySQL` · AbstractProduct: `UnidadeDeTrabalho`, `RepositorioDeLivros`, `RepositorioDeMovimentacao`, `RepositorioDeRelacionamento`, `RepositorioDeMultas` · Products: `*EmMemoria`, `*MySQL` · escolha única: `SelecaoDeFabrica` chamada por `config.Sistema` | `R08AbstractFactoryTest.R08_perfilTesteSomenteMemoria`, `R08_perfilProducaoMesmaUnidadeDeTrabalho` (+ `R08_unidadeDeTrabalhoEmMemoriaDesfazTudoEmFalha`, `R08_integracaoMySQLReconstituiEstado` com MySQL) | R08_AbstractFactory, S08 |
| R9 | Consulta com recursos opcionais | **Decorator** | Component: `…consulta.ConsultaAcervo` · ConcreteComponent: `ConsultaAcervoOriginal` · Decorator: `DecoradorDeConsulta` · ConcreteDecorator: `ConsultaComMedicao`, `ConsultaComCache` · montagem: `MontadorDeConsulta` | `R09DecoratorTest.R09_segundaConsultaUsaCacheEMede`, `R09_cacheNaoAutorizaReserva` (+ `R09_falhaDoCacheConsultaOrigem`) | R09_Decorator, S09 |
| R10 | Organização do acervo | **Composite** | Component: `…organizacao.ComponenteAcervo` · Composite: `GrupoAcervo` (`TipoGrupo` UNIDADE/SECAO/SUBSECAO) · Leaf: `ItemAcervo` · Client: `OrganizacaoDoAcervo` | `R10CompositeTest.R10_unidadeComDuasSecoesTotalizaCinco`, `R10_moverLivroNaoAlteraEstado` (+ `R10_recusaCicloELivroEmDoisGrupos`) | R10_Composite, S10 |

## Transições da máquina de estados → métodos

| Transição (Trabalho 02) | Método que a implementa | Guarda / ação no corpo |
|---|---|---|
| T1 `/ inserirLivro` → LIVRE | construtor `Livro(id, titulo)` | — |
| T2 `solicitarReserva [estudante cadastrado]` LIVRE → RESERVADO | `Livre.solicitarReserva` | `if (!estudanteCadastrado) throw` |
| T3 `efetivarEmprestimo [reserva do mesmo estudante]` RESERVADO → EMPRESTADO | `Reservado.efetivarEmprestimo` | `if (!reservaDoMesmoEstudante) throw` |
| T4 `renovarEmprestimo [numRenovacoes = 0]` EMPRESTADO → RENOVADO | `Emprestado.renovarEmprestimo` | guarda antes; depois `Renovado.entry` → `estenderVencimento(30)` |
| T5 `renovarEmprestimo [numRenovacoes < 2]` RENOVADO → RENOVADO | `Renovado.renovarEmprestimo` | guarda antes; autotransição reexecuta `entry` |
| T6 `registrarDevolucao / publicarEventoDevolucao` → DEVOLVIDO | `Emprestado/Renovado.registrarDevolucao` | a fachada publica o evento após o commit; `OuvinteDeMulta` emite a multa se houver atraso (D3) |
| T7 `inspecionar [bom estado]` DEVOLVIDO → LIVRE | `Devolvido.inspecionar` | `if (bomEstado)` |
| T8 `inspecionar [danificado]` DEVOLVIDO → REPARANDO | `Devolvido.inspecionar` | `else` |
| T9 `enviarParaReparo [dano no acervo]` LIVRE → REPARANDO | `Livre.enviarParaReparo` | — |
| T10 `retornarDoReparo` REPARANDO → LIVRE | `Reparando.retornarDoReparo` | — |
| laudo `avaliarReparo [irreparável]` REPARANDO → INCINERADO | `Reparando.avaliarReparo` | `if (!reparavel)`; `Incinerado.entry` → `registrarIncineracao` |
| T11 `excluirLivro` LIVRE/INCINERADO → final | `Livre.excluirLivro`, `Incinerado.excluirLivro` | — |

Qualquer outro evento cai na implementação padrão de `EstadoLivro`, que lança `TransicaoInvalidaException` sem alterar o livro.
