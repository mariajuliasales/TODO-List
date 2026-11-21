package service

import model.StatusEnum
import model.Tarefa
import repository.TarefaRepository
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

    private static Tarefa adicionar(TarefaService service, String nome, int prioridade = 3, List<Integer> antecedencias = []) {
        service.adicionar(nome, "Descrição de " + nome, DATA_TERMINO, HORA_TERMINO, prioridade, "Estudos", antecedencias)
    }

    private static Tarefa novaTarefa(int id, String nome) {
        new Tarefa(id, nome, "Descrição de " + nome, DATA_TERMINO, HORA_TERMINO, 3, "Estudos")
    }
}
