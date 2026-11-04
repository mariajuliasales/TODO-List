# Todo List

Desenvolvido por Maria Julia Sales.

Aplicação web para organizar tarefas pessoais. 
Permite criar, editar, excluir, filtrar e acompanhar o andamento das tarefas por status (TODO, DOING e DONE).
  
## Funcionalidades
 
- Criar, editar e excluir tarefas
- Alterar o status de cada tarefa (TODO, DOING, DONE) 
- Buscar por nome e filtrar por status, categoria e prioridade
- Contadores com o total de tarefas e a quantidade em cada status
- Validação dos campos do formulário
- Layout responsivo
- Alarmes por tarefa no backend (um ou mais por tarefa, com antecedência configurável)

## Tecnologias
 
**Frontend**
- HTML5
- CSS3
- JavaScript
- LocalStorage

**Backend**
- Java

## Alarmes (backend)

- Ao cadastrar uma tarefa, informe a hora de término e responda **s** em "Acionar alarme?".
- A antecedência aceita minutos, horas ou dias (`30m`, `2h`, `1d`).
- Uma tarefa pode ter vários alarmes. Use a opção **6. Gerenciar Alarmes da Tarefa** para adicionar ou remover alarmes.
- Os alarmes são verificados ao abrir a aplicação e a cada 30 segundos enquanto ela está aberta. Cada alarme dispara uma única vez.
- Tarefas em `DONE` não disparam alarmes. Se o prazo da tarefa mudar, os alarmes voltam a valer.
- As tarefas são salvas no arquivo `tarefas.dat`, criado na pasta em que a aplicação é executada.