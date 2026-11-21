package model

import spock.lang.PendingFeature
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

    def "deve aceitar a alteração da prioridade para #novaPrioridade"() {
        given: "uma tarefa com prioridade 3"
        def tarefa = novaTarefa(1, 3)

        when: "a prioridade é alterada para um valor entre 1 e 5"
        tarefa.prioridade = novaPrioridade

        then: "a nova prioridade é aceita"
        tarefa.prioridade == novaPrioridade

        where:
        novaPrioridade << [1, 5]
    }

    def "deve lançar erro e manter a prioridade anterior quando a nova prioridade for #novaPrioridade"() {
        given: "uma tarefa com prioridade 3"
        def tarefa = novaTarefa(1, 3)

        when: "a prioridade é alterada para um valor fora de 1 a 5"
        tarefa.prioridade = novaPrioridade

        then: "ocorre um erro de argumento inválido"
        def erro = thrown(IllegalArgumentException)
        erro.message == "Prioridade deve ser entre 1 e 5."

        and: "a prioridade anterior é mantida"
        tarefa.prioridade == 3

        where:
        novaPrioridade << [0, 6]
    }

    /*
     * BUG CONHECIDO (spec, seção 2.2 / CA09): o construtor de Tarefa atribui a
     * prioridade diretamente, sem a validação de 1 a 5 que existe em setPrioridade.
     * Por isso, hoje é possível criar uma tarefa com prioridade 0 ou 6.
     *
     * Este teste descreve o comportamento correto e está marcado com @PendingFeature:
     * enquanto o bug existir, ele falha e o Spock o reporta como ignorado (skipped),
     * sem quebrar o build. Quando o construtor for corrigido, o teste passará e o
     * Spock acusará erro, avisando que a anotação @PendingFeature deve ser removida.
     * A correção está fora do escopo desta feature.
     */
    @PendingFeature(reason = "Construtor de Tarefa não valida a prioridade (CA09)")
    def "deve lançar erro ao criar uma tarefa com prioridade #prioridade"() {
        given: "uma prioridade fora do intervalo de 1 a 5"

        when: "a tarefa é criada"
        novaTarefa(1, prioridade)

        then: "ocorre um erro de argumento inválido"
        thrown(IllegalArgumentException)

        where:
        prioridade << [0, 6]
    }

    def "deve lançar erro ao adicionar um alarme com antecedência repetida"() {
        given: "uma tarefa com um alarme de 30 minutos"
        def tarefa = novaTarefa(1, 3)
        tarefa.adicionarAlarme(new Alarme(30))

        when: "outro alarme de 30 minutos é adicionado"
        tarefa.adicionarAlarme(new Alarme(30))

        then: "ocorre um erro de argumento inválido"
        def erro = thrown(IllegalArgumentException)
        erro.message == "Já existe um alarme com essa antecedência."

        and: "a tarefa continua com um único alarme"
        tarefa.alarmes*.antecedenciaMinutos == [30]
    }

    def "deve adicionar um alarme com antecedência diferente das existentes"() {
        given: "uma tarefa com um alarme de 30 minutos"
        def tarefa = novaTarefa(1, 3)
        tarefa.adicionarAlarme(new Alarme(30))

        when: "um alarme de 60 minutos é adicionado"
        tarefa.adicionarAlarme(new Alarme(60))

        then: "a tarefa fica com os dois alarmes"
        tarefa.alarmes*.antecedenciaMinutos == [30, 60]
    }

    def "deve remover o alarme pela posição na lista"() {
        given: "uma tarefa com alarmes de 30 e 60 minutos"
        def tarefa = tarefaComAlarmes(30, 60)

        when: "o alarme da posição 0 é removido"
        def removido = tarefa.removerAlarme(0)

        then: "a remoção tem sucesso e resta só o alarme de 60 minutos"
        removido
        tarefa.alarmes*.antecedenciaMinutos == [60]
    }

    def "deve falhar sem remover nada quando a posição do alarme for #posicao"() {
        given: "uma tarefa com alarmes de 30 e 60 minutos"
        def tarefa = tarefaComAlarmes(30, 60)

        when: "se tenta remover um alarme em uma posição inválida"
        def removido = tarefa.removerAlarme(posicao)

        then: "a remoção falha e os dois alarmes continuam na tarefa"
        !removido
        tarefa.alarmes*.antecedenciaMinutos == [30, 60]

        where:
        posicao << [-1, 2]
    }

    def "deve falhar ao remover um alarme de uma tarefa sem alarmes"() {
        given: "uma tarefa sem alarmes"
        def tarefa = novaTarefa(1, 3)

        when: "se tenta remover o alarme da posição 0"
        def removido = tarefa.removerAlarme(0)

        then: "a remoção falha"
        !removido
        tarefa.alarmes.isEmpty()
    }

    def "deve marcar todos os alarmes como não disparados ao reiniciar os alarmes"() {
        given: "uma tarefa com dois alarmes já disparados"
        def tarefa = tarefaComAlarmes(30, 60)
        tarefa.alarmes.each { it.disparado = true }

        when: "os alarmes são reiniciados"
        tarefa.reiniciarAlarmes()

        then: "nenhum alarme está marcado como disparado"
        tarefa.alarmes.size() == 2
        tarefa.alarmes.every { !it.disparado }
    }

    def "deve reiniciar os alarmes sem erro quando a tarefa não tem alarmes"() {
        given: "uma tarefa sem alarmes"
        def tarefa = novaTarefa(1, 3)

        when: "os alarmes são reiniciados"
        tarefa.reiniciarAlarmes()

        then: "nenhum erro ocorre e a tarefa continua sem alarmes"
        noExceptionThrown()
        tarefa.alarmes.isEmpty()
    }

    private static Tarefa tarefaComAlarmes(int... antecedencias) {
        def tarefa = novaTarefa(1, 3)
        antecedencias.each { tarefa.adicionarAlarme(new Alarme(it)) }
        tarefa
    }

    private static Tarefa novaTarefa(int id, int prioridade) {
        new Tarefa(id, "Tarefa " + id, "Descrição " + id,
                LocalDate.of(2026, 11, 20), LocalTime.of(14, 30), prioridade, "Estudos")
    }
}
