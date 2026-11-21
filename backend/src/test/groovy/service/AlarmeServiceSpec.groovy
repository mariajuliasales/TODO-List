package service

import model.Alarme
import model.StatusEnum
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

    def "não deve disparar o alarme de uma tarefa concluída"() {
        given: "uma tarefa DONE com alarme de 30 minutos já vencido"
        def tarefa = tarefaComAlarme(30)
        tarefa.status = StatusEnum.DONE

        when: "a verificação é feita às 09:45"
        def avisos = service.verificar([tarefa], horario(9, 45))

        then: "nenhum aviso é gerado e o alarme continua não disparado"
        avisos.isEmpty()
        !tarefa.alarmes[0].disparado
    }

    def "deve gerar um único aviso quando vários alarmes da mesma tarefa vencem juntos"() {
        given: "uma tarefa com alarmes de 30 e 60 minutos, ambos vencidos às 09:45"
        def tarefa = novaTarefa(1, 30, 60)

        when: "a verificação é feita às 09:45"
        def avisos = service.verificar([tarefa], horario(9, 45))

        then: "é gerado um único aviso e os dois alarmes ficam disparados"
        avisos.size() == 1
        tarefa.alarmes.size() == 2
        tarefa.alarmes.every { it.disparado }
    }

    def "deve gerar um aviso para cada tarefa com alarme vencido"() {
        given: "duas tarefas com alarmes vencidos e uma tarefa sem alarmes"
        def tarefa1 = novaTarefa(1, 30)
        def tarefa2 = novaTarefa(2, 60)
        def tarefaSemAlarmes = novaTarefa(3)

        when: "a verificação é feita às 09:45"
        def avisos = service.verificar([tarefa1, tarefa2, tarefaSemAlarmes], horario(9, 45))

        then: "são gerados dois avisos, um para cada tarefa com alarme vencido"
        avisos.size() == 2
        avisos[0].contains('"Tarefa 1"')
        avisos[1].contains('"Tarefa 2"')
    }

    def "deve iniciar o aviso com '#titulo' quando a prioridade for #prioridade"() {
        given: "uma tarefa com prioridade #prioridade e alarme de 30 minutos"
        def tarefa = new Tarefa(1, "Estudar", "Revisar Spock", DATA_PRAZO, HORA_PRAZO, prioridade, "Estudos")
        tarefa.adicionarAlarme(new Alarme(30))

        when: "o alarme dispara às 09:45"
        def avisos = service.verificar([tarefa], horario(9, 45))

        then: "o aviso começa com o título esperado"
        avisos.size() == 1
        avisos[0].startsWith(titulo + ":")

        where:
        prioridade || titulo
        1          || "ALARME"
        2          || "ALARME"
        3          || "ALARME"
        4          || "[URGENTE] ALARME"
        5          || "[URGENTE] ALARME"
    }

    def "deve informar '#trechoEsperado' quando o alarme dispara às #hora:#minuto"() {
        given: "uma tarefa com prazo às 10:00 e alarme de 30 minutos"
        def tarefa = tarefaComAlarme(30)

        when: "o alarme dispara antes ou depois do prazo"
        def avisos = service.verificar([tarefa], horario(hora, minuto))

        then: "o aviso informa quanto falta ou há quanto tempo venceu"
        avisos.size() == 1
        avisos[0].contains(trechoEsperado)

        where:
        hora | minuto || trechoEsperado
        9    | 45     || "vence em 15 min"
        10   | 20     || "venceu há 20 min"
    }

    def "deve informar '#situacao' quando a tarefa está com status #status"() {
        given: "uma tarefa com status #status e alarme de 30 minutos"
        def tarefa = tarefaComAlarme(30)
        tarefa.status = status

        when: "o alarme dispara às 09:45"
        def avisos = service.verificar([tarefa], horario(9, 45))

        then: "o aviso informa a situação da tarefa"
        avisos.size() == 1
        avisos[0].endsWith(situacao)

        where:
        status           || situacao
        StatusEnum.DOING || "Tarefa em andamento."
        StatusEnum.TODO  || "Tarefa ainda não iniciada."
    }

    def "deve incluir o nome da tarefa e o prazo formatado no aviso"() {
        given: "a tarefa 'Estudar' com prazo em 20/11/2026 às 14:30 e alarme de 30 minutos"
        def tarefa = new Tarefa(1, "Estudar", "Revisar Spock", DATA_PRAZO, LocalTime.of(14, 30), 3, "Estudos")
        tarefa.adicionarAlarme(new Alarme(30))

        when: "o alarme dispara às 14:15"
        def avisos = service.verificar([tarefa], horario(14, 15))

        then: "o aviso contém o nome e o prazo no formato dd/MM/yyyy às HH:mm"
        avisos.size() == 1
        avisos[0].contains('"Estudar"')
        avisos[0].contains("20/11/2026 às 14:30")

        and: "o aviso completo tem o formato esperado"
        avisos[0] == 'ALARME: "Estudar" vence em 15 min (20/11/2026 às 14:30). Tarefa ainda não iniciada.'
    }

    private static LocalDateTime horario(int hora, int minuto) {
        LocalDateTime.of(DATA_PRAZO, LocalTime.of(hora, minuto))
    }

    private static Tarefa tarefaComAlarme(int antecedenciaMinutos) {
        def tarefa = new Tarefa(1, "Estudar", "Revisar Spock", DATA_PRAZO, HORA_PRAZO, 3, "Estudos")
        tarefa.adicionarAlarme(new Alarme(antecedenciaMinutos))
        tarefa
    }

    /** Cria a tarefa "Tarefa {id}", TODO, prioridade 3, prazo 10:00, com os alarmes informados. */
    private static Tarefa novaTarefa(int id, int... antecedenciasMinutos) {
        def tarefa = new Tarefa(id, "Tarefa " + id, "Descrição " + id, DATA_PRAZO, HORA_PRAZO, 3, "Estudos")
        antecedenciasMinutos.each { tarefa.adicionarAlarme(new Alarme(it)) }
        tarefa
    }
}
