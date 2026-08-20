package service;

import model.StatusEnum;
import model.Tarefa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TarefaService {

    private List<Tarefa> tarefas = new ArrayList<>();
    private final AtomicInteger contadorId = new AtomicInteger(0); // Contador inicia em 0

    public Tarefa adicionar(String nome, String descricao, LocalDate dataTermino, int prioridade, String categoria) {
        int novoId = contadorId.getAndIncrement();
        Tarefa novaTarefa = new Tarefa(novoId, nome, descricao, dataTermino, prioridade, categoria);
        tarefas.add(novaTarefa);

        rebalancearPrioridades();
        return novaTarefa;
    }

    private void rebalancearPrioridades() {
        tarefas.sort(Comparator.comparingInt(Tarefa::getPrioridade).reversed());
    }

    public List<Tarefa> listar() {
        return tarefas;
    }

    public List<Tarefa> listarPorCategoria(String categoria) {
        return tarefas.stream()
                .filter(t -> t.getCategoria().equalsIgnoreCase(categoria))
                .collect(Collectors.toList());
    }

    public List<Tarefa> listarPorPrioridade(int prioridade) {
        return tarefas.stream()
                .filter(t -> t.getPrioridade() == prioridade)
                .collect(Collectors.toList());
    }

    public List<Tarefa> listarPorStatus(StatusEnum status) {
        return tarefas.stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

}
