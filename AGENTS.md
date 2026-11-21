# AGENTS.md

Guia para agentes de IA que trabalham neste repositório. Itens marcados como **A CONFIRMAR** não puderam ser verificados no código e devem ser confirmados com a mantenedora antes de qualquer ação baseada neles.

## Visão geral

Aplicação **Todo List** para organizar tarefas pessoais. O repositório tem duas partes **independentes** (o frontend não chama o backend; não há API HTTP):

- **Backend (`backend/`)**: aplicação Java de **console** (menu em texto, via `Scanner`) com persistência em arquivo.
- **Frontend (`frontend/`)**: aplicação web estática em HTML/CSS/JavaScript com persistência no `localStorage` do navegador.

### Funcionalidades do backend (console)

Menu principal em [backend/src/main/java/Main.java](backend/src/main/java/Main.java):

1. **Cadastrar tarefa**: nome, descrição, data de término (`dd/MM/yyyy`), hora de término (`HH:mm`, padrão `23:59`), categoria, prioridade (1 a 5) e alarmes opcionais.
2. **Listar tarefas**: todas, ou filtradas por **categoria** (sem diferenciar maiúsculas/minúsculas), **prioridade** ou **status**.
3. **Atualizar tarefa**: altera todos os campos; se o prazo mudar, os alarmes são reiniciados.
4. **Atualizar status**: `TODO`, `DOING`, `DONE` (`StatusEnum`). Toda tarefa nova começa como `TODO`.
5. **Deletar tarefa** por ID.
6. **Gerenciar alarmes da tarefa**: adicionar/remover alarmes.

Regras dos alarmes:
- Uma tarefa pode ter vários alarmes; não pode haver dois com a mesma antecedência.
- A antecedência é informada como `30m`, `2h`, `1d` (ou número puro, em minutos) e deve ser maior que zero.
- A antecedência sugerida depende da prioridade: prioridade ≥ 4 → 1 dia; 3 → 2 h; demais → 30 min (`AlarmeService.sugerirAntecedencia`).
- Os alarmes são verificados ao iniciar e a cada 30 segundos (thread daemon `verificador-alarmes`). Cada alarme dispara uma única vez.
- Tarefas `DONE` não disparam alarmes. Prioridade ≥ 4 gera aviso `[URGENTE]`.
- As tarefas são mantidas ordenadas por prioridade (maior primeiro).
- Persistência: serialização Java (`ObjectOutputStream`) no arquivo `tarefas.dat`, criado no diretório de execução (com `gradlew run`, é `backend/`). O arquivo está no `.gitignore`.

### Funcionalidades do frontend (web)

Conforme o README e o código em `frontend/js/`:
- Criar, editar e excluir tarefas (formulário em modal, com validação de campos).
- Alterar status (`TODO`, `DOING`, `DONE`).
- Buscar por nome e filtrar por status, categoria e prioridade.
- Contadores do total e da quantidade por status.
- Layout responsivo.
- Categorias fixas: `Estudos`, `Trabalho`, `Pessoal`, `Casa`, `Outros` ([frontend/js/config/constants.js](frontend/js/config/constants.js)).
- Dados salvos no `localStorage` com a chave `todo-list-tasks`.
- **Não há alarmes no frontend** (só no backend).

## Stack e estrutura

| Item | Detalhe |
|---|---|
| Linguagem backend | Java (sem framework; apenas biblioteca padrão) |
| Versão do Java | **Java 21** (versão-alvo do projeto). Atenção: o `build.gradle` ainda não fixa a versão (sem toolchain/`sourceCompatibility`), então o Gradle usa o JDK que estiver instalado. Não use recursos de Java posteriores ao 21. Fixar a versão no `build.gradle` exige autorização (ver Restrições). |
| Build | Gradle via wrapper (`gradlew`), versão **9.6.0** ([gradle-wrapper.properties](backend/gradle/wrapper/gradle-wrapper.properties)); plugins `java`, `application` e `groovy` |
| Testes | **Spock 2.5** (`spock-core:2.5-groovy-4.0`) com **Groovy 4.0.33**, rodando na JUnit Platform (`useJUnitPlatform()`). Para mocks de classes: `byte-buddy` 1.18.17 e `objenesis` 3.6. `junit-platform-launcher` (exigido pelo Gradle 9 para iniciar os testes). JUnit Jupiter 5 (BOM `5.10.0`) continua declarado. Todas são dependências só de teste. |
| Dependências de produção | Nenhuma |
| Frontend | HTML5, CSS3, JavaScript (ES modules, sem framework, sem bundler, sem `package.json`), LocalStorage |

### Estrutura de pastas

```
TODO-List/
├── README.md
├── AGENTS.md
├── backend/                     # Aplicação Java de console (projeto Gradle "todolist")
│   ├── build.gradle             # group org.example, version 1.0-SNAPSHOT, mainClass = 'Main'
│   ├── settings.gradle
│   ├── gradlew / gradlew.bat / gradle/wrapper/
│   ├── src/main/java/
│   │   ├── Main.java            # Ponto de entrada e toda a interação com o usuário (menu, leitura e conversão de entradas)
│   │   ├── model/               # Entidades: Tarefa, Alarme, StatusEnum (Serializable)
│   │   ├── repository/          # TarefaRepository: lê/grava a lista de tarefas em arquivo
│   │   └── service/             # Regras de negócio: TarefaService (CRUD, filtros, alarmes) e AlarmeService (disparo e mensagens)
│   └── src/test/groovy/         # Testes Spock, nos mesmos pacotes de src/main/java
│       ├── model/               # Ex.: AlarmeSpec.groovy
│       ├── repository/
│       └── service/
└── frontend/                    # Aplicação web estática
    ├── index.html               # Página única; carrega js/main.js como módulo
    ├── css/                     # base.css, components.css, modal.css, responsive.css
    └── js/
        ├── main.js              # Orquestra a tela
        ├── config/constants.js  # Chave do storage, status e categorias
        ├── models/task.js       # Validação da tarefa
        ├── services/            # taskRepository.js (localStorage) e taskService.js (filtros, contagens)
        ├── ui/                  # filters.js, modal.js, taskForm.js, taskList.js
        └── utils/format.js      # Formatação
```

### Testes

- Os testes são escritos em **Groovy com Spock**, em `backend/src/test/groovy/`, no mesmo pacote da classe testada (ex.: `model.Alarme` → `src/test/groovy/model/AlarmeSpec.groovy`).
- Cada classe de teste tem o sufixo **`Spec`** e estende `spock.lang.Specification`.
- Nomes dos métodos de teste em **português**, como frases descritivas (ex.: `def "deve lançar erro quando a antecedência for zero"()`).
- Use os blocos `given:` / `when:` / `then:` (e `where:` para tabelas de dados), com uma descrição curta em cada bloco.
- A especificação e o plano desta suíte de testes estão em [docs/spec.md](docs/spec.md) e [docs/plano.md](docs/plano.md).

## Como executar e testar

Todos os comandos do backend são executados dentro de `backend/`:

```bash
cd backend

# Compilar
./gradlew build

# Executar a aplicação (console interativo; stdin já está ligado no build.gradle)
./gradlew run

# Executar os testes (Spock)
./gradlew test

# Executar uma classe de teste específica
./gradlew test --tests 'model.AlarmeSpec'
```

No Windows, use `gradlew.bat` no lugar de `./gradlew`.

Observações:
- `./gradlew run` usa `backend/` como diretório de trabalho, então lê/grava `backend/tarefas.dat`. Em testes, **não** use esse arquivo: passe ao `TarefaRepository` um caminho temporário (ex.: `@TempDir` do Spock, `spock.lang.TempDir`).
- O resultado dos testes fica em `backend/build/reports/tests/test/index.html`.
- `Main` chama `System.exit(0)` ao sair e usa `LocalDateTime.now()`; para testar alarmes, chame `TarefaService.verificarAlarmes(LocalDateTime)` / `AlarmeService.verificar(...)` com um horário fixo.

**Frontend**: não há build. Execute com a extensão **Live Server** (VS Code): abra `frontend/index.html` e use "Open with Live Server". Não abra o arquivo direto no navegador (`file://`), porque o `index.html` carrega `js/main.js` como módulo (`type="module"`) e isso pode ser bloqueado.

**Testes do frontend**: não existem e, por enquanto, não serão criados. Não crie testes nem configure ferramentas de teste para o frontend.

## Convenções

### Backend (Java)
- **Idioma**: nomes de domínio, métodos, variáveis e mensagens ao usuário em **português** (`Tarefa`, `Alarme`, `adicionar`, `listarPorCategoria`, `antecedenciaMinutos`). Exceção: o enum `StatusEnum` e seus valores (`TODO`, `DOING`, `DONE`).
- **Pacotes**: nomes simples em minúsculo, sem prefixo de domínio (`model`, `repository`, `service`). `Main` fica no pacote padrão.
- **Classes**: PascalCase; sufixo por camada: `XxxService`, `XxxRepository`; enums com sufixo `Enum`.
- **Métodos**: camelCase, verbos no infinitivo (`adicionar`, `atualizar`, `deletar`, `listar`, `buscarPorId`, `verificarAlarmes`); getters/setters no padrão JavaBean (`getNome`, `isDisparado`).
- **Constantes**: `private static final` em UPPER_SNAKE_CASE (`HORA_PADRAO`, `INTERVALO_VERIFICACAO_SEGUNDOS`).
- **Camadas** (não há controller nem framework web):
  - `Main` → interface de console: lê entrada, converte formatos, imprime resultados. Não contém regra de negócio.
  - `service` → regras de negócio. `TarefaService` mantém a lista em memória, gera IDs (`AtomicInteger`), ordena por prioridade e persiste após cada alteração. Métodos públicos que acessam a lista são `synchronized` (por causa da thread de alarmes).
  - `repository` → somente leitura/gravação do arquivo.
  - `model` → entidades com validações simples (lançam `IllegalArgumentException`).
- **Retornos**: operações por ID retornam `boolean` (sucesso/não encontrado) ou `null` quando a tarefa não existe.
- **Indentação**: 4 espaços.
- **Commits**: Conventional Commits (`feat(escopo): ...`, `docs: ...`, `chore(build): ...`, `refactor: ...`, `test: ...`), **sempre em inglês**. Os commits antigos em português são anteriores a essa regra.
- **Branches**: prefixos `feat/...` e `feature/...` aparecem no histórico; PRs são abertos contra `main`.

### Frontend (JavaScript)
- Código em **inglês** (`taskService`, `filterTasks`, `showCounters`), textos da interface em português.
- Arquivos em camelCase, separados em `config/`, `models/`, `services/`, `ui/`, `utils/`; ES modules com `import`/`export`.

## Restrições

Pergunte antes de:

- **Alterar código de produção (`backend/src/main`)**, inclusive refatorações pequenas para facilitar testes. Se um teste revelar um bug, descreva-o e peça autorização antes de corrigir.
- **Alterar o frontend (`frontend/`)**, de qualquer forma.
- **Adicionar, remover ou atualizar dependências** (no `build.gradle` ou em qualquer outro lugar), inclusive bibliotecas de teste (ex.: Mockito, AssertJ). Avise qual e por quê.
- Alterar arquivos de build (`build.gradle`, `settings.gradle`, wrapper do Gradle).
- Fazer commit, push, criar branches ou abrir PRs.
- Apagar ou modificar `tarefas.dat` ou outros dados locais do usuário.

Nunca:

- **Incluir chaves de API, tokens, senhas, credenciais ou dados sensíveis** em arquivos, testes, logs ou commits.
- Criar testes que leiam ou gravem o `tarefas.dat` real.
