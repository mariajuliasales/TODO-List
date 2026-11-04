package repository;

import model.Tarefa;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TarefaRepository {

    private final File arquivo;

    public TarefaRepository(String caminho) {
        this.arquivo = new File(caminho);
    }

    @SuppressWarnings("unchecked")
    public List<Tarefa> carregar() {
        if (!arquivo.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (List<Tarefa>) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.out.println("Não foi possível ler o arquivo de tarefas. Iniciando com lista vazia.");
            return new ArrayList<>();
        }
    }

    public void salvar(List<Tarefa> tarefas) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            out.writeObject(new ArrayList<>(tarefas));
        } catch (IOException e) {
            System.out.println("Não foi possível salvar as tarefas: " + e.getMessage());
        }
    }
}
