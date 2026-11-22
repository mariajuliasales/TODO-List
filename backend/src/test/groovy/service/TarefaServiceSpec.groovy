package service

import model.Alarme
import model.StatusEnum
import model.Tarefa
import repository.TarefaRepository
import spock.lang.PendingFeature
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalTime

class TarefaServiceSpec extends Specification {

    private static final LocalDate DATA_TERMINO = LocalDate.of(2026, 11, 20)
    private static final LocalTime HORA_TERMINO = LocalTime.of(14, 30)

    /*
     * O repositório é um mock: nenhum arquivo é lido ou gravado.
     * O construtor do TarefaService chama repository.carregar() e usa o resultado,
     * então todo teste precisa definir o retorno de carregar() ANTES de criar o serviço.
     */
    TarefaRepository repository = Mock()

    def "deve gerar IDs sequenciais a partir de 1 quando não há tarefas salvas"() {
        given: "um repositório sem tarefas salvas"
        repository.carregar() >> []
        def service = new TarefaService(repository)

        when: "três tarefas são adicionadas"
        def primeira = adicionar(service, "Primeira")
        def segunda = adicionar(service, "Segunda")
        def terceira = adicionar(service, "Terceira")

        then: "as tarefas recebem os IDs 1, 2 e 3"
        [primeira, segunda, terceira]*.id == [1, 2, 3]
    }

    def "deve continuar a partir do maior ID salvo quando já existem tarefas"() {
        given: "um repositório com tarefas salvas de IDs 4 e 7"
        repository.carregar() >> [novaTarefa(4, "Quatro"), novaTarefa(7, "Sete")]
        def service = new TarefaService(repository)

        when: "uma nova tarefa é adicionada"
        def nova = adicionar(service, "Nova")

        then: "ela recebe o ID 8"
        nova.id == 8
    }

    def "deve adicionar a tarefa com status TODO e os alarmes informados, salvando uma vez"() {
        given: "um serviço sem tarefas"
        repository.carregar() >> []
        def service = new TarefaService(repository)

        when: "uma tarefa é adicionada com alarmes de 30 e 60 minutos"
        def tarefa = adicionar(service, "Estudar", 3, [30, 60])

        then: "os dados são salvos uma única vez"
        1 * repository.salvar(_)

        and: "a tarefa fica com status TODO e os dois alarmes, ainda não disparados"
        tarefa.status == StatusEnum.TODO
        tarefa.alarmes*.antecedenciaMinutos == [30, 60]
        tarefa.alarmes.every { !it.disparado }

        and: "a tarefa aparece na lista do serviço"
        service.listar()*.id == [tarefa.id]
    }

    def "deve adicionar a tarefa sem alarmes quando nenhuma antecedência é informada"() {
        given: "um serviço sem tarefas"
        repository.carregar() >> []
        def service = new TarefaService(repository)

        when: "uma tarefa é adicionada com a lista de antecedências vazia"
        def tarefa = adicionar(service, "Mercado", 2, [])

        then: "a tarefa é criada sem alarmes"
        tarefa.alarmes.isEmpty()
        service.listar()*.nome == ["Mercado"]
    }

    def "deve manter a lista ordenada da maior para a menor prioridade ao adicionar"() {
        given: "um serviço sem tarefas"
        repository.carregar() >> []
        def service = new TarefaService(repository)

        when: "são adicionadas tarefas com prioridades 2, 5 e 3, nessa ordem"
        adicionar(service, "Prioridade 2", 2)
        adicionar(service, "Prioridade 5", 5)
        adicionar(service, "Prioridade 3", 3)

        then: "a lista fica na ordem de prioridade 5, 3, 2"
        service.listar()*.prioridade == [5, 3, 2]
    }

    def "deve lançar erro sem adicionar nem salvar quando as antecedências são #antecedencias"() {
        given: "um serviço sem tarefas"
        repository.carregar() >> []
        def service = new TarefaService(repository)

        when: "uma tarefa é adicionada com um alarme inválido"
        adicionar(service, "Inválida", 3, antecedencias)

        then: "ocorre um erro de argumento inválido"
        thrown(IllegalArgumentException)

        and: "nada é salvo"
        0 * repository.salvar(_)

        and: "a tarefa não aparece na lista"
        service.listar().isEmpty()

        // O ID consumido pela tentativa que falhou não é verificado: comportamento aceito (spec, seção 2.4).

        where:
        antecedencias << [[30, 30], [0]]
    }

    def "não deve alterar as tarefas do serviço quando a lista devolvida por listar é alterada"() {
        given: "um serviço com duas tarefas salvas"
        repository.carregar() >> [novaTarefa(1, "Estudar"), novaTarefa(2, "Mercado")]
        def service = new TarefaService(repository)

        when: "a lista devolvida por listar é esvaziada"
        service.listar().clear()

        then: "o serviço continua com as duas tarefas"
        service.listar()*.nome == ["Estudar", "Mercado"]
    }

    def "deve devolver listas vazias ao listar e filtrar quando não há tarefas"() {
        given: "um serviço sem tarefas"
        repository.carregar() >> []
        def service = new TarefaService(repository)

        when: "se lista e se filtra por categoria, prioridade e status"
        def todas = service.listar()
        def porCategoria = service.listarPorCategoria("Estudos")
        def porPrioridade = service.listarPorPrioridade(3)
        def porStatus = service.listarPorStatus(StatusEnum.TODO)

        then: "todos os resultados são listas vazias"
        todas.isEmpty()
        porCategoria.isEmpty()
        porPrioridade.isEmpty()
        porStatus.isEmpty()
    }

    def "deve filtrar pela categoria #categoria sem diferenciar maiúsculas e minúsculas"() {
        given: "uma tarefa na categoria Trabalho e outra na categoria Estudos"
        repository.carregar() >> [
                novaTarefa(1, "Relatório", 3, "Trabalho"),
                novaTarefa(2, "Prova", 3, "Estudos")
        ]
        def service = new TarefaService(repository)

        when: "se filtra pela categoria"
        def resultado = service.listarPorCategoria(categoria)

        then: "só aparecem as tarefas da categoria, ou nenhuma"
        resultado*.nome == nomesEsperados

        where:
        categoria  || nomesEsperados
        "Trabalho" || ["Relatório"]
        "trabalho" || ["Relatório"]
        "TRABALHO" || ["Relatório"]
        "Casa"     || []
        null       || []
    }

    def "deve filtrar apenas as tarefas com a prioridade informada"() {
        given: "tarefas com prioridades 5, 3 e 3"
        repository.carregar() >> [
                novaTarefa(1, "Urgente", 5),
                novaTarefa(2, "Média A", 3),
                novaTarefa(3, "Média B", 3)
        ]
        def service = new TarefaService(repository)

        when: "se filtra pela prioridade 3"
        def resultado = service.listarPorPrioridade(3)

        then: "só aparecem as tarefas com prioridade 3"
        resultado*.nome == ["Média A", "Média B"]
    }

    def "deve filtrar apenas as tarefas com o status informado"() {
        given: "uma tarefa em cada status"
        repository.carregar() >> [
                novaTarefa(1, "A fazer", 3, "Estudos", StatusEnum.TODO),
                novaTarefa(2, "Fazendo", 3, "Estudos", StatusEnum.DOING),
                novaTarefa(3, "Feita", 3, "Estudos", StatusEnum.DONE)
        ]
        def service = new TarefaService(repository)

        when: "se filtra pelo status DOING"
        def resultado = service.listarPorStatus(StatusEnum.DOING)

        then: "só aparece a tarefa com status DOING"
        resultado*.nome == ["Fazendo"]
    }

    def "deve encontrar a tarefa pelo ID quando ela existe"() {
        given: "um serviço com tarefas de IDs 4 e 7"
        repository.carregar() >> [novaTarefa(4, "Quatro"), novaTarefa(7, "Sete")]
        def service = new TarefaService(repository)

        when: "a tarefa de ID 7 é buscada"
        def tarefa = service.buscarPorId(7)

        then: "a tarefa é devolvida"
        tarefa.id == 7
        tarefa.nome == "Sete"
    }

    def "deve devolver nulo ao buscar um ID inexistente"() {
        given: "um serviço com tarefas de IDs 4 e 7"
        repository.carregar() >> [novaTarefa(4, "Quatro"), novaTarefa(7, "Sete")]
        def service = new TarefaService(repository)

        when: "o ID 99 é buscado"
        def tarefa = service.buscarPorId(99)

        then: "o resultado é nulo"
        tarefa == null
    }

    def "deve atualizar o status da tarefa e salvar"() {
        given: "uma tarefa salva com status TODO"
        repository.carregar() >> [novaTarefa(1, "Estudar")]
        def service = new TarefaService(repository)

        when: "o status é alterado para DOING"
        def tarefa = service.atualizarStatus(1, StatusEnum.DOING)

        then: "os dados são salvos uma única vez"
        1 * repository.salvar(_)

        and: "a tarefa é devolvida com o novo status"
        tarefa.id == 1
        tarefa.status == StatusEnum.DOING
        service.buscarPorId(1).status == StatusEnum.DOING
    }

    def "deve informar falha e não salvar ao tentar #operacao com ID inexistente"() {
        given: "um serviço com uma tarefa de ID 1, que tem um alarme"
        def tarefa = novaTarefa(1, "Estudar")
        tarefa.adicionarAlarme(new Alarme(30))
        repository.carregar() >> [tarefa]
        def service = new TarefaService(repository)

        when: "a operação é feita com o ID 99, que não existe"
        def resultado = acao(service)

        then: "o resultado indica falha"
        resultado == resultadoEsperado

        and: "nada é salvo"
        0 * repository.salvar(_)

        and: "a tarefa existente não é alterada"
        service.listar()*.id == [1]
        tarefa.status == StatusEnum.TODO
        tarefa.alarmes*.antecedenciaMinutos == [30]

        where:
        operacao                 | acao                                                       || resultadoEsperado
        "atualizar o status"     | { TarefaService s -> s.atualizarStatus(99, StatusEnum.DONE) } || null
        "atualizar a tarefa"     | { TarefaService s ->
                                       s.atualizar(99, "Novo", "Nova", DATA_TERMINO, HORA_TERMINO, 4, "Casa") } || false
        "deletar"                | { TarefaService s -> s.deletar(99) }                       || false
        "adicionar alarme"       | { TarefaService s -> s.adicionarAlarme(99, 60) }           || false
        "remover alarme"         | { TarefaService s -> s.removerAlarme(99, 0) }              || false
    }

    def "deve atualizar todos os campos da tarefa, reordenar a lista e salvar"() {
        given: "tarefas salvas com prioridades 4 e 2"
        repository.carregar() >> [novaTarefa(1, "Alta", 4), novaTarefa(2, "Baixa", 2)]
        def service = new TarefaService(repository)

        when: "a tarefa de prioridade 2 é atualizada com novos valores e prioridade 5"
        def atualizado = service.atualizar(2, "Relatório", "Entregar relatório",
                LocalDate.of(2026, 12, 1), LocalTime.of(9, 0), 5, "Trabalho")

        then: "os dados são salvos uma única vez"
        1 * repository.salvar(_)

        and: "a atualização tem sucesso e a tarefa fica com os novos valores"
        atualizado
        def tarefa = service.buscarPorId(2)
        tarefa.nome == "Relatório"
        tarefa.descricao == "Entregar relatório"
        tarefa.dataTermino == LocalDate.of(2026, 12, 1)
        tarefa.horaTermino == LocalTime.of(9, 0)
        tarefa.prioridade == 5
        tarefa.categoria == "Trabalho"

        and: "a lista é reordenada pela nova prioridade"
        service.listar()*.id == [2, 1]
    }

    def "deve deixar o alarme #estadoEsperado quando a tarefa é atualizada com #situacao"() {
        given: "uma tarefa com prazo em 20/11/2026 às 14:30 e um alarme já disparado"
        def tarefa = novaTarefa(1, "Estudar")
        tarefa.adicionarAlarme(new Alarme(30))
        tarefa.alarmes[0].disparado = true
        repository.carregar() >> [tarefa]
        def service = new TarefaService(repository)

        when: "a tarefa é atualizada"
        service.atualizar(1, "Estudar", "Revisar Spock", novaData, novaHora, 3, "Estudos")

        then: "o alarme fica no estado esperado"
        tarefa.alarmes[0].disparado == disparadoEsperado

        where:
        situacao       | novaData                    | novaHora              || disparadoEsperado
        "outro prazo"  | LocalDate.of(2026, 11, 21)  | HORA_TERMINO          || false
        "outra hora"   | DATA_TERMINO                | LocalTime.of(16, 0)   || false
        "o mesmo prazo"| DATA_TERMINO                | HORA_TERMINO          || true

        estadoEsperado = disparadoEsperado ? "disparado" : "não disparado"
    }

    def "deve lançar erro e não salvar quando a tarefa é atualizada com prioridade 0"() {
        given: "uma tarefa salva com prioridade 3"
        repository.carregar() >> [novaTarefa(1, "Estudar")]
        def service = new TarefaService(repository)

        when: "a tarefa é atualizada com prioridade 0"
        service.atualizar(1, "Estudar", "Revisar Spock", DATA_TERMINO, HORA_TERMINO, 0, "Estudos")

        then: "ocorre um erro de argumento inválido"
        thrown(IllegalArgumentException)

        and: "nada é salvo"
        0 * repository.salvar(_)
    }

    /*
     * BUG CONHECIDO (spec, seção 2.4 / CA57): TarefaService.atualizar chama setNome,
     * setDescricao, setDataTermino e setHoraTermino ANTES de setPrioridade. Quando a
     * prioridade é inválida, a exceção só acontece depois que esses campos já foram
     * alterados na tarefa em memória, e nada é salvo. A tarefa fica parcialmente alterada.
     *
     * Este teste descreve o comportamento correto (nenhum campo muda) e está marcado com
     * @PendingFeature: enquanto o bug existir, ele falha e o Spock o reporta como ignorado
     * (skipped), sem quebrar o build. Quando o bug for corrigido, o teste passará e o Spock
     * acusará erro, avisando que a anotação @PendingFeature deve ser removida.
     * A correção está fora do escopo desta feature.
     */
    @PendingFeature(reason = "atualizar altera a tarefa parcialmente quando a prioridade é inválida (CA57)")
    def "não deve alterar nenhum campo da tarefa quando a atualização tem prioridade inválida"() {
        given: "uma tarefa salva com seus valores originais"
        repository.carregar() >> [novaTarefa(1, "Estudar")]
        def service = new TarefaService(repository)
        def tarefa = service.buscarPorId(1)

        when: "a tarefa é atualizada com novos valores e prioridade 0"
        service.atualizar(1, "Novo nome", "Nova descrição",
                LocalDate.of(2026, 12, 1), LocalTime.of(9, 0), 0, "Trabalho")

        then: "ocorre um erro de argumento inválido"
        thrown(IllegalArgumentException)

        and: "nenhum campo da tarefa muda"
        tarefa.nome == "Estudar"
        tarefa.descricao == "Descrição de Estudar"
        tarefa.dataTermino == DATA_TERMINO
        tarefa.horaTermino == HORA_TERMINO
        tarefa.prioridade == 3
        tarefa.categoria == "Estudos"
    }

    private static Tarefa adicionar(TarefaService service, String nome, int prioridade = 3, List<Integer> antecedencias = []) {
        service.adicionar(nome, "Descrição de " + nome, DATA_TERMINO, HORA_TERMINO, prioridade, "Estudos", antecedencias)
    }

    /** Monta uma tarefa como se já estivesse salva (usada como retorno de repository.carregar()). */
    private static Tarefa novaTarefa(int id, String nome, int prioridade = 3, String categoria = "Estudos",
                                     StatusEnum status = StatusEnum.TODO) {
        def tarefa = new Tarefa(id, nome, "Descrição de " + nome, DATA_TERMINO, HORA_TERMINO, prioridade, categoria)
        tarefa.status = status
        tarefa
    }
}
