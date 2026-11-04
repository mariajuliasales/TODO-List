package service;

import model.Alarme;
import model.StatusEnum;
import model.Tarefa;
import repository.TarefaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TarefaService {

    private final TarefaRepository repository;
    private final AlarmeService alarmeService = new AlarmeService();
    private final List<Tarefa> tarefas;
    private final AtomicInteger contadorId;

    public TarefaService(TarefaRepository repository) {
        this.repository = repository;
        this.tarefas = repository.carregar();

        int maiorId = tarefas.stream().mapToInt(Tarefa::getId).max().orElse(0);
        this.contadorId = new AtomicInteger(maiorId + 1);
    }

    public synchronized Tarefa adicionar(String nome, String descricao, LocalDate dataTermino, LocalTime horaTermino,
                                         int prioridade, String categoria, List<Integer> antecedenciasAlarme) {
        int novoId = contadorId.getAndIncrement();
        Tarefa novaTarefa = new Tarefa(novoId, nome, descricao, dataTermino, horaTermino, prioridade, categoria);
        for (int minutos : antecedenciasAlarme) {
            novaTarefa.adicionarAlarme(new Alarme(minutos));
        }
        tarefas.add(novaTarefa);

        rebalancearPrioridades();
        salvar();
        return novaTarefa;
    }

    private void rebalancearPrioridades() {
        tarefas.sort(Comparator.comparingInt(Tarefa::getPrioridade).reversed());
    }

    private void salvar() {
        repository.salvar(tarefas);
    }

    public synchronized List<Tarefa> listar() {
        return new ArrayList<>(tarefas);
    }

    public synchronized Tarefa buscarPorId(int id) {
        for (Tarefa t : tarefas) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public synchronized List<Tarefa> listarPorCategoria(String categoria) {
        return tarefas.stream()
                .filter(t -> t.getCategoria().equalsIgnoreCase(categoria))
                .collect(Collectors.toList());
    }

    public synchronized List<Tarefa> listarPorPrioridade(int prioridade) {
        return tarefas.stream()
                .filter(t -> t.getPrioridade() == prioridade)
                .collect(Collectors.toList());
    }

    public synchronized List<Tarefa> listarPorStatus(StatusEnum status) {
        return tarefas.stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    public synchronized Tarefa atualizarStatus(int id, StatusEnum novoStatus) {
        Tarefa tarefa = buscarPorId(id);
        if (tarefa != null) {
            tarefa.setStatus(novoStatus);
            salvar();
        }
        return tarefa;
    }

    public synchronized boolean atualizar(int id, String novoNome, String novaDescricao, LocalDate novaDataTermino,
                                          LocalTime novaHoraTermino, int novaPrioridade, String novaCategoria) {
        Tarefa t = buscarPorId(id);
        if (t == null) {
            return false; 
        }

        LocalDateTime prazoAntigo = t.getPrazo();

        t.setNome(novoNome);
        t.setDescricao(novaDescricao);
        t.setDataTermino(novaDataTermino);
        t.setHoraTermino(novaHoraTermino);
        t.setPrioridade(novaPrioridade);
        t.setCategoria(novaCategoria);

        if (!prazoAntigo.equals(t.getPrazo())) {
            t.reiniciarAlarmes();
        }

        rebalancearPrioridades();
        salvar();
        return true;
    }

    public synchronized boolean deletar(int id) {
        boolean removido = tarefas.removeIf(t -> t.getId() == id);
        if (removido) {
            salvar();
        }
        return removido;
    }

    public synchronized boolean adicionarAlarme(int id, int antecedenciaMinutos) {
        Tarefa t = buscarPorId(id);
        if (t == null) {
            return false;
        }
        t.adicionarAlarme(new Alarme(antecedenciaMinutos));
        salvar();
        return true;
    }

    public synchronized boolean removerAlarme(int id, int indiceAlarme) {
        Tarefa t = buscarPorId(id);
        if (t == null || !t.removerAlarme(indiceAlarme)) {
            return false;
        }
        salvar();
        return true;
    }

    public int sugerirAntecedencia(int prioridade) {
        return alarmeService.sugerirAntecedencia(prioridade);
    }

    public synchronized List<String> verificarAlarmes(LocalDateTime agora) {
        List<String> avisos = alarmeService.verificar(tarefas, agora);
        if (!avisos.isEmpty()) {
            salvar();
        }
        return avisos;
    }
}
