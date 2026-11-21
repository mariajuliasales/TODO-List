# Spec: Testes unitários do backend com Spock

- **Branch:** `feature/testes-unitarios`
- **Status:** aguardando aprovação
- **Referência do projeto:** [AGENTS.md](../AGENTS.md)

Itens marcados como **A CONFIRMAR** dependem de decisão da mantenedora.

---

## 1. Contexto e objetivo

### Contexto

O backend é uma aplicação Java de console que cadastra tarefas, filtra por categoria, prioridade e status, e dispara alarmes antes do prazo. As tarefas são salvas no arquivo `tarefas.dat`.

Hoje o backend **não tem nenhum teste**. Toda verificação é manual, pelo menu do console. Isso traz três problemas:

- **Regras de alarme são difíceis de testar à mão.** Para ver um alarme disparar, é preciso esperar o horário certo ou criar tarefas com prazos próximos.
- **Mudanças podem quebrar regras sem ninguém perceber.** Exemplos: ordem por prioridade, reinício dos alarmes quando o prazo muda, alarmes que não devem disparar para tarefas concluídas.
- **Testar à mão mexe nos dados reais**, porque o console grava no `tarefas.dat`.

### Objetivo

Criar testes unitários automáticos com **Spock 2.x (Groovy 4)** para as classes de `model`, `service` e `repository`, que:

- verifiquem as regras de negócio e os casos de borda descritos na seção 2;
- rodem com um único comando (`./gradlew test`), sem interação;
- não dependam do relógio do sistema nem do `tarefas.dat` real;
- gerem um relatório de cobertura (JaCoCo), com meta informal de 80% das linhas em `model`, `service` e `repository`.

### Como os testes serão feitos

- Testes em `backend/src/test/groovy/`, nos mesmos pacotes do código de produção, com sufixo `Spec` (ex.: `service/TarefaServiceSpec.groovy`).
- Nomes dos testes em português, como frases (ex.: "não dispara alarme de tarefa concluída").
- Nos testes do `TarefaService`, o `TarefaRepository` é substituído por um **mock** do Spock. Assim dá para verificar quando os dados são salvos, sem gravar arquivo.
- Nos testes do `TarefaRepository`, é usado um arquivo real em **pasta temporária**, apagada ao final do teste.
- Nos testes de alarme, o "agora" é sempre um horário fixo passado pelo teste.

---

## 2. Regras de negócio

Esta seção descreve o comportamento **atual** do código. Os testes verificam esse comportamento, exceto nos casos marcados como **bug conhecido**.

### 2.1 `Alarme` (model)

**Comportamentos esperados**
- Um alarme guarda a antecedência em minutos e começa como "não disparado".
- O momento de disparo é o prazo da tarefa menos a antecedência.
- Ao virar texto, mostra a antecedência formatada seguida de "antes". Se já disparou, acrescenta "(já disparado)".
- `formatarMinutos` converte minutos em texto:

| Minutos | Texto |
|---|---|
| 0 | `menos de 1 min` |
| 1 | `1 min` |
| 30 | `30 min` |
| 60 | `1 h` |
| 90 | `1 h e 30 min` |
| 1440 | `1 dia` |
| 1441 | `1 dia` (os minutos são omitidos quando há dias) |
| 1500 | `1 dia e 1 h` |
| 2880 | `2 dias` |

**Casos de borda**
- Antecedência 0 ou negativa: rejeitada com erro.
- Antecedência de 1 minuto: aceita (menor valor válido).

### 2.2 `Tarefa` (model)

**Comportamentos esperados**
- Uma tarefa nova começa com status `TODO` e sem alarmes.
- O prazo é a junção da data e da hora de término.
- A prioridade alterada depois da criação só aceita valores de 1 a 5.
- Uma tarefa não pode ter dois alarmes com a mesma antecedência.
- Um alarme é removido pela posição na lista.
- "Reiniciar alarmes" marca todos como não disparados.
- Na comparação entre tarefas, a de maior prioridade vem primeiro.

**Casos de borda**
- Prioridade 0 e 6 na alteração: rejeitadas. Prioridade 1 e 5: aceitas.
- Remover alarme na posição -1, ou em uma posição igual ao tamanho da lista: não remove nada e informa que falhou.
- Remover alarme de uma tarefa sem alarmes: informa que falhou.
- Reiniciar alarmes de uma tarefa sem alarmes: não dá erro.

**Bug conhecido: o construtor não valida a prioridade**
- Ao criar uma tarefa, a prioridade não é validada. Só a alteração posterior é validada. Com isso, é possível cadastrar uma tarefa com prioridade 0 ou 9.
- O teste exige o comportamento **correto** (rejeitar prioridade fora de 1–5) e fica marcado com `@PendingFeature`, com um comentário explicando o bug. Enquanto o bug existir, esse teste não quebra o build. Quando o bug for corrigido, o Spock avisa que a marcação pode ser removida.
- A correção está fora do escopo (seção 5).

**Valores nulos (A CONFIRMAR)**
- A tarefa aceita nome, descrição e categoria nulos, sem validação. Uma tarefa com data ou hora nula quebra ao calcular o prazo. O código não define se nulo é permitido. Proposta: **não testar** nulos no `model`, porque o console nunca envia esses valores, e registrar a questão para uma decisão futura.

### 2.3 `AlarmeService` (service)

**Comportamentos esperados**
- Antecedência sugerida pela prioridade: 1 ou 2 → 30 min; 3 → 2 h; 4 ou 5 → 1 dia.
- Ao verificar os alarmes em um horário "agora":
  - dispara cada alarme cujo momento de disparo já chegou (inclusive no minuto exato);
  - marca o alarme como disparado, para não disparar de novo;
  - ignora tarefas com status `DONE`;
  - gera **um aviso por tarefa**, mesmo que vários alarmes dela disparem juntos.
- O aviso contém:
  - `[URGENTE] ALARME` para prioridade 4 ou 5, e `ALARME` para as demais;
  - o nome da tarefa;
  - "vence em ..." se o prazo ainda não passou, ou "venceu há ..." se já passou;
  - o prazo no formato `dd/MM/yyyy às HH:mm`;
  - "Tarefa em andamento." para `DOING`, e "Tarefa ainda não iniciada." para `TODO`.

**Casos de borda**
- Lista de tarefas vazia: nenhum aviso.
- Tarefa sem alarmes: nenhum aviso.
- Um minuto antes do momento de disparo: não dispara.
- Tarefa `DONE` com alarme vencido: não dispara, e o alarme continua "não disparado" (se a tarefa voltar para `TODO`, ele ainda pode disparar).
- Prazo já vencido quando o alarme dispara: o aviso diz "venceu há".
- Prioridade fora de 1–5 (possível por causa do bug do construtor): prioridade 0 sugere 30 min; prioridade 9 sugere 1 dia. Estes casos ficam fora dos testes, porque não são valores válidos.

**Status inválido**
- O status é um enum (`TODO`, `DOING`, `DONE`), então não existe "texto de status inválido" nas camadas testadas. A conversão de texto para status acontece só no `Main`, que está fora do escopo.

### 2.4 `TarefaService` (service)

**Comportamentos esperados**
- **IDs:** a primeira tarefa recebe o ID 1 e as seguintes são sequenciais. Se já existem tarefas salvas, o próximo ID é o maior ID existente + 1.
- **Adicionar:** cria a tarefa com status `TODO` e os alarmes informados, mantém a lista ordenada da maior para a menor prioridade e salva.
- **Listar:** devolve uma cópia da lista. Alterar a cópia não altera as tarefas do serviço.
- **Buscar por ID:** devolve a tarefa, ou nada (`null`) se o ID não existe.
- **Filtros:**
  - por categoria, sem diferenciar maiúsculas e minúsculas;
  - por prioridade;
  - por status.
- **Atualizar status:** altera, salva e devolve a tarefa.
- **Atualizar tarefa:** altera todos os campos, reordena e salva. Se o prazo mudou, reinicia os alarmes. Se o prazo é o mesmo, os alarmes ficam como estavam.
- **Deletar:** remove e salva.
- **Adicionar e remover alarme:** alteram a tarefa e salvam.
- **Verificar alarmes:** salva só se algum alarme disparou.

**Casos de borda**
- Nenhuma tarefa cadastrada: listar e todos os filtros devolvem lista vazia.
- Filtro sem resultado: devolve lista vazia.
- Filtro por categoria nula: devolve lista vazia, sem erro.
- Adicionar tarefa sem alarmes (lista de antecedências vazia): a tarefa é criada sem alarmes.
- ID inexistente em atualizar status, atualizar, deletar, adicionar alarme e remover alarme: informa que falhou (`null` ou `false`) e **não salva**.
- Adicionar tarefa com alarme inválido (antecedência 0 ou repetida): dá erro, a tarefa não é criada e nada é salvo.
- Adicionar alarme com antecedência repetida ou 0 em tarefa existente: dá erro e nada é salvo.
- Remover alarme com posição inválida: informa que falhou e não salva.
- Verificar alarmes sem nenhum alarme vencido: não salva.

**Comportamento a decidir: `atualizar` com prioridade inválida (A CONFIRMAR)**
- Nome, descrição, data e hora são alterados **antes** de a prioridade ser validada. Com prioridade inválida, o erro acontece depois que esses campos já mudaram em memória, e nada é salvo. A tarefa fica parcialmente alterada.
- Proposta: tratar como o bug do construtor (teste do comportamento correto, "nenhum campo muda", com `@PendingFeature` e comentário; correção fora do escopo).

**Comportamento a decidir: ID perdido quando `adicionar` falha (A CONFIRMAR)**
- Quando um alarme inválido impede a criação da tarefa, o número de ID já foi usado e a próxima tarefa pula esse número.
- Proposta: considerar aceitável e não testar o ID seguinte.

**Valores nulos (A CONFIRMAR)**
- Atualizar status para nulo é aceito e deixa a tarefa sem status. Adicionar com lista de antecedências nula dá erro. O console nunca envia esses valores. Proposta: **não testar**, como no `model`.

### 2.5 `TarefaRepository` (repository)

**Comportamentos esperados**
- Salva a lista de tarefas no arquivo informado.
- Carrega a lista salva com todos os dados: campos, status, alarmes e se cada alarme já disparou.
- Salvar de novo substitui o conteúdo anterior.

**Casos de borda**
- Arquivo não existe: devolve lista vazia.
- Arquivo vazio (0 bytes): devolve lista vazia, sem erro.
- Arquivo com conteúdo inválido: devolve lista vazia, sem erro.
- Arquivo com outro tipo de objeto (que não seja uma lista): devolve lista vazia, sem erro.
- Salvar uma lista vazia e carregar: devolve lista vazia.
- Salvar em um caminho que não pode ser gravado (ex.: uma pasta): não dá erro. A mensagem impressa no console não é verificada.

---

## 3. Critérios de aceite

### Alarme
- **CA01** — Dado uma antecedência de 0 ou de -1 minuto, quando o alarme é criado, então ocorre um erro de argumento inválido.
- **CA02** — Dado uma antecedência de 30 minutos, quando o alarme é criado, então ele guarda 30 minutos e está "não disparado".
- **CA03** — Dado um prazo às 10:00 e um alarme de 30 minutos, quando o momento de disparo é calculado, então o resultado é 09:30 do mesmo dia.
- **CA04** — Dado um alarme de 2 h não disparado, quando ele vira texto, então o texto é "2 h antes"; e, depois de disparado, é "2 h antes (já disparado)".
- **CA05** — Dado cada valor da tabela da seção 2.1, quando `formatarMinutos` é chamado, então o texto é o indicado na tabela.

### Tarefa
- **CA06** — Dado uma tarefa recém-criada, quando seus dados são consultados, então o status é `TODO` e a lista de alarmes está vazia.
- **CA07** — Dado uma tarefa com término em 20/11/2026 às 14:30, quando o prazo é consultado, então o resultado é 20/11/2026 14:30.
- **CA08** — Dado uma tarefa existente, quando a prioridade é alterada para 1 ou 5, então a alteração é aceita; e quando é alterada para 0 ou 6, então ocorre um erro e a prioridade anterior é mantida.
- **CA09** — Dado uma prioridade 0 ou 6, quando uma tarefa é criada com ela, então deveria ocorrer um erro. *(Bug conhecido: teste marcado com `@PendingFeature`.)*
- **CA10** — Dado uma tarefa com alarme de 30 minutos, quando outro alarme de 30 minutos é adicionado, então ocorre um erro e a tarefa continua com um alarme; e quando um alarme de 60 minutos é adicionado, então a tarefa fica com dois.
- **CA11** — Dado uma tarefa com dois alarmes, quando o alarme da posição 0 é removido, então resta só o outro e o resultado é "sucesso".
- **CA12** — Dado uma tarefa com dois alarmes, quando se tenta remover a posição -1 ou a posição 2, então nada é removido e o resultado é "falhou".
- **CA13** — Dado uma tarefa com alarmes disparados, quando os alarmes são reiniciados, então todos ficam "não disparados".
- **CA14** — Dado tarefas com prioridades 2, 5 e 3, quando são ordenadas pela comparação natural, então a ordem é 5, 3, 2.

### AlarmeService
- **CA15** — Dado cada prioridade de 1 a 5, quando a antecedência é sugerida, então o resultado é 30, 30, 120, 1440 e 1440 minutos.
- **CA16** — Dado uma lista de tarefas vazia, quando os alarmes são verificados, então nenhum aviso é gerado.
- **CA17** — Dado uma tarefa `TODO` com prazo às 10:00 e alarme de 30 minutos, quando a verificação é feita às 09:29, então nenhum aviso é gerado e o alarme continua "não disparado".
- **CA18** — Dado a mesma tarefa do CA17, quando a verificação é feita às 09:30 e às 09:45, então em cada caso é gerado um aviso e o alarme fica "disparado".
- **CA19** — Dado um alarme que já disparou, quando a verificação é feita de novo, então nenhum novo aviso é gerado.
- **CA20** — Dado uma tarefa `DONE` com alarme vencido, quando os alarmes são verificados, então nenhum aviso é gerado e o alarme continua "não disparado".
- **CA21** — Dado uma tarefa com dois alarmes vencidos, quando os alarmes são verificados, então é gerado um único aviso e os dois alarmes ficam "disparados".
- **CA22** — Dado duas tarefas com alarmes vencidos e uma sem alarmes, quando os alarmes são verificados, então são gerados dois avisos.
- **CA23** — Dado uma tarefa com prioridade 4 ou 5, quando seu alarme dispara, então o aviso começa com "[URGENTE] ALARME"; e com prioridade 1, 2 ou 3, começa com "ALARME".
- **CA24** — Dado um alarme que dispara antes do prazo, quando o aviso é gerado, então ele contém "vence em"; e se dispara depois do prazo, contém "venceu há".
- **CA25** — Dado uma tarefa `DOING`, quando seu alarme dispara, então o aviso contém "Tarefa em andamento."; e para uma tarefa `TODO`, contém "Tarefa ainda não iniciada.".
- **CA26** — Dado a tarefa "Estudar" com prazo em 20/11/2026 às 14:30, quando seu alarme dispara, então o aviso contém "Estudar" e "20/11/2026 às 14:30".

### TarefaService
- **CA27** — Dado que não há tarefas salvas, quando três tarefas são adicionadas, então elas recebem os IDs 1, 2 e 3.
- **CA28** — Dado que existem tarefas salvas com IDs 4 e 7, quando uma nova tarefa é adicionada, então ela recebe o ID 8.
- **CA29** — Dado uma tarefa com antecedências de 30 e 60 minutos, quando ela é adicionada, então fica com status `TODO`, dois alarmes, e os dados são salvos uma vez.
- **CA30** — Dado tarefas adicionadas com prioridades 2, 5 e 3, quando a lista é consultada, então a ordem é 5, 3, 2.
- **CA31** — Dado uma tarefa com antecedências 30 e 30 (ou 0), quando ela é adicionada, então ocorre um erro, a tarefa não aparece na lista e nada é salvo.
- **CA32** — Dado tarefas cadastradas, quando a lista devolvida por `listar` é alterada, então as tarefas do serviço não mudam.
- **CA33** — Dado que não há tarefas, quando se lista ou se filtra por categoria, prioridade ou status, então o resultado é uma lista vazia.
- **CA34** — Dado uma tarefa na categoria "Trabalho", quando se filtra por "trabalho" ou "TRABALHO", então ela é encontrada; e quando se filtra por "Casa" ou por categoria nula, então a lista vem vazia.
- **CA35** — Dado tarefas com prioridades e status diferentes, quando se filtra por uma prioridade ou por um status, então só aparecem as tarefas correspondentes.
- **CA36** — Dado uma tarefa existente, quando é buscada pelo ID, então ela é devolvida; e quando se busca um ID inexistente, então o resultado é nulo.
- **CA37** — Dado uma tarefa existente, quando seu status é alterado para `DOING`, então ela é devolvida com o novo status e os dados são salvos.
- **CA38** — Dado um ID inexistente, quando se tenta atualizar o status, atualizar a tarefa, deletar, adicionar alarme ou remover alarme, então o resultado é "falhou" (`null` ou `false`) e nada é salvo.
- **CA39** — Dado uma tarefa existente, quando todos os seus campos são atualizados, então os novos valores ficam gravados na tarefa, a lista é reordenada e os dados são salvos.
- **CA40** — Dado uma tarefa com alarme disparado, quando ela é atualizada com outro prazo, então o alarme volta a "não disparado"; e quando é atualizada com o mesmo prazo, então continua "disparado".
- **CA41** — Dado uma tarefa existente, quando ela é atualizada com prioridade 0, então ocorre um erro e nada é salvo.
- **CA42** — Dado uma tarefa existente, quando ela é deletada, então some da lista, o resultado é "sucesso" e os dados são salvos.
- **CA43** — Dado uma tarefa existente, quando um alarme de 15 minutos é adicionado, então a tarefa passa a tê-lo e os dados são salvos; e quando o alarme tem antecedência repetida ou 0, então ocorre um erro e nada é salvo.
- **CA44** — Dado uma tarefa com um alarme, quando ele é removido, então a tarefa fica sem alarmes e os dados são salvos; e quando a posição é inválida, então o resultado é "falhou" e nada é salvo.
- **CA45** — Dado uma tarefa com prioridade 3, quando o serviço sugere uma antecedência, então o resultado é 120 minutos.
- **CA46** — Dado uma tarefa com alarme vencido, quando os alarmes são verificados, então é devolvido um aviso e os dados são salvos; e quando nenhum alarme venceu, então nenhum aviso é devolvido e nada é salvo.

### TarefaRepository
- **CA47** — Dado um caminho de arquivo que não existe, quando as tarefas são carregadas, então o resultado é uma lista vazia.
- **CA48** — Dado duas tarefas com status e alarmes (um deles disparado), quando são salvas e depois carregadas por um novo repositório no mesmo arquivo, então todos os dados voltam iguais.
- **CA49** — Dado um arquivo com uma lista salva, quando outra lista é salva no mesmo arquivo, então ao carregar vem só a segunda lista.
- **CA50** — Dado um arquivo vazio, com conteúdo inválido ou com outro tipo de objeto, quando as tarefas são carregadas, então o resultado é uma lista vazia e nenhum erro é lançado.
- **CA51** — Dado uma lista vazia, quando ela é salva e carregada, então o resultado é uma lista vazia.
- **CA52** — Dado um caminho que é uma pasta, quando se tenta salvar, então nenhum erro é lançado.

### Gerais
- **CA53** — Dado o projeto com o JDK 25, quando `./gradlew test` é executado em `backend/`, então todos os testes passam (o teste do CA09 aparece como pendente/ignorado, sem quebrar o build).
- **CA54** — Dado os testes executados, quando o relatório JaCoCo é gerado, então ele mostra a cobertura de `model`, `service` e `repository`, sem a classe `Main`, e o build não falha por cobertura.
- **CA55** — Dado a feature concluída, quando se compara `backend/src/main` com a branch `main`, então não há nenhuma alteração.
- **CA56** — Dado a execução dos testes, quando ela termina, então o arquivo `backend/tarefas.dat` não foi criado nem alterado.

---

## 4. Impacto no que já existe

### Código de produção
**Nenhuma alteração em `backend/src/main`.** Isso inclui não corrigir os bugs encontrados e não fazer refatorações para facilitar os testes. Qualquer mudança dessas exige uma nova autorização.

### Arquivos alterados ou criados

| Arquivo | Mudança |
|---|---|
| `backend/build.gradle` | Adiciona o plugin `groovy`, o plugin `jacoco` (só relatório) e as dependências de teste aprovadas. |
| `backend/src/test/groovy/...` | Pasta nova com os 5 arquivos de teste (`AlarmeSpec`, `TarefaSpec`, `AlarmeServiceSpec`, `TarefaServiceSpec`, `TarefaRepositorySpec`). |
| `docs/spec.md` | Este documento. |
| `AGENTS.md` | Atualizado no final, para indicar Spock/JaCoCo, a pasta `src/test/groovy`, o sufixo `Spec` e os comandos de teste e de cobertura. Hoje ele fala em `src/test/java` e sufixo `Test`. |

### Dependências (todas de teste, aprovadas)
- Groovy 4
- `spock-core` 2.x (variante para Groovy 4)
- `byte-buddy` e `objenesis` (necessários para mockar a classe `TarefaRepository`)
- `junit-platform-launcher` (exigido pelo Gradle 9 para iniciar os testes; sem versão própria, segue a versão do JUnit já usada no projeto). Aprovado durante a Tarefa 1.
- JaCoCo (plugin do Gradle, sem biblioteca externa)

As dependências JUnit Jupiter que já existem **são mantidas**. Nenhuma dependência de produção é adicionada. O `.jar` da aplicação não muda.

### Riscos
- **Compatibilidade com o JDK 25.** O build roda com JDK 25 e o Java é compilado para essa versão. Versões antigas do Groovy podem não funcionar com ela. A primeira tarefa da implementação verifica isso, antes de escrever os testes. Se for necessário usar `options.release = 21` no `build.gradle`, isso será explicado e pedido antes.
- **Tempo de build.** A compilação do Groovy deixa `./gradlew build` um pouco mais lento. `./gradlew run` não é afetado.

### Forma de entrega
- Branch `feature/testes-unitarios`, um commit por etapa, mensagens em inglês.
- A spec é commitada antes de qualquer código.

---

## 5. Fora do escopo

- **Corrigir bugs do código de produção**, inclusive o do construtor de `Tarefa` (CA09) e os itens A CONFIRMAR da seção 2.4.
- **Qualquer alteração em `backend/src/main`**, inclusive refatorações.
- **Testar o `Main`**: menu, leitura de entradas, conversão de `30m`/`2h`/`1d`, mensagens no console e `System.exit`.
- **Testar a verificação automática a cada 30 segundos** (thread em segundo plano).
- **Testes de concorrência** (vários acessos ao mesmo tempo).
- **Testes de integração ou ponta a ponta** (aplicação inteira rodando, com arquivo real e console).
- **Testes do frontend** e qualquer ferramenta de teste para ele.
- **Meta de cobertura obrigatória.** O JaCoCo só gera relatório; o build não falha por cobertura.
- **Fixar a versão do Java no build** (`options.release = 21`), a menos que seja necessário para os testes funcionarem e autorizado.
- **Integração contínua (CI)**: não será configurada.
