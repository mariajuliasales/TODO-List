package model

import spock.lang.Specification

import java.time.LocalDateTime

class AlarmeSpec extends Specification {

    def "deve guardar a antecedência de #antecedencia minuto(s) e começar como não disparado"() {
        given: "uma antecedência válida"

        when: "o alarme é criado"
        def alarme = new Alarme(antecedencia)

        then: "o alarme guarda a antecedência e ainda não disparou"
        alarme.antecedenciaMinutos == antecedencia
        !alarme.disparado

        where:
        antecedencia << [1, 30]
    }

    def "deve lançar erro quando a antecedência for #antecedencia"() {
        given: "uma antecedência menor ou igual a zero"

        when: "o alarme é criado"
        new Alarme(antecedencia)

        then: "ocorre um erro de argumento inválido"
        def erro = thrown(IllegalArgumentException)
        erro.message == "A antecedência do alarme deve ser maior que zero."

        where:
        antecedencia << [0, -1]
    }

    def "deve calcular o momento de disparo subtraindo a antecedência do prazo"() {
        given: "um prazo às 10:00 e um alarme de 30 minutos"
        def prazo = LocalDateTime.of(2026, 11, 20, 10, 0)
        def alarme = new Alarme(30)

        when: "o momento de disparo é calculado"
        def momento = alarme.getMomentoDisparo(prazo)

        then: "o resultado é 09:30 do mesmo dia"
        momento == LocalDateTime.of(2026, 11, 20, 9, 30)
    }

    def "deve mostrar a antecedência no texto quando o alarme ainda não disparou"() {
        given: "um alarme de 2 horas não disparado"
        def alarme = new Alarme(120)

        when: "o alarme é convertido em texto"
        def texto = alarme.toString()

        then: "o texto mostra apenas a antecedência"
        texto == "2 h antes"
    }

    def "deve indicar no texto quando o alarme já disparou"() {
        given: "um alarme de 2 horas já disparado"
        def alarme = new Alarme(120)
        alarme.disparado = true

        when: "o alarme é convertido em texto"
        def texto = alarme.toString()

        then: "o texto indica que o alarme já disparou"
        texto == "2 h antes (já disparado)"
    }

    def "deve formatar #minutos minutos como '#textoEsperado'"() {
        given: "uma quantidade de minutos"

        when: "os minutos são formatados"
        def texto = Alarme.formatarMinutos(minutos)

        then: "o texto corresponde ao esperado"
        texto == textoEsperado

        where:
        minutos || textoEsperado
        0       || "menos de 1 min"
        1       || "1 min"
        30      || "30 min"
        60      || "1 h"
        90      || "1 h e 30 min"
        1440    || "1 dia"
        1441    || "1 dia"          // os minutos são omitidos quando há dias
        1500    || "1 dia e 1 h"
        2880    || "2 dias"
    }
}
