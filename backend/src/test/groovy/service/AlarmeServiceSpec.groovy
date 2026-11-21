package service

import model.Alarme
import model.Tarefa
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AlarmeServiceSpec extends Specification {

    /** Prazo usado nos testes de disparo: 20/11/2026 às 10:00. */
    private static final LocalDate DATA_PRAZO = LocalDate.of(2026, 11, 20)
    private static final LocalTime HORA_PRAZO = LocalTime.of(10, 0)

    @Subject
    AlarmeService service = new AlarmeService()

    def "deve sugerir #minutosEsperados minutos de antecedência para a prioridade #prioridade"() {
        given: "uma tarefa com prioridade #prioridade"

        when: "a antecedência é sugerida"
        def sugestao = service.sugerirAntecedencia(prioridade)

        then: "a sugestão é de #minutosEsperados minutos"
        sugestao == minutosEsperados

        where:
        prioridade || minutosEsperados
        1          || 30
        2          || 30
        3          || 120
        4          || 1440
        5          || 1440
    }

    def "não deve gerar avisos quando a lista de tarefas está vazia"() {
        given: "nenhuma tarefa"
        def tarefas = []

        when: "os alarmes são verificados"
        def avisos = service.verificar(tarefas, horario(10, 0))

        then: "nenhum aviso é gerado"
        avisos.isEmpty()
    }

    def "não deve disparar o alarme antes do momento de disparo"() {
        given: "uma tarefa TODO com prazo às 10:00 e alarme de 30 minutos"
        def tarefa = tarefaComAlarme(30)

        when: "a verificação é feita às 09:29"
        def avisos = service.verificar([tarefa], horario(9, 29))

        then: "nenhum aviso é gerado e o alarme continua não disparado"
        avisos.isEmpty()
        !tarefa.alarmes[0].disparado
    }

    def "deve disparar o alarme quando a verificação é feita às #hora:#minuto"() {
        given: "uma tarefa TODO com prazo às 10:00 e alarme de 30 minutos"
        def tarefa = tarefaComAlarme(30)

        when: "a verificação é feita no momento de disparo ou depois dele"
        def avisos = service.verificar([tarefa], horario(hora, minuto))

        then: "um aviso é gerado e o alarme fica disparado"
        avisos.size() == 1
        tarefa.alarmes[0].disparado

        where:
        hora | minuto
        9    | 30
        9    | 45
    }

    def "não deve disparar novamente um alarme que já disparou"() {
        given: "uma tarefa cujo alarme já disparou às 09:30"
        def tarefa = tarefaComAlarme(30)
        service.verificar([tarefa], horario(9, 30))

        when: "a verificação é feita de novo às 09:45"
        def avisos = service.verificar([tarefa], horario(9, 45))

        then: "nenhum novo aviso é gerado e o alarme continua disparado"
        avisos.isEmpty()
        tarefa.alarmes[0].disparado
    }

    private static LocalDateTime horario(int hora, int minuto) {
        LocalDateTime.of(DATA_PRAZO, LocalTime.of(hora, minuto))
    }

    private static Tarefa tarefaComAlarme(int antecedenciaMinutos) {
        def tarefa = new Tarefa(1, "Estudar", "Revisar Spock", DATA_PRAZO, HORA_PRAZO, 3, "Estudos")
        tarefa.adicionarAlarme(new Alarme(antecedenciaMinutos))
        tarefa
    }
}
