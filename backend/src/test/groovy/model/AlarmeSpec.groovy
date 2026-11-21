package model

import spock.lang.Specification

class AlarmeSpec extends Specification {

    def "deve guardar a antecedência informada e começar como não disparado"() {
        given: "uma antecedência de 30 minutos"
        int antecedencia = 30

        when: "o alarme é criado"
        def alarme = new Alarme(antecedencia)

        then: "o alarme guarda os 30 minutos e ainda não disparou"
        alarme.antecedenciaMinutos == 30
        !alarme.disparado
    }
}
