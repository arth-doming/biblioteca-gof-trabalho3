# Instruções para atualizar o Astah — Trabalho 3 (padrões GoF)

O enunciado pede: *"Modelos UML atualizados no Astah (.asta) e exportados em PDF: diagrama de classes de cada padrão, a máquina de estados de Livro e os diagramas de sequência dos cenários."* Este roteiro diz exatamente o que criar ou mudar. Os nomes abaixo são os mesmos do código; copie-os sem alterar, porque o professor avalia a coerência entre código e modelo.

**Referência visual:** `docs/uml/Modelos_UML_Trabalho3.pdf` tem todos os diagramas prontos (um por página). Este arquivo descreve cada um em texto, para montar no Astah.

## Checklist

- [ ] 0. Preparar o arquivo
- [ ] 1. Renomear o enum `EstadoLivro` → `CodigoEstadoLivro`
- [ ] 2. Corrigir a transição T6 na máquina de estados
- [ ] 3. Atualizar as assinaturas das interfaces
- [ ] 4. Criar os 10 diagramas de classes dos padrões
- [ ] 5. Criar os 10 diagramas de sequência dos cenários
- [ ] 6. Exportar tudo em PDF e salvar no projeto

## 0. Preparar o arquivo

1. Na pasta do projeto, abra `modelos/astah/` e copie `Trabalho02_RA2_completo.asta` com o nome **`Trabalho03_GoF.asta`**. Trabalhe só nessa cópia; o original fica como backup.
2. Abra a cópia no Astah. Na árvore de estrutura (painel *Structure*, à esquerda), crie um pacote **`GoF`** (botão direito no projeto → *Create Model Element* → *Package*). Os diagramas novos ficam dentro dele.
3. **Reaproveite as classes que já existem.** Para colocar uma classe existente num diagrama novo, arraste-a da árvore *Structure* para o diagrama. Não crie outra com o mesmo nome: o Astah duplicaria o elemento, e o modelo perderia a coerência. Classes que já existem no modelo do Trabalho 02: `Acervo`, `Boleto`, `Emprestimo`, `Livro`, `Movimentacao`, `Multa`, `Relacionamento`, `TratadorDeAcervo`, `TratadorDeAtendente`, `TratadorDeDevolucao`, `TratadorDeEmprestimo`, `TratadorDeEstudante`, `TratadorDeMulta`, `TratadorDePessoa`, `iAtendente`, `iEstudante`, `iLivro`, `iMulta`, `iPessoa`.
4. O **papel no padrão** (Context, State, Strategy etc.) vai como **estereótipo** da classe: selecione a classe → painel de propriedades (*Base*) → campo *Stereotype* → digite o papel, por exemplo `ConcreteState`.

## 1. Renomear o enum (decisão D1)

1. Na árvore *Structure*, ache a enumeração **`EstadoLivro`** (está no diagrama de classes do Trabalho 02, com LIVRE … INCINERADO).
2. Renomeie para **`CodigoEstadoLivro`** (duplo clique no nome ou F2). Os diagramas que a usam se atualizam sozinhos.
3. Na classe `Livro`, o atributo `estado` passa a ser do tipo **`EstadoLivro`**, que agora é a classe abstrata do State, criada no passo 4. Faça essa troca depois de criar a classe.

> Por quê: o nome `EstadoLivro` agora é da classe abstrata do padrão State; o enum só guarda o código gravado no banco.

## 2. Corrigir a transição T6 na máquina de estados (decisão D3)

Abra o diagrama **T02-1 Estados do Livro**. Hoje há quatro setas chegando em DEVOLVIDO: de EMPRESTADO e de RENOVADO, cada uma com `[sem atraso]` e `[atraso] / emitirBoletoDeMulta`.

1. Apague as duas setas `registrarDevolucao [atraso] / emitirBoletoDeMulta` (a de EMPRESTADO e a de RENOVADO).
2. Nas duas setas que sobram (EMPRESTADO → DEVOLVIDO e RENOVADO → DEVOLVIDO), troque o texto por: **`registrarDevolucao / publicarEventoDevolucao`**.
3. Acrescente uma nota (*Note*) ligada a essas setas: *"A multa é emitida pelo OuvinteDeMulta (Observer, R5) quando há atraso."*
4. Na nota rosa com a lista de transições, troque a linha da T6 por: `T6 registrarDevolucao / publicarEventoDevolucao -> DEVOLVIDO`.
5. A frase "Eventos = operações …" da nota pode ficar como está: `emitirBoletoDeMulta` continua existindo no TratadorDeDevolucao, agora como 2ª via do boleto.

Confira que todas as transições batem com esta tabela (cada uma corresponde a um método da classe do estado de origem):

| Transição | Método no código |
|---|---|
| [*] → LIVRE: `T1 / inserirLivro` | `construtor Livro()` |
| LIVRE → RESERVADO: `T2 solicitarReserva [estudante cadastrado]` | `Livre.solicitarReserva` |
| RESERVADO → EMPRESTADO: `T3 efetivarEmprestimo [reserva do mesmo estudante]` | `Reservado.efetivarEmprestimo` |
| EMPRESTADO → RENOVADO: `T4 renovarEmprestimo [numRenovacoes = 0]` | `Emprestado.renovarEmprestimo` |
| RENOVADO → RENOVADO: `T5 renovarEmprestimo [numRenovacoes < 2]` | `Renovado.renovarEmprestimo` |
| EMPRESTADO → DEVOLVIDO: `T6 registrarDevolucao / publicarEventoDevolucao` | `Emprestado.registrarDevolucao; multa: OuvinteDeMulta se houver atraso` |
| RENOVADO → DEVOLVIDO: `T6 registrarDevolucao / publicarEventoDevolucao` | `Renovado.registrarDevolucao; multa: OuvinteDeMulta se houver atraso` |
| DEVOLVIDO → LIVRE: `T7 inspecionar [bom estado]` | `Devolvido.inspecionar` |
| DEVOLVIDO → REPARANDO: `T8 inspecionar [danificado]` | `Devolvido.inspecionar` |
| LIVRE → REPARANDO: `T9 enviarParaReparo [dano no acervo]` | `Livre.enviarParaReparo` |
| REPARANDO → LIVRE: `T10 retornarDoReparo` | `Reparando.retornarDoReparo` |
| REPARANDO → INCINERADO: `avaliarReparo [laudo irrecuperável]` | `Reparando.avaliarReparo` |
| LIVRE → [final]: `T11 excluirLivro` | `Livre.excluirLivro` |
| INCINERADO → [final]: `T11 excluirLivro` | `Incinerado.excluirLivro` |

Estados e ações internas: **LIVRE**; **RESERVADO**; **EMPRESTADO**; **RENOVADO** (`entry / estenderVencimento(30 dias)`); **DEVOLVIDO** (`entry / registrarDataDevolucao`); **REPARANDO** (`do / reparar exemplar`); **INCINERADO** (`entry / registrarIncineracao`).

## 3. Atualizar as assinaturas das interfaces (decisões D6 e D7)

No diagrama de classes do Trabalho 02, as operações destas interfaces mudaram. Edite cada interface (duplo clique → aba *Operations*) para ficar **exatamente** assim:

**iLivro**
```
+ inserirLivro(titulo : String) : ResultadoDeCadastro<Livro>
+ consultarLivro(idLivro : int) : Optional<Livro>
+ atualizarLivro(idLivro : int, titulo : String) : boolean
+ excluirLivro(idLivro : int) : boolean
```
**iPessoa**
```
+ inserirPessoa(nome : String, sobrenome : String, cpf : String, email : String) : ResultadoDeCadastro<Pessoa>
+ consultarPessoa(cpf : String) : Optional<Pessoa>
+ atualizarPessoa(cpf : String, nome : String, sobrenome : String, email : String) : boolean
+ excluirPessoa(cpf : String) : boolean
```
**iEstudante**
```
+ inserirEstudante(cpf : String, matricula : String) : ResultadoDeCadastro<Estudante>
+ consultarEstudante(matricula : String) : Optional<Estudante>
+ atualizarEstudante(matricula : String, cpf : String) : boolean
+ excluirEstudante(matricula : String) : boolean
```
**iAtendente**
```
+ inserirAtendente(cpf : String, idAtendente : String) : ResultadoDeCadastro<Atendente>
+ consultarAtendente(idAtendente : String) : Optional<Atendente>
+ atualizarAtendente(idAtendente : String, cpf : String) : boolean
+ excluirAtendente(idAtendente : String) : boolean
```
**iEmprestimo**
```
+ solicitarReserva(idLivro : int, matricula : String) : ComprovanteDeReserva
+ consultarReservas(idLivro : int, matricula : String) : Optional<ComprovanteDeReserva>
+ consultarReservas(matricula : String) : List<ComprovanteDeReserva>
+ efetivarEmprestimo(idLivro : int, matricula : String) : ComprovanteDeEmprestimo
+ renovarEmprestimo(idLivro : int, matricula : String) : ComprovanteDeRenovacao
```
**iDevolucao**
```
+ registrarDevolucao(idLivro : int, matricula : String, idAtendente : String) : ComprovanteDeDevolucao
+ emitirBoletoDeMulta(idLivro : int, matricula : String) : Optional<Boleto>
+ inspecionar(idLivro : int, bomEstado : boolean) : ComprovanteDeSituacao
+ enviarParaReparo(idLivro : int) : ComprovanteDeSituacao
+ retornarDoReparo(idLivro : int) : ComprovanteDeSituacao
+ avaliarReparo(idLivro : int, reparavel : boolean) : ComprovanteDeSituacao
```
**iMulta**
```
+ emitirMulta(idLivro : int, matricula : String, dataVencimento : LocalDate, dataDevolucao : LocalDate) : Optional<Boleto>
+ consultarMulta(matricula : String) : List<Multa>
+ consultarBoleto(idLivro : int, matricula : String) : Optional<Boleto>
```
**iQuitacao**
```
+ registrarQuitacao(notificacao : NotificacaoDePagamento) : boolean
```
O que mudou em relação ao Trabalho 02:
- As inclusões devolvem `ResultadoDeCadastro<T>`, que diz se o cadastro foi criado agora ou já existia; as consultas devolvem `Optional<T>`; atualizar e excluir devolvem `boolean`.
- `inserirPessoa` ganhou o parâmetro `email`.
- As operações de circulação devolvem comprovantes (`ComprovanteDeReserva`, `ComprovanteDeEmprestimo`…) em vez das entidades (R4).
- `iMulta.emitirMulta` recebe as datas de vencimento e de devolução, e calcula os dias de atraso por dentro.

As classes que realizam essas interfaces (`TratadorDeAcervo`, `TratadorDePessoa`, `TratadorDeEstudante`, `TratadorDeAtendente`, `TratadorDeEmprestimo`, `TratadorDeDevolucao`, `TratadorDeMulta`) devem ter as mesmas operações.

## 4. Diagramas de classes dos 10 padrões

Dentro do pacote `GoF`, crie um *Class Diagram* para cada padrão, com o nome indicado. Em cada um: crie ou arraste as classes, ponha o papel como estereótipo e desenhe as relações listadas. Classes marcadas com ♻ já existem no modelo; arraste-as da árvore.

### R01 State — R1 — Ciclo de vida do livro (State)

| Classe | Tipo | Papel (estereótipo) | Atributos | Operações |
|---|---|---|---|---|
| `TratadorDeEmprestimo` ♻ | classe | Client | — | `solicitarReserva(idLivro, matricula)`<br>`renovarEmprestimo(idLivro, matricula)` |
| `FachadaDeCirculacao` | classe | Client | — | `solicitarReserva(...)`<br>`efetivarEmprestimo(...)`<br>`renovarEmprestimo(...)`<br>`registrarDevolucao(...)`<br>`inspecionar(idLivro, bomEstado)`<br>`avaliarReparo(idLivro, reparavel)` |
| `Livro` ♻ | classe | Context | `- idLivro : int`<br>`- titulo : String`<br>`- estado : EstadoLivro` | `+ solicitarReserva(estudanteCadastrado)`<br>`+ efetivarEmprestimo(reservaDoMesmoEstudante)`<br>`+ renovarEmprestimo(emprestimo)`<br>`+ registrarDevolucao()`<br>`+ inspecionar(bomEstado)`<br>`+ enviarParaReparo()`<br>`+ retornarDoReparo()`<br>`+ avaliarReparo(reparavel)`<br>`+ excluirLivro()`<br>`~ mudarPara(estado)`<br>`+ reconstituir(id, titulo, codigo) : Livro`<br>`+ getEstado() : CodigoEstadoLivro` |
| `EstadoLivro` | classe abstrata | State | — | `+ codigo() : CodigoEstadoLivro`<br>`+ solicitarReserva(livro, ok)`<br>`+ efetivarEmprestimo(livro, ok)`<br>`+ renovarEmprestimo(livro, emp)`<br>`+ registrarDevolucao(livro)`<br>`+ inspecionar(livro, bomEstado)`<br>`+ enviarParaReparo(livro)`<br>`+ retornarDoReparo(livro)`<br>`+ avaliarReparo(livro, reparavel)`<br>`+ excluirLivro(livro)`<br>`# recusar(evento)`<br>`+ de(codigo) : EstadoLivro` |
| `Livre` | classe | ConcreteState | — | `solicitarReserva`<br>`enviarParaReparo`<br>`excluirLivro` |
| `Reservado` | classe | ConcreteState | — | `efetivarEmprestimo` |
| `Emprestado` | classe | ConcreteState | — | `renovarEmprestimo [numRenovacoes = 0]`<br>`registrarDevolucao` |
| `Renovado` | classe | ConcreteState | — | `entry(emprestimo)`<br>`renovarEmprestimo [numRenovacoes < 2]`<br>`registrarDevolucao` |
| `Devolvido` | classe | ConcreteState | — | `inspecionar` |
| `Reparando` | classe | ConcreteState | — | `retornarDoReparo`<br>`avaliarReparo` |
| `Incinerado` | classe | ConcreteState | — | `entry(livro)`<br>`excluirLivro` |
| `CodigoEstadoLivro` | enumeração | — | valores: LIVRE, RESERVADO, EMPRESTADO, RENOVADO, DEVOLVIDO, REPARANDO, INCINERADO | — |
| `Emprestimo` ♻ | classe | — | `- dataVencimento : LocalDate`<br>`- numRenovacoes : int` | `+ estenderVencimento(dias)` |

Relações:
- `TratadorDeEmprestimo` → `FachadaDeCirculacao`: Dependência (tracejada, seta aberta)
- `FachadaDeCirculacao` → `Livro`: Dependência (tracejada, seta aberta)
- `Livro` → `EstadoLivro`: Associação (linha cheia, navegável) — rótulo/multiplicidade: *estado 1*
- `Livre` → `EstadoLivro`: Generalização (seta de herança, triângulo vazado)
- `Reservado` → `EstadoLivro`: Generalização (seta de herança, triângulo vazado)
- `Emprestado` → `EstadoLivro`: Generalização (seta de herança, triângulo vazado)
- `Renovado` → `EstadoLivro`: Generalização (seta de herança, triângulo vazado)
- `Devolvido` → `EstadoLivro`: Generalização (seta de herança, triângulo vazado)
- `Reparando` → `EstadoLivro`: Generalização (seta de herança, triângulo vazado)
- `Incinerado` → `EstadoLivro`: Generalização (seta de herança, triângulo vazado)
- `EstadoLivro` → `CodigoEstadoLivro`: Dependência (tracejada, seta aberta) — rótulo/multiplicidade: *persistência*
- `Renovado` → `Emprestimo`: Dependência (tracejada, seta aberta) — rótulo/multiplicidade: *estenderVencimento(30)*

### R02 Strategy — R2 — Políticas de multa (Strategy)

| Classe | Tipo | Papel (estereótipo) | Atributos | Operações |
|---|---|---|---|---|
| `OuvinteDeMulta` | classe | Client | — | `+ aoDevolver(evento)` |
| `TratadorDeMulta` ♻ | classe | Context | `- configuracao`<br>`- cobranca : ContratoDeCobranca`<br>`- multas : RepositorioDeMultas` | `+ emitirMulta(idLivro, matricula, vencimento, devolucao) : Optional<Boleto>`<br>`+ diasDeAtraso(vencimento, devolucao) : long` |
| `ConfiguracaoAdministrativa` | classe | — | `- vigente : PoliticaDeMulta` | `+ registrar(politica)`<br>`+ definirPoliticaVigente(nome)`<br>`+ politicaVigente() : PoliticaDeMulta` |
| `PoliticaDeMulta` | interface | Strategy | — | `+ nome() : String`<br>`+ calcular(diasAtraso) : BigDecimal` |
| `PoliticaOriginal` | classe | ConcreteStrategy | — | `R$ 1,00 × dias` |
| `PoliticaComTeto` | classe | ConcreteStrategy | `TETO = 20.00` | `min(dias × 1,00 ; 20,00)` |
| `PoliticaComTolerancia` | classe | ConcreteStrategy | `DIAS_TOLERANCIA = 2` | `max(0, dias − 2) × 1,00` |
| `Multa` ♻ | classe | — | `- valor : BigDecimal`<br>`- diasAtraso : long`<br>`- politicaAplicada : String`<br>`- dataEmissao : LocalDate`<br>`- quitada : boolean` | `+ quitar(data) : boolean` |
| `TratadorDeAdministracao` | classe | — | — | `+ definirPoliticaDeMulta(nome)` |

Relações:
- `OuvinteDeMulta` → `TratadorDeMulta`: Dependência (tracejada, seta aberta) — rótulo/multiplicidade: *iMulta*
- `TratadorDeMulta` → `ConfiguracaoAdministrativa`: Associação (linha cheia, navegável)
- `ConfiguracaoAdministrativa` → `PoliticaDeMulta`: Associação (linha cheia, navegável) — rótulo/multiplicidade: *vigente*
- `PoliticaOriginal` → `PoliticaDeMulta`: Realização (tracejada, triângulo vazado)
- `PoliticaComTeto` → `PoliticaDeMulta`: Realização (tracejada, triângulo vazado)
- `PoliticaComTolerancia` → `PoliticaDeMulta`: Realização (tracejada, triângulo vazado)
- `TratadorDeMulta` → `Multa`: Associação (linha cheia, navegável) — rótulo/multiplicidade: *cria; grava via RepositorioDeMultas*
- `TratadorDeAdministracao` → `ConfiguracaoAdministrativa`: Dependência (tracejada, seta aberta)

### R03 Adapter — R3 — Bancos incompatíveis (Adapter)

| Classe | Tipo | Papel (estereótipo) | Atributos | Operações |
|---|---|---|---|---|
| `TratadorDeMulta` ♻ | classe | Client | — | `+ emitirMulta(...)`<br>`+ registrarQuitacao(notificacao) : boolean` |
| `ContratoDeCobranca` | interface | Target | — | `+ registrarCobranca(SolicitacaoDeCobranca) : Boleto`<br>`+ aoReceberPagamento(Consumer<NotificacaoDePagamento>)`<br>`+ banco() : String` |
| `AdaptadorBancoA` | classe | Adapter | `- documentoPorNossoNumero : Map` | `+ registrarCobranca(s) : reais→centavos, CPF→dígitos, data→AAAAMMDD`<br>`- traduzir(AvisoDeLiquidacao)` |
| `AdaptadorBancoB` | classe | Adapter | `- documentoPorIdBoleto : Map` | `+ registrarCobranca(s) : 7.00→"7,00", CPF formatado, data ISO`<br>`- traduzir(EventoDePagamento)` |
| `BancoA` | classe | Adaptee | — | `+ incluirTitulo(centavos, documento, AAAAMMDD) : nossoNumero`<br>`+ cadastrarWebhook(Consumer<AvisoDeLiquidacao>)` |
| `BancoB` | classe | Adaptee | — | `+ emitirBoleto(PedidoDeBoleto) : RespostaDeBoleto`<br>`+ assinarEventos(Consumer<EventoDePagamento>)` |
| `SolicitacaoDeCobranca` | classe «record» | — | `numeroDocumento, valor : BigDecimal,`<br>`cpfPagador, nomePagador, vencimento` | — |
| `NotificacaoDePagamento` | classe «record» | — | `numeroDocumento, valorPago,`<br>`situacao : SituacaoDoPagamento, dataPagamento` | — |
| `Sistema` | classe | raiz de composição | — | `escolhe o adaptador pela opção 'banco'` |

Relações:
- `TratadorDeMulta` → `ContratoDeCobranca`: Associação (linha cheia, navegável) — rótulo/multiplicidade: *cobranca*
- `AdaptadorBancoA` → `ContratoDeCobranca`: Realização (tracejada, triângulo vazado)
- `AdaptadorBancoB` → `ContratoDeCobranca`: Realização (tracejada, triângulo vazado)
- `AdaptadorBancoA` → `BancoA`: Associação (linha cheia, navegável)
- `AdaptadorBancoB` → `BancoB`: Associação (linha cheia, navegável)
- `ContratoDeCobranca` → `SolicitacaoDeCobranca`: Dependência (tracejada, seta aberta)
- `ContratoDeCobranca` → `NotificacaoDePagamento`: Dependência (tracejada, seta aberta)
- `Sistema` → `AdaptadorBancoA`: Dependência (tracejada, seta aberta) — rótulo/multiplicidade: *cria*
- `Sistema` → `AdaptadorBancoB`: Dependência (tracejada, seta aberta) — rótulo/multiplicidade: *cria*

### R04 Facade — R4 — Entrada única de circulação (Facade)

| Classe | Tipo | Papel (estereótipo) | Atributos | Operações |
|---|---|---|---|---|
| `Main (CLI)` | classe | interface | — | — |
| `TratadorDeEmprestimo` ♻ | classe | Client | — | `f1–f4` |
| `TratadorDeDevolucao` ♻ | classe | Client | — | `f5, f6, inspeção` |
| `FachadaDeCirculacao` | classe | Facade | `- uow : UnidadeDeTrabalho` | `+ solicitarReserva(idLivro, matricula) : ComprovanteDeReserva`<br>`+ consultarReserva(idLivro, matricula)`<br>`+ efetivarEmprestimo(idLivro, matricula) : ComprovanteDeEmprestimo`<br>`+ renovarEmprestimo(idLivro, matricula) : ComprovanteDeRenovacao`<br>`+ registrarDevolucao(idLivro, matricula, idAtendente) : ComprovanteDeDevolucao`<br>`+ inspecionar / enviarParaReparo / retornarDoReparo / avaliarReparo` |
| `Relacionamento` ♻ | classe | subsistema | — | — |
| `Movimentacao` ♻ | classe | subsistema | — | — |
| `Acervo` ♻ | classe | subsistema | — | — |
| `Livro` ♻ | classe | subsistema | — | — |
| `UnidadeDeTrabalho` | interface | — | — | `+ executar(operacao)` |
| `PublicadorDeDevolucao` | classe | — | — | `+ publicar(evento)` |
| `ComprovanteDeEmprestimo` | classe «record» | — | `idEmprestimo, idLivro, tituloLivro,`<br>`matricula, data, dataVencimento` | — |

Relações:
- `Main (CLI)` → `TratadorDeEmprestimo`: Dependência (tracejada, seta aberta)
- `Main (CLI)` → `TratadorDeDevolucao`: Dependência (tracejada, seta aberta)
- `TratadorDeEmprestimo` → `FachadaDeCirculacao`: Dependência (tracejada, seta aberta)
- `TratadorDeDevolucao` → `FachadaDeCirculacao`: Dependência (tracejada, seta aberta)
- `FachadaDeCirculacao` → `Relacionamento`: Associação (linha cheia, navegável)
- `FachadaDeCirculacao` → `Movimentacao`: Associação (linha cheia, navegável)
- `FachadaDeCirculacao` → `Acervo`: Associação (linha cheia, navegável)
- `FachadaDeCirculacao` → `Livro`: Dependência (tracejada, seta aberta)
- `FachadaDeCirculacao` → `UnidadeDeTrabalho`: Associação (linha cheia, navegável)
- `FachadaDeCirculacao` → `PublicadorDeDevolucao`: Associação (linha cheia, navegável)
- `FachadaDeCirculacao` → `ComprovanteDeEmprestimo`: Dependência (tracejada, seta aberta) — rótulo/multiplicidade: *devolve*

### R05 Observer — R5 — Reações independentes à devolução (Observer)

| Classe | Tipo | Papel (estereótipo) | Atributos | Operações |
|---|---|---|---|---|
| `FachadaDeCirculacao` | classe | — | — | `+ registrarDevolucao(...) — publica após o commit` |
| `PublicadorDeDevolucao` | classe | Subject | `- ouvintes : List<OuvinteDeDevolucao>` | `+ registrar(ouvinte)`<br>`+ remover(ouvinte)`<br>`+ publicar(evento) : ResultadoDaPublicacao` |
| `OuvinteDeDevolucao` | interface | Observer | — | `+ nome() : String`<br>`+ aoDevolver(EventoDevolucao)` |
| `EventoDevolucao` | classe «record» | — | `idDevolucao, idLivro, tituloLivro, matricula,`<br>`emailEstudante, idAtendente,`<br>`dataVencimento, dataDevolucao` | `+ diasAtraso() : long`<br>`+ emAtraso() : boolean` |
| `OuvinteDeInspecao` | classe | ConcreteObserver | — | — |
| `OuvinteDeMulta` | classe | ConcreteObserver | — | — |
| `OuvinteDeIndicadores` | classe | ConcreteObserver | — | — |
| `OuvinteDeEmail` | classe | ConcreteObserver | — | — |
| `FilaDeInspecao` | classe | — | — | — |
| `iMulta` ♻ | interface | — | — | — |
| `IndicadoresDeCirculacao` | classe | — | — | — |
| `ServicoDeEmail` | interface | — | — | — |

Relações:
- `FachadaDeCirculacao` → `PublicadorDeDevolucao`: Associação (linha cheia, navegável)
- `PublicadorDeDevolucao` → `OuvinteDeDevolucao`: Associação (linha cheia, navegável) — rótulo/multiplicidade: *0..**
- `PublicadorDeDevolucao` → `EventoDevolucao`: Dependência (tracejada, seta aberta)
- `OuvinteDeInspecao` → `OuvinteDeDevolucao`: Realização (tracejada, triângulo vazado)
- `OuvinteDeMulta` → `OuvinteDeDevolucao`: Realização (tracejada, triângulo vazado)
- `OuvinteDeIndicadores` → `OuvinteDeDevolucao`: Realização (tracejada, triângulo vazado)
- `OuvinteDeEmail` → `OuvinteDeDevolucao`: Realização (tracejada, triângulo vazado)
- `OuvinteDeInspecao` → `FilaDeInspecao`: Associação (linha cheia, navegável)
- `OuvinteDeMulta` → `iMulta`: Associação (linha cheia, navegável)
- `OuvinteDeIndicadores` → `IndicadoresDeCirculacao`: Associação (linha cheia, navegável)
- `OuvinteDeEmail` → `ServicoDeEmail`: Associação (linha cheia, navegável)

### R06 Command — R6 — Solicitações de circulação como objetos (Command)

| Classe | Tipo | Papel (estereótipo) | Atributos | Operações |
|---|---|---|---|---|
| `Main (CLI)` | classe | Client (tela) | — | `cria a solicitação com id próprio` |
| `TratadorDeEmprestimo` ♻ | classe | — | — | `+ submeter(solicitacao) : R`<br>`+ solicitarReserva(idLivro, matricula)`<br>`+ renovarEmprestimo(idLivro, matricula)` |
| `ExecutorDeSolicitacoes` | classe | Invoker | `- executadas : ConcurrentHashMap<String, FutureTask>` | `+ executar(solicitacao) : R`<br>`+ jaExecutada(id) : boolean` |
| `SolicitacaoDeCirculacao<R>` | classe abstrata | Command | `- id : String`<br>`- autor : String`<br>`- criadaEm : Instant` | `+ executar(receptor) : R` |
| `SolicitacaoReserva` | classe | ConcreteCommand | `- idLivro`<br>`- matricula` | `+ executar(receptor) : ComprovanteDeReserva` |
| `SolicitacaoRenovacao` | classe | ConcreteCommand | `- idLivro`<br>`- matricula` | `+ executar(receptor) : ComprovanteDeRenovacao` |
| `FachadaDeCirculacao` | classe | Receiver | — | `+ solicitarReserva(...)`<br>`+ renovarEmprestimo(...)` |

Relações:
- `Main (CLI)` → `SolicitacaoRenovacao`: Dependência (tracejada, seta aberta) — rótulo/multiplicidade: *cria*
- `Main (CLI)` → `TratadorDeEmprestimo`: Dependência (tracejada, seta aberta) — rótulo/multiplicidade: *submeter*
- `TratadorDeEmprestimo` → `ExecutorDeSolicitacoes`: Associação (linha cheia, navegável)
- `ExecutorDeSolicitacoes` → `SolicitacaoDeCirculacao<R>`: Dependência (tracejada, seta aberta)
- `SolicitacaoReserva` → `SolicitacaoDeCirculacao<R>`: Generalização (seta de herança, triângulo vazado)
- `SolicitacaoRenovacao` → `SolicitacaoDeCirculacao<R>`: Generalização (seta de herança, triângulo vazado)
- `ExecutorDeSolicitacoes` → `FachadaDeCirculacao`: Associação (linha cheia, navegável) — rótulo/multiplicidade: *receptor*
- `SolicitacaoDeCirculacao<R>` → `FachadaDeCirculacao`: Dependência (tracejada, seta aberta)

### R07 TemplateMethod — R7 — Rotina comum para cadastros (Template Method)

| Classe | Tipo | Papel (estereótipo) | Atributos | Operações |
|---|---|---|---|---|
| `RotinaDeCadastro<D,T>` | classe abstrata | AbstractClass | `- uow : UnidadeDeTrabalho` | `+ incluir(dados) : ResultadoDeCadastro<T>  {final}`<br>`# buscarExistente(dados) : Optional<T>  {abstract}`<br>`# validar(dados)  {abstract}`<br>`# verificarPreRequisitos(dados)  «gancho»`<br>`# criar(dados) : T  {abstract}`<br>`# armazenar(entidade)  {abstract}`<br>`# apresentar(entidade, criada)  «gancho»`<br>`# descrever(entidade)  «gancho»` |
| `CadastroDeLivro` | classe | ConcreteClass | — | `buscarExistente, validar, criar, armazenar` |
| `CadastroDePessoa` | classe | ConcreteClass | — | `buscarExistente, validar, criar, armazenar` |
| `CadastroDeEstudante` | classe | ConcreteClass | — | `+ verificarPreRequisitos: Pessoa cadastrada` |
| `CadastroDeAtendente` | classe | ConcreteClass | — | `+ verificarPreRequisitos: Pessoa cadastrada` |
| `TratadorDeAcervo` ♻ | classe | — | — | — |
| `TratadorDePessoa` ♻ | classe | — | — | — |
| `TratadorDeEstudante` ♻ | classe | — | — | — |
| `TratadorDeAtendente` ♻ | classe | — | — | — |
| `iLivro` ♻ | interface | — | — | — |
| `iPessoa` ♻ | interface | — | — | — |
| `iEstudante` ♻ | interface | — | — | — |
| `iAtendente` ♻ | interface | — | — | — |

Relações:
- `CadastroDeLivro` → `RotinaDeCadastro<D,T>`: Generalização (seta de herança, triângulo vazado)
- `CadastroDePessoa` → `RotinaDeCadastro<D,T>`: Generalização (seta de herança, triângulo vazado)
- `CadastroDeEstudante` → `RotinaDeCadastro<D,T>`: Generalização (seta de herança, triângulo vazado)
- `CadastroDeAtendente` → `RotinaDeCadastro<D,T>`: Generalização (seta de herança, triângulo vazado)
- `TratadorDeAcervo` → `CadastroDeLivro`: Associação (linha cheia, navegável)
- `TratadorDePessoa` → `CadastroDePessoa`: Associação (linha cheia, navegável)
- `TratadorDeEstudante` → `CadastroDeEstudante`: Associação (linha cheia, navegável)
- `TratadorDeAtendente` → `CadastroDeAtendente`: Associação (linha cheia, navegável)
- `TratadorDeAcervo` → `iLivro`: Realização (tracejada, triângulo vazado)
- `TratadorDePessoa` → `iPessoa`: Realização (tracejada, triângulo vazado)
- `TratadorDeEstudante` → `iEstudante`: Realização (tracejada, triângulo vazado)
- `TratadorDeAtendente` → `iAtendente`: Realização (tracejada, triângulo vazado)

### R08 AbstractFactory — R8 — Famílias de acesso aos dados (Abstract Factory)

| Classe | Tipo | Papel (estereótipo) | Atributos | Operações |
|---|---|---|---|---|
| `Sistema` | classe | Client | — | `fabrica = SelecaoDeFabrica.para(perfil, mysql)` |
| `SelecaoDeFabrica` | classe | — | — | `+ para(Perfil, ConfiguracaoMySQL) : FabricaDeAcessoADados` |
| `FabricaDeAcessoADados` | interface | AbstractFactory | — | `+ unidadeDeTrabalho() : UnidadeDeTrabalho`<br>`+ criarRepositorioDeLivros()`<br>`+ criarRepositorioDeMovimentacao()`<br>`+ criarRepositorioDeRelacionamento()`<br>`+ criarRepositorioDeMultas()` |
| `FabricaEmMemoria` | classe | ConcreteFactory | — | — |
| `FabricaMySQL` | classe | ConcreteFactory | — | — |
| `UnidadeDeTrabalho` | interface | AbstractProduct | — | `+ executar(operacao)` |
| `RepositorioDeLivros` | interface | AbstractProduct | — | — |
| `RepositorioDeMovimentacao` | interface | AbstractProduct | — | — |
| `RepositorioDeRelacionamento` | interface | AbstractProduct | — | — |
| `RepositorioDeMultas` | interface | AbstractProduct | — | `+ salvar(multa, boleto)`<br>`+ quitar(numero, data) : boolean` |
| `UnidadeDeTrabalhoEmMemoria` | classe | Product | — | `log de desfazer` |
| `Repositorio*EmMemoria` | classe | Product (×4) | — | `construtor(UnidadeDeTrabalhoEmMemoria)` |
| `UnidadeDeTrabalhoMySQL` | classe | Product | — | `Connection por transação` |
| `Repositorio*MySQL` | classe | Product (×4) | — | `construtor(UnidadeDeTrabalhoMySQL)` |

Relações:
- `Sistema` → `SelecaoDeFabrica`: Dependência (tracejada, seta aberta)
- `Sistema` → `FabricaDeAcessoADados`: Dependência (tracejada, seta aberta)
- `FabricaEmMemoria` → `FabricaDeAcessoADados`: Realização (tracejada, triângulo vazado)
- `FabricaMySQL` → `FabricaDeAcessoADados`: Realização (tracejada, triângulo vazado)
- `UnidadeDeTrabalhoEmMemoria` → `UnidadeDeTrabalho`: Realização (tracejada, triângulo vazado)
- `UnidadeDeTrabalhoMySQL` → `UnidadeDeTrabalho`: Realização (tracejada, triângulo vazado)
- `Repositorio*EmMemoria` → `RepositorioDeLivros`: Realização (tracejada, triângulo vazado)
- `Repositorio*MySQL` → `RepositorioDeLivros`: Realização (tracejada, triângulo vazado)
- `Repositorio*EmMemoria` → `RepositorioDeMovimentacao`: Realização (tracejada, triângulo vazado)
- `Repositorio*MySQL` → `RepositorioDeMovimentacao`: Realização (tracejada, triângulo vazado)
- `Repositorio*EmMemoria` → `RepositorioDeRelacionamento`: Realização (tracejada, triângulo vazado)
- `Repositorio*MySQL` → `RepositorioDeRelacionamento`: Realização (tracejada, triângulo vazado)
- `Repositorio*EmMemoria` → `RepositorioDeMultas`: Realização (tracejada, triângulo vazado)
- `Repositorio*MySQL` → `RepositorioDeMultas`: Realização (tracejada, triângulo vazado)
- `FabricaEmMemoria` → `UnidadeDeTrabalhoEmMemoria`: Dependência (tracejada, seta aberta) — rótulo/multiplicidade: *cria*
- `FabricaEmMemoria` → `Repositorio*EmMemoria`: Dependência (tracejada, seta aberta) — rótulo/multiplicidade: *cria*
- `FabricaMySQL` → `UnidadeDeTrabalhoMySQL`: Dependência (tracejada, seta aberta) — rótulo/multiplicidade: *cria*
- `FabricaMySQL` → `Repositorio*MySQL`: Dependência (tracejada, seta aberta) — rótulo/multiplicidade: *cria*
- `Repositorio*EmMemoria` → `UnidadeDeTrabalhoEmMemoria`: Associação (linha cheia, navegável) — rótulo/multiplicidade: *mesma uow*
- `Repositorio*MySQL` → `UnidadeDeTrabalhoMySQL`: Associação (linha cheia, navegável) — rótulo/multiplicidade: *mesma uow*

### R09 Decorator — R9 — Consulta com recursos opcionais (Decorator)

| Classe | Tipo | Papel (estereótipo) | Atributos | Operações |
|---|---|---|---|---|
| `TratadorDeAcervo` ♻ | classe | Client | — | `+ consultarMetadados(idLivro)`<br>`+ buscarPorTitulo(trecho)` |
| `ConsultaAcervo` | interface | Component | — | `+ consultarMetadados(idLivro) : Optional<MetadadosLivro>`<br>`+ buscarPorTitulo(trecho) : List<MetadadosLivro>` |
| `ConsultaAcervoOriginal` | classe | ConcreteComponent | `- acervo : Acervo` | — |
| `DecoradorDeConsulta` | classe abstrata | Decorator | `# interna : ConsultaAcervo` | — |
| `ConsultaComMedicao` | classe | ConcreteDecorator | `- medidor : Medidor` | — |
| `ConsultaComCache` | classe | ConcreteDecorator | `- cache : ArmazenamentoDeCache` | `+ invalidar(idLivro)` |
| `MontadorDeConsulta` | classe | — | — | `+ montar(acervo, cache, medidor)  → Medicao(Cache(Original))` |
| `MetadadosLivro` | classe «record» | — | `idLivro, titulo  (sem estado)` | — |

Relações:
- `TratadorDeAcervo` → `ConsultaAcervo`: Associação (linha cheia, navegável)
- `ConsultaAcervoOriginal` → `ConsultaAcervo`: Realização (tracejada, triângulo vazado)
- `DecoradorDeConsulta` → `ConsultaAcervo`: Realização (tracejada, triângulo vazado)
- `DecoradorDeConsulta` → `ConsultaAcervo`: Associação (linha cheia, navegável) — rótulo/multiplicidade: *interna 1*
- `ConsultaComMedicao` → `DecoradorDeConsulta`: Generalização (seta de herança, triângulo vazado)
- `ConsultaComCache` → `DecoradorDeConsulta`: Generalização (seta de herança, triângulo vazado)
- `MontadorDeConsulta` → `ConsultaAcervo`: Dependência (tracejada, seta aberta) — rótulo/multiplicidade: *monta*
- `ConsultaAcervo` → `MetadadosLivro`: Dependência (tracejada, seta aberta)

### R10 Composite — R10 — Organização hierárquica do acervo (Composite)

| Classe | Tipo | Papel (estereótipo) | Atributos | Operações |
|---|---|---|---|---|
| `OrganizacaoDoAcervo` | classe | Client | — | `+ moverLivro(idLivro, destino)` |
| `ComponenteAcervo` | classe abstrata | Component | `- pai : GrupoAcervo` | `+ nome() : String`<br>`+ contarLivros() : int`<br>`+ listarLivros() : List<Integer>`<br>`+ raiz() : GrupoAcervo` |
| `ItemAcervo` | classe | Leaf | `- idLivro : int` | `contarLivros() = 1` |
| `GrupoAcervo` | classe | Composite | `- nome`<br>`- tipo : TipoGrupo`<br>`- filhos : List<ComponenteAcervo>` | `+ adicionar(c)  recusa ciclo / livro em 2 grupos`<br>`+ remover(c)`<br>`+ localizarItem(idLivro)` |
| `TipoGrupo` | enumeração | — | valores: UNIDADE, SECAO, SUBSECAO | — |

Relações:
- `OrganizacaoDoAcervo` → `GrupoAcervo`: Associação (linha cheia, navegável) — rótulo/multiplicidade: *raiz*
- `ItemAcervo` → `ComponenteAcervo`: Generalização (seta de herança, triângulo vazado)
- `GrupoAcervo` → `ComponenteAcervo`: Generalização (seta de herança, triângulo vazado)
- `GrupoAcervo` → `ComponenteAcervo`: Agregação (losango vazado no lado do todo) — rótulo/multiplicidade: *filhos 0..**
- `GrupoAcervo` → `TipoGrupo`: Dependência (tracejada, seta aberta)

Detalhes importantes:
- **R1:** em `EstadoLivro`, a classe e as operações que recusam o evento são normais (não abstratas); só `codigo()` é abstrata. Cada estado concreto sobrescreve apenas as operações que tem transição saindo dele.
- **R7:** marque `incluir()` como **leaf/final** (propriedades da operação → *Leaf*), porque o método-modelo não pode ser sobrescrito. Marque as operações `{abstract}` como abstratas.
- **R8:** `Repositorio*EmMemoria` e `Repositorio*MySQL` representam 4 classes cada (Livros, Movimentacao, Relacionamento, Multas). Pode desenhar as 8 ou deixar as duas caixas com uma nota explicando. A família é escolhida só em `SelecaoDeFabrica`.
- **R9:** a ordem da cadeia é `ConsultaComMedicao(ConsultaComCache(ConsultaAcervoOriginal))`. Vale pôr isso numa nota.

## 5. Diagramas de sequência dos cenários

Crie um *Sequence Diagram* por cenário (dentro de `GoF`). As linhas de vida são as classes abaixo; as mensagens seguem a ordem numerada. Linhas "↩" são retornos (seta tracejada); "nota" vira um *Note* ou *Combined Fragment*. Na mensagem, use a operação da classe destino, para que o Astah ligue a seta ao método.

### S01_terceiraRenovacaoRecusada — R1 — Terceira renovação recusada (State)

Linhas de vida: `Estudante`, `TratadorDeEmprestimo`, `FachadaDeCirculacao`, `Movimentacao`, `Livro`, `Renovado`, `Emprestimo`

1. `Estudante` → `TratadorDeEmprestimo`: `renovarEmprestimo(idLivro, matricula)`
2. `TratadorDeEmprestimo` → `FachadaDeCirculacao`: `renovarEmprestimo(idLivro, matricula)  [via executor]`
3. `FachadaDeCirculacao` → `Movimentacao`: `consultarEmprestimo(idLivro, matricula)`
4. ↩ `Movimentacao` → `FachadaDeCirculacao`: emprestimo (numRenovacoes = 2)
5. `FachadaDeCirculacao` → `Livro`: `renovarEmprestimo(emprestimo)`
6. `Livro` → `Renovado`: `renovarEmprestimo(livro, emprestimo)`
7. `Renovado` → `Emprestimo`: `getNumRenovacoes()`
8. ↩ `Emprestimo` → `Renovado`: 2
- *nota:* guarda [numRenovacoes < 2] falsa: nada foi alterado
9. ↩ `Renovado` → `FachadaDeCirculacao`: TransicaoInvalidaException
- *nota:* UnidadeDeTrabalho não confirma nada; vencimento continua igual
10. ↩ `FachadaDeCirculacao` → `Estudante`: Recusado: limite de 2 renovações

### S02_multaPorPolitica — R2 — Cálculo da multa pela política vigente (Strategy)

Linhas de vida: `OuvinteDeMulta`, `TratadorDeMulta`, `ConfiguracaoAdministrativa`, `PoliticaComTolerancia`, `ContratoDeCobranca`

1. `OuvinteDeMulta` → `TratadorDeMulta`: `emitirMulta(idLivro, matricula, vencimento, devolucao)`
2. `TratadorDeMulta` → `TratadorDeMulta`: `diasDeAtraso() = 5`
3. `TratadorDeMulta` → `ConfiguracaoAdministrativa`: `politicaVigente()`
4. ↩ `ConfiguracaoAdministrativa` → `TratadorDeMulta`: PoliticaComTolerancia
5. `TratadorDeMulta` → `PoliticaComTolerancia`: `calcular(5)`
6. ↩ `PoliticaComTolerancia` → `TratadorDeMulta`: 3.00
7. `TratadorDeMulta` → `TratadorDeMulta`: `new Multa(valor=3.00, politicaAplicada=COM_TOLERANCIA)`
8. `TratadorDeMulta` → `ContratoDeCobranca`: `registrarCobranca(solicitacao)`
9. ↩ `ContratoDeCobranca` → `TratadorDeMulta`: Boleto

### S03_seteReaisNosBancos — R3 — R$ 7,00 convertido por banco (Adapter)

Linhas de vida: `TratadorDeMulta`, `AdaptadorBancoA`, `BancoA`, `AdaptadorBancoB`, `BancoB`

- *nota:* Instalação com banco A
1. `TratadorDeMulta` → `AdaptadorBancoA`: `registrarCobranca(valor = 7.00, cpf)`
2. `AdaptadorBancoA` → `BancoA`: `incluirTitulo(700, "11144477735", 20261203)`
3. ↩ `BancoA` → `AdaptadorBancoA`: nossoNumero
4. ↩ `AdaptadorBancoA` → `TratadorDeMulta`: Boleto(valor = 7.00)
- *nota:* Instalação com banco B — mesmo código no TratadorDeMulta
5. `TratadorDeMulta` → `AdaptadorBancoB`: `registrarCobranca(valor = 7.00, cpf)`
6. `AdaptadorBancoB` → `BancoB`: `emitirBoleto(valor = "7,00", "111.444.777-35")`
7. ↩ `BancoB` → `AdaptadorBancoB`: idBoleto
8. ↩ `AdaptadorBancoB` → `TratadorDeMulta`: Boleto(valor = 7.00)
9. `BancoA` → `AdaptadorBancoA`: `webhook(AvisoDeLiquidacao) ×2`
10. `AdaptadorBancoA` → `TratadorDeMulta`: `registrarQuitacao(NotificacaoDePagamento)`
- *nota:* 2ª notificação: RepositorioDeMultas.quitar() — UPDATE ... WHERE quitada = FALSE — devolve false

### S04_efetivarEmprestimo — R4 — Efetivar empréstimo pela fachada (Facade)

Linhas de vida: `Estudante`, `TratadorDeEmprestimo`, `FachadaDeCirculacao`, `UnidadeDeTrabalho`, `Acervo`, `Movimentacao`, `Livro`

1. `Estudante` → `TratadorDeEmprestimo`: `efetivarEmprestimo(idLivro, matricula)`
2. `TratadorDeEmprestimo` → `FachadaDeCirculacao`: `efetivarEmprestimo(idLivro, matricula)`
3. `FachadaDeCirculacao` → `UnidadeDeTrabalho`: `executar(operacao)`
4. `FachadaDeCirculacao` → `Acervo`: `consultarLivro(idLivro)`
5. `FachadaDeCirculacao` → `Movimentacao`: `consultarReservaAtiva(idLivro)`
6. `FachadaDeCirculacao` → `Livro`: `efetivarEmprestimo(reservaDoMesmoEstudante)`
- *nota:* Reservado → Emprestado
7. `FachadaDeCirculacao` → `Movimentacao`: `remover(reserva)`
8. `FachadaDeCirculacao` → `Movimentacao`: `adicionar(emprestimo)`
9. `FachadaDeCirculacao` → `Acervo`: `atualizarLivro(livro)`
10. ↩ `UnidadeDeTrabalho` → `FachadaDeCirculacao`: commit (ou desfaz tudo se algum passo falhar)
11. ↩ `FachadaDeCirculacao` → `Estudante`: ComprovanteDeEmprestimo

### S05_devolucaoAtrasada — R5 — Devolução atrasada notifica ouvintes (Observer)

Linhas de vida: `Atendente`, `TratadorDeDevolucao`, `FachadaDeCirculacao`, `PublicadorDeDevolucao`, `OuvinteDeInspecao`, `OuvinteDeMulta`, `OuvinteDeIndicadores`, `OuvinteDeEmail`

1. `Atendente` → `TratadorDeDevolucao`: `registrarDevolucao(idLivro, matricula, idAtendente)`
2. `TratadorDeDevolucao` → `FachadaDeCirculacao`: `registrarDevolucao(...)`
- *nota:* transação: Livro → DEVOLVIDO, adicionar(devolucao), remover(emprestimo), commit
3. `FachadaDeCirculacao` → `PublicadorDeDevolucao`: `publicar(EventoDevolucao)`
4. `PublicadorDeDevolucao` → `OuvinteDeInspecao`: `aoDevolver(e)  → fila de inspeção`
5. `PublicadorDeDevolucao` → `OuvinteDeMulta`: `aoDevolver(e)  → emitirMulta (atraso)`
6. `PublicadorDeDevolucao` → `OuvinteDeIndicadores`: `aoDevolver(e)`
7. `PublicadorDeDevolucao` → `OuvinteDeEmail`: `aoDevolver(e)`
8. ↩ `OuvinteDeEmail` → `PublicadorDeDevolucao`: exceção capturada (os demais já rodaram)
9. ↩ `PublicadorDeDevolucao` → `FachadaDeCirculacao`: ResultadoDaPublicacao
10. ↩ `FachadaDeCirculacao` → `Atendente`: ComprovanteDeDevolucao (livro DEVOLVIDO)

### S06_renovacaoRepetida — R6 — Mesma renovação enviada duas vezes (Command)

Linhas de vida: `Tela`, `TratadorDeEmprestimo`, `ExecutorDeSolicitacoes`, `SolicitacaoRenovacao`, `FachadaDeCirculacao`

1. `Tela` → `SolicitacaoRenovacao`: `new(id = REQ-1, autor, criadaEm, idLivro, matricula)`
2. `Tela` → `TratadorDeEmprestimo`: `submeter(solicitacao)`
3. `TratadorDeEmprestimo` → `ExecutorDeSolicitacoes`: `executar(solicitacao)`
4. `ExecutorDeSolicitacoes` → `ExecutorDeSolicitacoes`: `putIfAbsent(REQ-1, tarefa) = null`
5. `ExecutorDeSolicitacoes` → `SolicitacaoRenovacao`: `executar(receptor)`
6. `SolicitacaoRenovacao` → `FachadaDeCirculacao`: `renovarEmprestimo(idLivro, matricula)`
7. ↩ `FachadaDeCirculacao` → `Tela`: ComprovanteDeRenovacao (+30 dias)
- *nota:* Clique repetido
8. `Tela` → `TratadorDeEmprestimo`: `submeter(solicitacao)`
9. `TratadorDeEmprestimo` → `ExecutorDeSolicitacoes`: `executar(solicitacao)`
10. `ExecutorDeSolicitacoes` → `ExecutorDeSolicitacoes`: `putIfAbsent(REQ-1, ...) = tarefa existente`
11. ↩ `ExecutorDeSolicitacoes` → `Tela`: mesmo comprovante (não executa de novo)

### S07_estudanteSemPessoa — R7 — Inclusão de estudante (Template Method)

Linhas de vida: `Administrador`, `TratadorDeEstudante`, `CadastroDeEstudante`, `Relacionamento`

1. `Administrador` → `TratadorDeEstudante`: `inserirEstudante(cpf, matricula)`
2. `TratadorDeEstudante` → `CadastroDeEstudante`: `incluir(dados)  {final}`
3. `CadastroDeEstudante` → `Relacionamento`: `consultarEstudante(matricula)  [buscarExistente]`
- *nota:* se existir: apresentar(existente, false) e encerra
4. `CadastroDeEstudante` → `CadastroDeEstudante`: `validar(dados)`
5. `CadastroDeEstudante` → `Relacionamento`: `consultarPessoa(cpf)  [verificarPreRequisitos]`
6. ↩ `Relacionamento` → `CadastroDeEstudante`: vazio
7. ↩ `CadastroDeEstudante` → `Administrador`: PreRequisitoNaoAtendidoException — criar/armazenar não rodam

### S08_familiaPorPerfil — R8 — Família escolhida pelo perfil (Abstract Factory)

Linhas de vida: `Main`, `Sistema`, `SelecaoDeFabrica`, `FabricaMySQL`, `UnidadeDeTrabalhoMySQL`, `RepositorioDeLivrosMySQL`

1. `Main` → `Sistema`: `new Sistema(opcoes{perfil = PRODUCAO})`
2. `Sistema` → `SelecaoDeFabrica`: `para(PRODUCAO, mysql)`
3. `SelecaoDeFabrica` → `FabricaMySQL`: `new FabricaMySQL(config)`
4. `FabricaMySQL` → `UnidadeDeTrabalhoMySQL`: `new (sem abrir conexão)`
5. `Sistema` → `FabricaMySQL`: `criarRepositorioDeLivros()`
6. `FabricaMySQL` → `RepositorioDeLivrosMySQL`: `new(uow)`
- *nota:* Movimentacao e Relacionamento recebem a MESMA uow

### S09_cacheNaoAutoriza — R9 — Cache não autoriza reserva (Decorator)

Linhas de vida: `Estudante`, `TratadorDeAcervo`, `ConsultaComMedicao`, `ConsultaComCache`, `TratadorDeEmprestimo`, `FachadaDeCirculacao`, `Acervo`

1. `Estudante` → `TratadorDeAcervo`: `consultarMetadados(id)`
2. `TratadorDeAcervo` → `ConsultaComMedicao`: `consultarMetadados(id)`
3. `ConsultaComMedicao` → `ConsultaComCache`: `consultarMetadados(id)  [mede]`
4. ↩ `ConsultaComCache` → `Estudante`: MetadadosLivro(id, titulo) do cache — sem estado
5. `Estudante` → `TratadorDeEmprestimo`: `solicitarReserva(id, matricula)`
6. `TratadorDeEmprestimo` → `FachadaDeCirculacao`: `solicitarReserva(...)`
7. `FachadaDeCirculacao` → `Acervo`: `consultarLivro(id)  — estado atual`
8. ↩ `Acervo` → `FachadaDeCirculacao`: Livro [EMPRESTADO]
9. ↩ `FachadaDeCirculacao` → `Estudante`: Recusado pelo estado

### S10_contarEMover — R10 — Contagem e movimentação na árvore (Composite)

Linhas de vida: `Cliente`, `unidade:GrupoAcervo`, `exatas:GrupoAcervo`, `humanas:GrupoAcervo`, `OrganizacaoDoAcervo`

1. `Cliente` → `unidade:GrupoAcervo`: `contarLivros()`
2. `unidade:GrupoAcervo` → `exatas:GrupoAcervo`: `contarLivros()`
3. ↩ `exatas:GrupoAcervo` → `unidade:GrupoAcervo`: 3 (soma de ItemAcervo)
4. `unidade:GrupoAcervo` → `humanas:GrupoAcervo`: `contarLivros()`
5. ↩ `humanas:GrupoAcervo` → `unidade:GrupoAcervo`: 2
6. ↩ `unidade:GrupoAcervo` → `Cliente`: 5
7. `Cliente` → `OrganizacaoDoAcervo`: `moverLivro(idLivro, humanas)`
8. `OrganizacaoDoAcervo` → `exatas:GrupoAcervo`: `remover(item)`
9. `OrganizacaoDoAcervo` → `humanas:GrupoAcervo`: `adicionar(item)`
- *nota:* Livro e Movimentacao não são tocados: o item guarda só o idLivro

## 6. Exportar em PDF e salvar

1. Salve o `Trabalho03_GoF.asta` em `modelos/astah/`.
2. Exporte cada diagrama: abra o diagrama → *File* → *Export Image* (PNG, para conferir) e, para o PDF, *File* → *Print* com a impressora **Microsoft Print to PDF**. Se a sua versão do Astah tiver exportação direta para PDF, pode usar.
3. Junte os PDFs num só, **`modelos/Modelos_Astah_Trabalho3.pdf`**, nesta ordem: máquina de estados, classes R01–R10, sequências S01–S10, e o diagrama de classes geral do Trabalho 02 já com as interfaces atualizadas. Para juntar, dá para usar o próprio *Print* selecionando vários diagramas, ou um site/programa de mesclar PDF.
4. Avise o resto da equipe para acrescentar no README, na seção *Modelos UML*, a linha: ``- `modelos/Modelos_Astah_Trabalho3.pdf`: modelos do Astah exportados (entrega oficial).``

## Conferência final

- [ ] Nenhuma classe duplicada na árvore *Structure* (procure por nomes repetidos).
- [ ] O enum se chama `CodigoEstadoLivro`; `EstadoLivro` é a classe abstrata com os 7 estados como subclasses.
- [ ] As setas que chegam em DEVOLVIDO dizem `registrarDevolucao / publicarEventoDevolucao`.
- [ ] As interfaces têm as assinaturas da seção 3.
- [ ] São 10 diagramas de classes e 10 de sequência, com os papéis como estereótipos.
- [ ] O PDF está em `modelos/` e abre normalmente.
