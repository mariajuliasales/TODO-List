# Relatório: testes unitários com Spock no backend

Feature: testes unitários com Spock 2.5 (Groovy 4) para `model`, `service` e `repository`, sem alterar `backend/src/main`.
Issue: #5 · PR: #6 (`feature/testes-unitarios` → `dev`)
Base: `docs/spec.md`, `docs/plano.md`, `git log main..feature/testes-unitarios` e os históricos das sessões do Claude Code.

## Consumo de tokens por etapa

Os valores somam todas as chamadas ao modelo (Claude Opus 5.5) registradas nos históricos locais do Claude Code: entrada, escrita e leitura de cache, e saída. A maior parte é **leitura de cache**, que é o contexto da conversa reenviado a cada chamada. Por isso, a coluna "Observação" também mostra os tokens de **saída**, que são o que o modelo de fato escreveu. As etapas foram separadas pelo horário das mensagens.

| Etapa | Consumo aproximado | Observação |
|---|---|---|
| AGENTS.md | ~0,49 M tokens | 9 chamadas, ~8,9 mil de saída. Inclui as respostas sobre Java 21, Live Server e testes do frontend. |
| Spec | ~1,28 M tokens | Criação da spec: ~0,49 M (~26 mil de saída). Revisão da spec e do plano (decisões dos itens "A CONFIRMAR" e aprovação): ~0,79 M (~17 mil de saída). |
| Plano | ~0,35 M tokens | 3 chamadas, ~11,9 mil de saída. Primeira versão do plano. A reorganização entrou na linha da Spec. |
| Implementação | ~19,3 M tokens | 96 chamadas, ~80 mil de saída. Tarefas 1 a 22 numa única sessão longa. O contexto cresceu a cada tarefa, e quase tudo é leitura de cache. |
| Validação | ~3,1 M tokens | ~23 mil de saída. Duas verificações dos critérios de aceite, a inclusão de CA58 a CA60 na spec e no plano e a Tarefa 23. |
| Skill | ~1,2 M tokens | ~6 mil de saída. Criação de `skills/feature-com-spec/SKILL.md`. |
| Pull Request (com MCP) | ~4,8 M tokens | ~42 mil de saída, em 5 sessões. Inclui a descrição do PR, a ajuda para configurar o MCP e as tentativas que falharam por conexão e por permissão. |
| **Total** | **~30,5 M tokens** | ~212 mil de saída. |

## Situações em que o agente errou, divergiu ou houve problemas

### 1. Tarefa 1 implementada e commitada antes de a spec ser aprovada
- **O que aconteceu:** logo depois de o plano ser criado, o agente implementou a Tarefa 1 (Spock no `build.gradle` e o primeiro `AlarmeSpec`). Em seguida, implementou também a Tarefa 2 da numeração antiga. A spec ainda tinha itens marcados como "A CONFIRMAR" e o status "aguardando aprovação", e o agente não avisou. O commit `281b7cc` (build) foi feito antes do commit `a376e0c` (`docs: approve spec and reorganize implementation plan`).
- **Como foi percebido:** na revisão da spec e do plano, a mantenedora decidiu os itens pendentes (valores nulos, `atualizar` com prioridade inválida, ID perdido). Essa revisão mudou o plano: surgiu o CA57 e as tarefas foram renumeradas. Ao implementar a nova Tarefa 2, o agente percebeu que a alteração no `AlarmeSpec.groovy` já pertencia à nova Tarefa 3.
- **Como foi corrigido:** a spec foi aprovada e o plano reorganizado no commit `a376e0c`. A partir daí, cada tarefa foi implementada só depois da aprovação, com um commit por tarefa.

### 2. Dependência que não estava na spec (`junit-platform-launcher`)
- **O que aconteceu:** a lista de dependências da spec não incluía o `junit-platform-launcher`, que o Gradle 9 exige para rodar testes.
- **Como foi percebido:** na Tarefa 1, `./gradlew test` falhou com "Failed to load JUnit Platform".
- **Como foi corrigido:** o agente parou, testou a correção numa cópia fora do projeto e pediu autorização. Depois da aprovação, adicionou a linha ao `build.gradle` e registrou a dependência na seção 4 da spec ("Aprovado durante a Tarefa 1").

### 3. Regra atribuída à spec sem que estivesse nela
- **O que aconteceu:** na Tarefa 13 (`TarefaRepositorySpec`), o agente disse que a spec mandava não verificar a mensagem do console em nenhum dos casos de erro. A spec só dizia isso para o caso "salvar em uma pasta". A regra geral existia numa versão antiga da spec e se perdeu quando ela foi reescrita.
- **Como foi percebido:** a mantenedora perguntou se a spec realmente definia isso.
- **Como foi corrigido:** o agente reconheceu que a decisão para os arquivos inválidos tinha sido dele, explicou o motivo (capturar `System.out` deixaria o teste mais frágil) e manteve a spec como estava.

### 4. Casos de borda da spec sem critério de aceite
- **O que aconteceu:** as 22 tarefas foram concluídas e os 57 critérios estavam atendidos. Mesmo assim, três casos de borda descritos na spec não tinham critério de aceite nem teste:
  - tarefa `DONE` com alarme vencido que volta para `TODO` (o alarme deve disparar na verificação seguinte);
  - filtros por prioridade e por status sem resultado, com tarefas cadastradas;
  - uma única tarefa sem alarmes não gera aviso.
- **Como foi percebido:** na validação, ao cruzar os critérios de aceite com os testes e listar os casos de borda sem cobertura.
- **Como foi corrigido:** foram criados os critérios CA58 a CA60 na spec e a Tarefa 23 no plano (commit `267f22d`), e os testes vieram no commit `6b877c2`. O pedido era começar no CA57, mas esse número já existia, então o agente usou o CA58. A suíte passou de 104 para 108 testes.

### 5. Descrição do PR fora do combinado
- **O que aconteceu:** a primeira versão da descrição terminava com `Closes #5`, e a mantenedora pediu `Refs #5`. Depois, uma versão aprovada ficou sem a linha sobre o último commit (a skill).
- **Como foi percebido:** nas revisões da mantenedora e na conferência que o próprio agente fez antes de abrir o PR.
- **Como foi corrigido:** a descrição passou a usar `Refs #5` e ganhou a linha da skill. O PR foi aberto sem a linha final, terminando exatamente em `Refs #5`.

### 6. MCP do GitHub: conexão e erro 403
- **O que aconteceu:** primeiro, o MCP do GitHub não conectou ("Authorization header is badly formatted") e, em duas sessões seguintes, nem apareceu entre as ferramentas. Quando passou a conectar, a criação do PR falhou com `403 Resource not accessible by personal access token`, porque o token não tinha permissão de escrita em pull requests.
- **Como foi percebido:** pelas mensagens de erro do próprio MCP. A leitura do repositório funcionava, mas a criação do PR não.
- **Como foi corrigido:** a mantenedora ajustou o token e o PR #6 foi aberto pelo MCP na nova tentativa. O `gh pr create` não foi usado como alternativa, porque a mantenedora proibiu. A descrição aprovada ficou guardada em `~/pr-testes-unitarios.md`, fora do repositório, e uma anotação na memória do agente permitiu retomar o trabalho entre as sessões.

## Lições aprendidas

- Aprovar e commitar a spec antes da primeira tarefa. Implementar antes da aprovação gerou retrabalho na numeração do plano.
- Todo caso de borda descrito na spec precisa de um critério de aceite. A validação cruzada (critérios × testes × casos de borda) deve ser uma etapa fixa antes de dar a feature por concluída.
- Regras gerais da spec podem se perder quando ela é reescrita. Quando o agente decidir algo que a spec não define, ele deve dizer que a decisão é dele.
- Conferir as permissões do token do MCP (Pull requests: Read and write) antes da etapa de PR evita sessões perdidas.
- Uma sessão longa encarece cada chamada, porque todo o contexto é reenviado: a implementação concentrou ~63% do consumo total. Dividir a implementação em sessões menores tende a reduzir o custo.
