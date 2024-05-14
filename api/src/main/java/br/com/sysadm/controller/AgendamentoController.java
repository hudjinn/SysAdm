package br.com.sysadm.controller;

import br.com.sysadm.model.Agendamento;
import br.com.sysadm.model.Clinica;
import br.com.sysadm.model.Medico;
import br.com.sysadm.repository.AgendamentoRepository;
import br.com.sysadm.repository.ClinicaRepository;
import br.com.sysadm.repository.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @GetMapping("/clinica/{clinicaId}/medico/{medicoId}")
    public ResponseEntity<List<Agendamento>> getAgendamentos(
            @PathVariable Long clinicaId, 
            @PathVariable Long medicoId, 
            @RequestParam("start") LocalDateTime start, 
            @RequestParam("end") LocalDateTime end) {
        
        Clinica clinica = clinicaRepository.findById(clinicaId).orElse(null);
        Medico medico = medicoRepository.findById(medicoId).orElse(null);

        if (clinica == null || medico == null) {
            return ResponseEntity.notFound().build();
        }

        List<Agendamento> agendamentos = agendamentoRepository.findByClinicaAndMedicoAndDataHoraAgendamentoBetween(clinica, medico, start, end);
        return ResponseEntity.ok(agendamentos);
    }

    @GetMapping
    public List<Agendamento> getAllAgendamentos() {
        return agendamentoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> getAgendamentoById(@PathVariable Long id) {
        return agendamentoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Agendamento createAgendamento(@RequestBody Agendamento agendamento) {
        return agendamentoRepository.save(agendamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> updateAgendamento(@PathVariable Long id, @RequestBody Agendamento agendamentoDetails) {
        return agendamentoRepository.findById(id)
                .map(agendamento -> {
                    agendamento.setDataHoraAgendamento(agendamentoDetails.getDataHoraAgendamento());
                    agendamento.setEmailPaciente(agendamentoDetails.getEmailPaciente());
                    agendamento.setNomePaciente(agendamentoDetails.getNomePaciente());
                    agendamento.setTelefonePaciente(agendamentoDetails.getTelefonePaciente());
                    agendamento.setStatus(agendamentoDetails.getStatus());
                    agendamento.setHorario(agendamentoDetails.getHorario());
                    agendamento.setClinica(agendamentoDetails.getClinica());
                    agendamento.setMedico(agendamentoDetails.getMedico());
                    Agendamento updatedAgendamento = agendamentoRepository.save(agendamento);
                    return ResponseEntity.ok(updatedAgendamento);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgendamento(@PathVariable Long id) {
        return agendamentoRepository.findById(id)
                .map(agendamento -> {
                    agendamentoRepository.delete(agendamento);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}