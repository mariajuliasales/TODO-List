package service

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

    private static Tarefa adicionar(TarefaService service, String nome) {
        service.adicionar(nome, "Descrição de " + nome, DATA_TERMINO, HORA_TERMINO, 3, "Estudos", [])
    }

    private static Tarefa novaTarefa(int id, String nome) {
        new Tarefa(id, nome, "Descrição de " + nome, DATA_TERMINO, HORA_TERMINO, 3, "Estudos")
    }
}
