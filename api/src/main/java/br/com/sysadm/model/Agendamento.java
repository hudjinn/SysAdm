package br.com.sysadm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomePaciente;

    @Column(nullable = false)
    private String emailPaciente;

    @Column
    private String telefonePaciente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_atendimento_id")
    private HorarioAtendimento horarioAtendimento;

    @Column(nullable = false)
    private LocalDateTime dataHoraAgendamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento status = StatusAgendamento.AGENDADO;  // Inicializa como AGENDADO por padrão

    // Construtores
    public Agendamento() {
        // O status é inicializado por padrão no campo
    }

    public Agendamento(HorarioAtendimento horarioAtendimento, LocalDateTime dataHoraAgendamento, StatusAgendamento status, String observacoes) {
        this.horarioAtendimento = horarioAtendimento;
        this.dataHoraAgendamento = dataHoraAgendamento;
        this.status = status;
    }

    // Getters e setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HorarioAtendimento getHorarioAtendimento() {
        return horarioAtendimento;
    }

    public void setHorarioAtendimento(HorarioAtendimento horarioAtendimento) {
        this.horarioAtendimento = horarioAtendimento;
    }

    public LocalDateTime getDataHoraAgendamento() {
        return dataHoraAgendamento;
    }

    public void setDataHoraAgendamento(LocalDateTime dataHoraAgendamento) {
        this.dataHoraAgendamento = dataHoraAgendamento;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }
    public String getNomePaciente() {
        return nomePaciente;
    }
    public void setNomePaciente(String nomePaciente) {
        this.nomePaciente = nomePaciente;
    }
    public String getEmailPaciente() {
        return emailPaciente;
    }
    public void setEmailPaciente(String emailPaciente) {
        this.emailPaciente = emailPaciente;
    }
    public String getTelefonePaciente() {
        return telefonePaciente;
    }
    public void setTelefonePaciente(String telefonePaciente) {
        this.telefonePaciente = telefonePaciente;
    }
}
