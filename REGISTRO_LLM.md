# Registro de uso de LLM

Ferramenta: Claude (Anthropic), modelo **Claude Opus 5.5**, no app Claude, com acesso a um ambiente de execução (Java 21 compilando com `--release 17`).
Data: 21/11/2026.

> A coluna **Decisão da equipe** deve ser preenchida pela equipe depois de revisar cada item. O enunciado considera não implementado o código que nenhum integrante consegue explicar.

## 1. Pedidos feitos ao modelo

| # | Pedido (resumo) | O que o modelo entregou |
|---|---|---|
| 1 | "Observe os diagramas feitos pela equipe e gere os códigos" — enviados o enunciado *Trabalho_Implementacao_GoF.pdf* e o projeto com os modelos dos Trabalhos 01/02 (arquivos Astah, imagens dos diagramas de classes, estados, sequência e componentes, PDF de pendências) | Projeto Maven completo: domínio, 10 padrões, CLI, 39 testes (41 após a revisão), diagramas UML, README, rastreabilidade e este registro |

## 2. Decisões tomadas pelo modelo e decisão da equipe

| # | Decisão | Motivo | Decisão da equipe (aceitar / corrigir / descartar) |
|---|---|---|---|
| D1 | O enum `EstadoLivro` do Trabalho 02 virou `CodigoEstadoLivro`; o nome `EstadoLivro` passou para a classe abstrata do State | Sugestão do próprio PDF de pendências; evita colisão de nomes | **Aceito.** Falta renomear o enum no Astah. |
| D2 | Estados como singletons sem dados; o Livro é o contexto | Os estados não guardam informação própria | **Aceito.** |
| D3 | A ação `emitirBoletoDeMulta` da transição T6 é executada pelo `OuvinteDeMulta` (Observer), não dentro do estado | O R5 exige que multa, inspeção, indicadores e e-mail reajam ao evento de devolução | **Corrigido no modelo.** O código foi mantido, porque o R5 exige o Observer. A ação da T6 passou a ser `/ publicarEventoDevolucao`, e o `OuvinteDeMulta` emite a multa quando há atraso. Diagrama gerado já ajustado; pendente no Astah. |
| D4 | `avaliarReparo(reparavel=true)` mantém o livro em REPARANDO (sem transição) | A máquina só define a saída para laudo irrecuperável | **Aceito.** |
| D5 | Prazo do empréstimo = 14 dias (`Emprestimo.PRAZO_EMPRESTIMO_DIAS`) | O enunciado não define; renovação = +30 dias (definido) | **Aceito.** O enunciado não define o prazo; confirmar com o professor. |
| D6 | Operações de inclusão devolvem `ResultadoDeCadastro<T>` em vez da entidade, e consultas devolvem `Optional` | Permite distinguir "criado" de "já existia" (R7) e evitar `null` | **Aceito no código, corrigir o modelo.** Atualizar no Astah os retornos das operações de iLivro, iPessoa, iEstudante e iAtendente. Pendente no Astah. |
| D7 | `inserirPessoa` recebe também o e-mail | `Pessoa` tem e-mail no diagrama de classes e o boleto/recibo é enviado por e-mail | **Aceito no código, corrigir o modelo.** Acrescentar o parâmetro `email` em `inserirPessoa` no Astah. Pendente no Astah. |
| D8 | Multas e boletos ficam em memória dentro do `TratadorDeMulta` (componente Financeiro) | O R8 só exige repositórios de Livro, Movimentacao e Relacionamento; o Financeiro é outro componente | **Corrigido.** Multas e boletos agora são gravados pelo `RepositorioDeMultas`, criado pela mesma fábrica de dados (memória ou MySQL, tabela `multa`) e na mesma unidade de trabalho. A quitação é atômica no repositório. Teste: `R03_multaGravadaNaFamiliaDeDadosSobreviveAoServico`. |
| D9 | Transação em memória implementada com log de desfazer + trava; MySQL com `Connection` por transação em `ThreadLocal` | Necessário para a fachada ser transacional nas duas famílias | **Aceito.** Perfil de produção testado com MySQL real (teste de integração e `--demo`). |
| D10 | Executor do Command guarda também falhas: reenviar um id que falhou devolve a mesma falha | Mesma semântica para sucesso e erro; evita reexecução acidental | **Corrigido.** O executor mantém registradas só as recusas de regra de negócio; uma falha técnica libera o id para nova tentativa. Teste: `R06_falhaTecnicaLiberaIdParaNovaTentativa`. |
| D11 | Cache com validade de 5 minutos e invalidado quando o título muda ou o livro é excluído | O cache guarda só metadados, mas o título pode mudar | **Aceito.** |
| D12 | Bancos A e B simulados com APIs diferentes (centavos/AAAAMMDD/código numérico × texto "7,00"/CPF formatado/ISO/status textual) | Permitido pelo enunciado; o importante é a conversão no adaptador | **Aceito.** |
| D13 | Diagramas gerados por script (Graphviz + SVG) a partir de `docs/uml/modelos.py`, com fontes PlantUML; os `.asta` não foram alterados | O modelo não consegue editar arquivos `.asta` | **Corrigir.** O enunciado exige os modelos no Astah (.asta) exportados em PDF; os diagramas gerados servem de referência. Pendente: atualizar o Astah. |

## 3. Correções feitas durante a geração e na revisão

| Problema encontrado | Correção |
|---|---|
| Teste usava `BigDecimal.TWO`, que só existe a partir do Java 19 | Trocado por `BigDecimal.valueOf(2)` para manter Java 17 |
| Classe auxiliar `MultaAcesso` criada só para expor `quitar()` | Removida; `Multa.quitar` ficou pública e `synchronized` |
| Cálculo de dias de atraso duplicado na fachada | Centralizado em `Devolucao.diasDeAtraso()` |
| URL padrão do MySQL com `&serverTimezone=UTC` quebrava o comando no PowerShell (`mvn.cmd` corta no `&`) e a conexão usava root sem senha | URL sem `&`; usuário local `biblioteca` criado por `docs/mysql-criar-usuario.sql`; teste de integração habilitado por `-Dmysql.integracao=true` |
| Rótulo da transição T6 omitia o caso sem atraso | Ajustado; depois substituído pela correção do D3 (`/ publicarEventoDevolucao`) |
| D8 — multas só em memória, perdidas ao reiniciar no perfil de produção | Criados `RepositorioDeMultas` (+ versões em memória e MySQL), novo produto da fábrica abstrata; `TratadorDeMulta` passou a usá-lo |
| D10 — falha técnica bloqueava para sempre o id da solicitação | `ExecutorDeSolicitacoes` remove o registro quando a falha não é `RegraDeNegocioException` |

## 4. Limitações conhecidas (para a equipe verificar)

- `mvn test` foi executado com Maven e JUnit 5 na máquina da equipe (Java 17, Maven do IntelliJ): 41 testes, 0 falhas. Com `-Dmysql.integracao=true` (MySQL 9.7 local, usuário `biblioteca` criado por `docs/mysql-criar-usuario.sql`), o teste de integração também passou — 41/41, BUILD SUCCESS.
- O `--demo` no perfil de produção rodou de ponta a ponta gravando no MySQL (reserva, empréstimo, renovações, devolução atrasada, multa e boleto).
- O Astah precisa ser atualizado manualmente (ver README, seção *Modelos UML*).

## 5. Itens descartados pela equipe

Nenhum.
