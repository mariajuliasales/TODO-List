package service

import spock.lang.Specification
import spock.lang.Subject

class AlarmeServiceSpec extends Specification {

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
}
