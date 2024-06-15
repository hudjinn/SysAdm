package br.com.sysadm.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.com.sysadm.model.Consulta;
import br.com.sysadm.model.Paciente;
import br.com.sysadm.model.Agendamento;
import br.com.sysadm.repository.AgendamentoRepository;
import br.com.sysadm.repository.ConsultaRepository;
import br.com.sysadm.repository.PacienteRepository;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/consultas")
public class ConsultaController {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @GetMapping
    public List<Consulta> listarTodos() {
        return consultaRepository.findAll();
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<Consulta>> listarPorPaciente(@PathVariable Long pacienteId) {
        List<Consulta> consultas = consultaRepository.findByPacienteId(pacienteId);
        if (consultas.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(consultas);
        }
    }

    @GetMapping("/paciente/cpf/{cpf}")
    public ResponseEntity<List<Consulta>> listarPorCpf(@PathVariable String cpf) {
        List<Consulta> consultas = consultaRepository.findByPacienteCpf(cpf);
        if (consultas.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(consultas);
        }
    }

    @GetMapping("/agendamento/{agendamentoId}")
    public ResponseEntity<List<Consulta>> listarPorAgendamento(@PathVariable Long agendamentoId) {
        List<Consulta> consultas = consultaRepository.findByAgendamentoId(agendamentoId);
        if (consultas.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(consultas);
        }
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarConsulta(@RequestParam Long agendamentoId, @RequestParam(required = false) Long pacienteId, @RequestParam(required = false) String cpf, @RequestBody Consulta consulta) {
        if (agendamentoId == null) {
            return ResponseEntity.badRequest().body("Agendamento ID é obrigatório");
        }
        Optional<Agendamento> agendamentoOptional = agendamentoRepository.findById(agendamentoId);
        if (!agendamentoOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Agendamento não encontrado");
        }
        consulta.setAgendamento(agendamentoOptional.get());

        Optional<Paciente> pacienteOptional;
        if (pacienteId != null) {
            pacienteOptional = pacienteRepository.findById(pacienteId);
        } else if (cpf != null) {
            pacienteOptional = pacienteRepository.findByCpf(cpf);
        } else {
            return ResponseEntity.badRequest().body("Paciente ID ou CPF é obrigatório");
        }

        if (!pacienteOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Paciente não encontrado");
        }
        consulta.setPaciente(pacienteOptional.get());

        consultaRepository.save(consulta);
        return ResponseEntity.status(HttpStatus.CREATED).body(consulta);
    }

    @PatchMapping("/atualizar/{id}")
    public ResponseEntity<?> atualizarConsulta(@PathVariable Long id, @RequestBody Consulta consultaAtualizada) {
        Optional<Consulta> consultaExistente = consultaRepository.findById(id);
        if (consultaExistente.isPresent()) {
            Consulta consulta = consultaExistente.get();
            if (consultaAtualizada.getQueixaPrincipal() != null) consulta.setQueixaPrincipal(consultaAtualizada.getQueixaPrincipal());
            if (consultaAtualizada.getDiagnostico() != null) consulta.setDiagnostico(consultaAtualizada.getDiagnostico());
            if (consultaAtualizada.getDataConsulta() != null) consulta.setDataConsulta(consultaAtualizada.getDataConsulta());
            if (consultaAtualizada.getPaciente() != null) consulta.setPaciente(consultaAtualizada.getPaciente());
            if (consultaAtualizada.getAgendamento() != null) consulta.setAgendamento(consultaAtualizada.getAgendamento());
            consultaRepository.save(consulta);
            return ResponseEntity.ok(consulta);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Consulta não encontrada!");
        }
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<?> deletarConsulta(@PathVariable Long id) {
        Optional<Consulta> consulta = consultaRepository.findById(id);
        if (consulta.isPresent()) {
            consultaRepository.deleteById(id);
            return ResponseEntity.ok().body("Consulta deletada com sucesso!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Consulta não encontrada!");
        }
    }
}
