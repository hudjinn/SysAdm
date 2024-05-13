package br.com.sysadm.dto;

import java.util.Map;

public class MedicoComHorariosDTO {
    private String medicoCpf;
    private Map<String, String[]> horarios;  // Dia da semana e intervalos [inicio, fim]

    // Getters e Setters
    public String getMedicoCpf() {
        return medicoCpf;
    }
    public void setMedicoCpf(String medicoCpf) {
        this.medicoCpf = medicoCpf;
    }
    public Map<String, String[]> getHorarios() {
        return horarios;
    }
    public void setHorarios(Map<String, String[]> horarios) {
        this.horarios = horarios;
    }
}

