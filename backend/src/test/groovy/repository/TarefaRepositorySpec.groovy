package repository

import model.Alarme
import model.StatusEnum
import model.Tarefa
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalTime

class TarefaRepositorySpec extends Specification {

    /** Pasta temporária criada pelo Spock para cada teste e apagada ao final. Nunca usa o tarefas.dat real. */
    @TempDir
    Path pastaTemporaria

    def "deve retornar lista vazia quando o arquivo não existe"() {
        given: "um repositório apontando para um arquivo que não existe"
        def caminho = pastaTemporaria.resolve("inexistente.dat")
        def repository = new TarefaRepository(caminho.toString())

        when: "as tarefas são carregadas"
        def tarefas = repository.carregar()

        then: "o resultado é uma lista vazia"
        tarefas.isEmpty()
    }

    def "deve carregar as mesmas tarefas que foram salvas, com status e alarmes"() {
        given: "duas tarefas com status e alarmes, uma delas com alarme já disparado"
        def estudar = novaTarefa(1, "Estudar", 3, "Estudos")
        estudar.status = StatusEnum.DOING
        estudar.adicionarAlarme(new Alarme(30))
        estudar.adicionarAlarme(new Alarme(120))
        estudar.alarmes[0].disparado = true

        def mercado = novaTarefa(2, "Mercado", 5, "Casa")
        mercado.status = StatusEnum.DONE

        and: "um arquivo no qual as tarefas são salvas"
        def caminho = arquivo("tarefas.dat")
        new TarefaRepository(caminho).salvar([estudar, mercado])

        when: "as tarefas são carregadas por um novo repositório no mesmo arquivo"
        def carregadas = new TarefaRepository(caminho).carregar()

        then: "as duas tarefas voltam com todos os campos iguais"
        carregadas.size() == 2
        dadosDe(carregadas[0]) == dadosDe(estudar)
        dadosDe(carregadas[1]) == dadosDe(mercado)

        and: "os alarmes voltam com a antecedência e o estado de disparo"
        carregadas[0].alarmes*.antecedenciaMinutos == [30, 120]
        carregadas[0].alarmes*.disparado == [true, false]
        carregadas[1].alarmes.isEmpty()
    }

    def "deve manter apenas a última lista quando o arquivo é salvo duas vezes"() {
        given: "um arquivo com uma lista salva"
        def caminho = arquivo("tarefas.dat")
        def repository = new TarefaRepository(caminho)
        repository.salvar([novaTarefa(1, "Primeira", 3, "Estudos"), novaTarefa(2, "Segunda", 3, "Estudos")])

        when: "outra lista é salva no mesmo arquivo"
        repository.salvar([novaTarefa(3, "Terceira", 4, "Trabalho")])

        then: "ao carregar, vem só a segunda lista"
        def carregadas = new TarefaRepository(caminho).carregar()
        carregadas*.id == [3]
        carregadas*.nome == ["Terceira"]
    }

    def "deve carregar lista vazia quando uma lista vazia é salva"() {
        given: "um arquivo no qual uma lista vazia é salva"
        def caminho = arquivo("tarefas.dat")
        new TarefaRepository(caminho).salvar([])

        when: "as tarefas são carregadas"
        def carregadas = new TarefaRepository(caminho).carregar()

        then: "o resultado é uma lista vazia"
        carregadas.isEmpty()
    }

    private String arquivo(String nome) {
        pastaTemporaria.resolve(nome).toString()
    }

    private static Tarefa novaTarefa(int id, String nome, int prioridade, String categoria) {
        new Tarefa(id, nome, "Descrição de " + nome,
                LocalDate.of(2026, 11, 20), LocalTime.of(14, 30), prioridade, categoria)
    }

    /** Tarefa não implementa equals, então os campos são comparados um a um. */
    private static Map dadosDe(Tarefa tarefa) {
        [id         : tarefa.id,
         nome       : tarefa.nome,
         descricao  : tarefa.descricao,
         dataTermino: tarefa.dataTermino,
         horaTermino: tarefa.horaTermino,
         prioridade : tarefa.prioridade,
         categoria  : tarefa.categoria,
         status     : tarefa.status]
    }
}
