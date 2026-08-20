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

    public Tarefa atualizarStatus(int id, StatusEnum novoStatus) {
        for (Tarefa tarefa : tarefas) {
            if (tarefa.getId() == id) {
                tarefa.setStatus(novoStatus);
                return tarefa;
            }
        }
        return null;
    }

    public boolean atualizar(int id, String novoNome, String novaDescricao, LocalDate novaDataTermino, int novaPrioridade, String novaCategoria) {
        for (Tarefa t : tarefas) {
            if (t.getId() == id) {
                t.setNome(novoNome);
                t.setDescricao(novaDescricao);
                t.setDataTermino(novaDataTermino);
                t.setPrioridade(novaPrioridade);
                t.setCategoria(novaCategoria);

                rebalancearPrioridades();
                return true;
            }
        }
        return false; // tarefa nao encontrada
    }
}
