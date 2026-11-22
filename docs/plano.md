# Plano de implementação: Testes unitários do backend com Spock

- **Spec:** [docs/spec.md](spec.md)
- **Branch:** `feature/testes-unitarios`
- **Ordem:** configuração e documentação primeiro; depois, por classe testada, da mais simples para a mais complexa: `Alarme` → `Tarefa` → `AlarmeService` → `TarefaRepository` → `TarefaService`.

## Regras para todas as tarefas

- Todos os comandos são executados dentro de `backend/`.
- **Nenhuma tarefa altera `backend/src/main`.**
- Um commit por tarefa, em inglês, no formato Conventional Commits (ex.: `test(model): add Alarme constructor specs`). Ao concluir a tarefa, marque `[x]` no título dela neste arquivo, no mesmo commit.
- Ao final de cada tarefa, `./gradlew test` deve terminar com `BUILD SUCCESSFUL` (todos os testes anteriores continuam passando).
- Para ver o resultado de cada teste: abrir `backend/build/reports/tests/test/index.html`.
- Nenhum teste usa `LocalDateTime.now()` nem o arquivo `backend/tarefas.dat`.

## Antes de começar

Concluído: `AGENTS.md`, `docs/spec.md` e `docs/plano.md` foram commitados antes de qualquer código.

---

## [X] Tarefa 1 – Configurar o Spock e provar que funciona
- O que será feito:
  - Escolher versões de Groovy 4 e `spock-core` 2.x (variante `-groovy-4.0`) compatíveis com o JDK 25.
  - No `build.gradle`: adicionar o plugin `groovy` e as dependências de teste Groovy 4, `spock-core`, `byte-buddy` e `objenesis`. As dependências JUnit existentes ficam como estão.
  - Criar o `AlarmeSpec` com um único teste simples: um alarme de 30 minutos guarda 30 minutos e começa "não disparado". Esse teste usa uma classe de produção, o que prova que o Groovy consegue ler as classes compiladas pelo JDK 25.
  - **Se não funcionar com o JDK 25:** parar, relatar o erro e, se `options.release = 21` for a solução, explicar e pedir autorização antes de aplicar.
- Arquivos criados ou alterados:
  - `backend/build.gradle`
  - `backend/src/test/groovy/model/AlarmeSpec.groovy` (novo)
- Critérios de aceite relacionados: CA02 (parcial: apenas o teste de configuração), CA53 (parcial)
- Como validar:
  - `java -version` → mostra a versão 25.
  - `./gradlew test --tests 'model.AlarmeSpec'` → `BUILD SUCCESSFUL`; o relatório mostra 1 teste do `AlarmeSpec` passando.
  - `./gradlew run` continua abrindo o menu (sair com `0`).

## [x] Tarefa 2 – Atualizar o AGENTS.md com a configuração de testes
- O que será feito:
  - Na tabela "Stack e estrutura": incluir Groovy 4 e Spock 2.x (testes), `byte-buddy`, `objenesis` e `junit-platform-launcher`.
  - Na estrutura de pastas: incluir `backend/src/test/groovy/`, espelhando os pacotes de produção.
  - Trocar a orientação de testes em `src/test/java` com sufixo `Test` por testes em `src/test/groovy` com sufixo `Spec`, nomes de métodos em português e blocos `given`/`when`/`then`.
  - Atualizar os comandos de teste (ex.: `./gradlew test --tests 'model.AlarmeSpec'`).
  - Trocar a referência ao `@TempDir` do JUnit pelo `@TempDir` do Spock.
- Arquivos criados ou alterados: `AGENTS.md`
- Critérios de aceite relacionados: nenhum diretamente (documentação; ver seção 4 da spec)
- Como validar:
  - `grep -n "src/test/java\|Test\.java\|TarefaServiceTest" ../AGENTS.md` → nenhuma saída.
  - O comando de exemplo do `AGENTS.md` funciona: `./gradlew test --tests 'model.AlarmeSpec'` → `BUILD SUCCESSFUL`.

## [x] Tarefa 3 – Alarme: criação, momento de disparo e texto
- O que será feito:
  - Testar que antecedência 0 e -1 geram erro de argumento inválido (tabela de dados).
  - Completar o teste de criação (antecedência de 1 minuto também é aceita).
  - Testar o momento de disparo (prazo 10:00 com alarme de 30 min → 09:30).
  - Testar o texto do alarme antes e depois de disparado.
- Arquivos criados ou alterados: `backend/src/test/groovy/model/AlarmeSpec.groovy`
- Critérios de aceite relacionados: CA01, CA02, CA03, CA04
- Como validar: `./gradlew test --tests 'model.AlarmeSpec'` → `BUILD SUCCESSFUL`, sem falhas no relatório.

## [x] Tarefa 4 – Alarme: formatação de minutos
- O que será feito: testar `formatarMinutos` com uma tabela de dados com os 9 valores da tabela da seção 2.1 da spec.
- Arquivos criados ou alterados: `backend/src/test/groovy/model/AlarmeSpec.groovy`
- Critérios de aceite relacionados: CA05
- Como validar: `./gradlew test --tests 'model.AlarmeSpec'` → `BUILD SUCCESSFUL`; o relatório mostra uma linha para cada um dos 9 valores, todas passando.

## [x] Tarefa 5 – Tarefa: estado inicial, prazo e ordenação
- O que será feito:
  - Criar o `TarefaSpec`.
  - Testar que uma tarefa nova começa com status `TODO` e sem alarmes.
  - Testar o prazo (20/11/2026 às 14:30).
  - Testar a ordenação natural (prioridades 2, 5 e 3 → 5, 3, 2).
- Arquivos criados ou alterados: `backend/src/test/groovy/model/TarefaSpec.groovy` (novo)
- Critérios de aceite relacionados: CA06, CA07, CA14
- Como validar: `./gradlew test --tests 'model.TarefaSpec'` → `BUILD SUCCESSFUL`, sem falhas.

## [x] Tarefa 6 – Tarefa: validação de prioridade e bug do construtor
- O que será feito:
  - Testar a alteração de prioridade: 1 e 5 aceitas; 0 e 6 geram erro e a prioridade anterior é mantida.
  - Escrever o teste do comportamento correto do construtor (prioridade 0 e 6 devem gerar erro), marcado com `@PendingFeature` e com um comentário explicando o bug.
- Arquivos criados ou alterados: `backend/src/test/groovy/model/TarefaSpec.groovy`
- Critérios de aceite relacionados: CA08, CA09, CA53 (parcial: comportamento do `@PendingFeature`)
- Como validar:
  - `./gradlew test --tests 'model.TarefaSpec'` → `BUILD SUCCESSFUL`.
  - No relatório, o teste do construtor aparece como **ignorado** (skipped), não como falha.
  - `grep -c "<skipped" build/test-results/test/TEST-model.TarefaSpec.xml` → maior que 0.

## [x] Tarefa 7 – Tarefa: adicionar, remover e reiniciar alarmes
- O que será feito:
  - Testar alarme duplicado (erro, continua com 1) e alarme diferente (fica com 2).
  - Testar remoção pela posição 0 (sucesso) e pelas posições -1 e 2 (falha, nada removido).
  - Testar remoção em tarefa sem alarmes (falha).
  - Testar o reinício dos alarmes (todos "não disparados"), inclusive em tarefa sem alarmes (sem erro).
- Arquivos criados ou alterados: `backend/src/test/groovy/model/TarefaSpec.groovy`
- Critérios de aceite relacionados: CA10, CA11, CA12, CA13
- Como validar: `./gradlew test --tests 'model.TarefaSpec'` → `BUILD SUCCESSFUL`, sem falhas (o teste do construtor continua ignorado).

## [x] Tarefa 8 – AlarmeService: antecedência sugerida
- O que será feito: criar o `AlarmeServiceSpec` e testar a antecedência sugerida para as prioridades de 1 a 5 (tabela de dados).
- Arquivos criados ou alterados: `backend/src/test/groovy/service/AlarmeServiceSpec.groovy` (novo)
- Critérios de aceite relacionados: CA15
- Como validar: `./gradlew test --tests 'service.AlarmeServiceSpec'` → `BUILD SUCCESSFUL`; 5 linhas na tabela, todas passando.

## [x] Tarefa 9 – AlarmeService: quando o alarme dispara
- O que será feito (sempre com horário "agora" fixo):
  - Lista vazia → nenhum aviso.
  - Tarefa com prazo 10:00 e alarme de 30 min: às 09:29 não dispara; às 09:30 e às 09:45 dispara.
  - Alarme já disparado não dispara de novo.
- Arquivos criados ou alterados: `backend/src/test/groovy/service/AlarmeServiceSpec.groovy`
- Critérios de aceite relacionados: CA16, CA17, CA18, CA19
- Como validar: `./gradlew test --tests 'service.AlarmeServiceSpec'` → `BUILD SUCCESSFUL`, sem falhas.

## [x] Tarefa 10 – AlarmeService: tarefas concluídas e vários alarmes
- O que será feito:
  - Tarefa `DONE` com alarme vencido → nenhum aviso; alarme continua "não disparado".
  - Tarefa com dois alarmes vencidos → um único aviso; os dois ficam "disparados".
  - Duas tarefas com alarmes vencidos e uma sem alarmes → dois avisos.
- Arquivos criados ou alterados: `backend/src/test/groovy/service/AlarmeServiceSpec.groovy`
- Critérios de aceite relacionados: CA20, CA21, CA22
- Como validar: `./gradlew test --tests 'service.AlarmeServiceSpec'` → `BUILD SUCCESSFUL`, sem falhas.

## [x] Tarefa 11 – AlarmeService: texto do aviso
- O que será feito:
  - Título: `[URGENTE] ALARME` para prioridade 4 e 5; `ALARME` para 1, 2 e 3 (tabela de dados).
  - "vence em" antes do prazo; "venceu há" depois do prazo.
  - "Tarefa em andamento." para `DOING`; "Tarefa ainda não iniciada." para `TODO`.
  - Aviso contém o nome "Estudar" e "20/11/2026 às 14:30".
- Arquivos criados ou alterados: `backend/src/test/groovy/service/AlarmeServiceSpec.groovy`
- Critérios de aceite relacionados: CA23, CA24, CA25, CA26
- Como validar: `./gradlew test --tests 'service.AlarmeServiceSpec'` → `BUILD SUCCESSFUL`, sem falhas.

## [x] Tarefa 12 – TarefaRepository: salvar e carregar
- O que será feito:
  - Criar o `TarefaRepositorySpec`, usando pasta temporária (`@TempDir`) em todos os testes.
  - Arquivo inexistente → lista vazia.
  - Salvar duas tarefas (com status e alarmes, um deles disparado) e carregar com um novo repositório → dados iguais.
  - Salvar duas vezes → ao carregar, vem só a segunda lista.
  - Salvar lista vazia → ao carregar, vem lista vazia.
- Arquivos criados ou alterados: `backend/src/test/groovy/repository/TarefaRepositorySpec.groovy` (novo)
- Critérios de aceite relacionados: CA47, CA48, CA49, CA51
- Como validar:
  - `./gradlew test --tests 'repository.TarefaRepositorySpec'` → `BUILD SUCCESSFUL`, sem falhas.
  - `backend/tarefas.dat` não foi criado nem alterado (ver validação da Tarefa 22).

## [x] Tarefa 13 – TarefaRepository: arquivos inválidos e erro de gravação
- O que será feito:
  - Arquivo vazio, arquivo com conteúdo inválido e arquivo com outro tipo de objeto → lista vazia, sem erro (tabela de dados).
  - Salvar em um caminho que é uma pasta → nenhum erro lançado.
- Arquivos criados ou alterados: `backend/src/test/groovy/repository/TarefaRepositorySpec.groovy`
- Critérios de aceite relacionados: CA50, CA52
- Como validar: `./gradlew test --tests 'repository.TarefaRepositorySpec'` → `BUILD SUCCESSFUL`, sem falhas.

## [x] Tarefa 14 – TarefaService: geração de IDs
- O que será feito:
  - Criar o `TarefaServiceSpec`, com o `TarefaRepository` substituído por um mock do Spock. O mock sempre responde `carregar()` com uma lista (sem isso, o serviço falha ao ser criado).
  - Este é o primeiro mock de classe: confirma que `byte-buddy` e `objenesis` funcionam.
  - Sem tarefas salvas → três tarefas adicionadas recebem os IDs 1, 2 e 3.
  - Com tarefas salvas de IDs 4 e 7 → a nova tarefa recebe o ID 8.
- Arquivos criados ou alterados: `backend/src/test/groovy/service/TarefaServiceSpec.groovy` (novo)
- Critérios de aceite relacionados: CA27, CA28
- Como validar: `./gradlew test --tests 'service.TarefaServiceSpec'` → `BUILD SUCCESSFUL`, sem falhas.

## [x] Tarefa 15 – TarefaService: adicionar tarefa
- O que será feito:
  - Adicionar com 30 e 60 min → status `TODO`, dois alarmes, `salvar` chamado uma vez.
  - Adicionar sem alarmes → tarefa sem alarmes.
  - Prioridades 2, 5 e 3 → lista na ordem 5, 3, 2.
  - Alarme inválido (30 e 30, ou 0) → erro, tarefa não aparece, `salvar` não é chamado. O ID seguinte **não** é verificado (comportamento aceito, spec seção 2.4).
- Arquivos criados ou alterados: `backend/src/test/groovy/service/TarefaServiceSpec.groovy`
- Critérios de aceite relacionados: CA29, CA30, CA31
- Como validar: `./gradlew test --tests 'service.TarefaServiceSpec'` → `BUILD SUCCESSFUL`, sem falhas.

## [x] Tarefa 16 – TarefaService: listar, buscar e filtrar
- O que será feito:
  - Alterar a lista devolvida por `listar` não muda o serviço.
  - Sem tarefas → listar e os três filtros devolvem lista vazia.
  - Categoria "Trabalho" encontrada com "trabalho" e "TRABALHO"; "Casa" e categoria nula → lista vazia.
  - Filtro por prioridade e por status devolve só as tarefas correspondentes.
  - Buscar por ID existente e inexistente (`null`).
- Arquivos criados ou alterados: `backend/src/test/groovy/service/TarefaServiceSpec.groovy`
- Critérios de aceite relacionados: CA32, CA33, CA34, CA35, CA36
- Como validar: `./gradlew test --tests 'service.TarefaServiceSpec'` → `BUILD SUCCESSFUL`, sem falhas.

## [x] Tarefa 17 – TarefaService: atualizar status e IDs inexistentes
- O que será feito:
  - Atualizar status para `DOING` → tarefa devolvida com o novo status e `salvar` chamado.
  - ID inexistente em atualizar status, atualizar, deletar, adicionar alarme e remover alarme → `null`/`false` e `salvar` não chamado (tabela de dados ou um teste por operação).
- Arquivos criados ou alterados: `backend/src/test/groovy/service/TarefaServiceSpec.groovy`
- Critérios de aceite relacionados: CA37, CA38
- Como validar: `./gradlew test --tests 'service.TarefaServiceSpec'` → `BUILD SUCCESSFUL`, sem falhas.

## [x] Tarefa 18 – TarefaService: atualizar tarefa
- O que será feito:
  - Atualizar todos os campos → valores novos na tarefa, lista reordenada, `salvar` chamado.
  - Prazo diferente → alarme volta a "não disparado"; mesmo prazo → continua "disparado".
  - Prioridade 0 → erro e `salvar` não chamado.
  - Escrever o teste do comportamento correto "nenhum campo muda quando a prioridade é inválida", marcado com `@PendingFeature` e com um comentário explicando o bug.
- Arquivos criados ou alterados: `backend/src/test/groovy/service/TarefaServiceSpec.groovy`
- Critérios de aceite relacionados: CA39, CA40, CA41, CA57, CA53 (parcial: comportamento do `@PendingFeature`)
- Como validar:
  - `./gradlew test --tests 'service.TarefaServiceSpec'` → `BUILD SUCCESSFUL`, sem falhas.
  - No relatório, o teste de "nenhum campo muda" aparece como **ignorado** (skipped), não como falha.

## [x] Tarefa 19 – TarefaService: deletar e gerenciar alarmes
- O que será feito:
  - Deletar → tarefa some, resultado `true`, `salvar` chamado.
  - Adicionar alarme de 15 min → tarefa passa a tê-lo, `salvar` chamado; antecedência repetida ou 0 → erro, `salvar` não chamado.
  - Remover alarme → tarefa sem alarmes, `salvar` chamado; posição inválida → `false`, `salvar` não chamado.
- Arquivos criados ou alterados: `backend/src/test/groovy/service/TarefaServiceSpec.groovy`
- Critérios de aceite relacionados: CA42, CA43, CA44
- Como validar: `./gradlew test --tests 'service.TarefaServiceSpec'` → `BUILD SUCCESSFUL`, sem falhas.

## [x] Tarefa 20 – TarefaService: sugestão de antecedência e verificação de alarmes
- O que será feito:
  - Sugestão de antecedência para prioridade 3 → 120.
  - Verificar alarmes com alarme vencido → um aviso e `salvar` chamado; sem alarme vencido → nenhum aviso e `salvar` não chamado.
- Arquivos criados ou alterados: `backend/src/test/groovy/service/TarefaServiceSpec.groovy`
- Critérios de aceite relacionados: CA45, CA46
- Como validar: `./gradlew test --tests 'service.TarefaServiceSpec'` → `BUILD SUCCESSFUL`, sem falhas.

## [x] Tarefa 21 – Relatório de cobertura (JaCoCo)
- O que será feito:
  - No `build.gradle`: adicionar o plugin `jacoco`, com relatório HTML que exclui a classe `Main`. Sem regra que faça o build falhar por cobertura.
  - No `AGENTS.md`: incluir o JaCoCo na stack e o comando do relatório de cobertura.
  - Anotar no commit (ou na descrição do PR) a cobertura de linhas obtida em `model`, `service` e `repository` (meta informal: 80%).
- Arquivos criados ou alterados: `backend/build.gradle`, `AGENTS.md`
- Critérios de aceite relacionados: CA54
- Como validar:
  - `./gradlew test jacocoTestReport` → `BUILD SUCCESSFUL`.
  - Abrir `backend/build/reports/jacoco/test/html/index.html` → aparecem os pacotes `model`, `service` e `repository`, e a classe `Main` não aparece.

## [x] Tarefa 22 – Verificação final e teste manual pelo console
- O que será feito:
  1. Verificação automática dos critérios gerais (CA53, CA55, CA56).
  2. Depois da verificação do CA56: teste manual pelo console, feito pela mantenedora, cobrindo listagem, filtros, mudança de status e alarme.
- Arquivos criados ou alterados: `docs/plano.md` (marcar as tarefas concluídas)
- Critérios de aceite relacionados: CA53, CA55, CA56
- Como validar:
  - **CA56:** antes de rodar os testes, `sha256sum tarefas.dat 2>/dev/null || echo "não existe"`; rodar `./gradlew clean test`; repetir o comando → resultado igual ao de antes.
  - **CA53:** `./gradlew clean test` → `BUILD SUCCESSFUL`; no relatório, nenhuma falha e só os testes com `@PendingFeature` como ignorados.
  - **CA55:** `git diff --stat main -- src/main` → nenhuma saída.
  - **Teste manual pelo console** (só depois do CA56, porque o console grava no `tarefas.dat` real):
    1. Se existir, guardar uma cópia: `cp tarefas.dat tarefas.dat.bak`.
    2. `./gradlew run` e cadastrar três tarefas:
       - "Relatório", categoria "Trabalho", prioridade 5, término hoje, hora daqui a 5 minutos, com alarme de `10m`;
       - "Mercado", categoria "Casa", prioridade 2, término amanhã, sem alarme;
       - "Prova", categoria "Estudos", prioridade 3, término amanhã, sem alarme.
    3. **Alarme:** logo após cadastrar "Relatório", aparece um aviso começando com `[URGENTE] ALARME` e contendo "vence em".
    4. **Listagem:** opção 2 → 1 lista as três tarefas na ordem de prioridade: Relatório (5), Prova (3), Mercado (2).
    5. **Filtros:** opção 2 → 2 com "trabalho" (minúsculo) mostra só "Relatório"; opção 2 → 3 com prioridade 2 mostra só "Mercado"; opção 2 → 4 com `TODO` mostra as três.
    6. **Mudança de status:** opção 4 muda "Mercado" para `DOING`; opção 2 → 4 com `DOING` mostra só "Mercado".
    7. **Alarme em tarefa concluída:** primeiro, opção 4 muda "Prova" para `DONE`; depois, opção 6 adiciona em "Prova" um alarme de `2d` (que já estaria vencido) → nenhum aviso aparece para "Prova".
    8. Sair com `0`. Restaurar os dados: `mv tarefas.dat.bak tarefas.dat` (ou `rm tarefas.dat` se o arquivo não existia antes).

---

## Critérios de aceite × tarefas

| Critério | Tarefas |
|---|---|
| CA01 | 3 |
| CA02 | 1, 3 |
| CA03 | 3 |
| CA04 | 3 |
| CA05 | 4 |
| CA06 | 5 |
| CA07 | 5 |
| CA08 | 6 |
| CA09 | 6 |
| CA10 | 7 |
| CA11 | 7 |
| CA12 | 7 |
| CA13 | 7 |
| CA14 | 5 |
| CA15 | 8 |
| CA16 | 9 |
| CA17 | 9 |
| CA18 | 9 |
| CA19 | 9 |
| CA20 | 10 |
| CA21 | 10 |
| CA22 | 10 |
| CA23 | 11 |
| CA24 | 11 |
| CA25 | 11 |
| CA26 | 11 |
| CA27 | 14 |
| CA28 | 14 |
| CA29 | 15 |
| CA30 | 15 |
| CA31 | 15 |
| CA32 | 16 |
| CA33 | 16 |
| CA34 | 16 |
| CA35 | 16 |
| CA36 | 16 |
| CA37 | 17 |
| CA38 | 17 |
| CA39 | 18 |
| CA40 | 18 |
| CA41 | 18 |
| CA42 | 19 |
| CA43 | 19 |
| CA44 | 19 |
| CA45 | 20 |
| CA46 | 20 |
| CA47 | 12 |
| CA48 | 12 |
| CA49 | 12 |
| CA50 | 13 |
| CA51 | 12 |
| CA52 | 13 |
| CA53 | 1, 6, 18, 22 |
| CA54 | 21 |
| CA55 | 22 |
| CA56 | 12, 22 |
| CA57 | 18 |
