---
name: feature-com-spec
description: Implementa uma feature seguindo o fluxo AGENTS.md → spec → plano → implementação → validação, com revisão da pessoa entre cada etapa, uma tarefa e um commit por vez. Use quando pedirem para implementar uma feature de forma planejada, com spec e plano antes do código.
---

# Feature com spec

## 1. Objetivo

Implementar uma feature seguindo o fluxo **spec → plano → implementação**.

Cada etapa gera um documento ou uma mudança pequena, e a pessoa revisa antes de seguir. Nenhum código é escrito antes de a spec ser aprovada.

## 2. Entradas

- **Descrição da feature:** o que deve ser feito, em poucas frases.
- **Projeto:** o repositório onde a feature será implementada.

Se faltar alguma das duas, pergunte antes de começar.

## 3. Passo a passo

Em todas as etapas:
- Use só informações encontradas no projeto ou dadas pela pessoa. Quando não tiver certeza, escreva **A CONFIRMAR** e liste esses itens no fim da resposta.
- Ao terminar a etapa, pare e espere a revisão. Não avance sozinho para a próxima.

### Etapa 1 – AGENTS.md

Se o projeto já tiver um `AGENTS.md`, leia e use. Se não tiver, crie.

**O agente gera:** o arquivo `AGENTS.md` na raiz, com estas seções:
- **Visão geral:** o que a aplicação faz e as funcionalidades existentes.
- **Stack e estrutura:** linguagens, frameworks, versões, ferramenta de build e o papel de cada pasta principal.
- **Como executar e testar:** comandos exatos.
- **Convenções:** nomes de classes, métodos e pacotes; organização em camadas; idioma do código e dos commits.
- **Restrições:** o que não fazer sem perguntar. No mínimo:
  - não alterar código de produção sem autorização;
  - não adicionar dependências sem avisar;
  - nunca incluir chaves, senhas ou dados sensíveis em arquivos ou commits;
  - não apagar nem alterar dados locais da pessoa.

**A pessoa revisa:**
- se a descrição do projeto está correta;
- os itens A CONFIRMAR;
- se as restrições cobrem o que importa para ela.

### Etapa 2 – Perguntas de esclarecimento

**O agente gera:** de 5 a 10 perguntas numeradas sobre a feature, cada uma com uma **sugestão de resposta**. Cubra:
- o que entra e o que fica fora do escopo;
- os comportamentos e casos de borda mais importantes;
- dependências novas e mudanças no build;
- metas (desempenho, cobertura etc.), se houver;
- como entregar no Git (branch, commits, idioma).

Antes de perguntar, explore o código. Se encontrar algo que muda a feature (por exemplo, um bug ou uma limitação), conte na pergunta.

**A pessoa revisa:** responde às perguntas. Pergunta sem resposta = sugestão aceita. Diga isso na mensagem.

### Etapa 3 – Spec

**O agente gera:** o arquivo `docs/spec.md`, usando o modelo da seção 5, com:
- contexto e objetivo;
- regras de negócio por parte do sistema, com comportamentos esperados e casos de borda;
- critérios de aceite numerados (CA01, CA02...) no formato Dado / Quando / Então;
- impacto no que já existe;
- o que fica fora do escopo.

A spec descreve o comportamento **atual** do código e o comportamento **desejado** da feature. Se encontrar um bug fora do escopo, registre como "bug conhecido" e deixe a correção em "Fora do escopo".

**A pessoa revisa:**
- se os critérios de aceite descrevem o que ela quer;
- os itens A CONFIRMAR;
- os bugs e comportamentos estranhos encontrados.

Quando estiver tudo certo, ela aprova. Mude o status da spec para **aprovada** e remova as marcações A CONFIRMAR resolvidas.

### Etapa 4 – Plano

**O agente gera:** o arquivo `docs/plano.md`, usando o modelo da seção 5:
- tarefas pequenas e numeradas, cada uma feita e revisada em poucos minutos;
- **Tarefa 1:** preparar o ambiente (dependências, configuração) e provar que funciona com um exemplo mínimo;
- se o `AGENTS.md` precisar mudar por causa da feature, uma tarefa logo depois da Tarefa 1;
- tarefas organizadas da parte mais simples para a mais complexa;
- **última tarefa:** verificação final e, se fizer sentido, um teste manual feito pela pessoa;
- uma tabela no final relacionando **cada** critério de aceite às tarefas que o cobrem.

Confira que todo critério da spec aparece na tabela.

**A pessoa revisa:**
- o tamanho das tarefas (dividir as grandes);
- a ordem das tarefas;
- se todo critério está coberto.

### Etapa 5 – Implementação (uma tarefa por vez)

A pessoa pede uma tarefa pelo número. Para cada tarefa:

**O agente faz:**
1. Lê de novo o `AGENTS.md`, a spec e o plano, porque eles podem ter mudado.
2. Confere o estado do repositório (`git status`, último commit).
3. Implementa **só** essa tarefa e altera **só** os arquivos listados nela.
4. Roda a validação da tarefa e também a suíte completa de testes.
5. Responde com:
   - os arquivos criados ou alterados;
   - um resumo do que cada teste ou mudança verifica, com o critério de aceite correspondente;
   - o comando de validação e o resultado obtido;
   - uma sugestão de mensagem de commit.

**O agente para e pergunta quando:**
- precisa de uma dependência ou mudança no build que não foi aprovada (explique o motivo e mostre a solução testada, se possível);
- precisaria alterar código fora do escopo;
- encontra um comportamento que contradiz a spec.

**A pessoa revisa:**
- roda a validação;
- confere o código;
- marca a tarefa como `[x]` no plano;
- faz o commit da tarefa.

### Etapa 6 – Validação

**O agente gera:** uma verificação dos critérios de aceite, sem alterar arquivos:
- uma tabela com as colunas **Critério**, **Atendido?** (sim / não / parcial), **Teste que cobre** (arquivo e método) e **Observação**;
- a lista de casos de borda da spec que não têm teste.

**A pessoa revisa:** os itens "não" e "parcial" e os casos de borda sem teste.

Se faltar algo, o ciclo se repete:
1. Novos critérios na spec, continuando a numeração.
2. Uma nova tarefa no fim do plano, com a tabela atualizada.
3. Implementação da tarefa.
4. Validação de novo.

## 4. Regras

- **Não escrever código antes da spec aprovada.** Antes disso, só leitura, perguntas e documentos.
- **Implementar uma tarefa por vez.** Só a tarefa pedida, mesmo que a próxima pareça simples.
- **Fazer um commit por tarefa.** O commit inclui a marcação `[x]` da tarefa no plano. O agente só faz commit quando a pessoa pedir.
- **Não alterar nada fora do escopo.** Só os arquivos listados na tarefa. Código de produção, build, dependências e dados locais só com autorização.
- **Se a spec estiver errada, atualizar a spec antes do código.** Depois, atualizar o plano. Só então implementar.
- **Bug encontrado fora do escopo:** não corrigir. Registrar na spec como "bug conhecido" e, se for o caso, escrever um teste do comportamento correto, marcado como pendente (o mecanismo de "teste esperado falhar" do framework de testes do projeto), com um comentário explicando o bug.
- **Relatar com honestidade:** se um teste falhar, mostrar a saída. Se uma validação não foi feita, dizer que não foi feita.

## 5. Modelos

### docs/spec.md

```markdown
# Spec: <nome da feature>

- **Branch:** `<nome-da-branch>`
- **Status:** aguardando aprovação
- **Referência do projeto:** [AGENTS.md](../AGENTS.md)

---

## 1. Contexto e objetivo

### Contexto
<Situação atual e o problema que a feature resolve.>

### Objetivo
<O que a feature deve entregar, em poucos itens.>

### Como será feito
<Decisões técnicas principais: onde ficam os arquivos, convenções, ferramentas.>

---

## 2. Regras de negócio

### 2.1 <Parte do sistema>

**Comportamentos esperados**
- <comportamento>

**Casos de borda**
- <caso de borda: entrada → resultado esperado>

**Bug conhecido: <título>** (se houver)
- <o que acontece hoje>
- <como será tratado nos testes>
- A correção está fora do escopo (seção 5).

### 2.2 <Parte do sistema>
...

---

## 3. Critérios de aceite

### <Parte do sistema>
- **CA01** — Dado <situação>, quando <ação>, então <resultado esperado>.
- **CA02** — Dado ..., quando ..., então ...

### Gerais
- **CAxx** — Dado <a feature concluída>, quando <a suíte de testes é executada>, então <todos passam>.
- **CAxx** — Dado <a feature concluída>, quando <se compara o código fora do escopo com a branch principal>, então <não há alterações>.

---

## 4. Impacto no que já existe

### Código de produção
<O que muda ou "Nenhuma alteração em <pasta>".>

### Arquivos alterados ou criados

| Arquivo | Mudança |
|---|---|
| `<caminho>` | <o que muda> |

### Dependências
- <dependência e motivo>

### Riscos
- <risco e como será verificado>

### Forma de entrega
- Branch `<nome>`, um commit por tarefa do plano, mensagens em <idioma>.
- A spec é commitada antes de qualquer código.

---

## 5. Fora do escopo

- <o que esta feature não vai fazer>
```

### docs/plano.md

```markdown
# Plano de implementação: <nome da feature>

- **Spec:** [docs/spec.md](spec.md)
- **Branch:** `<nome-da-branch>`
- **Ordem:** <critério de ordenação das tarefas>

## Regras para todas as tarefas

- <onde rodar os comandos>
- Nenhuma tarefa altera <pasta fora do escopo>.
- Um commit por tarefa, no formato <padrão de commit>. Ao concluir a tarefa, marque `[x]` no título dela, no mesmo commit.
- Ao final de cada tarefa, <comando da suíte completa> deve passar.

## Antes de começar

Commitar `docs/spec.md` e `docs/plano.md` antes de qualquer código.

---

## [ ] Tarefa 1 – Preparar o ambiente e provar que funciona
- O que será feito:
- Arquivos criados ou alterados:
- Critérios de aceite relacionados:
- Como validar: (comando a rodar e resultado esperado)

## [ ] Tarefa 2 – <título curto>
- O que será feito:
- Arquivos criados ou alterados:
- Critérios de aceite relacionados:
- Como validar:

## [ ] Tarefa N – Verificação final
- O que será feito: verificar os critérios gerais e, se fizer sentido, um teste manual feito pela pessoa.
- Arquivos criados ou alterados: `docs/plano.md`
- Critérios de aceite relacionados:
- Como validar:

---

## Critérios de aceite × tarefas

| Critério | Tarefas |
|---|---|
| CA01 | |
| CA02 | |
```
