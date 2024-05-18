package br.com.sysadm.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.com.sysadm.model.Agendamento;
import br.com.sysadm.model.Clinica;
import br.com.sysadm.model.Medico;
import br.com.sysadm.model.StatusAgendamento;
import br.com.sysadm.model.Horario;
import br.com.sysadm.repository.AgendamentoRepository;
import br.com.sysadm.repository.ClinicaRepository;
import br.com.sysadm.repository.HorarioRepository;
import br.com.sysadm.repository.MedicoRepository;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private EmailController emailController;
    
    @PostMapping
    public ResponseEntity<Map<String, String>> criarAgendamento(@RequestBody Agendamento agendamento) {
        Clinica clinica = clinicaRepository.findById(agendamento.getClinica().getId())
                .orElseThrow(() -> new RuntimeException("Clínica não encontrada"));
        Medico medico = medicoRepository.findById(agendamento.getMedico().getId())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado"));
    
        agendamento.setClinica(clinica);
        agendamento.setMedico(medico);
    
        // Verificar se já existe um agendamento para o mesmo médico, dia e horário
        LocalDate dataAgendamento = agendamento.getDataAgendamento();
        LocalTime horaAgendamento = agendamento.getHoraAgendamento();
        Optional<Agendamento> agendamentoExistente = agendamentoRepository
                .findByMedicoAndDataAgendamentoAndHoraAgendamento(medico, dataAgendamento, horaAgendamento);
        
        if (agendamentoExistente.isPresent()) {
            if (agendamentoExistente.get().getStatus() != StatusAgendamento.CANCELADO) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Já existe um agendamento para este médico, dia e horário."));
            } else {
                // Remove o agendamento cancelado para substituí-lo por um novo
                agendamentoRepository.delete(agendamentoExistente.get());
            }
        }
    
        agendamento.setStatus(StatusAgendamento.AGENDADO); // Define o status padrão
        Agendamento novoAgendamento = agendamentoRepository.save(agendamento);
        // Enviando e-mail de Confirmação
        String email = agendamento.getEmailPaciente();
        String subject = "Confirmação de Agendamento";
        String text = "Seu agendamento foi criado com sucesso. Detalhes: " +
                      "\nPROTOCOLO DE AGENDAMENTO: " + agendamento.getId() +
                      "\nData: " + agendamento.getDataAgendamento() +
                      "\nHora: " + agendamento.getHoraAgendamento() +
                      "\nClínica: " + agendamento.getClinica().getNome() +
                      "\nMédico: " + agendamento.getMedico().getNome();
        if (novoAgendamento.getId() < 15) {
            try {
                emailController.enviarEmail(email, subject, text);
            } catch (Exception e) {
                // Logar o erro, mas não interromper a execução
                System.err.println("Erro ao enviar e-mail: " + e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("id", novoAgendamento.getId().toString(), "message", "Agendamento criado com sucesso!"));
    }

    @GetMapping
    public ResponseEntity<List<Agendamento>> getAgendamentos(
            @RequestParam(value = "clinicaId", required = false) Long clinicaId,
            @RequestParam(value = "medicoId", required = false) Long medicoId,
            @RequestParam(value = "dia", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {

        List<Agendamento> agendamentos = agendamentoRepository.findAll();

        if (clinicaId != null && medicoId != null && dia != null) {
            agendamentos = agendamentoRepository.findByClinicaIdAndMedicoIdAndDataAgendamento(
                    clinicaId, medicoId, dia);
        }

        return ResponseEntity.ok(agendamentos);
    }

    @PatchMapping("/atualizar/{id}")
    public ResponseEntity<?> atualizarAgendamento(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Optional<Agendamento> agendamentoOptional = agendamentoRepository.findById(id);
        if (agendamentoOptional.isPresent()) {
            Agendamento agendamentoExistente = agendamentoOptional.get();
    
            // Atualiza atributos com base no que está presente no JSON
            updates.forEach((atributo, valor) -> {
                switch (atributo) {
                    case "status":
                        if (valor instanceof String) {
                            try {
                                StatusAgendamento status = StatusAgendamento.valueOf((String) valor);
                                agendamentoExistente.setStatus(status);
                            } catch (IllegalArgumentException e) {
                                throw new RuntimeException("Status inválido: " + valor);
                            }
                        }
                        break;
                    case "nomePaciente":
                        if (valor instanceof String) agendamentoExistente.setNomePaciente((String) valor);
                        break;
                    case "emailPaciente":
                        if (valor instanceof String) agendamentoExistente.setEmailPaciente((String) valor);
                        break;
                    case "dataAgendamento":
                        if (valor instanceof String)
                            agendamentoExistente.setDataAgendamento(LocalDate.parse((String) valor));
                        break;
                    case "horaAgendamento":
                        if (valor instanceof String)
                            agendamentoExistente.setHoraAgendamento(LocalTime.parse((String) valor));
                        break;
                    case "clinica":
                        if (valor instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> clinicaMap = (Map<String, Object>) valor;
                            if (clinicaMap.containsKey("id")) {
                                Long clinicaId = Long.valueOf(clinicaMap.get("id").toString());
                                Clinica clinica = clinicaRepository.findById(clinicaId)
                                        .orElseThrow(() -> new RuntimeException("Clínica não encontrada"));
                                agendamentoExistente.setClinica(clinica);
                            }
                        }
                        break;
                    case "medico":
                        if (valor instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> medicoMap = (Map<String, Object>) valor;
                            if (medicoMap.containsKey("id")) {
                                Long medicoId = Long.valueOf(medicoMap.get("id").toString());
                                Medico medico = medicoRepository.findById(medicoId)
                                        .orElseThrow(() -> new RuntimeException("Médico não encontrado"));
                                agendamentoExistente.setMedico(medico);
                            }
                        }
                        break;
                }
            });
    
            Agendamento agendamentoAtualizado = agendamentoRepository.save(agendamentoExistente);
            String email = agendamentoAtualizado.getEmailPaciente();
            String subject = "[Importante]Atualização de Agendamento";
            String text = "Seu agendamento foi ALTERADO! Detalhes: " +
                          "\nStatus: " + agendamentoAtualizado.getStatus() +
                          "\nData: " + agendamentoAtualizado.getDataAgendamento() +
                          "\nHora: " + agendamentoAtualizado.getHoraAgendamento() +
                          "\nClínica: " + agendamentoAtualizado.getClinica().getNome() +
                          "\nMédico: " + agendamentoAtualizado.getMedico().getNome();
            
            if (agendamentoAtualizado.getId() < 15) {
                try {
                    emailController.enviarEmail(email, subject, text);
                } catch (Exception e) {
                    // Logar o erro, mas não interromper a execução
                    System.err.println("Erro ao enviar e-mail: " + e.getMessage());
                }
            }
            return ResponseEntity.ok(agendamentoAtualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    

    @DeleteMapping("/remover/{id}")
    public ResponseEntity<Void> deletarAgendamento(@PathVariable Long id) {
        Agendamento agendamentoExistente = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        agendamentoRepository.delete(agendamentoExistente);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/horarios-disponiveis")
    public ResponseEntity<List<Horario>> getHorariosDisponiveis(
            @RequestParam("clinicaId") Long clinicaId,
            @RequestParam("medicoId") Long medicoId,
            @RequestParam("dia") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {

        DayOfWeek diaSemana = dia.getDayOfWeek();

        List<Horario> horariosDisponiveis = new ArrayList<>();
        List<Horario> horarios = horarioRepository.findByClinicaIdAndMedicoId(clinicaId, medicoId);
        List<Agendamento> agendamentos = agendamentoRepository.findByClinicaIdAndMedicoIdAndDataAgendamento(clinicaId, medicoId, dia);

        List<LocalTime> horariosAgendados = agendamentos.stream()
                .map(Agendamento::getHoraAgendamento)
                .toList();

        for (Horario horario : horarios) {
            if (horario.getDiaSemana().equals(diaSemana)) {
                LocalTime inicio = horario.getHorarioInicio();
                LocalTime fim = horario.getHorarioFim();
                while (inicio.isBefore(fim)) {
                    if (!horariosAgendados.contains(inicio)) {
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
                    }
                    inicio = inicio.plusMinutes(30);
                }
            }
        }

        return ResponseEntity.ok(horariosDisponiveis);
    }

    @GetMapping("/horarios")
    public ResponseEntity<List<Map<String, Object>>> getTodosHorarios(
            @RequestParam("clinicaId") Long clinicaId,
            @RequestParam("medicoId") Long medicoId,
            @RequestParam("dia") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {

        DayOfWeek diaSemana = dia.getDayOfWeek();

        List<Horario> horarios = horarioRepository.findByClinicaIdAndMedicoId(clinicaId, medicoId);
        List<Agendamento> agendamentos = agendamentoRepository.findByClinicaIdAndMedicoIdAndDataAgendamento(clinicaId, medicoId, dia);

        List<LocalTime> horariosAgendados = agendamentos.stream()
                .filter(agendamento -> agendamento.getStatus() != StatusAgendamento.CANCELADO) // Exclui agendamentos cancelados
                .map(Agendamento::getHoraAgendamento)
                .toList();

        List<Map<String, Object>> horariosComStatus = new ArrayList<>();

        for (Horario horario : horarios) {
            if (horario.getDiaSemana().equals(diaSemana)) {
                LocalTime inicio = horario.getHorarioInicio();
                LocalTime fim = horario.getHorarioFim();
                while (inicio.isBefore(fim)) {
                    Map<String, Object> horarioMap = new HashMap<>();
                    horarioMap.put("diaSemana", diaSemana);
                    horarioMap.put("horarioInicio", inicio);
                    horarioMap.put("horarioFim", inicio.plusMinutes(30));
                    horarioMap.put("agendado", horariosAgendados.contains(inicio));

                    horariosComStatus.add(horarioMap);

                    inicio = inicio.plusMinutes(30);
                }
            }
        }

        return ResponseEntity.ok(horariosComStatus);
    }

    @GetMapping("/dias-disponiveis")
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
