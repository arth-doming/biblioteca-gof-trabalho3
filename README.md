# Sistema de empréstimo de livros com padrões GoF — Trabalho 3

PUCPR · Modelagem de Sistemas Computacionais · Prof. Edson Emílio Scalabrin · 2º semestre de 2026

Equipe:
- Arthur Domingues
- Carlos Eduardo Aguiar Sacerdote
- Gustavo Fagundes de Amorim
- Rafael de Freitas Brandão

Implementação executável do sistema de reserva, empréstimo, renovação e devolução de livros dos Trabalhos 01 e 02, aplicando os dez padrões GoF da lista de problemas. Os nomes de classes, interfaces e operações seguem o modelo do Astah (tratadores, `Acervo`, `Relacionamento`, `Movimentacao`, `iLivro`/`iPessoa`/`iEstudante`/`iAtendente`, máquina de estados T02-1).

## Requisitos

- Java 17 ou superior
- Maven 3.8+
- MySQL 8 só para o perfil de produção (os testes não precisam de banco)

## Compilar e rodar os testes (um comando)

```bash
mvn test
```

São 41 testes em 10 classes, `R01StateTest` … `R10CompositeTest`, com nomes de método que identificam o requisito (`R01_terceiraRenovacaoRecusada`, `R06_renovacaoRepetidaAcrescentaTrintaDias` etc.). O teste `R08_integracaoMySQLReconstituiEstado` só roda quando o MySQL é habilitado (ver abaixo):

```bash
mvn test -Dmysql.integracao=true
```

## Executar a interface (linha de comando)

```bash
mvn -q compile exec:java                           # menu interativo, perfil teste (memória)
mvn -q compile exec:java -Dexec.args="--demo"      # fluxo completo da demonstração
mvn -q compile exec:java -Dbanco=B -Dpolitica=COM_TOLERANCIA -Dexec.args="--demo"
```

O `--demo` executa o fluxo pedido na demonstração: cadastros → reserva → empréstimo → renovação (inclusive a mesma solicitação enviada duas vezes) → terceira renovação recusada → devolução com 5 dias de atraso → multa e boleto → livro DEVOLVIDO não pode ser reservado → inspeção → nova reserva → consulta com cache e medição.

No menu interativo, a opção 15 adianta o relógio simulado para provocar atraso.

### Perfil de produção (MySQL)

A única configuração manual é o banco. Uma vez só, abra o MySQL Workbench conectado como root e rode [`docs/mysql-criar-usuario.sql`](docs/mysql-criar-usuario.sql). O script cria o usuário local `biblioteca` (senha `biblioteca`) com acesso aos bancos `biblioteca` e `biblioteca_teste`. Bancos e tabelas são criados automaticamente na primeira conexão (`src/main/resources/schema-mysql.sql`).

```bash
mvn -q compile exec:java -Dperfil=producao -Dexec.args="--demo"
# outro servidor/usuário: -Dmysql.url=jdbc:mysql://host:porta/base -Dmysql.usuario=... -Dmysql.senha=...
```

No Windows, se o Maven estiver fora do PATH, dá para usar o do IntelliJ: `& "C:\Program Files\JetBrains\IntelliJ IDEA <versão>\plugins\maven\lib\maven3\bin\mvn.cmd" ...`. No PowerShell, ponha cada `-D...` entre aspas simples e evite `&` dentro das URLs, porque o `mvn.cmd` repassa os argumentos ao cmd, que corta a linha no `&`.

O Oracle continua sendo réplica de leitura mantida fora da aplicação: nenhuma fábrica cria conexão de escrita para ele.

## Organização do código

```
br.pucpr.biblioteca
├── ui/            Main (CLI) — chama só os tratadores
├── tratadores/    TratadorDeEmprestimo, TratadorDeDevolucao, TratadorDeAcervo, TratadorDePessoa,
│                  TratadorDeEstudante, TratadorDeAtendente, TratadorDeAdministracao + interfaces iXxx
├── circulacao/    FachadaDeCirculacao + comprovantes                       (R4 Facade)
├── comando/       SolicitacaoDeCirculacao, ExecutorDeSolicitacoes           (R6 Command)
├── dominio/       Pessoa, Papel, Estudante, Atendente, Transacao, Reserva, Emprestimo, Devolucao
│   └── livro/     Livro + EstadoLivro e os 7 estados                       (R1 State)
├── acesso/        Acervo, Relacionamento, Movimentacao (pontos internos de acesso do Trabalho 02)
├── cadastro/      RotinaDeCadastro e Cadastro*                              (R7 Template Method)
├── dados/         FabricaDeAcessoADados, famílias memoria/ e mysql/        (R8 Abstract Factory)
├── devolucao/     PublicadorDeDevolucao e ouvintes                          (R5 Observer)
├── financeiro/    TratadorDeMulta (iMulta, iQuitacao)
│   ├── multa/     PoliticaDeMulta e políticas, Multa, Boleto               (R2 Strategy)
│   └── cobranca/  ContratoDeCobranca, AdaptadorBancoA/B, BancoA/B          (R3 Adapter)
├── consulta/      ConsultaAcervo e decoradores de cache/medição             (R9 Decorator)
├── organizacao/   ComponenteAcervo, GrupoAcervo, ItemAcervo                 (R10 Composite)
└── config/        Sistema (raiz de composição), RelogioSimulado
```

`config.Sistema` é o único lugar que conhece as classes concretas. É ali que se escolhe a família de dados pelo perfil, o banco da instalação, a política de multa inicial, os decoradores da consulta e os ouvintes da devolução.

A tabela requisito → padrão → classes → teste está em [`docs/RASTREABILIDADE.md`](docs/RASTREABILIDADE.md).

## Modelos UML

- `docs/uml/Modelos_UML_Trabalho3.pdf`: diagrama de classes de cada padrão, a máquina de estados do Livro (com o método que implementa cada transição) e um diagrama de sequência por cenário de verificação (S01 a S10).
- `docs/uml/puml/`: os mesmos diagramas em PlantUML, para editar no IntelliJ (plugin PlantUML) ou em plantuml.com.
- `docs/uml/modelos.py` + `gerar_uml.py`: fonte única dos diagramas; `python3 docs/uml/gerar_uml.py` regera os PDFs (usa Graphviz, `rsvg-convert` e `pypdf`).
- `docs/INSTRUCOES_ASTAH.md`: passo a passo para atualizar o Astah com os padrões.
- `modelos/astah/`: arquivos `.asta`. `Trabalho03_GoF.asta` é o modelo do Trabalho 3, com os diagramas de classes R01–R10, os diagramas de sequência S01–S10, a máquina de estados corrigida e a visão geral T03; os demais são dos Trabalhos 01/02.
- `modelos/Modelos_Astah_Trabalho3.pdf`: exportação do Astah (22 páginas); `Modelos_Astah_Trabalho3_A4.pdf` é a mesma exportação em A4.

## Decisões que valem para a arguição

**R1 State.** A guarda da renovação está no começo de `Emprestado.renovarEmprestimo` (`numRenovacoes != 0`) e de `Renovado.renovarEmprestimo` (`numRenovacoes >= 2`). Ela é avaliada antes de `estenderVencimento`, então a recusa não deixa o vencimento alterado. Um evento que o estado não aceita cai na implementação padrão de `EstadoLivro`, que lança `TransicaoInvalidaException` sem tocar no livro. A transação da fachada também não confirma nada. O estado é gravado como `CodigoEstadoLivro` (texto na coluna `estado`) e reconstituído por `Livro.reconstituir` → `EstadoLivro.de(codigo)`.

**R2 Strategy.** Uma política nova é só mais uma classe que implementa `PoliticaDeMulta`, registrada com `ConfiguracaoAdministrativa.registrar`. O `TratadorDeMulta` não muda (o teste `R02_novaPoliticaSemAlterarServico` mostra isso). Trocar a política não recalcula multas antigas porque `Multa` guarda `valor` e `politicaAplicada` no momento da emissão. Dias de atraso = `ChronoUnit.DAYS.between(vencimento, devolucao)`, nunca negativo. Valores em `BigDecimal` com 2 casas, sem `double`.

**R3 Adapter.** Cada adaptador concentra as conversões do seu banco: centavos, formato de CPF, formato de data, identificador e situação. O banco da instalação é escolhido em `Sistema` pela opção `banco`. Multas e boletos são gravados pelo `RepositorioDeMultas` da família de dados em uso. Uma notificação repetida é ignorada porque a quitação é atômica no repositório: no MySQL é `UPDATE multa SET quitada = TRUE WHERE numero = ? AND quitada = FALSE`, e a segunda notificação afeta 0 linhas.

**R4 Facade.** A fachada roda tudo dentro de `UnidadeDeTrabalho.executar`. Em `efetivarEmprestimo` a ordem é: `Acervo.consultarLivro` → `Movimentacao.consultarReservaAtiva` → `Livro.efetivarEmprestimo` → `Movimentacao.remover(reserva)` → `Movimentacao.adicionar(emprestimo)` → `Acervo.atualizarLivro`. Se um passo falhar, a unidade de trabalho desfaz todas as escritas: na família em memória, por um log de desfazer; no MySQL, por `rollback`. A interface não acessa `Movimentacao` porque só conhece os tratadores, e eles só conhecem a fachada.

**R5 Observer.** Para incluir um ouvinte novo, basta implementar `OuvinteDeDevolucao` e registrá-lo no publicador (em `Sistema`). O publicador roda cada ouvinte em `try/catch`: se o e-mail falhar, a falha aparece no comprovante e os outros ouvintes rodam normalmente. A publicação acontece depois do commit, então nada desfaz a devolução. O evento carrega vencimento, data e estudante porque, depois do commit, o empréstimo já foi removido da `Movimentacao`.

**R6 Command.** A tela cria a solicitação (com id próprio), o `ExecutorDeSolicitacoes` invoca e a fachada (receptor) executa, delegando ao Livro. A repetição é detectada por `ConcurrentHashMap.putIfAbsent`: em duas submissões simultâneas só uma registra a tarefa e a executa, e a outra espera o mesmo resultado. Uma recusa de regra de negócio fica registrada, mas uma falha técnica libera o identificador para nova tentativa. Não há desfazer porque reserva e renovação produzem efeitos de negócio que só outro evento do domínio reverte, e um "undo" de renovação contornaria a regra das duas renovações.

**R7 Template Method.** São fixos a ordem e o método `incluir` (`final`). Variam `buscarExistente`, `validar`, `criar` e `armazenar`. Os ganchos são `verificarPreRequisitos` (vazio por padrão, usado por Estudante e Atendente), `apresentar` e `descrever`. Como a verificação do pré-requisito vem antes de `criar`/`armazenar` e tudo roda numa unidade de trabalho, não sobra gravação parcial.

**R8 Abstract Factory.** A família é escolhida uma vez, em `SelecaoDeFabrica.para(perfil)`, chamada só por `Sistema`. Cada fábrica cria uma unidade de trabalho e entrega a mesma instância a todos os seus repositórios (Livro, Movimentacao, Relacionamento e também Multas); no MySQL é uma `Connection` por transação, guardada em `ThreadLocal`. Os construtores dos repositórios aceitam só a unidade de trabalho da própria família e são visíveis só no pacote, então misturar famílias não compila.

**R9 Decorator.** `MontadorDeConsulta` monta `Medicao(Cache(Original))`. A medição fica por fora para contar também os acertos do cache; na ordem inversa, eles não seriam medidos. Se o cache falhar, o decorador registra um aviso e consulta a origem. O cache guarda só `MetadadosLivro(idLivro, titulo)`, sem estado, e a reserva lê sempre o `Livro` do `Acervo`.

**R10 Composite.** `GrupoAcervo.contarLivros` soma os filhos recursivamente (cada `ItemAcervo` vale 1). `adicionar` recusa componente que já tem pai, ciclo (grupo dentro de um descendente seu) e livro que já está na árvore. Mover só remove o nó de um grupo e o insere em outro: o item guarda apenas `idLivro`, então `Livro` e `Movimentacao` não são tocados.

## Alteração ao vivo — roteiro

- **Nova política de multa:** criar `PoliticaFimDeSemanaGratis implements PoliticaDeMulta` em `financeiro/multa`, registrar em `ConfiguracaoAdministrativa` (ou em `Sistema` via `configuracao().registrar(...)`) e selecionar com `-Dpolitica=NOME` ou pela opção 14 do menu.
- **Novo ouvinte da devolução:** criar `OuvinteDeAuditoria implements OuvinteDeDevolucao` em `devolucao` e acrescentar `publicador.registrar(new OuvinteDeAuditoria())` em `Sistema`. Nenhum tratador nem a fachada mudam.

## Uso de LLM

| Ferramenta / modelo | Onde foi usado |
|---|---|
| Claude (Anthropic), modelo Claude Opus 5.5, no app Claude | Geração da estrutura do projeto, código de todos os pacotes, testes JUnit, CLI, scripts dos diagramas UML, este README, `docs/RASTREABILIDADE.md` e o rascunho de `REGISTRO_LLM.md` |

O que foi pedido, aceito, corrigido ou descartado está em [`REGISTRO_LLM.md`](REGISTRO_LLM.md). Todo o código precisa ser revisado e explicado pela equipe antes da entrega.
