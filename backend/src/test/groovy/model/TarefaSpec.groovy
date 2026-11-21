package model

import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TarefaSpec extends Specification {

    def "deve começar com status TODO e sem alarmes quando a tarefa é criada"() {
        given: "os dados de uma nova tarefa"
        def dataTermino = LocalDate.of(2026, 11, 20)
        def horaTermino = LocalTime.of(14, 30)

        when: "a tarefa é criada"
        def tarefa = new Tarefa(1, "Estudar", "Revisar Spock", dataTermino, horaTermino, 3, "Estudos")

        then: "o status é TODO e não há alarmes"
        tarefa.status == StatusEnum.TODO
        tarefa.alarmes.isEmpty()
    }

    def "deve combinar a data e a hora de término no prazo da tarefa"() {
        given: "uma tarefa com término em 20/11/2026 às 14:30"
        def tarefa = novaTarefa(1, 3)

        when: "o prazo é consultado"
        def prazo = tarefa.prazo

        then: "o prazo é 20/11/2026 às 14:30"
        prazo == LocalDateTime.of(2026, 11, 20, 14, 30)
    }

    def "deve ordenar as tarefas da maior para a menor prioridade"() {
        given: "tarefas com prioridades 2, 5 e 3"
        def tarefas = [novaTarefa(1, 2), novaTarefa(2, 5), novaTarefa(3, 3)]

        when: "as tarefas são ordenadas pela comparação natural"
        def ordenadas = tarefas.sort(false)

        then: "a ordem das prioridades é 5, 3, 2"
        ordenadas*.prioridade == [5, 3, 2]
    }

    private static Tarefa novaTarefa(int id, int prioridade) {
        new Tarefa(id, "Tarefa " + id, "Descrição " + id,
                LocalDate.of(2026, 11, 20), LocalTime.of(14, 30), prioridade, "Estudos")
    }
}
