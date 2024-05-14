package br.com.sysadm.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import br.com.sysadm.model.Agendamento;
import br.com.sysadm.model.Horario;
import br.com.sysadm.repository.AgendamentoRepository;
import br.com.sysadm.repository.ClinicaRepository;
import br.com.sysadm.repository.HorarioRepository;
import br.com.sysadm.repository.MedicoRepository;

@RestController
public class AgendamentoController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    /**
     * Lista todos os agendamentos, incluindo id da clínica, id do médico, id do agendamento, data e hora,
     * status do agendamento, id do paciente, nome do paciente e email do paciente.
     */
    @GetMapping("/agendamentos")
    public ResponseEntity<List<Agendamento>> getAgendamentos(
            @RequestParam(value = "clinicaId", required = false) Long clinicaId,
            @RequestParam(value = "medicoId", required = false) Long medicoId,
            @RequestParam(value = "dia", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {

        List<Agendamento> agendamentos = agendamentoRepository.findAll();
        
        if (clinicaId != null && medicoId != null && dia != null) {
            agendamentos = agendamentoRepository.findByClinicaIdAndMedicoIdAndDataHoraAgendamentoBetween(
                    clinicaId, medicoId,
                    dia.atStartOfDay(),
                    dia.atTime(LocalTime.MAX));
        }

        return ResponseEntity.ok(agendamentos);
    }

    /**
     * Cria um novo agendamento com os parâmetros fornecidos: id da clínica, id do médico, dia, hora, id do paciente,
     * nome do paciente e email do paciente.
     */
    @PostMapping("/agendamentos")
    public ResponseEntity<Agendamento> criarAgendamento(@RequestBody Agendamento agendamento) {
        Agendamento novoAgendamento = agendamentoRepository.save(agendamento);
        return ResponseEntity.ok(novoAgendamento);
    }

    /**
     * Atualiza um agendamento existente com base no id do agendamento.
     */
    @PatchMapping("/agendamentos/{id}")
    public ResponseEntity<Agendamento> atualizarAgendamento(@PathVariable Long id, @RequestBody Agendamento agendamento) {
        Agendamento agendamentoExistente = agendamentoRepository.findById(id).orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        agendamentoExistente.setStatus(agendamento.getStatus());
        agendamentoExistente.setNomePaciente(agendamento.getNomePaciente());
        agendamentoExistente.setEmailPaciente(agendamento.getEmailPaciente());
        agendamentoExistente.setDataHoraAgendamento(agendamento.getDataHoraAgendamento());
        agendamentoExistente.setClinica(agendamento.getClinica());
        agendamentoExistente.setMedico(agendamento.getMedico());
        Agendamento agendamentoAtualizado = agendamentoRepository.save(agendamentoExistente);
        return ResponseEntity.ok(agendamentoAtualizado);
    }

    /**
     * Deleta um agendamento existente com base no id do agendamento.
     */
    @DeleteMapping("/agendamentos/{id}")
    public ResponseEntity<Void> deletarAgendamento(@PathVariable Long id) {
        Agendamento agendamentoExistente = agendamentoRepository.findById(id).orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        agendamentoRepository.delete(agendamentoExistente);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtém a lista de horários disponíveis para uma clínica e um médico em um dia específico.
     */
    @GetMapping("/agendamentos/horarios-disponiveis")
    public ResponseEntity<List<Horario>> getHorariosDisponiveis(
            @RequestParam("clinicaId") Long clinicaId,
            @RequestParam("medicoId") Long medicoId,
            @RequestParam("dia") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {

        DayOfWeek diaSemana = dia.getDayOfWeek();

        List<Horario> horariosDisponiveis = new ArrayList<>();
        List<Horario> horarios = horarioRepository.findByClinicaIdAndMedicoId(clinicaId, medicoId);

        for (Horario horario : horarios) {
            if (horario.getDiaSemana().equals(diaSemana)) {
                LocalTime inicio = horario.getHorarioInicio();
                LocalTime fim = horario.getHorarioFim();
                while (inicio.isBefore(fim)) {
                    LocalDateTime dataHoraInicio = LocalDateTime.of(dia, inicio);
                    LocalDateTime dataHoraFim = dataHoraInicio.plusMinutes(30);
                    if (dataHoraFim.isAfter(LocalDateTime.of(dia, fim))) {
                        dataHoraFim = LocalDateTime.of(dia, fim);
                    }
                    Horario novoHorario = new Horario();
                    novoHorario.setDiaSemana(diaSemana);
                    novoHorario.setHorarioInicio(inicio);
                    novoHorario.setHorarioFim(dataHoraFim.toLocalTime());
                    horariosDisponiveis.add(novoHorario);
                    inicio = inicio.plusMinutes(30);
                }
            }
        }

        return ResponseEntity.ok(horariosDisponiveis);
    }

    /**
     * Obtém os dias da semana disponíveis para agendamentos com base na clínica e no médico fornecidos.
     */
    @GetMapping("/agendamentos/dias-disponiveis")
    public ResponseEntity<List<DayOfWeek>> getDiasDisponiveis(
            @RequestParam("clinicaId") Long clinicaId,
            @RequestParam("medicoId") Long medicoId) {

        List<Horario> horarios = horarioRepository.findByClinicaIdAndMedicoId(clinicaId, medicoId);
        List<DayOfWeek> diasDisponiveis = horarios.stream()
                .map(Horario::getDiaSemana)
                .distinct()
                .toList();

        return ResponseEntity.ok(diasDisponiveis);
    }
}
