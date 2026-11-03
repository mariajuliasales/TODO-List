package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Tarefa implements Comparable<Tarefa>, Serializable {

    private int id;
    private String nome;
    private String descricao;
    private LocalDate dataTermino;
    private LocalTime horaTermino;
    private int prioridade; // 1 a 5
    private String categoria;
    private StatusEnum status;
    private final List<Alarme> alarmes = new ArrayList<>();

    public Tarefa(int id, String nome, String descricao, LocalDate dataTermino, LocalTime horaTermino, int prioridade, String categoria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.dataTermino = dataTermino;
        this.horaTermino = horaTermino;
        this.prioridade = prioridade;
        this.categoria = categoria;
        this.status = StatusEnum.TODO;
    }

    public int getId(){
        return id;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataTermino() {
        return dataTermino;
    }
    public void setDataTermino(LocalDate dataTermino) {
        this.dataTermino = dataTermino;
    }

    public LocalTime getHoraTermino() {
        return horaTermino;
    }
    public void setHoraTermino(LocalTime horaTermino) {
        this.horaTermino = horaTermino;
    }

    public LocalDateTime getPrazo() {
        return LocalDateTime.of(dataTermino, horaTermino);
    }

    public int getPrioridade() {
        return prioridade;
    }
    public void setPrioridade(int prioridade) {
        if (prioridade < 1 || prioridade > 5) {
            throw new IllegalArgumentException("Prioridade deve ser entre 1 e 5.");
        }
        this.prioridade = prioridade;
    }

    public String getCategoria() {
        return categoria;
    }
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public StatusEnum getStatus() {
        return status;
    }
    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public List<Alarme> getAlarmes() {
        return alarmes;
    }

    public void adicionarAlarme(Alarme alarme) {
        for (Alarme a : alarmes) {
            if (a.getAntecedenciaMinutos() == alarme.getAntecedenciaMinutos()) {
                throw new IllegalArgumentException("Já existe um alarme com essa antecedência.");
            }
        }
        alarmes.add(alarme);
    }

    public boolean removerAlarme(int indice) {
        if (indice < 0 || indice >= alarmes.size()) {
            return false;
        }
        alarmes.remove(indice);
        return true;
    }

    public void reiniciarAlarmes() {
        alarmes.forEach(a -> a.setDisparado(false));
    }

    @Override
    public int compareTo(Tarefa o) {
        return Integer.compare(o.getPrioridade(), this.prioridade);
    }

    @Override
    public String toString() {
        return String.format("ID: %d [%s] Prioridade: %d | %s - %s (Até: %s às %s) | Categoria: %s | Alarmes: %d", id,
                status, prioridade, nome, descricao, dataTermino, horaTermino, categoria, alarmes.size());
    }
}
