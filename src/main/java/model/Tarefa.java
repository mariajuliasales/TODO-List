package model;

import java.time.LocalDate;

public class Tarefa implements Comparable<Tarefa>{

    private int id;
    private String nome;
    private String descricao;
    private LocalDate dataTermino;
    private int prioridade; // 1 a 5
    private String categoria;
    private StatusEnum status;

    public Tarefa(int id, String nome, String descricao, LocalDate dataTermino, int prioridade, String categoria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.dataTermino = dataTermino;
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

    @Override
    public int compareTo(Tarefa o) {
        return Integer.compare(o.getPrioridade(), this.prioridade);
    }

    @Override
    public String toString() {
        return String.format("ID: %d [%s] Prioridade: %d | %s - %s (Até: %s) | Categoria: %s", id,
                status, prioridade, nome, descricao, dataTermino, categoria);
    }
}
