package service;

import model.Alarme;
import model.StatusEnum;
import model.Tarefa;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AlarmeService {

    private static final DateTimeFormatter FORMATO_PRAZO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    public int sugerirAntecedencia(int prioridade) {
        if (prioridade >= 4) return 1440; 
        if (prioridade == 3) return 120;  
        return 30;
    }

    public List<String> verificar(List<Tarefa> tarefas, LocalDateTime agora) {
        List<String> avisos = new ArrayList<>();

        for (Tarefa tarefa : tarefas) {
            if (tarefa.getStatus() == StatusEnum.DONE) {
                continue;
            }

            boolean disparou = false;
            for (Alarme alarme : tarefa.getAlarmes()) {
                if (!alarme.isDisparado() && !agora.isBefore(alarme.getMomentoDisparo(tarefa.getPrazo()))) {
                    alarme.setDisparado(true);
                    disparou = true;
                }
            }

            if (disparou) {
                avisos.add(montarAviso(tarefa, agora));
            }
        }

        return avisos;
    }

    private String montarAviso(Tarefa tarefa, LocalDateTime agora) {
        LocalDateTime prazo = tarefa.getPrazo();
        String titulo = tarefa.getPrioridade() >= 4 ? "[URGENTE] ALARME" : "ALARME";

        String tempo;
        if (agora.isBefore(prazo)) {
            tempo = "vence em " + Alarme.formatarMinutos(minutosEntre(agora, prazo));
        } else {
            tempo = "venceu há " + Alarme.formatarMinutos(minutosEntre(prazo, agora));
        }

        String situacao = tarefa.getStatus() == StatusEnum.DOING
                ? "Tarefa em andamento."
                : "Tarefa ainda não iniciada.";

        return String.format("%s: \"%s\" %s (%s). %s",
                titulo, tarefa.getNome(), tempo, prazo.format(FORMATO_PRAZO), situacao);
    }

    private long minutosEntre(LocalDateTime inicio, LocalDateTime fim) {
        return Math.round(Duration.between(inicio, fim).toSeconds() / 60.0);
    }
}
