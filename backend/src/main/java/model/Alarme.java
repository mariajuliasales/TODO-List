package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Alarme implements Serializable {

    private final int antecedenciaMinutos;
    private boolean disparado;

    public Alarme(int antecedenciaMinutos) {
        if (antecedenciaMinutos <= 0) {
            throw new IllegalArgumentException("A antecedência do alarme deve ser maior que zero.");
        }
        this.antecedenciaMinutos = antecedenciaMinutos;
        this.disparado = false;
    }

    public int getAntecedenciaMinutos() {
        return antecedenciaMinutos;
    }

    public boolean isDisparado() {
        return disparado;
    }
    public void setDisparado(boolean disparado) {
        this.disparado = disparado;
    }

    public LocalDateTime getMomentoDisparo(LocalDateTime prazo) {
        return prazo.minusMinutes(antecedenciaMinutos);
    }

    @Override
    public String toString() {
        String texto = formatarMinutos(antecedenciaMinutos) + " antes";
        return disparado ? texto + " (já disparado)" : texto;
    }


    public static String formatarMinutos(long totalMinutos) {
        long dias = totalMinutos / 1440;
        long horas = (totalMinutos % 1440) / 60;
        long minutos = totalMinutos % 60;

        StringBuilder sb = new StringBuilder();
        if (dias > 0) {
            sb.append(dias).append(dias == 1 ? " dia" : " dias");
        }
        if (horas > 0) {
            if (!sb.isEmpty()){
            sb.append(" e ");
        }
            sb.append(horas).append(" h");
        }
        if (minutos > 0 && dias == 0) {
            if (!sb.isEmpty()) sb.append(" e ");
            sb.append(minutos).append(" min");
        }
        if (sb.isEmpty()) {
            sb.append("menos de 1 min");
        }
        return sb.toString();
    }
}
